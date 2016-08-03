package filesystem.dataStructures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class NodesWrapper {

	private List<Node> nodes;

	public NodesWrapper(List<Node> nodes) {
		this.nodes = nodes;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	/**
	 *
	 * @return return sorted list of nodes. Nodes are sorted by type and name. First directories, second files ...
	 */
	public List<Node> getNodesSortedByTypeAndName() {

		LinkedList<Node> sortNodes = new LinkedList<>();
		LinkedList<Node> files = new LinkedList<>();

		for (Node node : nodes) {  // make two list .. files and directories
			if (node instanceof Directory) {
				sortNodes.add(node);
			} else {
				files.add(node);
			}
		}

		Collections.sort(sortNodes, Node.getAlphaNumericalNameComparator());  // sort directories by name
		Collections.sort(files, Node.getAlphaNumericalNameComparator()); // sort all files

		sortNodes.addAll(files);  // append files

		return sortNodes;

	}
}
