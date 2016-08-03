/*
 * created 2.2.2012
 */
package logging;

/**
 * Vsechny tridy, ktere budou chtit logovat, tak budou muset implementovat tuto metodu, aby bylo jasne, od koho logovaci
 * zprava prisla.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public interface Loggable {

	public String getDescription();
}
