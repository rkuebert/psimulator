/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import commands.LongTermCommand;
import commands.completer.Completer;
import java.util.Map;
import shell.apps.CommandShell.CommandShell;
import static shell.apps.CommandShell.CommandShell.CISCO_CONFIG_MODE;
import shell.apps.CommandShell.ShellMode;

/**
 * ConfigureCommand command on Cisco IOS.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ConfigureCommand extends CiscoCommand implements LongTermCommand {

	public ConfigureCommand(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {

		String nextWord = nextWord();

		if (nextWord.isEmpty()) {
			parser.setRunningCommand(this,true);
			print("Configuring from terminal, memory, or network [terminal]? ");
			return;
		}

		if ("terminal".startsWith(nextWord)) {
			// zmen rovnou do CONFIGURE_MODE
			parser.changeMode(CISCO_CONFIG_MODE);
			return;
		}

		if ("memory".startsWith(nextWord) || "network".startsWith(nextWord) || "replace".startsWith(nextWord) || "?".startsWith(nextWord)) {
			parser.printService("only supported keyword by this version of simulator is \"terminal\"");
			return;
		}

		printLine("?Must be \"terminal\", \"replace\", \"memory\" or \"network\"");
	}

	@Override
	public void catchUserInput(String nextWord) { // TODO: prosetrit spravnou funknost
		parser.deleteRunningCommand();
		if ("terminal".startsWith(nextWord) || nextWord.isEmpty()) {
			// zmen rovnou do CONFIGURE_MODE
			parser.changeMode(CISCO_CONFIG_MODE);
			return;
		}

		if ("memory".startsWith(nextWord) || "network".startsWith(nextWord) || "?".startsWith(nextWord)) {
			printLine("?Must be \"terminal\"");
			printService("only supported keyword \"terminal\"");
			return;
		}

		debug("chyba: "+nextWord);
		parser.invalidInputDetected();
	}

	@Override
	public void catchSignal(Signal signal) {
		switch (signal) {
			case CTRL_C:
				parser.getShell().setShellMode(ShellMode.COMMAND_READ);
				parser.changeMode(CISCO_CONFIG_MODE);
				break;
		}
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		Completer tmp = completers.get(CommandShell.CISCO_PRIVILEGED_MODE);
		tmp.addCommand("configure terminal");
	}
}
