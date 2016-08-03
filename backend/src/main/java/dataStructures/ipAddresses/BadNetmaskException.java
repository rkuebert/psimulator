/*
 * Erstellt am 30.10.2011.
 */

package dataStructures.ipAddresses;

/**
 *
 * @author neiss
 */
public class BadNetmaskException extends RuntimeException {

    /**
     * Creates a new instance of <code>BadNetmaskException</code> without detail message.
     */
    public BadNetmaskException() {
    }


    /**
     * Constructs an instance of <code>BadNetmaskException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BadNetmaskException(String msg) {
        super(msg);
    }
}
