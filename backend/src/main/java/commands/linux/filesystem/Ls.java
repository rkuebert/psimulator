/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.dataStructures.Directory;
import filesystem.dataStructures.Node;
import filesystem.dataStructures.NodesWrapper;
import filesystem.exceptions.FileNotFoundException;
import java.util.List;

/**
 * Linux command ls. Supported options are -a, -l.
 *
 * @author Tomas Pitrinec
 */
public class Ls extends FileSystemCommand {

	boolean opt_a = false;
	boolean opt_l = false;

	public Ls(AbstractCommandParser parser) {
		super(parser, "ls");
	}

	@Override
	protected void parseOption(char c) {
		if (c == 'a') {
			opt_a = true;
		} else if (c == 'l') {
			opt_l = true;
		} else {
			invalidOption(c);
		}
	}

	/**
	 * Soubory a slozky, ktery se maj vypsat jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		if (opt_a || opt_l) {
			parser.getShell().printLine("Sorry unimplemented funcionality");
			return;
		}

		if (files.isEmpty()) { // no dir to list => list current directory
			files.add("");
		}

		String currentDir = getCurrentDir();

		for (String filePath : files) {
			try {

				filePath = resolvePath(currentDir, filePath);

				NodesWrapper nodeswrap = parser.device.getFilesystem().listDir(filePath);

				List<Node> nodes = nodeswrap.getNodesSortedByTypeAndName();

				int i = 0;
				for (Node node : nodes) {

					if (i % 4 == 0) {
						parser.getShell().printLine("");
					}
					i++;

					parser.getShell().print(node.getName());

					if (node instanceof Directory) {
						parser.getShell().print("/");
					}

					parser.getShell().print("\t");

				}

				parser.getShell().printLine("");

			} catch (FileNotFoundException ex) {
				parser.getShell().printLine("ls: " + filePath + " directory not found");
			}
		}

	}

	@Override
	protected void controlComand() {
		// nothing to control
	}
}
