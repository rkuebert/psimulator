/*
 * Erstellt am 26.10.2011.
 */
package psimulator2;

import config.configTransformer.Loader;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import logging.Logger;
import logging.LoggingCategory;
import logging.networkEvents.EventServer;
import shared.Components.NetworkModel;
import shared.Serializer.AbstractNetworkSerializer;
import shared.Serializer.NetworkModelSerializerXML;
import shared.Serializer.SaveLoadException;
import telnetd.BootException;
import telnetd.TelnetD;
import telnetd.pridaneTridy.TelnetProperties;
import utils.Util;

/**
 *
 * @author Tomáš Pitřinec
 * @author Martin Lukáš
 */
public class Main {

	public static String configFileName;
	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {

		System.out.println("Starting Psimulator2, build "+format.format(new Date()));


		// check jvm version  ... 1.7 or higher

		Double jvmVersion = Double.parseDouble(System.getProperty("java.version").substring(0, 3));

		if (jvmVersion < 1.7) { // lower versions than JVM 1.7 aka JRE 7 are not supported
			System.err.println("Please install JRE 7 or higher.");
			System.exit(1);
		}

		// nejdriv se nastavi logger:
		Logger.setLogger();

		if (args.length < 1) {
			Logger.log(Logger.ERROR, LoggingCategory.XML_LOAD_SAVE,
					"No configuration file attached, run again with configuration file as first argument.");
		}

		//parsovani parametru prikazovy radky:
		configFileName = args[0];
		int firstTelnetPort = 11000;
		if (args.length >= 2) {
			try {
				firstTelnetPort = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				Logger.log(Logger.ERROR, LoggingCategory.XML_LOAD_SAVE, "Second argument "+args[1]+" is not port number. Using default value: " + firstTelnetPort);
			}
		}

		int eventServerPort = 12000;
		if (args.length >= 3) {
			try {
				eventServerPort = Integer.parseInt(args[2]);
			} catch (NumberFormatException ex) {
				Logger.log(Logger.WARNING, LoggingCategory.XML_LOAD_SAVE, "Third argument "+args[2]+" is not port number. Using default value: " + eventServerPort);
			}
		}

		// serializace xml do ukladacich struktur:
		AbstractNetworkSerializer serializer = new NetworkModelSerializerXML();	// vytvori se serializer
		NetworkModel networkModel = null;
		try {

			networkModel = serializer.loadNetworkModelFromFile(new File(configFileName));	// nacita se xmlko do ukladacich struktur

		} catch (SaveLoadException ex) {
			Logger.log(Logger.DEBUG, LoggingCategory.XML_LOAD_SAVE, Util.stackToString(ex));
			Logger.log(Logger.ERROR, LoggingCategory.XML_LOAD_SAVE, "Cannot load network model from: " + configFileName);
		}

		// nastaveni promennejch systemu pro telnet a pro ukladani:
		TelnetProperties.setStartPort(firstTelnetPort);
		Psimulator.getPsimulator().configModel = networkModel;
		Psimulator.getPsimulator().lastConfigFile = configFileName;

		// samotnej start systemu z ukladacich struktur
		Loader loader = new Loader(networkModel, configFileName);	// vytvari se simulator loader
		loader.loadFromModel();	// simulator se startuje z tech ukladacich struktur

		// startovani telnetu:
		TelnetD telnetDaemon;
		Logger.log(Logger.INFO, LoggingCategory.TELNET, "Starting telnet listeners");
		try {

			telnetDaemon = TelnetD.createTelnetD(TelnetProperties.getProperties());
			// @TODO pridat metodu na kontrolu obsazení portů
			telnetDaemon.start();

			Logger.log(Logger.INFO, LoggingCategory.TELNET, "Telnet listeners successfully started");

		} catch (BootException ex) {
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
			Logger.log(Logger.ERROR, LoggingCategory.TELNET, "Error occured when creating telnet servers.");
		}

		// SETUP EVENT SERVER
		int tryMark = 0;
		int maxTryMark = 10;

		while(true){
			if(Util.availablePort(eventServerPort))
				break; // break while

			Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Port "+eventServerPort+" not available, using:" + (eventServerPort+1) );

			tryMark++;
			eventServerPort++;

			if(tryMark>maxTryMark)
				Logger.log(Logger.ERROR, LoggingCategory.EVENTS_SERVER, "Cannot start event server, no port available");
		}


		EventServer eventServer = new EventServer(eventServerPort);
		Thread thread = new Thread(eventServer);
		thread.start();

		Psimulator.getPsimulator().eventServer=eventServer;

		Logger.addListener(eventServer.getListener().getPacketTranslator());

		Logger.log("PACKET FLOW SERVER: ", Logger.IMPORTANT, LoggingCategory.EVENTS_SERVER, "Server sucessfully started, listening on port: " + eventServerPort);

	}
}
