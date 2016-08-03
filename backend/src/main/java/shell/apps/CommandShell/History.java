package shell.apps.CommandShell;

import device.Device;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class History {

	private ArrayList<String> commands;
	private ListIterator<String> commandIterartor;
	private String historyPathFile;
	private boolean calledNext = false;
	private boolean calledPrevious = false;
	private String activeHistoryLine;
	private Device deviceReference;
	private Date loaded;
	private Date saved;
	private int lastSavedSize;

	public History(String historyPathFile, Device deviceReference) {
		this.historyPathFile = historyPathFile;
		this.deviceReference = deviceReference;
	}

	public Date getLoaded() {
		return loaded;
	}

	public Date getSaved() {
		return saved;
	}

	public ArrayList<String> getCommands() {
		return commands;
	}

	/**
	 * reseting history command iteration method. When command is commited or ..
	 */
	private void resetIterator() {
		this.commandIterartor = this.commands.listIterator(this.commands.size());
	}

	/**
	 * if this history object was not used for last, then it should be activated with this method. This method basicaly
	 * just reset history commands iteration and load data from file if needed
	 */
	public void activate() {

		if (this.loaded == null) {
			this.load();
		}

		this.resetIterator();
	}

	/**
	 * add commited command into history list
	 *
	 * @param command
	 */
	public void addCommand(String command) {

		if (command == null) {
			return;
		}

		command = command.trim();

		if (command.isEmpty() || command.equalsIgnoreCase("")) { // do not add empty command
			return;
		}

		if (!this.commands.isEmpty()) {		// if history is not empty

			String lastCommand = this.commands.get(this.commands.size() - 1).trim();

			if (command.equalsIgnoreCase(lastCommand)) { // do not add two same commands
				resetIterator();
				return;
			}
		}

		this.calledNext = false;
		this.calledPrevious = false;
		this.activeHistoryLine = null;
		this.commands.add(command);
		resetIterator();
	}

	/**
	 * iterating in history to the oldest command
	 *
	 * @param sb
	 * @return
	 */
	public String handlePrevious(StringBuilder sb) {

		String ret = "";

		if (this.commands.isEmpty()) {
			return ret;
		}

		if (!this.commandIterartor.hasNext()) // there is no next = iterator pointing at the end => store active commandLine
		{
			this.activeHistoryLine = sb.toString();
		}

		if (calledNext) { // double iterate
			if (this.commandIterartor.hasPrevious()) {
				this.commandIterartor.previous();
			}

		}

		if (this.commandIterartor.hasPrevious()) {

			ret = this.commandIterartor.previous();
			calledNext = false;
			calledPrevious = true;


			sb.setLength(0);
			sb.append(ret);

		}

		return ret;

	}

	/**
	 * iterating in history to the newest command
	 *
	 * @param sb
	 * @return
	 */
	public String handleNext(StringBuilder sb) {

		String ret = "";

		if (this.commands.isEmpty()) {
			return ret;
		}

		if (calledPrevious) { // double iterate
			if (this.commandIterartor.hasNext()) {
				this.commandIterartor.next();
			}
		}


		if (this.commandIterartor.hasNext()) {
			ret = this.commandIterartor.next();

			this.calledNext = true;
			this.calledPrevious = false;

			sb.setLength(0);
			sb.append(ret);

		} else if (this.activeHistoryLine != null) {
			ret = this.activeHistoryLine;
			this.activeHistoryLine = null;

			this.calledNext = false;
			this.calledPrevious = false;

			sb.setLength(0);
			sb.append(ret);

		}

		return ret;
	}

	public void save() {

		if (lastSavedSize == this.commands.size()) // nothing to save
		{
			return;
		}

		this.deviceReference.getFilesystem().runOutputFileJob(this.historyPathFile, new OutputFileJob() {

			@Override
			public int workOnFile(OutputStream output) throws Exception {

				PrintWriter historyWriter = new PrintWriter(output);

				for (String command : commands) {
					historyWriter.println(command);
				}

				historyWriter.flush();

				return 0;
			}
		});

		this.lastSavedSize = this.commands.size();
		this.saved = new Date();


	}

	public void load() {

		final LinkedList<String> tempList = new LinkedList<>();

		if (!this.deviceReference.getFilesystem().isFile(historyPathFile)) // if there is no such history file
		{
			Logger.log(Logger.INFO, LoggingCategory.TELNET, "History file: " + historyPathFile + "not found. Using empty history.");
			commands = new ArrayList<>();
			return;
		}
		try {
			this.deviceReference.getFilesystem().runInputFileJob(this.historyPathFile, new InputFileJob() {

				@Override
				public int workOnFile(InputStream input) throws Exception {

					Scanner sc = new Scanner(input);

					while (sc.hasNextLine()) {
						tempList.add(sc.nextLine().trim());
					}

					return 0;
				}
			});
		} catch (FileNotFoundException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Something weird. Catched FileNotFoundException but file existence was confirmed. Using empty history.");
			commands = new ArrayList<>();
		}

// copy LINKEDLIST INTO ARRAYLIST -- using temporary linkedlist because of unknown size of list
		commands = new ArrayList<>(tempList.size() + 20);
		commands.addAll(tempList);


		this.loaded = new Date();
	}
}
