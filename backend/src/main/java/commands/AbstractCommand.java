/*
 * created 6.3.2012
 */
package commands;

import device.Device;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.IpNetworkModule;
import networkModule.NetworkModule;

/**
 * Parent of all commands in parsing system.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class AbstractCommand implements Loggable {

	public final AbstractCommandParser parser;

	/**
	 * Constructor - don't put any unnecessary reference here.
	 *
	 * @param parser
	 */
	public AbstractCommand(AbstractCommandParser parser) {
		this.parser = parser;
	}

	/**
	 * Call this method for starting a commaad.
	 *
	 * @return
	 */
	public abstract void run();

	/**
	 * Returns next word or "".
	 * @return
	 */
	public String nextWord() {
		return parser.nextWord();
	}

	/**
	 * Returns peek of next word.
	 * @return
	 */
	public String nextWordPeek() {
		return parser.nextWordPeek();
	}

	/**
	 * Shortcut: returns value of pointer in list of words.
	 *
	 * @return
	 */
	protected int getRef() {
		return parser.ref;
	}

	/**
	 * Shortcut: returns device.
	 *
	 * @return
	 */
	protected Device getDevice() {
		return parser.device;
	}

	/**
	 * Shortcut: returns TCP/IP metwork module.
	 *
	 * @return
	 */
	protected IpNetworkModule getNetMod() {
		NetworkModule nm = parser.device.getNetworkModule();
		if (nm.isStandardTcpIpNetMod()) {
			return (IpNetworkModule) nm;
		} else {
			Logger.log(getDescription(), Logger.ERROR, LoggingCategory.GENERIC_COMMANDS, "Prikaz zavolal TcpIpNetmod, kterej ale device nema.");
			return null;
		}
	}

	@Override
	public String getDescription() {
		return getDevice().getName() + ": command " + this.getClass().getSimpleName().toLowerCase();
	}

	/**
	 * Shortcut: print line to a shell.
	 */
	public void printLine(String s) {
		parser.getShell().printLine(s);
	}

	/**
	 * Shortcut: print string to a shell.
	 */
	public void print(String s) {
		parser.getShell().print(s);
	}

	/**
	 * Shortcut: print lines to a shell with given delay.
	 */
	protected void printWithDelay(String s, int delay) {
		parser.getShell().printWithDelay(s, delay);
	}

	/**
	 * Shortcut: print line to a shell - use iff you need to print something simulator specific.
	 */
	protected void printService(String s) {
		parser.printService(s);
	}
}
