/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package commands;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public interface LongTermCommand {

	public enum Signal {
		/**
		 * Ctrl+C
		 */
		CTRL_C,
		/**
		 * Ctrl+Z
		 */
		CTRL_Z,
		/**
		 * CTRL+D
		 */
		CTRL_D,
		/**
		 * CTRL_SHIFT_6
		 */
		CTRL_SHIFT_6,
		

	}

	public void catchSignal(Signal signal);

	public void catchUserInput(String line);
}
