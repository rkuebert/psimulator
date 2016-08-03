/*
 * Vytvoreno 30.1.2012.
 */
package commands;

import commands.LongTermCommand.Signal;
import device.Device;
import java.util.ArrayList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import psimulator2.Psimulator;
import shell.apps.CommandShell.CommandShell;
import shell.apps.CommandShell.ShellMode;
import utils.Util;

/**
 * Abstraktni parser prikazu spolecnej pro linux i pro cisco.
 *
 * Parser si bude muset pamatovat posledni spusteny prikaz, aby kdyz dostane signal SIG_INT, aby dokazal poslat prikaz
 * vypnuti posledni spustene aplikace/prikazu.
 *
 * @author Stanislav Rehak
 */
public abstract class AbstractCommandParser implements Loggable {

	protected CommandShell shell;
	public final Device device;
	/**
	 * Seznam slov v prijatem radku.
	 */
	protected List<String> words = new ArrayList<>();
	/**
	 * Obcas je potreba cely radek.
	 */
	protected String line;
	/**
	 * ukazatel do seznamu slov.
	 */
	protected int ref;

	/**
	 * Vesmes docasne uloziste modu, aby se nemusel predavat do vsech funkci, kde je potreba.
	 * Vlastnikem modu je CommandShell, protoze preci Shell musi vedet, ve kterem stavu on sam je.
	 */
	protected int mode;

	/**
	 * Kvuli prikazum, ktere bezi dlouho, ty budou muset bezet ve vlastnim vlakne a zde bude na ne odkaz.
	 * Az prikaz skonci, tak tohle bude null.
	 */
	protected LongTermCommand runningCommand = null;

	public AbstractCommandParser(Device networkDevice, CommandShell shell) {
		this.device = networkDevice;
		this.shell = shell;
	}

	/**
	 * Add completers for every mode you want to complete commands.
	 * Call in constructor of concrete Completer.
	 */
	protected abstract void addCompleters();
	/**
	 * Add data to completers.
	 * Call in constructor of concrete Completer.
	 */
	protected abstract void addCompletionData();

	/**
	 * Zpracuje radek.
	 * Tuto metodu bude volat CommandShell
	 *
	 * @param line radek ke zpracovani
	 * @param mode aktualni mod toho shellu (pro cisco)
	 * @return navratovy kod
	 */
	public void processLine(String line, int mode) {
		try {	// chytani vyjimek, aby se nepropagovaly dal do telnetu

			if (runningCommand != null) {
				runningCommand.catchUserInput(line);
				return;
			}

			this.line = line;
			this.mode = mode;
			this.ref = 0;
			words.clear();
			words.addAll(Util.splitLine(line));

			if (line.isEmpty()) {
				return;
			}


			if (!processSharedCommands()) {	// zkusi se, jestli to neni spolecnej prikaz
				processLineForParsers();	// jinak se zavola konkretni parser
			}

		} catch (Exception ex) {
			Logger.log(this, Logger.WARNING, LoggingCategory.GENERIC_COMMANDS, "Nekde byla hozena vyjimka.", ex);
		}

		Logger.log(this, Logger.DEBUG, LoggingCategory.LINUX_COMMANDS, "Ukoncena metoda processLine u abstraktniho parseru, tim se vraci rizeni behu programu shellu.", null); // mam to pro linux
	}

	public void catchUserInput(String line) {
		if (runningCommand != null) {
			runningCommand.catchUserInput(line);
		} else {
			Logger.log(this, Logger.WARNING, LoggingCategory.GENERIC_COMMANDS, "zavolan catchUserInput a pritom neni spusten zadny prikaz!!! Zahazuju vstup..", null);
		}
	}

	public abstract void catchSignal(Signal signal);

	public CommandShell getShell() {
		return shell;
	}

	public LongTermCommand getRunningCommand() {
		return runningCommand;
	}

	/**
	 * Zaregistruje dele bezici prikaz.
	 * @param runningCommand
	 * @param inputExpected Jestli prikaz ocekava vstup, podle tyhle promenny se nastavuje mod shellu.
	 */
	public void setRunningCommand(LongTermCommand runningCommand, boolean inputExpected) {
		this.runningCommand = runningCommand;
		if (inputExpected) {
			shell.setShellMode(ShellMode.INPUT_FIELD);
		} else {
			shell.setShellMode(ShellMode.NORMAL_READ); //jen pokus
		}
	}

	public void deleteRunningCommand() {
		this.runningCommand = null;
		shell.setShellMode(ShellMode.COMMAND_READ);
	}

	/**
	 * Tuto metodu musi implementovat parsery.
	 * V tuto chvili uz jsou naplneny promenne words i mode.
	 *
	 */
	protected abstract void processLineForParsers();

	/**
	 * Vrati shellu informaci, zda ma vypisovat prompt.
	 * @return
	 */
	public boolean isCommandRunning() {
		if (runningCommand == null) {
			return true;
		}
		return false;
	}

	/**
	 * Slouzi k servisnim vypisum o napr nepodporovanych prikazech.
	 * @param line
	 */
	public void printService(String line) {
		shell.printLine(Psimulator.getNameOfProgram()+": "+line);
	}

	/**
	 * Tahle metoda postupne vraci words, podle vnitrni promenny ref. Pocita s tim, ze prazdny retezec ji nemuze prijit.
	 *
	 * @return prazdny retezec, kdyz je na konci seznamu
	 */
	public String nextWord() {
		String res;
		if (ref < words.size()) {
			res = words.get(ref);
			ref++;
		} else {
			res = "";
		}
		return res;
	}

	/**
	 * Tahle metoda postupne dela to samy, co horni, ale nezvysuje citac. Slouzi, kdyz je potreba zjistit, co je dal za
	 * slovo, ale zatim jenom zjistit.
	 *
	 * @return prazdny retezec, kdyz je na konci seznamu
	 */
	public String nextWordPeek() {
		String res;
		if (ref < words.size()) {
			res = words.get(ref);
		} else {
			res = "";
		}
		return res;
	}

	public List<String> getWords() {
		return words;
	}

	protected int getRef() {
		return ref;
	}

	/**
	 * Tomasova debugovaci metoda.
	 * @return
	 */
	public String getWordsAsString(){
		String vratit="Words: ";
		for(String s:words){
			vratit+="^"+s+"^ ";
		}
		return vratit;
	}

	/**
	 * Obsluhuje prikazy spolecny pro linux i cisco.
	 * @return true, kdyz to byl spolecny prikaz a je tedy vyrizen.
	 */
	private boolean processSharedCommands() {

		String commandName = nextWordPeek();

		if(commandName.equals("save")||commandName.equals("uloz")){
			PsimulatorSave cmd = new PsimulatorSave(this);
			cmd.run();
			return true;
		}

//		if (commandName.equals("nat")) {
//			NatDynamicRecords cmd = new NatDynamicRecords(this);
//			cmd.run();
//			return true;
//		}

		if (commandName.equals("rnetconn")) {
			nextWord();	// potreba pro zvetseni citace
			Rnetconn cmd = new Rnetconn(this);
			cmd.run();
			return true;
		}

		return false;
	}

	@Override
	public String getDescription() {
		return device.getName()+": AbstractCommandParser";
	}


}
