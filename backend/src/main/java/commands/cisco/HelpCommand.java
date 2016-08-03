/*
 * created 11.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import commands.completer.Completer;
import java.util.Map;
import psimulator2.Main;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
class HelpCommand extends CiscoCommand {

	public HelpCommand(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		String s ="";
		s += "There is no such command in real cisco. \n" +
                    "It is used only for a hint. This implementation has more commands then real cisco:\n" +
                    " help - writes this hint\n" +
                    " kill - for leaving console from any state of cisco\n" +
                    " save - for saving current configuration of all devices to XML file, " +
					" rnetconn      command to manage connection of simulator to real network"+
					"               for help in english type: rnetconn help"+
                    "without paramater it saves to "+Main.configFileName+"\n\n" +

                    "These commands are implemented:\n";
		s +=    "\nuser mode\n" +
                "  show ip nat translations\n"+
                "  show ip route\n" +
                "  traceroute\n" +
                "  ping\n" +
                "  enable\n" +
                "  exit\n" +

                "\nprivilege mode\n" +
                "  configure terminal\n" +
                "  disable\n" +
                "  show ip nat translations\n"+
                "  show ip route\n" +
                "  show running-config\n" +
                "  traceroute\n" +
                "  ping\n" +
                "  exit\n" +

                "\nconfigure mode\n" +
                "  (no) ip classless\n" +
                "  (no) ip route\n" +
                "  (no) ip nat pool\n" +
                "  (no) ip nat inside source list\n" +
                "  (no) ip nat inside source static\n" +
                "  access-list\n" +
                "  interface\n" +
                "  end\n" +
                "  exit\n" +

                "\nconfigure interface mode\n" +
                "  (no) ip address\n" +
                "  (no) ip nat inside\n" +
                "  (no) ip nat outside\n" +
                "  (no) shutdown\n" +
				"  interface\n" +
                "  end\n" +
                "  exit\n\n";
		printWithDelay(s, 10);
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		getDevice().addCommandToAllCompleters("help");
	}
}
