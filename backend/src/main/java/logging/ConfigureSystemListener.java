/*
 * created 2.2.2012
 */

package logging;

import java.util.Map;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ConfigureSystemListener {

	public static void configure(Map<LoggingCategory, Integer> configuration) {
		for (LoggingCategory category : LoggingCategory.values()) {
			configuration.put(category, Logger.IMPORTANT);
		}
		configuration.put(LoggingCategory.NETWORK_MODEL_LOAD_SAVE, Logger.DEBUG);
		configuration.put(LoggingCategory.NetworkAddressTranslation, Logger.INFO);
		configuration.put(LoggingCategory.NET, Logger.INFO);


		/************************************** my changes ***************************************/

//		configuration.put(LoggingCategory.CABEL_SENDING, Logger.DEBUG);

//		configuration.put(LoggingCategory.LINK, Logger.DEBUG);
//		configuration.put(LoggingCategory.ETHERNET_LAYER, Logger.DEBUG);

//		configuration.put(LoggingCategory.TELNET, Logger.IMPORTANT);

//		configuration.put(LoggingCategory.PING_APPLICATION, Logger.DEBUG);
//		configuration.put(LoggingCategory.TRANSPORT, Logger.INFO);

//		configuration.put(LoggingCategory.IP_LAYER, Logger.DEBUG);



//		configuration.put(LoggingCategory.WRAPPER_CISCO, Logger.DEBUG);

//		configuration.put(LoggingCategory.TRACEROUTE_APPLICATION, Logger.DEBUG);
//		configuration.put(LoggingCategory.PHYSICAL, Logger.INFO);
//		configuration.put(LoggingCategory.PACKET_DROP, Logger.INFO);

//		configuration.put(LoggingCategory.ARP, Logger.INFO);
//		configuration.put(LoggingCategory.ARP_CACHE, Logger.DEBUG);



//		configuration.put(LoggingCategory.CISCO_COMMAND_PARSER, Logger.DEBUG);
//		configuration.put(LoggingCategory.COMPLETER, Logger.DEBUG);
	}
}
