package filesystem;

import commands.AbstractCommandParser;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ReplayScriptConfig {

	FileSystem fileSystem;

	public ReplayScriptConfig(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public int replay(String fileName, final AbstractCommandParser parser) throws FileNotFoundException{

		if(fileSystem == null)
			return -1;
		
			fileSystem.runInputFileJob(fileName, new InputFileJob() {

				@Override
				public int workOnFile(InputStream input) throws Exception {

					Scanner in = new Scanner(input);

					while (in.hasNextLine()) {

						String command = in.nextLine();

						if (command.startsWith("#")) // ignore commentary
						{
							continue;
						}

						int cmntPosition = command.indexOf("#");

						command = command.substring(0, cmntPosition).trim();

						if (parser != null) {
							parser.processLine(command, parser.getShell().getMode());  
						} else {
							Logger.log(Logger.WARNING, LoggingCategory.FILE_SYSTEM, "Parser object is null, cannot process command:\"" + command + "\"");
						}

					}

					return 0;
				}
			});
		

		return 0;
	}
}