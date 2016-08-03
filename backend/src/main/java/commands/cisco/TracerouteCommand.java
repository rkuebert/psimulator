/*
 * created 7.4.2012
 */

package commands.cisco;

import applications.CiscoTracerouteApplication;
import commands.AbstractCommandParser;
import commands.ApplicationNotifiable;
import commands.LongTermCommand;
import commands.completer.Completer;
import dataStructures.ipAddresses.IpAddress;
import java.util.Map;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class TracerouteCommand extends CiscoCommand implements LongTermCommand, ApplicationNotifiable {

	private CiscoTracerouteApplication app;
	private int retCode = 0;
	private IpAddress target;

	public TracerouteCommand(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		parse();
		if (retCode == 0) {
			parser.setRunningCommand(this, false);
			app = new CiscoTracerouteApplication(parser.device, this);
			app.setMaxTTL(30);
			app.setQueriesPerTTL(3);
			app.setTarget(target);
			app.setTimeout(3_000);
			app.start();
		}

	}

	@Override
	public void catchSignal(Signal signal) {
		switch (signal) {
			case CTRL_SHIFT_6:
				app.kill();
				break;
			default:
				//
		}
	}

	@Override
	public void catchUserInput(String line) {
		// ok
	}

	@Override
	public void applicationFinished() {
		parser.deleteRunningCommand();
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		completers.get(CommandShell.CISCO_USER_MODE).addCommand("traceroute");
		completers.get(CommandShell.CISCO_PRIVILEGED_MODE).addCommand("traceroute");
	}

	private void parse() {
		String dalsi = nextWord();
        try {
            target = new IpAddress(dalsi);
        } catch (Exception e) {
            printLine("Translating \"" + dalsi + "\"...domain server (255.255.255.255)");
            printLine("% Unrecognized host or address.");
            printService("Hostname translation is not implemented.");
            retCode = 1;
        }
	}
}
