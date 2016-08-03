/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetd.pridaneTridy;

import device.Device;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import logging.Logger;
import logging.LoggingCategory;
import shared.telnetConfig.ConfigRecord;
import shared.telnetConfig.TelnetConfig;
import utils.Util;

/**
 * x
 *
 * @author Martin Lukáš
 */
public class TelnetProperties {

	public enum Shell {

		LINUX, CISCO
	}
	private static Properties properties = new Properties();
	private static int lastPort = 11000;
	private static List<String> listenerNames = new LinkedList<String>();
	private static boolean finalConfiguration = false;
	private static TelnetConfig telnetConfig = new TelnetConfig();

	/**
	 * this method should be executed only once
	 */
	private static void commonSetup() {

		finalConfiguration = true;

		properties.setProperty("terminals", "vt100,ansi,windoof,xterm");
		properties.setProperty("term.vt100.class", "telnetd.io.terminal.vt100");
		properties.setProperty("term.vt100.aliases", "default,vt100-am,vt102,dec-vt100");
		properties.setProperty("term.ansi.class", "telnetd.io.terminal.ansi");
		properties.setProperty("term.ansi.aliases", "color-xterm,xterm-color,vt220,linux,screen");   // vt320 is not working properly
		// properties.setProperty("term.ansi.aliases", "color-xterm,xterm-color,vt320,vt220,linux,screen");
		properties.setProperty("term.windoof.class", "telnetd.io.terminal.Windoof");
		properties.setProperty("term.windoof.aliases", "");
		properties.setProperty("term.xterm.class", "telnetd.io.terminal.xterm");
		properties.setProperty("term.xterm.aliases", "");
		properties.setProperty("shells", "std");
		//   properties.setProperty("shell.std.class", "telnetd.shell.DummyShell");
		properties.setProperty("shell.std.class", "shell.TelnetSession");


		StringBuilder listeners = null;

		for (String name : listenerNames) {
			if (listeners == null) {
				listeners = new StringBuilder(name);
			} else {
				listeners.append(",").append(name);
			}
		}

		properties.setProperty("listeners", listeners.toString());

	}

	public static Properties getProperties() {

		if (!TelnetProperties.finalConfiguration) {
			commonSetup();
		}

		return properties;

	}
	
	public static TelnetConfig getTelnetConfig(){
		return telnetConfig;
	}

	public static void addListener(Device device) {


		String name = String.valueOf(device.configID);
		int port = lastPort;
		lastPort += 1;

		int maxTestedPort = 10;
		int testedPort = 0;

		while (!utils.Util.availablePort(port)) {
			port++;
			lastPort++;
			testedPort++;
			if (testedPort > maxTestedPort) {
				Logger.log(Logger.ERROR, LoggingCategory.TELNET, "Cannot start telnet server for device "+name+" . I have tried 10 port, but none one is available");
				return;
			}
		}

		device.setTelnetPort(port);

		Logger.log("TELNET LISTENING PORT: ", Logger.IMPORTANT, LoggingCategory.TELNET, "Device: " + Util.zarovnej(device.getName(), 7) + " listening port: " + device.getTelnetPort() + " (" + device.getName() + ")");

		// now add records for UI editor
		ConfigRecord cfgr = new ConfigRecord();
		cfgr.setComponentId(device.configID);
		cfgr.setPort(device.getTelnetPort());
		
		telnetConfig.put(device.configID, cfgr);
		
		listenerNames.add(name);

		properties.setProperty(name + ".loginshell", "std");
		properties.setProperty(name + ".port", String.valueOf(port));
		properties.setProperty(name + ".floodprotection", "5");
		properties.setProperty(name + ".maxcon", "25");
		properties.setProperty(name + ".time_to_warning", "3600000");
		properties.setProperty(name + ".time_to_timedout", "60000");
		properties.setProperty(name + ".housekeepinginterval", "1000");
		properties.setProperty(name + ".inputmode", "character");
		properties.setProperty(name + ".connectionfilter", "none");


	}

	public static void setStartPort(int port) {
		lastPort = port;
	}
}

/*
 * ==============DEFAULTNÍ KONFIGURACE Z NÁVODU NA WEBU PROJEKTU================ #Unified telnet proxy properties
 * #Daemon configuration example. #Created: 15/11/2004 wimpi
 *
 *
 * ############################ # Telnet daemon properties # ############################
 *
 * ##################### # Terminals Section # #####################
 *
 * # List of terminals available and defined below terminals=vt100,ansi,windoof,xterm
 *
 * # vt100 implementation and aliases term.vt100.class=net.wimpi.telnetd.io.terminal.vt100
 * term.vt100.aliases=default,vt100-am,vt102,dec-vt100
 *
 * # ansi implementation and aliases term.ansi.class=net.wimpi.telnetd.io.terminal.ansi
 * term.ansi.aliases=color-xterm,xterm-color,vt320,vt220,linux,screen
 *
 * # windoof implementation and aliases term.windoof.class=net.wimpi.telnetd.io.terminal.Windoof term.windoof.aliases=
 *
 * # xterm implementation and aliases term.xterm.class=net.wimpi.telnetd.io.terminal.xterm term.xterm.aliases=
 *
 * ################## # Shells Section # ##################
 *
 * # List of shells available and defined below shells=dummy
 *
 * # shell implementations shell.dummy.class=net.wimpi.telnetd.shell.DummyShell
 *
 * ##################### # Listeners Section # ##################### listeners=std
 *
 *
 * # std listener specific properties
 *
 * #Basic listener and connection management settings std.port=6666 std.floodprotection=5 std.maxcon=25
 *
 *
 * # Timeout Settings for connections (ms) std.time_to_warning=3600000 std.time_to_timedout=60000
 *
 * # Housekeeping thread active every 1 secs std.housekeepinginterval=1000
 *
 * std.inputmode=character
 *
 * # Login shell std.loginshell=dummy
 *
 * # Connection filter class std.connectionfilter=none
 */
