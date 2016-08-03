/*
 * Erstellt am 30.10.2011.
 */

package dataStructures.ipAddresses;

/**
 *
 * @author neiss
 */
public class BadIpException extends RuntimeException {

    /**
     * Creates a new instance of <code>BadIpException</code> without detail message.
     */
    public BadIpException() {
    }


    /**
     * Constructs an instance of <code>BadIpException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BadIpException(String msg) {
        super(msg);
    }
}
