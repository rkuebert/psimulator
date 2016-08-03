/*
 * Erstellt am 21.3.2012.
 */

package commands;

import device.Device;
import networkModule.L2.EthernetInterface;
import networkModule.L2.EthernetLayer;
import networkModule.SwitchNetworkModule;
import physicalModule.RealSwitchport;
import physicalModule.Switchport;
import psimulator2.Psimulator;
import utils.Util;

/**
 * This command is used to manage connections from simulator to real network.
 * @author Tomas Pitrinec
 */
public class Rnetconn extends AbstractCommand {

	public Rnetconn(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		parsujAVykonejPrikaz();
	}

	private void parsujAVykonejPrikaz(){

		String prikaz = nextWord();

		if (prikaz.equals("")) {
			parser.printService("This command isn't present on real cisco or linux device. It's in this simulator to manage connection to real network.");
			printHelp();
		} else if (prikaz.equals("list")) {
			listAllRealSwitchports();
		} else if (prikaz.equals("help")) {
			printHelp();
		} else if (prikaz.equals("help-cz")) {
			printHelpCz();
		} else if (prikaz.equals("tie")) {
			connectSwitchport();
		} else if (prikaz.equals("untie")) {
			disconnectSwitchport();
		} else{
			printLine("Unsupported command: "+prikaz);
			printHelp();
		}

		printLine("");	// na konci odradkovani
	}

	/**
	 * Najde a vypise vsechny realny switchporty v systemu.
	 */
	private void listAllRealSwitchports() {
		printLine("This is the list of all real switchports on all devices in simulated network. Real switchport is the swichport on simulated network device, that can be connect to real computer.");
		for (Device dev : Psimulator.getPsimulator().devices) {
			for (Switchport swport : dev.physicalModule.getSwitchports().values()) {
				if (swport.isReal()) {
					printLine("Device name\tinterface\tswitchport no.\tstate");
					printSwitchportSettings(dev, (RealSwitchport) swport);
				}
			}
		}
	}

	/**
	 * Vypise nastaveni jednoho realnyho switchportu.
	 * @param dev
	 * @param swport
	 */
	private void printSwitchportSettings(Device dev, RealSwitchport swport){

		// zkusim najit jmeno interface:
		String ifaceName;
		try {
			ifaceName = ((SwitchNetworkModule) (dev.getNetworkModule())).ethernetLayer.getIfaceToSwitchport(swport.number).name;
		} catch (Exception ex) { //napr. vyjimka pri pretypovani nebo nullpointer
			ifaceName = "unknown";
		}

		String vratit = Util.zarovnej(dev.getName(),16)+ Util.zarovnej(ifaceName, 16)+Util.zarovnej(swport.number+"", 16);

		if(swport.isConnected()){
			vratit+="tied with "+swport.getIfaceName();
		} else {
			vratit +="not tied";
		}
		printLine(vratit);
	}


	private void printHelp() {
		printLine("");
		printLine("Usage:");
		printLine("This command is not present on real linux or cisco device. It's only command of this simulator to manage connection to real network.");
		printLine("The command can manage all real switchports on all simulated devices in virtual network.");
		printLine("SYNOPSIS: rnetconn command options");
		printLine("  The possible commands are:");
		printLine("    rnetconn list                                 list all real switchports in virtual network");
		printLine("    rnetconn tie device switchport_num iface      tie real switchport switchport_num on virtual device to iface");
		printLine("    rnetconn untie device switchport_num          untie switchport from its device");
		printLine("    help                                          print this help and exit");
		printLine("    help-cz                                       print this help in czech and exit");
		printLine("");
		printLine("Hint: For connection to real network work it has to exist 2 virtual interface on (real) host system. First (tap0) will be used "
				+ "for communication of host system with simulator and the second (sim0) will be used and controlled by simulator. These two "
				+ "interfaces has to be connected to each other by virtual cable (with help of VDE util and script virt_iface.sh). Interface "
				+ "sim0 has to be tied together with some switchport of device in virtual network. Interface sim0 shouldn't be configured in "
				+ "host system. ");
	}

	private void printHelpCz() {
		printLine("");
		printLine("Usage:");
		printLine("Tento prikaz neexistuje na realnem cisco ani linux pocitaci. Je implementovan pouze v tomto simulatoru ke sprave pripojeni na realnou sit.");
		printLine("Timto prikazem je mozno obsluhovat realna pripojeni vsech virtualnich sitovych zarizeni v simulatoru.");
		printLine("");
		printLine("SYNOPSIS: rnetconn prikaz options");
		printLine("  The mozne prikazy jsou:");
		printLine("    rnetconn list                                 zobrazi vsechny realne switchporty na vsech zarizenich virtualni site");
		printLine("    rnetconn tie device switchport_num iface      svaze realny switchport switchport_num zarizeni device s iface hostitelskeho pocitace");
		printLine("    rnetconn untie device switchport_num          ukonci spojeni switchportu switchport_num na zarizeni device");
		printLine("    help                                          vypise napovedu v anglictine a ukonci se");
		printLine("    help-cz                                       vypise napovedu v cestine a ukonci se");
		printLine("");
		printLine("Napoveda: Pro propojení simulátoru a reálné sítě je nutné navázat spojení mezi počítačem, na kterém je simulátor spuštěn "
				+ "(hostitelským počítačem), a nějakým počítačem z virtuální síťě simulátoru (virtuálním počítačem). K tomu je "
				+ "nutné na hostitelském počítači vytvořit 2 virtuální síťová rozhraní propojená virtuálním kabelem, tato rozhraní "
				+ "se vytváří programem VDE za pomoci skriptu virt_iface.sh. Jedno z vytvořených rozhraní (nazývané obvykle tap0) "
				+ "bude využívat hostitelský počítač pro komunikaci se simulátorem. Druhé (nazývané sim0) bude využíváno a ovládáno "
				+ "virtuálním počítačem. K tomu je třeba svázat rozhraní sim0 se switchportem virtuálního počítače, aby s ním "
				+ "tvořilo logický celek.");
		printLine("Vice informaci najdete v souboru readme.txt a na wiki strance https://code.google.com/p/psimulator/w/list");
		printLine("");
	}

	/**
	 * Zparsuje a provede prikazy connect, tedy pripojeni rozhrani na realnej switchport.
	 */
	private void connectSwitchport() {

		// zparsovani:
		String deviceName = nextWord();
		int switchportNumber;
		try{
			switchportNumber = Integer.parseInt(nextWord());
		} catch (NumberFormatException ex){
			printLine("Bad switchport number.");
			printHelp();
			return;
		}
		String ifaceName = nextWord();

		// hledani pocitace a switchportu:
		Switchport swport = findSwitchport(deviceName,switchportNumber);
		if(swport==null) return; // hlaseni by jiz pripadne byla vypsana
		if(swport.isConnected()){
			printLine("Switchport "+switchportNumber+" on "+deviceName+" is tied yet");
			return;
		}

		// vsechno je v poradku, jde se pripojit:
		RealSwitchport rport = (RealSwitchport) swport;
		int navrKod = rport.start(ifaceName);
		if (navrKod == 1) {
			printLine("An error occured while trying to connect to interface. For more informations see the programms main console.");
		} else if (navrKod == 2) {
			printLine("Switchport was tied! Can not tie tied switchport!");	// pridal jsem kontrolu (o par radku vejs), takze tohle byse nemelo stavat
		} else {
			printLine("Switchport was tied.");
		}

	}

	/**
	 * Zparsuje a provede prikazy connect, tedy pripojeni rozhrani na realnej switchport.
	 */
	private void disconnectSwitchport() {

		// zparsovani:
		String deviceName = nextWord();
		int switchportNumber;
		try{
			switchportNumber = Integer.parseInt(nextWord());
		} catch (NumberFormatException ex){
			printLine("Bad switchport number.");
			printHelp();
			return;
		}

		// hledani pocitace a switchportu:
		Switchport swport = findSwitchport(deviceName,switchportNumber);
		if(swport==null) return; // hlaseni by jiz pripadne byla vypsana
		if(!swport.isConnected()){
			printLine("Switchport "+switchportNumber+" on "+deviceName+" is not tied, so it can't be untied.");
			return;
		}

		// vsechno je v poradku, jde se pripojit:
		RealSwitchport rport = (RealSwitchport)swport;
		rport.stop();
		printLine("Switchport "+switchportNumber+" on "+deviceName+" has been untied.");

	}


	/**
	 * vycleneno par radku z connect a disconnectSwitchport. Slouzi predevsim k chybovejm vypisum.
	 * @param dev
	 * @param switchportNumber
	 * @return
	 */
	private Switchport findSwitchport(String deviceName, int switchportNumber){

		// hledani pocitace:
		Device dev = Psimulator.getPsimulator().getDeviceByName(deviceName);
		if(dev==null){
			printLine("Device "+deviceName+" doesn't exist in this simulated network.");
			printHelp();
			return null;
		}

		//hledani switchportu:
		Switchport swport = dev.physicalModule.getSwitchports().get(switchportNumber);
		if(swport==null){
			printLine("On "+dev.getName()+" doesn't exist switchport number "+switchportNumber);
			printLine("For list of real switchports, which can be connected to real network type: rnetconn list");
			printHelp();
		} else if(!swport.isReal()){
			printLine("Switchport "+switchportNumber+" on "+dev.getName()+" is only simulator switchport, it can not be connected to real interface.");
			printLine("To list real switchports, which can be connected to real network type: rnetconn list");
			printHelp();
			swport=null;
		}
		return swport;
	}

}
