package psimulator.dataLayer.interfaces;

import java.util.Observer;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface LanguageInterface {
    /**
     * Sets current language of application.
     * @param languagePosition - Position of language
     */
    public void setCurrentLanguage(int languagePosition);
    /**
     * Gets avaiable language names as string array.
     * @return 
     */
    public Object[] getAvaiableLanguageNames();
    /**
     * Returns position of current language.
     * @return 
     */
    public int getCurrentLanguagePosition();
    
    /**
     * Gets translation of parameter.
     * @param string
     * @return 
     */
    public String getString(String string);
    
    /**
     * Adds observer to language change.
     * @param observer 
     */
    public void addLanguageObserver(Observer observer);
    
    /**
     * Removes observer from langage change.
     * @param observer 
     */
    public void deleteLanguageObserver(Observer observer);
}
