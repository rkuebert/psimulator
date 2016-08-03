/*
 * created 2.4.2012
 */

package commands.completer;

import commands.completer.Node.Item;
import java.util.ArrayList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import shell.apps.CommandShell.CommandShell;
import utils.Util;

/**
 * Command completer for commands.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class Completer implements Loggable {
	/**
	 * Has no value, only children.
	 */
	Node root = new Node(null);

	/**
	 * Temporary storage for asked command.
	 */
	private List<String> temp = new ArrayList<>();
	private int ref = 0;

	private String getNextWord() {
		if (temp.size() <= ref) {
			return null;
		}
		return temp.get(ref++);
	}

	/**
	 * Returns completed command iff there is only one possibility.
	 * Otherwise returns null
	 *
	 * Bude volan ze shellu uz konkretni Completer dle aktualniho modu.
	 *
	 * @param line
	 * @return
	 */
	public String complete(String line, CommandShell shell) {
		temp.clear();
		ref = 0;
		temp.addAll(Util.splitLine(line));

		Node act = root;

		String completedLine = "";

		while (true) {
			String word = getNextWord();
			log("slovo z prikazu: "+word);

			if (word == null) {
				if (completedLine.isEmpty()) {
					return null;
				}
				return completedLine;
			}

			Item completed = act.searchChildren(word);
			if (completed.morePossibilities) {
				handleMorePossibilities(act.possibilities, shell);
				return null;
			} else if (completed.child == null) {
				log("nenalezeno mezi detmi: "+word);
				return null;
			}

			log("nalezeno mezi detmi: "+word);
			act = completed.child;
			completedLine += act.value + " ";
		}
	}

	/**
	 * Writes possibilities to the shell.
	 * @param possibilities
	 * @param shell
	 */
	protected abstract void handleMorePossibilities(List<Node> possibilities, CommandShell shell);

	public void addCommand(String line) {
		List<String> tmp = new ArrayList<>();
		tmp.addAll(Util.splitLine(line));

		Node act = root;

		for (String word : tmp) {
			Node child = act.getChild(word);
			if (child == null) { // neni to tam, tak pridam
				child = new Node(word);
				act.addChild(child);
			} // uz to mam je, tak nemusim pridavat

			act = child;
		}
	}

	/**
	 * Neresi se zde zatim situace, kdy vkladam strom do podstromu s vice sousedama (viz radek uk = node.getFirstDescendant();)
	 * @param node
	 */
	public void addCommand(Node node) {
		Node act = root;

		Node child;
		Node uk = node;

		while (true) {
			child = act.getChild(uk.value);
			if (child == null) {
				act.addChild(uk);
				return;
			}
			act = child;
			uk = node.getFirstDescendant();
		}
	}

	@Override
	public String toString() {
		String s = "Completer: ";
		s += root;
		return s + "\n";
	}

	@Override
	public String getDescription() {
		return "completer";
	}

	protected void log(String s) {
		Logger.log(this, Logger.DEBUG, LoggingCategory.COMPLETER, s, null);
	}
}
