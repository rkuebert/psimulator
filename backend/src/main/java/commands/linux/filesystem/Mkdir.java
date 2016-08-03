/*
 * Erstellt am 11.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.exceptions.FileNotFoundException;

/**
 * Command mkdir. No parameters are supported.
 *
 * @author Tomas Pitrinec
 */
public class Mkdir extends FileSystemCommand {

// promenny parseru:
	public Mkdir(AbstractCommandParser parser) {
		super(parser, "mkdir");
	}

	@Override
	protected void parseOption(char c) {
		// zadny prepinace nepodporuju

		invalidOption(c);

	}

	@Override
	protected void controlComand() {
		if (files.isEmpty()) {
			missingOperand();
		}
	}

	@Override
	protected void executeCommand() {


		String currentDir = getCurrentDir();

		for (String filePath : files) {

			filePath = resolvePath(currentDir, filePath);


			try {
				boolean mkdir = parser.device.getFilesystem().createNewDir(filePath);

				if (mkdir) {
					continue;
				} else {
					parser.getShell().printLine("mkdir: " + filePath + "directory creation failed");
				}

			} catch (FileNotFoundException ex) {
				parser.getShell().printLine("mkdir: " + filePath + "directory creation failed");

			}




		}



	}
}
