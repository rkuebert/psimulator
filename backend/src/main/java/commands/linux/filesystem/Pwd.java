/*
 * Erstellt am 12.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 * just print current path
 * @author Tomas Pitrinec
 */
public class Pwd  extends FileSystemCommand {

	public Pwd(AbstractCommandParser parser) {
		super(parser, "pwd");
	}


	@Override
	protected void executeCommand() {
		parser.getShell().printLine(parser.getShell().getPrompt().getCurrentPath());
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	@Override
	protected void controlComand() {
		
	}


}
