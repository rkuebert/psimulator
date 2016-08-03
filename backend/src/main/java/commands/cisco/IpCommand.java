/*
 * created 6.3.2012
 */
package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import commands.completer.Completer;
import java.util.Map;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpCommand extends CiscoCommand {

	private final boolean no;
	private AbstractCommand command;
	private final int state;

	public IpCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
		this.state = parser.getShell().getMode();
	}

	@Override
	public void run() {

		String dalsi;

		dalsi = nextWord(); // route, classless, nat, address

		if (dalsi.isEmpty()) {
			incompleteCommand();
			return;
		}

		if (state == CommandShell.CISCO_CONFIG_MODE) {

			if (isCommandWithoutOutput("route", dalsi, 5)) {
				command = new IpRouteCommand(parser, no);
				command.run();
				return;
			}

			if (isCommandWithoutOutput("nat", dalsi, 3)) {
				command = new IpNatCommand(parser, no);
				command.run();
				return;
			}

			if (isCommandWithoutOutput("dhcp", dalsi, 2)) {
				command = new IpDhcpExcludedCommand(parser, no);
				command.run();
				return;
			}

			if (isCommandWithoutOutput("classless", dalsi, 2)) {
				if (no) {
					getNetMod().ipLayer.routingTable.classless = false;
				} else {
					getNetMod().ipLayer.routingTable.classless = true;
				}
				return;
			}
		}

		if (state == CommandShell.CISCO_CONFIG_IF_MODE) {
			if (isCommandWithoutOutput("address", dalsi, 3)) {
				command = new IpAddressCommand(parser, no);
				command.run();
				return;
			}

			if (isCommandWithoutOutput("nat", dalsi, 2)) {
				command = new IpNatInterfaceCommand(parser, no);
				command.run();
				return;
			}
		}

		if (dalsi.length() != 0 && ambiguous == false) { // jestli to je prazdny, tak to uz vypise isCommandWithoutOutput
			invalidInputDetected();
		}
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		Completer tmp = completers.get(CommandShell.CISCO_CONFIG_MODE);
		tmp.addCommand("ip classless");
		tmp.addCommand("no ip classless");

		// everything else is added through its commands
	}
}
