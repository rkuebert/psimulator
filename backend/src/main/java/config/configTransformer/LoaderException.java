/*
 * Erstellt am 2.3.2012.
 */
package config.configTransformer;

/**
 *
 * @author neiss
 */
public class LoaderException extends RuntimeException {

	/**
	 * Creates a new instance of
	 * <code>LoaderException</code> without detail message.
	 */
	public LoaderException() {
	}

	/**
	 * Constructs an instance of
	 * <code>LoaderException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public LoaderException(String msg) {
		super(msg);
	}
}
