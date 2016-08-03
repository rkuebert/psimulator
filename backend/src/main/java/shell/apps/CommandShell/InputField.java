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
public class InputField extends BasicInputField {

	boolean stop = false;
	private boolean returnValue = true;
	AbstractCommandParser parser;
	
	CommandShell commandShell;

	public InputField(BasicTerminalIO io, String name, AbstractCommandParser parser, CommandShell commandShell) {
		super(io, name);
		this.parser = parser;
		this.commandShell = commandShell;
	}

	@Override
	public void run() throws Exception {
		this.clearBuffer();
		this.stop = false;
		this.returnValue = true;
		
		while (!stop) {

			int inputValue = 0;

			try {
				inputValue = this.m_IO.read();
			} catch (SocketTimeoutException ex) {
				inputValue = 0;
			}

			if (this.commandShell.getShellMode().compareTo(ShellMode.INPUT_FIELD) != 0) {
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
						returnValue = false;
						this.stop();
						//termIO.write(BasicTerminalIO.CRLF);
						break;
					case TerminalIO.CTRL_D:  // ctrl+d is catched before this... probably somewhere in telnetd2 library structures, no need for this
						returnValue = false;
						this.stop();
						break;
				}

				continue;
			}


			switch (inputValue) { // HANDLE CONTROL CODES for text manipulation

				case TerminalIO.TABULATOR:
					//this.m_IO.write("\t");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečten TABULATOR, ale nic s ním nedělám. Input_Field mode");
					break;
				case TerminalIO.LEFT:
					moveCursorLeft(1);
					break;
				case TerminalIO.RIGHT:
					moveCursorRight(1);
					break;
				case TerminalIO.UP:
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno UP, ale nic s ním nedělám. Input_Field mode");
					break;
				case TerminalIO.DOWN:
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DOWN, ale nic s ním nedělám. Input_Field mode");
					break;

				case TerminalIO.DEL:
				case TerminalIO.DELETE:
					handleDelete();
					break;

				case TerminalIO.BACKSPACE:
					handleBackSpace();
					break;
				case TerminalIO.CTRL_W:
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+W, ale nic s ním nedělám. Input_Field mode");
					break; // break switch

				case TerminalIO.CTRL_L:	// clean screen
					
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+L, , ale nic s ním nedělám. Input_Field mode");
					break;

				case TerminalIO.ENTER:
					m_IO.write(BasicTerminalIO.CRLF);
					this.stop();
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
		m_IO.eraseToEndOfLine();
		m_IO.write(sb.substring(cursor, sb.length()));
		m_IO.moveLeft(sb.length() - cursor);
	}
	
	/**
	 * method that return readed line
	 *
	 * @return readed line or null if ctrl+c catched
	 */
	public String getValue() {
		if (returnValue) {
			return this.sb.toString();
		} else {
			return null;
		}
	}

	public void stop() {
		this.stop = true;
	}
}
