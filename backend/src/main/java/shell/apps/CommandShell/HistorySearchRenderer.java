package shell.apps.CommandShell;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import logging.Logger;
import logging.LoggingCategory;
import shell.ShellUtils;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class HistorySearchRenderer {

	ShellRenderer shellRenderer;
	private BasicTerminalIO m_IO;
	private StringBuilder sb;
	private String currentResult;
	private boolean stop = false;
	private static String searchPrompt = "(reverse-i-search)`";
	private History currentHistory;
	private int lastSearchedIndex = 0;
	private Stack<Integer> searchBuffer;

	public HistorySearchRenderer(ShellRenderer shellRenderer, BasicTerminalIO m_IO) {
		this.shellRenderer = shellRenderer;
		this.m_IO = m_IO;
		this.sb = new StringBuilder();

	}

	public String getResult() {
		return currentResult;
	}

	public int run(History history, String initialResult) {
		this.currentHistory = history;
		this.currentResult = initialResult;

		this.searchBuffer = new Stack<>();
		this.resetSearchIndex();
		this.stop = false;

		try {
			draw();

			while (!stop) {

				int inputValue;

				try {
					inputValue = this.m_IO.read();
				} catch (SocketTimeoutException ex) {
					inputValue = 0;
				}

				if (inputValue == 0) {
					continue;
				}


				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečetl jsem jeden znak: " + inputValue);

				if (ShellUtils.isPrintable(inputValue)) {  // is a regular character like abc...
					this.handleInput(inputValue);
					continue; // continue while
				}

				// else it is a control key

				switch (inputValue) {
					case TerminalIO.CTRL_R:
						if (lastSearchedIndex > 0) {
							lastSearchedIndex--;
						} else {
							resetSearchIndex();
							//break;
						}
						updateSearch();  // continue searching
						draw();
						break;

					case TerminalIO.BACKSPACE:

						if (this.sb.length() < 1) {
							this.resetSearchIndex();
							break;
						}

						this.sb.deleteCharAt(sb.length() - 1); // delete last character

						if (this.sb.length() < 1) {
							this.resetSearchIndex();
							this.draw();
							break;
						}

						if (checkLastOne()) {
							this.draw();
							break;
						}


						if (!this.searchBuffer.empty()) {
							this.lastSearchedIndex = this.searchBuffer.pop();
						} else {
							this.resetSearchIndex();
						}

						this.updateSearch();
						this.draw();
						break;
					case TerminalIO.ENTER:
					case TerminalIO.LEFT:
					case TerminalIO.RIGHT:
					case TerminalIO.UP:
					case TerminalIO.DOWN:
					case TerminalIO.CTRL_C:
						return inputValue;

				}

			}

		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "IOException occured when running historySearch:" + ex.toString());
		}

		this.currentHistory = null;
		return -1;

	}

	private void draw() throws IOException {
		shellRenderer.eraseLine();
		m_IO.write(searchPrompt);  // PRINT SEARCH PROMPT

		m_IO.write(this.sb.toString()); // PRINT ACCTUAL SEARCH STRING
		m_IO.write("':");

		if (currentResult != null) {
			m_IO.write(currentResult);
		}

	}

	private boolean checkLastOne() {

		if( !( lastSearchedIndex >=0 && lastSearchedIndex<currentHistory.getCommands().size() ) )  // if it is not a index of array
			return false;
		
		Pattern searchPattern = Pattern.compile(".*" + this.sb.toString() + ".*");
		return searchPattern.matcher(currentHistory.getCommands().get(lastSearchedIndex)).find();

	}

	private boolean updateSearch() {

		if (currentHistory == null) {  // if history object is null,
			return false;
		}

		Pattern searchPattern = Pattern.compile(".*" + this.sb.toString() + ".*");

		for (int i = lastSearchedIndex; i >= 0; i--) {
			String command = currentHistory.getCommands().get(i);

			if (searchPattern.matcher(command).find()) {  // FOUND COMMAND
				lastSearchedIndex = i;
				currentResult = command;
				return true;
			}

		}

		// DID NOT FOUND
		lastSearchedIndex = -1;
		//currentResult = null;
		return false;

	}

	/**
	 * handle printable input
	 *
	 * @param inputValue
	 */
	private void handleInput(int inputValue) throws IOException {
		this.sb.append((char) inputValue);

		

		boolean updated = this.updateSearch();

		if (updated) {
			searchBuffer.push(lastSearchedIndex);
		}

		draw();

	}

	private void resetSearchIndex() {
		this.lastSearchedIndex = this.currentHistory.getCommands().size() - 1;
	}
}
