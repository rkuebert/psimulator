package psimulator.dataLayer.interfaces;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface SaveableInterface {
    /**
     * Saves to preferences.
     */
    public void savePreferences();
    /**
     * Loads from preferences.
     */
    public void loadPreferences();
}
