/*
 * created 6.4.2012
 */
package applications;

import commands.cisco.TracerouteCommand;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoTracerouteApplication extends TracerouteApplication {

	public CiscoTracerouteApplication(Device device, TracerouteCommand command) {
		super(device, command);
	}

	@Override
	public String getDescription() {
		return device.getName() + ": traceroute_app_cisco";
	}

	@Override
	protected void startMessage() {
		command.printLine("Type escape sequence to abort.");
		command.printLine("Tracing the route to " + target);
		command.printLine("");
	}

	@Override
	protected void lineBeginning(int ttl, String address) {
		command.print(ttl + " "+ address + " ");
	}

	@Override
	protected void printPacket(TracerouteApplication.Record record) {
		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Vypisuju paket seq=", record.packet.seq);
		if (record.delay == null) { // prints arrived timeout
			command.print("* ");
		} else {
			switch (record.packet.type) {
				case REPLY:
				case TIME_EXCEEDED:
					command.print(Math.round(record.delay) + " msec ");
					break;
				case UNDELIVERED:
					switch (record.packet.code) {
						case PORT_UNREACHABLE:
							command.print(Math.round(record.delay) + " msec ");
							break;
//						case FRAGMENTAION_REQUIRED:
//							command.print("F "); // podle dokumentace se asi nevypisuje
//							break;
						case HOST_UNREACHABLE:
							command.print("H ");
							break;
						case ZERO:
							command.print("N ");
							break;
						case PROTOCOL_UNREACHABLE:
							command.print("P ");
							break;
						default:
							command.print("? ");
					}
					break;
				// zadny default: tady neni, nic jinyho se asi nema vypisovat
			}
		}
	}

	@Override
	protected void printTimeout() {
		command.print("* "); // prints timeout without arrival
	}
}
