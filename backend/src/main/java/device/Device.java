/*
 * Erstellt am 27.10.2011.
 */
package device;

import applications.Application;
import commands.AbstractCommandParser;
import commands.cisco.CiscoCommandParser;
import commands.completer.Completer;
import commands.linux.LinuxCommandParser;
import filesystem.FileSystem;
import java.util.HashMap;
import java.util.Map;
import networkModule.NetworkModule;
import physicalModule.AbstractPhysicalModule;
import physicalModule.PhysicMod;
import physicalModule.PhysicalModuleV2;
import shell.apps.CommandShell.CommandShell;
import telnetd.pridaneTridy.TelnetProperties;

/**
 *
 * @author neiss
 */
public class Device {

	public final int configID;	// configID z konfiguraku
	private String name;
	public final DeviceType type;
	public final AbstractPhysicalModule physicalModule;
	private NetworkModule networkModule;
	/**
	 * Completers for all available modes.
	 *
	 * Key - mode number - static modes from CommandShell<br />
	 * Value - Completer
	 */
	public Map<Integer, Completer> commandCompleters; // schvalne neinicializovano! vytvari se az v parserech
	/**
	 * List of running network applications. <br />
	 *
	 * Key - Application PID <br />
	 * Value - Application
	 */
	Map<Integer, Application> applications;
	private int pidCounter = 0;

	/**
	 * telnet port is configured by TelnetProperties.addListerner method, which is called in constructor
	 * telnetPort is allocated when simulator is started. There is no need to store it in file.
	 */
	private transient int telnetPort = -1;

	private FileSystem filesystem;
	/**
	 * Konstruktor. Nastavi zadany promenny, vytvori si fysickej modul.
	 *
	 * @param configID
	 * @param name
	 * @param type
	 *
	 */
	public Device(int configID, String name, DeviceType type) {
		this.configID = configID;
		this.name = name;
		this.type = type;
//		physicalModule = new PhysicMod(this);
		physicalModule = new PhysicalModuleV2(this);
		// telnetovy pripojeni se pridava jen kdyz to neni switch
		if(type!=DeviceType.simple_switch){
			TelnetProperties.addListener(this);  // telnetPort is configured in this method
		}
		this.applications = new HashMap<>();
	}

	public int getTelnetPort() {
		return telnetPort;
	}

	public void setTelnetPort(int telnetPort) {
		this.telnetPort = telnetPort;
	}

	public FileSystem getFilesystem() {
		return filesystem;
	}

	public void setFilesystem(FileSystem filesystem) {
		this.filesystem = filesystem;
	}



	public String getName() {
		return name;
	}

	public NetworkModule getNetworkModule() {
		return networkModule;
	}

	/**
	 * Tuto metodu pouzivat jen na zacatku behu programu pri konfiguraci!
	 *
	 * @param networkModule
	 */
	public void setNetworkModule(NetworkModule networkModule) {
		this.networkModule = networkModule;
	}

	/**
	 * Returns free PID for new applications.
	 * @return
	 */
	public int getFreePID() {
		if (pidCounter == Integer.MAX_VALUE) {
			pidCounter = 0;
		}
		pidCounter++;

		if (applications.containsKey(pidCounter)) {
			return getFreePID();
		}

		return pidCounter;
	}

	/**
	 * Register application to list of running applications.
	 * @param app
	 */
	public void registerApplication(Application app) {
		applications.put(app.getPID(), app);
	}

	/**
	 * Unregister application from list of running applications.
	 * @param app
	 */
	public void unregisterApplication(Application app) {
		applications.remove(app.getPID());
	}

	public Application getAppByName(String name) {
		for (Application app : applications.values()) {
			if (app.getName().equals(name)) {
				return app;
			}
		}
		return null;
	}

	/**
	 * Adds command to all registered completers.
	 */
	public void addCommandToAllCompleters(String command) {
		for (Completer completer : commandCompleters.values()) {
			completer.addCommand(command);
		}
	}

	/**
	 * Creates parser according to a DeviceType.
	 * @param cmd
	 * @return
	 */
	public AbstractCommandParser createParser(CommandShell cmd){

		switch (type) {
			case cisco_router:
				return new CiscoCommandParser(this, cmd);

			case linux_computer:
				return new LinuxCommandParser(this, cmd);

			case simple_switch:
				return null; // no parser

			default:
				throw new AssertionError();
		}
	}

	public enum DeviceType {

		cisco_router,
		linux_computer,
		simple_switch
	}
}
