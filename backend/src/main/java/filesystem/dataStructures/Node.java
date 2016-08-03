package filesystem.dataStructures;

import java.util.Comparator;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public abstract class Node {

	private static Comparator<Node> comparatorBuffer;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public abstract String toString();

	/**
	 *
	 * @return
	 */
	public static Comparator getAlphaNumericalNameComparator() {

		if (comparatorBuffer != null) {
			return comparatorBuffer;
		}

		comparatorBuffer = new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return o1.getName().compareTo(o2.getName());
			}
		};

		return comparatorBuffer;

	}
}
