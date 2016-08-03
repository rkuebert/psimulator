/*
 * created 5.4.2012
 */

package commands.linux;

import commands.completer.Completer;
import commands.completer.Node;
import java.util.List;
import shell.apps.CommandShell.CommandShell;

/**
 * Writes possibilities to a shell.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class LinuxCompleter extends Completer {

	@Override
	protected void handleMorePossibilities(List<Node> possibilities, CommandShell shell) {
		shell.printLine("");
		for (Node node : possibilities) {
			shell.print(node.value+"\t");
		}
		shell.printLine("");
		shell.printPrompt();

		shell.print(shell.getShellRenderer().getValue());
	}
}
