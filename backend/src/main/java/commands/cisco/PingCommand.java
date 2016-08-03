/*
 * created 8.3.2012
 */

package commands.cisco;

import applications.CiscoPingApplication;
import commands.AbstractCommandParser;
import commands.ApplicationNotifiable;
import commands.LongTermCommand;
import commands.completer.Completer;
import dataStructures.ipAddresses.IpAddress;
import java.util.Map;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PingCommand extends CiscoCommand implements ApplicationNotifiable, LongTermCommand {

	private final CiscoPingApplication app;

	public PingCommand(AbstractCommandParser parser) {
		super(parser);
		this.app = new CiscoPingApplication(getDevice(), this.parser, this);
	}


	@Override
	public void catchSignal(Signal signal) {
		switch (signal) {
			case CTRL_SHIFT_6:
				app.exit();
				break;
			case CTRL_C:
				if (debug) {
					app.exit();
				}
				break;

			default:
				// ok
		}
	}

	@Override
	public void catchUserInput(String input) {
//		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void run() {
		parser.setRunningCommand(this,false);

		boolean conti = processLine();
		if (conti) {
			// vytvor aplikaci
			app.start();
		} else {
			parser.deleteRunningCommand();
		}
	}

	/**
     * Ulozi vsechny parametry do tridnich promennych nebo vypise chybovou hlasku.
     * @param typVolby, ktery ma zpracovavat
     * @return true pokud se ma pokracovat v posilani pingu
     *         false pokud to vypsalo chybu a tedy uz nic nedelat
     */
    private boolean processParameters(String typVolby) {
        if (!check("timeout", typVolby) && !check("repeat", typVolby) && !check("size", typVolby)) {
            invalidInputDetected();
            return false;
        }

        String volba = nextWord();

        if (volba.equals("")) {
            incompleteCommand();
            return false;
        }


        if (check("timeout", typVolby)) {
			int timeout;
            try {
                timeout = Integer.valueOf(volba) * 1000; // TODO: povolit maximalne 0.2 s (200ms)
				if (timeout > 3600 || timeout < 0) {
					invalidInputDetected();
					return false;
				}
            } catch (NumberFormatException e) {
                invalidInputDetected();
                return false;
            }
			app.setTimeout(timeout);
        }


        if (check("repeat", typVolby)) {
			int count;
            try {
                count = Integer.valueOf(volba);
            } catch (NumberFormatException e) {
                invalidInputDetected();
                return false;
            }
			app.setCount(count);
        }
        if (check("size", typVolby)) {
            int n;
            try {
                n = Integer.valueOf(volba);
            } catch (NumberFormatException e) {
                invalidInputDetected();
                return false;
            }
            if (n < 36 || n > 18024) {
                invalidInputDetected();
                return false;
            }
            app.setSize(n);
        }

        typVolby = nextWord();
        if (! typVolby.equals("")) {
            return processParameters(typVolby);
        }

        return true;
    }

    /**
     * Parsuje prikaz ping.
     * @return
     */
    private boolean processLine() {
//        if (parser.words.size() < 2) {
		if (nextWordPeek().isEmpty()) { // TODO: overit funkcnost

            printService("podporovana syntaxe: <IP> (<size|timeout|repeat> <cislo>)* ");
            return false;
        }
        String ip = nextWord();
		IpAddress target;
        try {
            target = new IpAddress(ip);
        } catch (Exception e) {
			printWithDelay("Translating \"" + ip + "\"" + "...domain server (255.255.255.255)\n"
					+ "% Unrecognized host or address, or protocol not running.", 500);
            return false;
        }
		app.setTarget(target);

        String typVolby = nextWord();
        if (typVolby.equals("")) {
            return true;
        }

        return processParameters(typVolby);
    }

	/**
     * Tato metoda simuluje zkracovani prikazu tak, jak cini cisco.
     * @param command prikaz, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd prikaz, ktery zadal uzivatel
     * @return Vrati true, pokud retezec cmd je jedinym moznym prikazem, na ktery ho lze doplnit.
     */
    private boolean check(String command, String cmd) {

        int n = 1;
        if (command.equals("size")) n = 2;

        if (cmd.length() >= n && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny prikaz
            return true;
        }
        return false;
    }

	@Override
	public void applicationFinished() {
		parser.deleteRunningCommand();
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		completers.get(CommandShell.CISCO_USER_MODE).addCommand("ping");
		completers.get(CommandShell.CISCO_PRIVILEGED_MODE).addCommand("ping");
	}

}
