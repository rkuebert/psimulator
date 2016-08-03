package shell;

import commands.AbstractCommandParser;
import commands.LongTermCommand;
import commands.LongTermCommand.Signal;
import java.util.regex.Pattern;
import logging.Logger;
import logging.LoggingCategory;
import telnetd.io.TerminalIO;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ShellUtils {

	public static Pattern printablePatter = Pattern.compile(ShellUtils.getPrintableRegExp());

	public static String getPrintableRegExp() {
		return "\\p{Print}";
	}

	public static boolean handleSignalControlCodes(AbstractCommandParser parser, int code) {

		boolean handled = true;

		switch (code) {
			case TerminalIO.CTRL_C:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+C");
				parser.catchSignal(Signal.CTRL_C);
				break;
			case TerminalIO.CTRL_Z:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+Z");
				parser.catchSignal(Signal.CTRL_Z);
				break;
			case TerminalIO.CTRL_D:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+D");
				parser.catchSignal(Signal.CTRL_D);
				break;
			case TerminalIO.CTRL_SHIFT_6:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+SHIFT+6");
				parser.catchSignal(Signal.CTRL_SHIFT_6);
				break;
			default:  // if no control code was handled
				handled = false;

		}
		
		return handled;

	}

	public static boolean isPrintable(int znakInt) {
		return ShellUtils.printablePatter.matcher(String.valueOf((char) znakInt)).find();
	}
}
