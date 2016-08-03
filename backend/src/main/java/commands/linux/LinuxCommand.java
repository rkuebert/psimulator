/*
 * Erstellt am 16.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import psimulator2.Psimulator;

/**
 *
 * @author Tomas Pitrinec
 */
public abstract class LinuxCommand extends AbstractCommand {

	protected boolean ladiciVypisovani; //pro debug

	protected final IPLayer ipLayer;	// zkratka

	public LinuxCommand(AbstractCommandParser parser) {
		super(parser);
		if (Psimulator.getPsimulator().systemListener.configuration.get(LoggingCategory.LINUX_COMMANDS) == Logger.DEBUG) {
			ladiciVypisovani = true;
		}
		ipLayer = getNetMod().ipLayer;
		if (Psimulator.getPsimulator().systemListener.configuration.get(LoggingCategory.LINUX_COMMANDS) == Logger.DEBUG) {
			ladiciVypisovani = true;
		}

	}


	/**
	 * Zkratka volani pro pro route ze stary verze psimulatoru.
	 * @return
	 */
	protected String dalsiSlovo() {
		return parser.nextWord();
	}


}
