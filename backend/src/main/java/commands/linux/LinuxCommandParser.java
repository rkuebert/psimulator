/*
 * created 5.3.2012
 */
package commands.linux;

import commands.linux.filesystem.Touch;
import commands.linux.filesystem.Rm;
import commands.linux.filesystem.Pwd;
import commands.linux.filesystem.Mkdir;
import commands.linux.filesystem.Ls;
import commands.linux.filesystem.*;
import commands.AbstractCommand;
import commands.AbstractCommandParser;
import commands.LongTermCommand.Signal;
import commands.Rnetconn;
import commands.cisco.CiscoCommand;
import commands.completer.Node;
import device.Device;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import logging.*;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import networkModule.NetworkModule;
import networkModule.IpNetworkModule;
import shell.apps.CommandShell.CommandShell;
import utils.Util;

/**
 *
 * @author Tomáš Pitřinec
 */
public class LinuxCommandParser extends AbstractCommandParser implements Loggable{

	/**
	 * Mapa mezi nazvama prikazu a jejich tridou.
	 */
	private Map<String, Class> commands = new HashMap<>();

	private List<String> scriptCommands;



	public LinuxCommandParser(Device networkDevice, CommandShell shell) {
		super(networkDevice, shell);
		registerCommands();
		shell.getPrompt().setPrefix(device.getName()+":");
		shell.getPrompt().setCurrentPath("/");
		shell.getPrompt().showPath(true);
		shell.getPrompt().setSuffix("#");

		if (device.commandCompleters == null) {
			device.commandCompleters = new HashMap<>();
			addCompleters();
			addCompletionData();
		}

		printService("Type command 'help-en' for list of supported commands (or help for the same in czech).");
	}



	/**
	 * Metoda registrije spustitelny prikazy.
	 */
	private void registerCommands() {
		commands.put("ifconfig", Ifconfig.class);
		commands.put("exit", Exit.class);
		commands.put("route", Route.class);
		commands.put("ping", Ping.class);
//		commands.put("cping", PingCommand.class);	// zatim si pridavam cisco ping
		commands.put("ip", Ip.class);
		commands.put("help", Help.class);
		commands.put("help-en", Help.class);
		commands.put("man", Man.class);
		commands.put("traceroute", Traceroute.class);
		commands.put("iptables", Iptables.class);
		commands.put("rnetconn", Rnetconn.class);
		commands.put("service", Service.class);

		// prace s filesystemem:
		commands.put("cat", Cat.class);
		commands.put("cd", Cd.class);
		commands.put("ls", Ls.class);
		commands.put("mkdir", Mkdir.class);
		commands.put("pwd", Pwd.class);
		commands.put("rm", Rm.class);
		commands.put("mv", Mv.class);
		commands.put("cp", Cp.class);
		commands.put("touch", Touch.class);
		commands.put("editor",Editor.class); commands.put("mcedit",Editor.class);
		commands.put("echo", Echo.class);

	}


// verejny metody konkretniho parseru: --------------------------------------------------------------------------------

	@Override
	public void catchSignal(Signal sig) {
		if(sig==Signal.CTRL_C){
			Logger.log(this,Logger.DEBUG,LoggingCategory.LINUX_COMMANDS,"Dostal jsem signal ctrl+C, vykonava me vlakno "+Util.threadName(),null);
			shell.printLine("^C");
			if(runningCommand != null){
				runningCommand.catchSignal(sig);
			}
		}
		// zadny dalsi signaly nepotrebuju
	}

	/**
	 * Tady se predevsim zpracovava prichozi radek. Chytaj se tu vsechny vyjimky, aby se mohly zalogovat a nesly nikam
	 * dal.
	 */
	@Override
	protected void processLineForParsers() {
		try {	// nechci hazet pripadne hozeny vyjimky dal

			String commandName = nextWord();
			if (!commandName.isEmpty()) {	// kdyz je nejakej prikaz vubec poslanej, nejradsi bych posilani niceho zrusil

				AbstractCommand command = getLinuxCommand(commandName);
				if (command != null) { // pokud je to normalni prikaz
					command.run();
				} else if(commandName.startsWith("#")){ // komentar - nic se nedeje
					// nic nedelam
				} else if(commandName.contains("/")){ //obsahuje to lomitko, mohla by to bejt cesta
					nactiSkript(commandName);
				} else {
					shell.printLine("bash: " + commandName + ": command not found");
				}

			}

		} catch (Exception ex) {
			log(Logger.WARNING, "Some error in linux commands.", null);
			log(Logger.DEBUG, "Byla vyhozena vyjimka.", ex);
		}
		//log(Logger.DEBUG,"konec metody processLineForParsers",null);

	}

	/**
	 * Jen do tyhle metody pridavam volani pripadnyho skriptu.
	 * POZOR: metoda muze volana z vlakna nejaky aplikace, pak i ten skript muze byt odtamtud volanej.
	 */
	@Override
	public void deleteRunningCommand() {
		super.deleteRunningCommand();
		vykonejSkript();
	}

	@Override
	public String getDescription() {
		return device.getName()+": LinuxCommandParser";
	}


// privatni metody: ---------------------------------------------------------------------------------------------------

	/**
	 * Tahle metoda vrati instanci prikazu podle zadanyho jmena. Je to trochu hack, na druhou stranu se nemusi
	 * registrovat prikaz na dvou mistech.
	 *
	 * @param name
	 * @return
	 */
	private AbstractCommand getLinuxCommand(String name) {

		Class tridaPrikazu = commands.get(name);
		if (tridaPrikazu == null) {
			return null;
		} else if (CiscoCommand.class.isAssignableFrom(tridaPrikazu)) {
			log(Logger.WARNING, "Na linuxu se pokousite zavolat ciscovej prikaz, coz nejde, protoze ten ocekava cisco parser prikazu.", null);
			return null;
		} else {

			try {
				Class[] ctorArgs1 = new Class[1];
				ctorArgs1[0] = AbstractCommandParser.class;
				Constructor konstruktor = tridaPrikazu.getConstructor(ctorArgs1);
				//log(0, "Mam konstruktor pro nazev prikazu " + name, null);
				Object novaInstance = konstruktor.newInstance((AbstractCommandParser) this);
				//log(0, "Mam i vytvorenou novou instanci.", null);
				return (AbstractCommand) novaInstance;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
				log(Logger.WARNING, "Chyba privytvareni instance prikazu " + name, ex);
				return null;
			}
		}


	}

	/**
	 * Metoda na spusteni skriptu. Praci s filesystemem jsem zkopiroval z Cat.java.
	 * @param fileName
	 */
	private void nactiSkript(String fileName) {
//		getShell().printLine("Spustena metoda na spusteni skriptu.");
		String currentDir = getShell().getPrompt().getCurrentPath() + "/";
		try {

			String resolvedPath;

			if (fileName.startsWith("/")) // absolute resolving
			{
				resolvedPath = fileName;
			} else {
				resolvedPath = currentDir + fileName;
			}

			device.getFilesystem().runInputFileJob(resolvedPath, new InputFileJob() {

				@Override
				public int workOnFile(InputStream input) throws Exception {
					scriptCommands = new LinkedList<>();
					Scanner sc = new Scanner(input);
//					log(Logger.DEBUG, "Jdu zacit vykonavat skript.", null);
//					getShell().printLine("Zacinam vykonavat skript");
					while (sc.hasNextLine()) {
						scriptCommands.add(sc.nextLine());
					}
//					getShell().printLine("Koncim vykonavat skript");
//					log(Logger.DEBUG, "Koncim vykonavat skript.", null);
					return 0;
				}
			});
			vykonejSkript();
		} catch (FileNotFoundException ex) {
			log(Logger.DEBUG,"Byla chycena vyjimka.", null);
			getShell().printLine("bash: " + currentDir + fileName + ": No such file or directory");
		}

	}

	private void vykonejSkript(){
		while(scriptCommands != null && !scriptCommands.isEmpty() && runningCommand == null){
			String radek = scriptCommands.remove(0);
			processLine(radek, mode);
				// -> na tohle pozor, v parseru spustenim nad konkretnim radkem volam ten samej parser - mohlo by to delat neplechu
		}
	}


	private void log(int logLevel, String message, Object obj){
		Logger.log(this, logLevel, LoggingCategory.LINUX_COMMANDS, message, obj);
	}

	@Override
	protected final void addCompleters() {
		device.commandCompleters.put(CommandShell.DEFAULT_MODE, new LinuxCompleter());
	}

	@Override
	protected final void addCompletionData() {
		Iterator<String> it = commands.keySet().iterator();
		while (it.hasNext()) {
			String cmd = it.next();

			if (cmd.equals("ifconfig")) {
				Node ifconfig = new Node("ifconfig");

				NetworkModule nm = device.getNetworkModule();
				IPLayer ipLayer = ((IpNetworkModule) nm).ipLayer;

				for (NetworkInterface iface : ipLayer.getSortedNetworkIfaces()) {
					ifconfig.addChild(new Node(iface.name));
				}
				device.commandCompleters.get(CommandShell.DEFAULT_MODE).addCommand(ifconfig);
			} else {
				device.commandCompleters.get(CommandShell.DEFAULT_MODE).addCommand(cmd);
			}
		}
	}
}
