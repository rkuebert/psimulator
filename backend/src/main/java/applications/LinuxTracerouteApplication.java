/*
 * created 27.3.2012
 */
package applications;

import commands.linux.Traceroute;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;
import utils.Util;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class LinuxTracerouteApplication extends TracerouteApplication {

	Traceroute cmd;

	public LinuxTracerouteApplication(Device device, Traceroute command) {
		super(device, command);
		this.cmd = command;
	}

	@Override
	public String getDescription() {
		return device.getName() + ": traceroute_app_linux";
	}

	@Override
	protected void startMessage() {
		cmd.printLine("traceroute to " + target + " (" + target + "), " + maxTTL + " hops max, " + (payload + 4) + " byte packets");
	}

	@Override
	protected void lineBeginning(int ttl, String address) {
		cmd.print(" " + ttl + "  ");

		if (! address.isEmpty()) {
			cmd.print(address + " (" + address + ")  ");
		}
	}

	@Override
	protected void printPacket(Record record) {
		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Vypisuju paket seq=", record.packet.seq);
		if (record.delay == null) { // prints arrived timeout
			cmd.print("* ");
		} else {
			switch (record.packet.type) {
				case REPLY:
				case TIME_EXCEEDED:
					cmd.print(Util.zaokrouhli(record.delay) + " ms  ");
					break;
				case UNDELIVERED:
					switch (record.packet.code) {
						case PORT_UNREACHABLE:
							cmd.print(Util.zaokrouhli(record.delay) + " ms  ");
							break;
						case FRAGMENTAION_REQUIRED:
							cmd.print("!F ");
							break;
						case HOST_UNREACHABLE:
							cmd.print("!H ");
							break;
						case ZERO:
							cmd.print("!N ");
							break;
						case PROTOCOL_UNREACHABLE:
							cmd.print("!P ");
							break;
						default:
							cmd.print("!" + record.packet.code); // u nas nebude asi nastavat ?
					}
					break;
				// zadny default: tady neni, nic jinyho se asi nema vypisovat
			}
		}
	}

	@Override
	protected void printTimeout() {
		cmd.print("* "); // prints timeout without arrival
	}
}
