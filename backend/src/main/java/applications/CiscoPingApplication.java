/*
 * created 8.3.2012
 */

package applications;

import commands.ApplicationNotifiable;
import commands.cisco.CiscoCommandParser;
import dataStructures.packets.IcmpPacket;
import dataStructures.packets.IpPacket;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;
import shell.apps.CommandShell.CommandShell;

/**
 *
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoPingApplication extends PingApplication {

	private final CommandShell shell;
	private final CiscoCommandParser parser;

	public CiscoPingApplication(Device device, CiscoCommandParser parser, ApplicationNotifiable command) {
		super(device, command);
		this.parser = parser;
		this.shell = parser.getShell();
		this.count = 5;
		this.timeout = 2_000;
		if (Logger.isDebugOn(LoggingCategory.CISCO_COMMAND_PARSER)) {
			this.timeout = 1_000;
		}
		this.waitTime = 50; // cisco sends it right away
	}

	@Override
	public String getDescription() {
		return device.getName() + ": cisco" + getName() +" application";
	}

	@Override
	protected void startMessage() {
		String s = "";
        s += "\nType escape sequence to abort.\n"
                + "Sending " + count + ", " + payload + "-byte ICMP Echos to " + target + ", timeout is " + timeout / 1000 + " seconds:";

        shell.printWithDelay(s, 20);
	}

	@Override
	protected void handleIncommingPacket(IpPacket p, double delay) {

		IcmpPacket packet = (IcmpPacket) p.data;

		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, getName()+" handleIncommingPacket, type="+packet.type+", code="+packet.code+", seq="+packet.seq, packet);

		// http://www.cisco.com/en/US/products/sw/iosswrel/ps1831/products_tech_note09186a00800a6057.shtml

		switch (packet.type) {
			case REPLY:
				shell.print("!"); // ok
				break;
			case TIME_EXCEEDED:
				shell.print("&");
				break;
			case UNDELIVERED:
				switch (packet.code) {
					case ZERO:
						// cisco posila 'U' a '.', jak se mu chce
						if (Math.round(Math.random()) % 2 == 0) {
							shell.print("U");
						} else {
							shell.print(".");
						}
						break;
					case HOST_UNREACHABLE:
						shell.print(".");
						break;
					case FRAGMENTAION_REQUIRED:
						shell.print("M");
						break;
					default:
						shell.print(".");
				}
				break;
			case SOURCE_QUENCH:
				shell.printLine("Q");
				break;
			default:
				shell.printLine("?");
		}
	}

	@Override
	public void printStats() {
		String s;
		s = "\nSuccess rate is " + stats.uspech + " percent (" + stats.prijate + "/" + stats.odeslane + ")";
        if (stats.prijate > 0) {
            s += ", round-trip min/avg/max = " + Math.round(stats.min) + "/" + Math.round(stats.avg) + "/" + Math.round(stats.max) + " ms";
        }
        shell.printWithDelay(s, 10);
	}
}
