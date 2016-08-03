/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import commands.completer.Completer;
import java.util.Map;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class CiscoCommand extends AbstractCommand {

	protected final CiscoCommandParser parser; // hides field on purpose
	/**
	 * nevim, k cemu to tu je
	 */
	protected boolean ambiguous = false;
	protected final boolean debug;

	public CiscoCommand(AbstractCommandParser parser) {
		super(parser);
		this.parser = (CiscoCommandParser) parser;
		this.debug = Logger.isDebugOn(LoggingCategory.CISCO_COMMAND_PARSER);
	}

	protected void invalidInputDetected() {
		parser.invalidInputDetected();
	}

	protected void incompleteCommand() {
		parser.incompleteCommand();
	}

	protected void ambiguousCommand() {
		parser.ambiguousCommand();
	}

	protected void unsupportedCommand() {
		printService("This command is not yet implemented.");
	}

	protected void debug(String s) {
		if (debug) {
			printLine(s);
		}
	}

	 /**
     * Tato metoda simuluje zkracovani prikazu tak, jak cini cisco.
     * Metoda se take stara o vypisy typu: IncompleteCommand, AmbigiousCommand, InvalidInputDetected.
     * @param command prikaz, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd prikaz, ktery zadal uzivatel
     * @param min kolik musi mit mozny prikaz znaku
     * @return Vrati true, pokud retezec cmd je jedinym moznym prikazem, na ktery ho lze doplnit.
     */
    protected boolean isCommand(String command, String cmd, int min) {

        if (cmd.length() == 0) {
            incompleteCommand();
            return false;
        }

        if (cmd.length() >= min && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny prikaz
            return true;
        }

        if (command.startsWith(cmd)) {
            ambiguousCommand();
        } else {
            invalidInputDetected();
        }
        return false;
    }

    /**
     * Tato metoda simuluje zkracovani prikazu tak, jak cini cisco.
     * Metoda se take stara o vypis: AmbigiousCommand.
     * @param command prikaz, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd prikaz, ktery zadal uzivatel
     * @param min kolik musi mit mozny prikaz znaku
     * @return Vrati true, pokud retezec cmd je jedinym moznym prikazem, na ktery ho lze doplnit.
     */
    protected boolean isCommandWithoutOutput(String command, String cmd, int min) {

        if (cmd.length() == 0) {
            return false;
        }

        if (cmd.length() >= min && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny prikaz
            return true;
        }

        if (command.startsWith(cmd)) {
            ambiguousCommand();
            ambiguous = true;
        }
        return false;
    }

	/**
     * Zjisti, zda je rezetec prazdny.
     * Kdyz ano, tak to jeste vypise hlasku incompleteCommand.
     * @param s
     * @return
     */
    protected boolean isEmptyWithIcompleteCommand(String s) {
        if (s.equals("")) {
            incompleteCommand();
            return true;
        }
        return false;
    }

	protected abstract void fillCompleters(Map<Integer, Completer> completers);
}
