/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.exceptions.FileNotFoundException;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Linux command rm. Supported options are: -f -r, -i
 *
 * @author Tomas Pitrinec
 */
public class Rm extends FileSystemCommand {

	// options:
	boolean opt_f = false;
	boolean opt_r = false;
	boolean opt_i = false;

	public Rm(AbstractCommandParser parser) {
		super(parser, "rm");
	}

	@Override
	protected void parseOption(char c) {

		if (c == 'f') {
			opt_f = true;
		} else if (c == 'r') {
			opt_r = true;
		} else if (c == 'R') {
			opt_r = true;
		} else if (c == 'i') {
			opt_i = true;
		} else {
			invalidOption(c);
		}
	}

	/**
	 * Soubory a slozky ke smazani jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		if (opt_f || opt_i) {
			parser.getShell().printLine("Sorry unimplemented funcionality");
			return;
		}

		String currentDir = getCurrentDir();

		for (String filePath : files) {

			filePath = resolvePath(currentDir, filePath);

			if (!parser.device.getFilesystem().exists(filePath)) {
				parser.getShell().printLine("rm: " + filePath + "file doesn't exist");
				continue;
			}

			if (parser.device.getFilesystem().isDir(filePath) && !opt_r) {  // if path is a directory and -r parameter is not activated
				parser.getShell().printLine("rm: " + filePath + "path is a directory, use -r parametr");
				continue;
			}
			boolean deleted = false;
			try {
				deleted = parser.device.getFilesystem().rm_r(filePath);
			} catch (FileNotFoundException ex) {
				Logger.log(Logger.WARNING, LoggingCategory.LINUX_COMMANDS, "Cannot delete file. File is not found, but file existence was confirmed.");
			}

			if(!deleted){
				parser.getShell().printLine("rm: " + filePath + "deletion failed. Unknown reason");
			}

		}
	}

	@Override
	protected void controlComand() {
		if(files.isEmpty()){
			missingOperand();
		}
	}
}