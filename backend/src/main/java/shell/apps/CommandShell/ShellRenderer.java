/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import exceptions.TelnetConnectionException;
import filesystem.dataStructures.Directory;
import filesystem.dataStructures.Node;
import filesystem.dataStructures.NodesWrapper;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.logging.Level;
import logging.Logger;
import logging.LoggingCategory;
import shell.ShellUtils;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;

/**
 *
 * @author Martin Lukáš
 */
public class ShellRenderer extends BasicInputField {
	
	private CommandShell commandShell;
	/**
	 * flag signaling if line is returned... if ctrl+c is read then no line is returned
	 */
	private boolean returnValue = true;
	private boolean quit;
	private NodesWrapper tabPathSearchBuffer;
	
	public ShellRenderer(CommandShell commandShell, BasicTerminalIO termIO, String name) {
		super(termIO, name);
		this.commandShell = commandShell;

		// this.termIO = termIO;  // no need for this. Parent class Component has same protected member
	}
	
	public int quit() {
		this.quit = true;
		
		return 0;
	}

	/**
	 * hlavní funkce zobrazování shellu a čtení z terminálu, reakce na různé klávesy ENETER, BACKSCAPE, LEFT ....
	 *
	 * @return vrací přečtenou hodnotu z řádku, příkaz
	 * @throws Exception
	 * @throws TelnetConnectionException
	 */
	@Override
	public void run() throws Exception {
		this.m_IO.setAutoflushing(true);
		this.clearBuffer();
		this.returnValue = true;
		this.quit = false; // příznak pro ukončení čtecí smyčky jednoho příkazu

		
		
		while (!quit) {
			
			try {
				
				int inputValue;
				
				try {
					inputValue = this.m_IO.read();
				} catch (SocketTimeoutException ex) {
					inputValue = 0;
				}
				
				if (inputValue == 0) {
					continue;
				}

				// possible utf-8 characters handling, not used beacuse swingTelnetClient is not able to handle utf-8
//				if (inputValue > 127) { 
//
//					int size = 0;
//
//					if (inputValue >= 194 && inputValue <= 223) {
//						size = 2;
//					} else if (inputValue >= 224 && inputValue <= 239) {
//						size = 3;
//					} else if (inputValue >= 240 && inputValue <= 256) {
//						size = 4;
//					}
//
//					if (size <= 4 && size >= 2) {
//
//						byte[] buffer = new byte[size];
//						buffer[0] = (byte) inputValue;
//
//
//						for (int i = 1; i < size; i++) {
//							buffer[i] = (byte) this.m_IO.read();
//						}
//
//						String eh = new String(buffer, "UTF8");
//
//						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, " Tisknul jsem UTF-8 znak: " + eh);
//						m_IO.write(eh);
//						sb.insert(cursor, eh);
//						cursor++;
//						draw();
//						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Pozice kurzoru: " + cursor);
//						continue; // continue while
//					}
//
//				}

				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečetl jsem jeden znak: " + inputValue);
				
				if (inputValue != TerminalIO.TABULATOR) // if it is not tabulator => clear tab buffer
				{
					this.tabPathSearchBuffer = null;
				}
				
				if (ShellUtils.isPrintable(inputValue)) {  // is a regular character like abc...
					handleInput(inputValue);
					continue; // continue while
				}
				
				
				if (ShellUtils.handleSignalControlCodes(this.commandShell.getParser(), inputValue)) // if input was signaling control code && handled
				{
					switch (inputValue) {
						case TerminalIO.CTRL_C:
							quit = true;
							returnValue = false;
							//termIO.write(BasicTerminalIO.CRLF);
							break;
						case TerminalIO.CTRL_D:  // ctrl+d is catched before this... probably somewhere in telnetd2 library structures, no need for this
							quit = true;
							returnValue = false;
							m_IO.write("BYE");
							break;
						
					}
					
					continue;  // continue while cycle
				}
				
				switch (inputValue) { // HANDLE CONTROL CODES for text manipulation

					case TerminalIO.HOME_KEY:
						handleHome();
						break;
					case TerminalIO.END_KEY:
						handleEnd();
						break;
					case TerminalIO.CTRL_R:
						handleSearch();
						break;
					
					case TerminalIO.TABULATOR:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno TABULATOR");
						this.handleTabulator();
						break;
					
					case TerminalIO.LEFT:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno LEFT");
						moveCursorLeft(1);
						break;
					case TerminalIO.RIGHT:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno RIGHT");
						moveCursorRight(1);
						break;
					case TerminalIO.UP:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno UP");
						this.handleHistory(TerminalIO.UP);
						break;
					case TerminalIO.DOWN:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DOWN");
						this.handleHistory(TerminalIO.DOWN);
						break;
					
					case TerminalIO.DEL:
					case TerminalIO.DELETE:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DEL/DELETE");
						handleDelete();
						break;
					
					case TerminalIO.BACKSPACE:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno BACKSPACE");
						handleBackSpace();
						break;
					case TerminalIO.CTRL_W:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+W");	// @TODO vyladit smazání slova tak aby odpovídalo konvencím na linuxu
						while (cursor != 0) {
							sb.deleteCharAt(cursor - 1);
							moveCursorLeft(1);
							draw();
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "CTRL+W upravil pozici kurzoru na: " + cursor);
							
							if (cursor != 0 && Character.isSpaceChar(sb.charAt(cursor - 1))) // delete until space is found
							{
								break; // break while
							}
							
						}
						break; // break switch

					case TerminalIO.CTRL_L:	// clean screen
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+L");
						this.clearScreen();
						break;
					
					case TerminalIO.ENTER:
						quit = true;
						this.commandShell.getHistoryManager().getActiveHistory().addCommand(this.getValue());
						m_IO.write(BasicTerminalIO.CRLF);
						break;
					
					case -1:
					case -2:
						Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Shell renderer read Input(Code):" + inputValue);
						quit = true;
						break;
				}
				
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Pozice kurzoru: " + cursor + "Interpretován řídící kod: " + inputValue);
				
				
			} catch (IOException ex) {
				
				if (this.quit) // ok no problem, outer program code probably closed socket
				{
					Logger.log(Logger.INFO, LoggingCategory.TELNET, "Closing ShellRenderer");
					return;
				}
				quit = true;
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
				ShellUtils.handleSignalControlCodes(this.commandShell.getParser(), TerminalIO.CTRL_D);  //  CLOSING SESSION SIGNAL
				this.commandShell.quit();
			} catch (UnsupportedOperationException ex) {
				Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Unsuported exception catched in ShellRenderer: " + ex.toString());
			}
			
		}
		
		
		
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

	/**
	 * method that replace actual command line, redraw prompt and command line
	 *
	 * @param value
	 */
	@Override
	public void setValue(String value) {
		// set value to stringbuilder + redraw line

		this.clearBuffer();
		this.sb.append(value);
		
		this.drawLine();
		returnValue = true;
	}

	/**
	 * redraw command area from cursor position to the end
	 *
	 * @throws IOException
	 */
	@Override
	public void draw() throws IOException {
		
		m_IO.eraseToEndOfScreen();
		m_IO.write(sb.substring(cursor, sb.length()));
		m_IO.moveLeft(sb.length() - cursor);
	}

	/**
	 * redraw command area without redrawing prompt area
	 *
	 * @throws IOException
	 */
	private void drawCommand() {
		try {
			moveCursorLeft(cursor);
			m_IO.eraseToEndOfScreen();
			this.cursor = 0;
			m_IO.write(sb.toString());
			this.cursor = sb.length();
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "IOException occured when drawing command area in ShellRenderer");
		}
	}

	/**
	 * draw entire line, cursor is set at the end of line
	 *
	 * @throws IOException
	 */
	private void drawLine() {
		
		try {
			
			this.eraseLine();
			this.commandShell.printPrompt();
			this.cursor = 0;
			m_IO.write(sb.toString());
			this.cursor = sb.length();
			
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "IOException occured when drawing line in ShellRenderer");
		}
	}

	/**
	 * funkce obsluhující historii, respektive funkce volaná při přečtení kláves UP a DOWN
	 *
	 * @param key typ klávesy který byl přečten
	 * @throws IOException
	 */
	private void handleHistory(int key) throws IOException, TelnetConnectionException {
		if (!(key == TerminalIO.UP || key == TerminalIO.DOWN)) // historie se ovládá pomocí šipek nahoru a dolů, ostatní klávesy ignoruji
		{
			return;
		}
		
		this.eraseLine();
		
		this.commandShell.printPrompt();
		
		if (key == TerminalIO.UP) {
			//  this.sb.setLength(0);
			this.commandShell.getHistoryManager().getActiveHistory().handlePrevious(this.sb);
		} else if (key == TerminalIO.DOWN) {
			//  this.sb.setLength(0);
			this.commandShell.getHistoryManager().getActiveHistory().handleNext(this.sb);
		}
		
		m_IO.write(this.sb.toString());
		m_IO.moveLeft(m_IO.getColumns());
		m_IO.moveRight(sb.length() + this.commandShell.getPrompt().toString().length());
		this.cursor = sb.length();
		
	}

	/**
	 * handling tabulator -- auto completing command action
	 */
	private void handleTabulator() {
		
		
		String toCompleteValue = this.sb.substring(0, cursor);
		String completedValue = null;
		
		if (this.tabPathSearchBuffer != null && tabPathSearchBuffer.getNodes().size() > 1) // double tab action
		{
			
			List<Node> nodes = tabPathSearchBuffer.getNodesSortedByTypeAndName();
			
			int i = 0;
			for (Node node : nodes) {
				if (i % 4 == 0) {
					commandShell.printLine("");
				}
				i++;
				
				commandShell.print(node.getName());
				
				if (node instanceof Directory) {
					commandShell.print("/");
				}
				
				commandShell.print("\t");
				
				
				
				
			}
			
			commandShell.printLine("");
			this.tabPathSearchBuffer = null;
			this.drawLine();
			
			return;
		}
		
		this.tabPathSearchBuffer = this.commandShell.completePath(toCompleteValue);
		
		if (this.tabPathSearchBuffer != null && this.tabPathSearchBuffer.getNodes().size() == 1) // only one node to complete => complete line
		{
			String fileName = this.tabPathSearchBuffer.getNodes().get(0).getName();
			if (this.tabPathSearchBuffer.getNodes().get(0) instanceof Directory) {
				fileName += "/";
			}
			int pathStringIndex = toCompleteValue.lastIndexOf("/");
			
			if (pathStringIndex < toCompleteValue.length() - 1) {
				pathStringIndex++;
			}
			
			completedValue = toCompleteValue.substring(0, pathStringIndex) + fileName;
		}
		
		if (completedValue == null) {
			completedValue = this.commandShell.completeWord(toCompleteValue);
		}
		
		if (completedValue == null || completedValue.isEmpty()) {
			return;
		}
		
		String restOfLine = this.sb.substring(cursor, this.sb.length());
		
		if (restOfLine == null || restOfLine.trim().isEmpty()) {
			this.setValue(completedValue);
			return;
		}
		
		this.setValue(completedValue + restOfLine.trim());
		
		this.moveCursorLeft(restOfLine.length());
	}
	
	private void handleSearch() throws IOException {
		
		HistorySearchRenderer hSearch = new HistorySearchRenderer(this, m_IO);
		int ret = hSearch.run(this.commandShell.getHistoryManager().getActiveHistory(), this.sb.toString());
		
		switch (ret) {
			case TerminalIO.LEFT:
			case TerminalIO.RIGHT:
			case TerminalIO.UP:
			case TerminalIO.DOWN:
				this.setValue(hSearch.getResult());
				break;
			
			case TerminalIO.ENTER:
				this.setValue(hSearch.getResult());
				this.m_IO.write(TerminalIO.CRLF);
				this.quit();
				break;
			case TerminalIO.CTRL_C:
				this.m_IO.write("^C");
				this.m_IO.write(TerminalIO.CRLF);
				this.clearBuffer();
				this.drawLine();
				break;
			
		}
		
		
	}

	/**
	 * method that clear entire screen -- like CTRL-L
	 *
	 * @throws IOException
	 * @throws TelnetConnectionException
	 */
	public void clearScreen() throws IOException, TelnetConnectionException {
		this.m_IO.eraseScreen();
		m_IO.setCursor(0, 0);
		this.commandShell.printPrompt();
		this.cursor = 0;
		drawCommand();
	}

	/**
	 * method that erase whole line and set cursor at the begining
	 *
	 * @throws IOException
	 */
	public void eraseLine() throws IOException {
		m_IO.eraseLine();
		m_IO.moveLeft(m_IO.getColumns());  // kdyby byla lepsi cesta jak smazat řádku, nenašel jsem
	}
}
