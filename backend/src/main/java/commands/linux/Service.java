/*
 * Erstellt am 7.4.2012.
 */

package commands.linux;

import applications.Application;
import applications.DHCP_server;
import commands.AbstractCommandParser;
import psimulator2.Psimulator;

/**
 * Linuxovej prikaz service na zapinani sluzeb typu dhcp server apod.
 * Zatim udelana jednoduse, pri pridani dalsich sluzeb dopsat napr. napovedu.
 * @author Tomas Pitrinec
 */
public class Service extends LinuxCommand {

	String command;

	/**
	 * Aplikace, ktera bude pripadne spustena.
	 */
	Application newApp;

	public Service(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		if(parsujPrikaz()==0){ //parsuju, pokud vse v poradku
			vykonejPrikaz();
		}
	}

	/**
	 * Parses the command.
	 * @return 1 iff parsing error
	 */
	private int parsujPrikaz(){
		String serviceName = dalsiSlovo();
		if (serviceName.equals("dhcp-server")){
			newApp = new DHCP_server(getDevice());
		}else if(serviceName.equals("")){ // jmeno sluzby nebylo vubec zadano
			printHelp();
		} else { // zadanej nejakej nesmysl
			printLine(serviceName+": unrecognized service");
			return 1;
		}

		command = dalsiSlovo();
		if(command.equals("")){
			printLine("Usage: /etc/init.d/isc-dhcp-server {start|stop|restart|force-reload|status}");
			return 1;
		}
		// zbytek se kontroluje az u provadeni (nechce se mi 2x)

		return 0;
	}

	private void vykonejPrikaz(){
		Application app = getDevice().getAppByName(newApp.name);
		if(command.equals("start")){
			startNewApp(app);
		} else if(command.equals("stop")){
			stopApp(app);
		} else if(command.equals("restart")||command.equals("force-reload")){
			stopApp(app);
			startNewApp(app);
		} else if(command.equals("status")){
			if(app!=null){
				printLine("Status of ISC DHCP server: dhcpd is running.");
			} else {
				printLine("Status of ISC DHCP server: dhcpd is not running.");
			}
		} else {
			printLine("Usage: /etc/init.d/isc-dhcp-server {start|stop|restart|force-reload|status}");
		}
	}

	private void startNewApp(Application oldApp){
		if (oldApp == null) {
			newApp.start();
		}
		printLine("Starting ISC DHCP server: dhcpd.");
	}

	private void stopApp(Application app){
		if(app != null) {
			app.exit();
			printLine("Stopping ISC DHCP server: dhcpd.");
		} else {
			printLine("Stopping ISC DHCP server: dhcpd failed");
		}

	}

	private void printHelp(){
		printLine("Usage: service < option > | --status-all | [ service_name [ command | --full-restart ] ]");
		printService("There are this services in "+Psimulator.getNameOfProgram()+": dhcp-server");
	}

}
