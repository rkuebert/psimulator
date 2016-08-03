/*
 * created 2.2.2012
 */
package logging;

import dataStructures.packets.L2Packet;
import dataStructures.packets.L3Packet;
import dataStructures.packets.L4Packet;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

/**
 * Main server listener. Writes everything to server's console.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomáš Pitřinec
 */
public class SystemListener implements LoggerListener {

	/**
	 * Key - logging cathegory <br />
	 * Value - Logger.{ERROR,WARNING,IMPORTAN,INFO,DEBUG}, if INFO selected all facilities before it are also selected.
	 */
	public final Map<LoggingCategory, Integer> configuration = new EnumMap<>(LoggingCategory.class);
	private DateFormat currentTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private PrintWriter out;
	private DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private String file = psimulator2.Psimulator.getNameOfProgram()+"_exceptions_"+format.format(new Date())+".txt";

	public SystemListener() {
		ConfigureSystemListener.configure(configuration);
	}

	@Override
	public void listen(Loggable caller, int logLevel, LoggingCategory category, String message, Object object) {
		try {

			if (logLevel <= configuration.get(category)) {
				String begin = "[" + Logger.logLevelToString(logLevel) + "] " + category + ": " + caller.getDescription() + ": ";

				if (object instanceof Exception) {	// vyjimka
					System.out.println(begin + message);
					Exception ex = (Exception) object;
					ex.printStackTrace();

					try {
						out = new PrintWriter(new FileWriter(file, true));
					} catch (IOException exs) {
						System.err.println("Could not create Printwriter to log exceptions to file: " + file+", so this exception is not logged to a file.");
					}
					out.println(currentTime.format(new Date()) + " " + caller.getDescription()+": ");
					ex.printStackTrace(out);
					out.println();
					out.flush();

				} else if (object instanceof L2Packet || object instanceof L3Packet || object instanceof L4Packet) {	// paket
					System.out.println(begin + object.toString() + " | " + message);

				} else if (object != null) {	// nejakej jinej object, ten se vypisuje na konec
					System.out.println(begin + message+" (" + object.toString()+")");
				} else {
					System.out.println(begin + message);
				}
			}

		} catch (NullPointerException e) {
			System.out.println("An error occured during logging:-) \n"
					+ "LoggingCategory was maybe null or something..");
			System.exit(3);
		}
	}

	@Override
	public void listen(String name, int logLevel, LoggingCategory category, String message) {
		try {
			if (logLevel <= configuration.get(category)) {
				System.out.println("[" + Logger.logLevelToString(logLevel) + "] " + category + ": " + name + ": " + message);
			}
		} catch (NullPointerException e) {
			System.out.println("An error occured during logging:-) \n"
					+ "LoggingCategory was null.");
			System.exit(3);
		}
	}
}
