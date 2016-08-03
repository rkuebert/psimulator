package shell.apps.CommandShell;

import commands.AbstractCommandParser;
import java.io.IOException;
import java.net.SocketTimeoutException;
import logging.Logger;
import logging.LoggingCategory;
import shell.ShellUtils;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class NormalRead extends BasicInputField {

	boolean stop = false;
	AbstractCommandParser parser;
	CommandShell commandShell;

	public NormalRead(BasicTerminalIO io, String name, AbstractCommandParser parser, CommandShell commandShell) {
		super(io, name);
		this.parser = parser;
		this.commandShell = commandShell;
	}

	@Override
	public void run() throws Exception {
		this.stop = false;
		while (!stop) {

			int inputValue = 0;

			try {
				inputValue = this.m_IO.read();
			} catch (SocketTimeoutException ex) {
				inputValue = 0;
			}

			if (this.commandShell.getShellMode().compareTo(ShellMode.NORMAL_READ) != 0) {
				this.stop();
				continue;
			}

			if (inputValue == 0) {
				continue;
			}

			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečetl jsem jeden znak: " + inputValue);

			if (ShellUtils.isPrintable(inputValue)) {  // is a regular character like abc...
				handleInput(inputValue);
				continue; // continue while
			}

			if (ShellUtils.handleSignalControlCodes(parser, inputValue)) {

				switch (inputValue) {
					case TerminalIO.CTRL_C:
						this.stop();
						//termIO.write(BasicTerminalIO.CRLF);
						break;
					case TerminalIO.CTRL_D:  // ctrl+d is catched before this... probably somewhere in telnetd2 library structures, no need for this
						this.stop();
						break;
				}

				continue;
			}


			switch (inputValue) { // HANDLE CONTROL CODES for text manipulation

				case TerminalIO.TABULATOR:
					this.m_IO.write("\t");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečten TABULATOR, ale nic s ním nedělám. Normal_Read mode");
					break;
				case TerminalIO.LEFT:
					this.m_IO.write("^[[D");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno LEFT, ale nic s ním nedělám. Normal_Read mode");
					break;
				case TerminalIO.RIGHT:
					this.m_IO.write("^[[C");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno RIGHT, ale nic s ním nedělám. Normal_Read mode");
					break;
				case TerminalIO.UP:
					this.m_IO.write("^[[A");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno UP, ale nic s ním nedělám. Normal_Read mode");
					break;
				case TerminalIO.DOWN:
					this.m_IO.write("^[[B");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DOWN, ale nic s ním nedělám. Normal_Read mode");
					break;

				case TerminalIO.DEL:
				case TerminalIO.DELETE:
					this.m_IO.write("^[[3~");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DEL/DELETE, ale nic s ním nedělám. Normal_Read mode");
					break;

				case TerminalIO.BACKSPACE:
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno BACKSPACE, ale nic s ním nedělám. Normal_Read mode");
					break;
				case TerminalIO.CTRL_W:
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+W, ale nic s ním nedělám. Normal_Read mode");
					break; // break switch

				case TerminalIO.CTRL_L:	// clean screen
					this.m_IO.write("^L");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+L, , ale nic s ním nedělám. Normal_Read mode");
					break;

				case TerminalIO.ENTER:
					m_IO.write(BasicTerminalIO.CRLF);
					break;

				case -1:
				case -2:
					Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Shell renderer read Input(Code):" + inputValue);
					stop = true;
					break;
			}

			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Pozice kurzoru: " + cursor + "Interpretován řídící kod: " + inputValue);

		}
	}

	@Override
	public void draw() throws IOException {
		return;  // doesnt make any sence to draw something
	}

	public void stop() {
		this.stop = true;
	}
}
