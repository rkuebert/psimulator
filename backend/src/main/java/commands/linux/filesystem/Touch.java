/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.exceptions.FileNotFoundException;

/**
 *
 * @author Tomas Pitrinec
 */
public class Touch extends FileSystemCommand {

	public Touch(AbstractCommandParser parser) {
		super(parser, "touch");
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	@Override
	protected void controlComand() {
		if (files.isEmpty()) {
			missingOperand();
		}
	}

	/**
	 * Soubory k vytvoreni jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		String currentDir = getCurrentDir();

		for (String fileName : files) {

			fileName = resolvePath(currentDir, fileName);

			try {
				if ((!parser.device.getFilesystem().createNewFile(fileName) && !parser.device.getFilesystem().exists(fileName))) // if new file was not created and doesnt already exist
				{
					this.parser.getShell().printLine("touch: " + fileName + " touching file failed");
				}
			} catch (FileNotFoundException ex) {
				this.parser.getShell().printLine("touch: " + fileName + " touching file failed. Parent directory doesnt exist");
			}
		}

	}
}
