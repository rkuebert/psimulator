/*
 * Erstellt am 13.5.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Echo extends AbstractCommand {

	public Echo(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		printService("Command \"echo\" is not supported in simulator. For editing files use command command \"editor\".");
	}




}
