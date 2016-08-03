/*
 * created 2.4.2012
 */

package commands.cisco;

import commands.completer.Completer;
import commands.completer.Node;
import java.util.List;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoCompleter extends Completer {

	@Override
	protected void handleMorePossibilities(List<Node> possibilities, CommandShell shell) {
		log("vice moznosti: "+possibilities.toString());
		// do nothing
	}


}
