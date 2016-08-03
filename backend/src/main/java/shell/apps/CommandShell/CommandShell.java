/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import commands.AbstractCommandParser;
import device.Device;
import filesystem.dataStructures.Node;
import filesystem.dataStructures.NodesWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import shell.ShellUtils;
import shell.apps.TerminalApplication;
import telnetd.io.BasicTerminalIO;

/**
 *
 * @author Martin Lukáš
 */
public class CommandShell extends TerminalApplication implements Loggable {

	public static final int DEFAULT_MODE = 0;
	public static final int CISCO_USER_MODE = 0; // alias na ten defaultni
	public static final int CISCO_PRIVILEGED_MODE = 1;
	public static final int CISCO_CONFIG_MODE = 2;
	public static final int CISCO_CONFIG_IF_MODE = 3;
	public static final int CISCO_CONFIG_DHCP = 4;
	private ShellRenderer shellRenderer;
	private NormalRead normalRead;
	private InputField inputField;
	private Prompt prompt;
	private boolean quit = false;
	private AbstractCommandParser parser;
	private ShellMode shellMode = ShellMode.COMMAND_READ;
	private Thread thread;  // thread running blocking IO operations
	private HistoryManager historyManager;
	/**
	 * Stav shellu, na linuxuje to furt defaultni 0, na ciscu se to meni podle toho (enable, configure terminal atd.).
	 * Dle stavu se bude resit napovidani a historie.
	 */
	private int mode = DEFAULT_MODE;

	public CommandShell(BasicTerminalIO terminalIO, Device device) {
		super(terminalIO, device);
		this.thread = Thread.currentThread();
		this.thread.setName("CommandShell/Parser thread");

		this.historyManager = new HistoryManager(device);
		this.prompt = new Prompt("default promt: ", "/", "~#");
	}

	/**
	 * get active shell mode
	 *
	 * @return
	 */
	public ShellMode getShellMode() {
		return shellMode;
	}

	public BasicTerminalIO getTerminalIO() {
		return terminalIO;
	}

	/**
	 * set active shell mode
	 *
	 * @param shellMode
	 */
	public void setShellMode(ShellMode shellMode) {
		this.shellMode = shellMode;
	}

	public HistoryManager getHistoryManager() {
		return historyManager;
	}

	public void setPrompt(Prompt prompt) {
		this.prompt = prompt;
	}

	public Prompt getPrompt() {
		return prompt;
	}

	public void setMode(int mode) {
		this.mode = mode;
		this.historyManager.swapHistory(mode);
	}

	public int getMode() {
		return mode;
	}

//    public List<String> getCommandList() {
//        return this.pocitac.getCommandList();
//    }
	/**
	 * method that read command from command line
	 *
	 * @return whole line without \r\n
	 */
	public String readCommand() {

		try {
			this.getShellRenderer().run();
		} catch (InterruptedException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Blocking IO operation stopped");
		} catch (IOException ex) {
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Connection with user lost");
			this.quit();
			return null;
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Unknown exception occured: " + ex.toString());
			Logger.log(this, Logger.WARNING, LoggingCategory.TELNET, "readCommand exception occured ", ex);
			this.quit();
			return null;
		}

		return this.getShellRenderer().getValue();

	}

	public String readInput() {

		try {
			this.getInputField().run();
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection with user lost " + ex.toString());
			this.quit();
			return null;
		}

		return this.getInputField().getValue();

	}

	public InputField getInputField() {
		if (this.inputField == null) {
			this.inputField = new InputField(terminalIO, "InputField", this.getParser(), this);
		}

		return this.inputField;
	}

	public ShellRenderer getShellRenderer() {
		if (this.shellRenderer == null) {
			this.shellRenderer = new ShellRenderer(this, terminalIO, "ShellRenderer");
		}
		return shellRenderer;
	}

	/**
	 * method that read a single printable character from telnet input and handle control codes properly
	 *
	 * @return
	 */
	public char readPrintableCharacter() throws IOException {

		while (true) {

			int input = this.terminalIO.read();

			if (ShellUtils.isPrintable(input)) {
				return (char) input;
			}

			ShellUtils.handleSignalControlCodes(this.getParser(), input);
		}
	}

	/**
	 * method that read everything from telnet input like unprintable characters, control codes ... its up to you to
	 * handle that that
	 *
	 * @return
	 * @throws IOException
	 */
	public int rawRead() throws IOException {
		return this.terminalIO.read();
	}

	/**
	 * determine if there is something to read
	 *
	 * @return
	 */
	public boolean available() {
		return this.terminalIO.avaiable();
	}

	/**
	 * method used to printLine to the terminal, this method call print(text+"\r\n") nothing more
	 *
	 * @param text text to be printed to the terminal
	 */
	public void printLine(String text) {
		this.print((text + "\r\n"));
	}

	/**
	 * method used to print text to the terminal
	 *
	 * @param text text to be printed to the terminal
	 *
	 */
	public void print(String text) {
		try {

			terminalIO.write(text);
			if(!terminalIO.isAutoflushing())
				terminalIO.flush();

			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, text);
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection with user lost.");
		}
	}

	/**
	 * method that print lines with delay
	 *
	 * @param lines
	 * @param delay in milliseconds
	 *
	 */
	public void printWithDelay(String lines, int delay) {
		try {
			BufferedReader input = new BufferedReader(new StringReader(lines));
			String singleLine = "";
			while ((singleLine = input.readLine()) != null) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException ex) {
					System.err.println("Thread interruped exception occured in printWithDelay method");
				}

				printLine(singleLine);

			}
		} catch (IOException ex) {
			System.err.println("IO exception occured in printWithDelay method");
		}

	}

	/**
	 * just print prompt
	 */
	public void printPrompt() {
		if (this.getParser().isCommandRunning()) {
			print(getPrompt().toString());
		}
	}

	/**
	 * close session, terminal connection will be closed
	 */
	public void closeSession() {
		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Close session called");
		quit = true;
	}

	public AbstractCommandParser getParser() {
		if (this.parser == null) {
			this.parser = this.device.createParser(this);
		}
		return parser;
	}

	public void setParser(AbstractCommandParser parser) {
		this.parser = parser;
	}

	private NormalRead getNormalRead() {
		if (this.normalRead == null) {
			this.normalRead = new NormalRead(terminalIO, "NormalRead", this.getParser(), this);
		}
		return this.normalRead;

	}

	public String completeWord(String line) {

		// pass the line to device completer
		if (!device.commandCompleters.containsKey(mode)) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "This mode has no Completer created: " + mode);
			return null;
		}

		return device.commandCompleters.get(mode).complete(line, this);
	}

	/**
	 * try to complete filesystem path. if line last work doesnt look like path, then null is returned.
	 *
	 * @param line
	 * @return NodesWrapper ... always contain at last one node
	 */
	public NodesWrapper completePath(String line) {

		// get last work of line
		String[] words = line.split("\\s+");
		String lastWord = words[words.length - 1];

		if (lastWord.isEmpty() || !lastWord.contains("/")) // if last last word contain path delimiter, then it is a sign of path
		{
			return null;
		}

		int lastDelimOccurence = lastWord.lastIndexOf("/");
		String directory = lastWord.substring(0, lastDelimOccurence);
		String searchName = lastWord.substring(lastDelimOccurence, lastWord.length());

		if (directory.isEmpty() || directory.length() < 1) {
			directory = "/";
		}

		if (searchName.isEmpty() || searchName.trim().length() < 1) {
			return null;
		}

		while (searchName.startsWith("/")) {
			searchName = searchName.substring(1, searchName.length());
		}

		String resolvedPath;

		if (directory.startsWith("/")) // absolute resolving
		{
			resolvedPath = directory;
		} else {
			resolvedPath = getPrompt().getCurrentPath() + "/" + directory;
		}

		directory = resolvedPath;
		NodesWrapper nodes;

		try {
			nodes = device.getFilesystem().listDir(directory);
		} catch (filesystem.exceptions.FileNotFoundException ex) {
			return null;
		}

		List<Node> nodesList = nodes.getNodes();
		List<Node> possibleNodes = new LinkedList<>();

		for (Node node : nodesList) {
			if (node.getName().startsWith(searchName)) {
				possibleNodes.add(node);
			}
		}

		if (possibleNodes.size() < 1) // nothing found
		{
			return null;
		}

		NodesWrapper ret = new NodesWrapper(possibleNodes);

		return ret;

	}

	@Override
	public final int run() {

		try {
			terminalIO.setLinewrapping(true);
			terminalIO.setAutoflushing(true);
			//terminalIO.eraseScreen();
			//terminalIO.homeCursor();
		} catch (IOException ex) {
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
		}


		String line;

		this.shellMode = ShellMode.COMMAND_READ; // default start reading a command


		// load history
		if (historyManager == null || historyManager.getActiveHistory() == null) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "historyManager or active history object is null.. something goes wrong");
		}


		try {

			while (!quit) {


				switch (this.shellMode) {

					case COMMAND_READ:
						printPrompt();
						line = readCommand();

						if (line != null) {
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "COMMAND READ:" + line);
							this.getParser().processLine(line, mode);

						}
						break;
					case NORMAL_READ:
						try {
							this.getNormalRead().run();
						} catch (InterruptedException ex) {
							Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Blocking IO operation stopped");
						}
						break;
					case INPUT_FIELD:
						line = readInput();

						if (line != null) {
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "INPUT FIELD READ:" + line);
							this.getParser().catchUserInput(line);
						}

						break;

				}
			}

		} catch (Exception ex) {

			if (quit) // if there is a quit request, then it is ok
			{
				return 0;
			} else {
				Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Exception occured, when reading a line from telnet, closing program: " + "CommandShell");
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
				return -1;
			}
		} finally {

			// save history
			this.historyManager.saveAllHistory();

		}

		return 0;
	}

	@Override
	public int quit() {

		if (this.shellRenderer != null) {
			this.shellRenderer.quit();
		}

		if (this.childProcess != null) // quit child process
		{
			this.childProcess.quit();
		}
		
		if(historyManager != null )
		{
			historyManager.saveAllHistory();
		}

		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Quiting CommandShell");
		this.quit = true;
		return 0;
	}

	public void clearScreen() {
		try {
			this.getShellRenderer().clearScreen();
		} catch (Exception ex) {
		}

	}

	@Override
	public String getDescription() {
		return device.getName() + ": CommandShell";
	}
}
