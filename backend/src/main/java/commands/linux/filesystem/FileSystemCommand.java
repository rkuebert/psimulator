/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import commands.linux.LinuxCommand;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tomas Pitrinec
 */
public abstract class FileSystemCommand extends LinuxCommand {

	protected final String commandName;
	protected boolean parserError;
	/**
	 * Soubory, ktery uzivatel zadal.
	 */
	protected List<String> files = new LinkedList<>();
	protected String options = ""; // seznam prepinacu - celejch slov bez minusu!

	public FileSystemCommand(AbstractCommandParser parser, String commandName) {
		super(parser);
		this.commandName = commandName;
	}

	@Override
	public void run() {
		parseCommand();
		if (ladiciVypisovani) {
			printLine(toString());
		}

		if (!parserError) {
			controlComand();	// pokud by prisel na chybu, nastavil by parserError na true
		}
		if (!parserError) {
			executeCommand();
		}
	}

	protected void parseCommand() {

		// zpracuju radek:
		String slovo = nextWord();
		while (!slovo.isEmpty()) {
			if (slovo.startsWith("-") && slovo.length() > 1) { // samotny minus se povazuje za nazev adresare
				options += (slovo.substring(1, slovo.length()));
			} else {
				files.add(slovo);
			}
			slovo = nextWord();
		}

		// zpracuju prepinace - nejdriv odstranim duplicitni:
		Set<Character> optionsSet = new HashSet<>();
		for (int i = 0; i < options.length(); i++) {
			optionsSet.add(options.charAt(i));
		}
		// pak zavolam jejich zpracovani:
		for (Character c : optionsSet) {
			parseOption(c);
			if (parserError) {
				break;
			}
		}
	}

	/**
	 * Zpracuje jeden prepinac.
	 *
	 * @param c
	 */
	protected abstract void parseOption(char c);

	protected void invalidOption(char c) {
		parserError = true;
		printLine(commandName + ": invalid option -- '" + c + "'");
	}

	protected void missingOperand() {
		parserError = true;
		printLine(commandName + ": missing operand");
		printLine("Try `" + commandName + " --help' for more information. ");
	}

	/**
	 * Vykona prikaz.
	 */
	protected abstract void executeCommand();

	/**
	 * Pokud potreba, zkontroluje prikaz (napriklad byl-li zadan soubor k vytvoreni a tak). Provadi se po parseru a
	 * kontroluje ty veci, ktery jsou kazdymu prikazu specificky. Pokud prijde na chybu, nastavi parserError na true.
	 * Neni-li potreba konrolovat (napr. ls), necha se prazdna.
	 */
	protected abstract void controlComand();

	@Override
	public String toString() {

		String vratit = "----------------------------------\n"
				+ "  Parametry prikazu " + commandName
				+ "\n\t" + parser.getWordsAsString()
				+ "\n\t" + files
				+ "\n\t" + options
				+ "\n----------------------------------";

		return vratit;

	}
// staticky pomocny veci k parsovani: -------------------------------------------------------------------------------
	private static String[] specs;

	public static boolean containsSpecialCharacter(String s) {
		if (specs == null) {
			specs = new String[]{"?", "$", "|", "\\", ";", "<", ">", "!", "#", "*", "(", ")",};
			// tohle vsechno projde v nazvech souboru: ][][]  ][][][  {}{}}{  %%%  @@@  ^^^  +++  blabla  bla.bla  data  dddd  dddd?dddd  llll  4 (vypis ls)
		}

		for (int i = 0; i < specs.length; i++) {
			if (s.contains(specs[i])) {
				return true;
			}
		}



		return false;
	}

	protected String resolvePath(String currentDirectory, String filePath) {

		String resolvedPath;

		if (filePath.startsWith("/")) // absolute resolving
		{
			resolvedPath = filePath;
		} else {
			resolvedPath = currentDirectory + filePath;
		}

		return resolvedPath;
	}

	protected String getCurrentDir() {

		String currentDir = parser.getShell().getPrompt().getCurrentPath();

		if (currentDir.endsWith("/")) {
			return currentDir;
		} else {
			return currentDir + "/";
		}

	}
}
