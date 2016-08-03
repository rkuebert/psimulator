/*
 * Erstellt am 18.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public abstract class MvOrCp extends FileSystemCommand {

	public MvOrCp(AbstractCommandParser parser, String commandName) {
		super(parser, commandName);
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	/**
	 * Ve files jsou minimalne 2 cesty, pokud jich je vic, tak 1. az predposledni cesta se presune
	 * (zkopiruje) do posledni. Na rozdilny veci mezi mv a cp muzes pouzit processFile() nebo neco podobnyho.
	 */
	@Override
	protected void executeCommand() {

		String target = files.get(files.size() - 1);

		if (target.isEmpty()) {
			printLine("Unknown target");
			return;
		}

		for (String filePath : files) {
			if(target == filePath)
				continue;
			processFile(filePath, target);
		}


	}

	@Override
	protected void controlComand() {
		if (files.isEmpty()) { //nezadan operand
			printLine(commandName + ": missing file operand");
			printLine("Try `" + commandName + " --help' for more information.");
		} else if (files.size() == 1) { // chybi destinace
			printLine(commandName + ": missing destination file operand after `" + files.get(0) + "'");
			printLine("Try `" + commandName + " --help' for more information.");
		}
	}

	protected abstract int processFile(String source, String target);
}
