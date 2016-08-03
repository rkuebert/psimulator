/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

/**
 *
 * @author Martin Lukáš
 */
public class TelnetConnectionException extends Exception {

    public TelnetConnectionException(Throwable cause) {
        super(cause);
    }

    public TelnetConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TelnetConnectionException(String message) {
        super(message);
    }

    public TelnetConnectionException() {
    }
}
