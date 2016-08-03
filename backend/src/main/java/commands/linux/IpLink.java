/*
 * Erstellt am 15.3.2012.
 */

package commands.linux;

import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class IpLink extends LinuxCommand {

	public IpLink(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		parsujPrikaz();
        vykonejPrikaz();
	}

    private void parsujPrikaz() {

    }

    protected void vykonejPrikaz() {
		parser.printService("Command ip link is in simulator not yet supported.");
        parser.printService("Prikaz link neni v simulatoru zatim podporovan.");
    }

}
