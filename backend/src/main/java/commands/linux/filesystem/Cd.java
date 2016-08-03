/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 * Linux command cd, no options are supported.
 *
 * @author Tomas Pitrinec
 */
public class Cd extends FileSystemCommand {

	public Cd(AbstractCommandParser parser) {
		super(parser, "cd");
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	/**
	 * Prejde do prvni slozky ktera je ulozena v seznamu files.
	 */
	@Override
	protected void executeCommand() {

		if (files.isEmpty()) // set prompt to the root
		{
			parser.getShell().getPrompt().setCurrentPath("/");
			return;
		}

		String currentDirectory = parser.getShell().getPrompt().getCurrentPath();

		StringBuilder processPath = new StringBuilder(files.get(0));
		StringBuilder pathToSet;

		if (processPath.toString().startsWith("/")) // absolute path
		{
			pathToSet = processPath;
		} else {  // relative resolving
			pathToSet = new StringBuilder(currentDirectory);  // set current directory

			if (!pathToSet.toString().endsWith("/")) {  // append "/" if needed
				pathToSet.append("/");
			}

			pathToSet.append(processPath);   // append rest if relative path
		}


		if (pathToSet.toString().contentEquals("/../")) {
			return;
		}

		String normalizedPath = parser.device.getFilesystem().normalize(pathToSet.toString());

		if (normalizedPath != null && parser.device.getFilesystem().isDir(normalizedPath)) // if path is a directory => ok
		{
			parser.getShell().getPrompt().setCurrentPath(normalizedPath);
		} else { // not ok
			parser.getShell().printLine("cd: " + pathToSet.toString() + " directory not found");
		}
	}

	@Override
	protected void controlComand() {
		// nothing to control
	}
}
