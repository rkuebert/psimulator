/*
 * created 11.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import commands.completer.Completer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import shell.apps.CommandShell.CommandShell;

/**
 * Zvazit zruseni tohoto prikazu - melo by byt spise implementovano pres Completer!
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class QuestionCommand extends CiscoCommand {

	public QuestionCommand(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
        List<String> napoveda = new ArrayList<>();
        switch (parser.getShell().getMode()) {
                case CommandShell.CISCO_USER_MODE:
                    printLine("Exec commands:");
                    napoveda.add("  enable           Turn on privileged commands");
                    napoveda.add("  exit             Exit from the EXEC");
                    napoveda.add("  ping             Send echo messages");
                    napoveda.add("  show             Show running system information");
                    napoveda.add("  traceroute       Trace route to destination");
                    break;

                case CommandShell.CISCO_PRIVILEGED_MODE:
                    printLine("Exec commands:");
                    napoveda.add("  configure        Enter configuration mode");
                    napoveda.add("  disable          Turn off privileged commands");
                    napoveda.add("  enable           Turn on privileged commands");
                    napoveda.add("  ping             Send echo messages");
                    napoveda.add("  show             Show running system information");
                    napoveda.add("  traceroute       Trace route to destination");
                    break;

                case CommandShell.CISCO_CONFIG_MODE:
                    printLine("Configure commands:");
                    napoveda.add("  interface              Select an interface to configure");
                    napoveda.add("  ip                     Global IP configuration subcommands");
                    napoveda.add("  exit                   Exit from configure mode");
                    napoveda.add("  access-list            Add an access list entry");
                    break;

                case CommandShell.CISCO_CONFIG_IF_MODE:
                    printLine("Interface configuration commands:");
                    napoveda.add("  exit                    Exit from interface configuration mode");
                    napoveda.add("  ip                      Interface Internet Protocol config commands");
                    napoveda.add("  no                      Negate a command or set its defaults");
                    napoveda.add("  shutdown                Shutdown system elements");
            }
            sendList(napoveda);
    }

    /**
     * pomocna metoda pro vypis povolenych prikazu
     * @param n seznam, ktery se bude prochazet po prvcich a posilat uzivateli
     */
    private void sendList(List<String> n) {
        Collections.sort(n);
        for (String s : n) {
            printWithDelay(s, 50);
        }
    }

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		// 
	}
}
