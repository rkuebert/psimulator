/*
 * Erstellt am 8.3.2012.
 */
package commands;

import psimulator2.Psimulator;

/**
 *
 * @author Tomas Pitrinec
 */
public class PsimulatorSave extends AbstractCommand {

	public PsimulatorSave(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		parser.nextWord();
		int code = Psimulator.getPsimulator().saveSimulatorToConfigFile(parser.nextWord());
		if (code == 0) {
			printLine("File saved, for more information see the program console.");
		} else {
			printLine("ERROR: File not saved, for more information see the program console.");
		}
	}

//	@Override
//	public void catchUserInput(String input) {
//		// zatim nic nedela, v budoucnu mozna dotaz na prepsani souboru?
//	}

	@Override
	public String toString() {
		String vratit = "  Parametry prikazu save:"
				+ "\n\t" + parser.getWordsAsString();

		return vratit;
	}
}
