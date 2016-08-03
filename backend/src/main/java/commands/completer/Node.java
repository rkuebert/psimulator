/*
 * created 2.4.2012
 */

package commands.completer;

import java.util.ArrayList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Represents node in completion tree.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Node implements Loggable {

	public String value;

	private List<Node> childs = new ArrayList<>();
	public List<Node> possibilities = new ArrayList<>();

	/**
	 * Only root has null!
	 * @param value
	 */
	public Node(String value) {
		this.value = value;
	}

	public void addChild(Node node) {
		childs.add(node);
	}

	@Override
	public String toString() {
		String s = "[node: "+value;


		if (!childs.isEmpty()) {
			s += " | children:";
		}
		for (Node node : childs) {
			s += " "+ node;
		}

		s += "]";
		return s;
	}

	/**
	 * If this node has child with this name return child, nullotherwise.
	 * @param child
	 * @return
	 */
	public Node getChild(String child) {
		for (Node node : childs) {
			if (node.value.equals(child)) {
				return node;
			}
		}
		return null;
	}

	public Node getFirstDescendant() {
		if (childs.isEmpty()) {
			return null;
		}
		return childs.get(0);
	}

	public Item searchChildren(String child) {
		possibilities.clear();

		for (Node node : childs) {
			if (node.value.toLowerCase().startsWith(child.toLowerCase())) {
				log("slovo z prikazu pridavam do doplnitelnych: "+node.value);
				possibilities.add(node);
			}
		}

		if (possibilities.isEmpty()) {
			log("nenalezeno zadne podobne");
			return new Item(null, false);
		}

		if (possibilities.size() > 1) {
			return new Item(null, true);
		}

		log("ok - prave jedno nalezene");
		return new Item(possibilities.get(0), false);
	}

	private void log(String string) {
		Logger.log(this, Logger.DEBUG, LoggingCategory.COMPLETER, string, null);
	}

	@Override
	public String getDescription() {
		return "node";
	}

	public class Item {
		public final Node child;
		public final boolean morePossibilities;

		public Item(Node child, boolean more) {
			this.child = child;
			this.morePossibilities = more;
		}
	}
}
