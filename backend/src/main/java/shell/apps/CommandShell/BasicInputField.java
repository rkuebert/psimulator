package shell.apps.CommandShell;

import java.io.IOException;
import logging.Logger;
import logging.LoggingCategory;
import telnetd.io.BasicTerminalIO;
import telnetd.io.toolkit.ActiveComponent;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public abstract class BasicInputField extends ActiveComponent {

	protected int cursor = 0;
	protected StringBuilder sb = new StringBuilder(50); //buffer načítaného řádku, čtecí buffer

	public BasicInputField(BasicTerminalIO io, String name) {
		super(io, name);
	}

	protected void clearBuffer() {
		this.sb.setLength(0); // clear string builder
		this.cursor = 0;
	}

	/**
	 * funkce obstarávající posun kurzoru vlevo. Posouvá "blikající" kurzor, ale i "neviditelný" kurzor značící pracovní
	 * místo v čtecím bufferu
	 *
	 * @param times
	 */
	protected void moveCursorLeft(int times) {

		for (int i = 0; i < times; i++) {
			if (cursor == 0) {
				return;
			} else {
				try {
					m_IO.moveLeft(1);
					cursor--;
				} catch (IOException ex) {
					Logger.log(Logger.WARNING, LoggingCategory.TELNET, ex.toString());

				}


			}
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "VLEVO, pozice: " + cursor);

		}

	}

	/**
	 * funkce obstarávající posun kurzoru vpravo. Posouvá "blikající" kurzor, ale i "neviditelný" kurzor značící
	 * pracovní místo v čtecím bufferu
	 *
	 * @param times
	 */
	protected void moveCursorRight(int times) {

		for (int i = 0; i < times; i++) {


			if (cursor >= this.sb.length()) {
				return;
			} else {
				try {
					m_IO.moveRight(1);
					cursor++;
				} catch (IOException ex) {
					Logger.log(Logger.WARNING, LoggingCategory.TELNET, "VPRAVO, pozice: " + cursor);

				}


			}
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "VPRAVO, pozice: " + cursor);


		}
	}

	protected void handleEnd() {
		
		int toEndPositions = this.sb.length() - this.cursor;
		this.moveCursorRight(toEndPositions);

	}
	
	protected void handleHome(){
		this.moveCursorLeft(this.cursor);
	}

	protected void handleBackSpace() throws IOException {

		if (cursor != 0) {
			sb.deleteCharAt(cursor - 1);
			moveCursorLeft(1);
			draw();
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Backspace upravil pozici kurzoru na: " + cursor);
		}

	}

	protected void handleDelete() throws IOException {


		if (cursor != sb.length()) {
			sb.deleteCharAt(cursor);
			draw();
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "DELETE upravil pozici kurzoru na: " + cursor);
		}

	}

	protected void handleInput(int inputValue) throws IOException {

		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, " Tisknul jsem znak: " + String.valueOf((char) inputValue) + " ,který má kód: " + inputValue);
		m_IO.write(inputValue);
		sb.insert(cursor, (char) inputValue);
		cursor++;
		draw();
		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Pozice kurzoru: " + cursor);

	}

	protected void setValue(String input) {

		this.clearBuffer();
		this.sb.append(input);
		this.cursor = sb.length();

	}

	
	abstract public void run() throws Exception;

	
	abstract public void draw() throws IOException;
}
