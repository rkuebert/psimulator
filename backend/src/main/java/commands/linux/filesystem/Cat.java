/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Linux command cat. No options are supported.
 *
 * @author Tomas Pitrinec
 */
public class Cat extends FileSystemCommand {

	public Cat(AbstractCommandParser parser) {
		super(parser, "cat");
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
	 * Soubory k vypsani jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		String currentDir = getCurrentDir();

		for (String fileName : files) {
			try {

				fileName = resolvePath(currentDir, fileName);


			int ret = parser.device.getFilesystem().runInputFileJob(fileName, new InputFileJob() {

					@Override
					public int workOnFile(InputStream input) throws Exception {
						Scanner sc = new Scanner(input);

						while (sc.hasNextLine()) {
							parser.getShell().printLine(sc.nextLine());
						}

						return 0;
					}
				});

			if(ret < 0)
				throw new FileNotFoundException();

			} catch (FileNotFoundException ex) {
				parser.getShell().printLine("cat: " + fileName + ": file not found");
			}
		}

	}
}
