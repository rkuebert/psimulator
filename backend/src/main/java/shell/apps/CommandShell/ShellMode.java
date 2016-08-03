/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public enum ShellMode {
	
	/**
	 * mode for reading command
	 */
	COMMAND_READ,
	/**
	 * mode mostly used for listening signals and unexpected input,  when someone print into shell, reading data are pass on to the parser
	 */
	NORMAL_READ,
	/**
	 * mode for reading a single line, in this mode printing into shell is forbidden
	 */
	INPUT_FIELD
	
	
	
}
