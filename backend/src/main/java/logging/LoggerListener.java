/*
 * created 2.2.2012
 */
package logging;

/**
 * Vsechny tridy, ktere budou chtit zpracovavat logovaci zpravu, tak budou implementovat toto rozhrani.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public interface LoggerListener {

	public void listen(Loggable caller, int logLevel, LoggingCategory category, String message, Object object);
	public void listen(String name, int logLevel, LoggingCategory category, String message);

}
