/*
 * Erstellt am 7.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import utils.Util;

/**
 *
 * @author Tomas Pitrinec
 */
public class Exit extends AbstractCommand{

	public Exit(AbstractCommandParser parser) {
		super(parser);
	}


	@Override
    public void run() {
        printLine("logout");
        if(parser.getWords().size()==2 ){
            if (! Util.jeInteger(parser.getWords().get(1))) printLine("-bash: exit: "+parser.getWords().get(1)+": numeric argument required");
        }
        if(parser.getWords().size()>2 ){
            printLine("-bash: exit: too many arguments");
            return;
        }
        parser.getShell().closeSession();
    }
}
