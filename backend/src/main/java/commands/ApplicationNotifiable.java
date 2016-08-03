/*
 * created 8.3.2012
 */
package commands;

/**
 * Rozhrani pro prikazy, ktery volaj ke svymu vykonani jinou aplikaci. Nejde bohuzel dedit od AbstractCommandu, protoze
 * od nej dedej uz LinuxCommand a CiscoCommand. Rozsirujici je metoda applicationFinished.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public interface ApplicationNotifiable {


	public void printLine(String s);


	public void print(String s);


	/**
	 * Application notifies that application has finished.
	 */
	public void applicationFinished();
}
