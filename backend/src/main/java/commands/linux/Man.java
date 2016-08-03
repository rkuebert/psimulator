/*
 * Erstellt am 16.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Man extends AbstractCommand {

	public Man(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		vykonejPrikaz();
	}




	protected void vykonejPrikaz() {
        parser.printService("Manual pages are not implemented in this simulator. Use manual on the web: " +
                "http://linux.die.net/man/, or just use google.");

        parser.printService("Use help command for printing all available commands.");
    }

}
