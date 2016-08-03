package psimulator.dataLayer.language;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.interfaces.SaveableInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class LanguageManager extends Observable implements SaveableInterface {

    private Preferences prefs;
    private final String LANGUAGE_PREFS = "LANGUAGE";
    private final String CZECH = "Čeština";
    private final String ENGLISH = "English";
    private final String DEFAULT_LANGUAGE = ENGLISH;
    private Locale enLocale = new Locale("en", "US");
    private Locale czLocale = new Locale("cz", "CZ");
    //private Locale currentLocale = czLocale;
    private ResourceBundle currentResouceBundle;
    private HashMap<String, ResourceBundle> bundles;
    private Object[] avaiableLanguagesSorted;
    private int selectedLanguagePosition;

    public LanguageManager() {
        LanguageLoader languageLoader = new LanguageLoader();

        // initialize preferences store
        prefs = Preferences.userNodeForPackage(this.getClass());

        // create resource bundles for Czech and English
        ResourceBundle czechBundle = ResourceBundle.getBundle("resources/i18n/Czech", czLocale);
        ResourceBundle englishBundle = ResourceBundle.getBundle("resources/i18n/English", enLocale);

        // default bundle is Czech
        currentResouceBundle = czechBundle;

        // load all resource bundles avaiable in Languages directory
        bundles = languageLoader.getAllResouceBundles("./Languages", currentResouceBundle);

        // add english and czech to hashMap
        bundles.put(CZECH, czechBundle);
        bundles.put(ENGLISH, englishBundle);

        /*
        for (String key : bundles.keySet()) {
            System.out.println("Key: " + key + ", Value: " + bundles.get(key));
        }*/

        // prepare sorted array with language names
        List<String> list = new ArrayList(bundles.keySet());
        // alphabetic sort
        Collections.sort(list, Collator.getInstance());

        // create array
        avaiableLanguagesSorted = list.toArray();

        // load langauge set in Preferences
        loadPreferences();

    }

    /**
     * Sets current language to language at languagePosition
     * @param languagePosition 
     */
    public void setCurrentLanguage(int languagePosition) {
        // if language position not possible
        if (!(languagePosition >= 0 && languagePosition < avaiableLanguagesSorted.length)) {
            // should not happen
            return;
        }

        // get name according to position
        String language = (String) avaiableLanguagesSorted[languagePosition];
        // set changed language as current
        setCurrentLanguage(language, languagePosition);
        
        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.LANGUAGE);
    }

    /**
     * Gets all avaiable language names sorted in array
     * @return Set of Strings with names
     */
    public Object[] getAvaiableLanguageNames() {
        return avaiableLanguagesSorted;
    }

    /**
     * Returns position of current language in languages array
     * @return Position
     */
    public int getCurrentLanguagePosition() {
        return selectedLanguagePosition;
    }

    /**
     * Returns string from current ResourceBundle
     * 
     * @param string
     * @return Expression in current language
     */
    public String getString(String string) {
        return currentResouceBundle.getString(string);
    }

    /**
     * Saves current language to Preferences
     */
    @Override
    public final void savePreferences() {
        //System.out.println("Saving to preferences:" + LANGUAGE_PREFS + " - " + (String) avaiableLanguagesSorted[selectedLanguagePosition]);
        prefs.put(LANGUAGE_PREFS, (String) avaiableLanguagesSorted[selectedLanguagePosition]);
    }

    /**
     * Loads current language from Preferences
     */
    @Override
    public final void loadPreferences() {
        // load language saved in preferences, if no language stored, than default is lodaded
        String language = prefs.get(LANGUAGE_PREFS, DEFAULT_LANGUAGE);
        //System.out.println("Loaded from preferences:" + LANGUAGE_PREFS + " - " + language);
        initializeLanguage(language);
    }

    /**
     * Initializes current language according to language if exists.If no, the DEFAULT_LANGUAGE is set/
     * @param language Language to initialize to
     */
    private void initializeLanguage(String language) {
        // find position of loaded language
        int position = findLanguagePosition(language);
        // if position not found (language of loaded name is not avaiable)
        if (position == -1) {
            // set default language
            position = findLanguagePosition(DEFAULT_LANGUAGE);
            language = DEFAULT_LANGUAGE;
        }
        // set current language
        setCurrentLanguage(language, position);
    }

    /**
     * Changes program current language to language in parameter (if exists).
     * @param language Language to switch the program
     * @param languagePosition Position of language in avaiableLanguagesSorted
     */
    private void setCurrentLanguage(String language, int languagePosition) {
        // if there is language of given name
        if (bundles.containsKey(language)) {
            selectedLanguagePosition = languagePosition;
            //System.out.println("Switching to " + language);
            currentResouceBundle = bundles.get(language);
            // save change to preferences
            savePreferences();
        } else {
            // should not happen
            //System.err.println("Neocekavana chyba v nastavovani jazyka");
        }
    }

    /**
     * Finds language in avaiableLanguagesSorted array and returns its position
     * @param language
     * @return position if found, otherwise -1
     */
    private int findLanguagePosition(String language) {
        int val = -1;
        // set position of default language in array
        for (int i = 0; i < avaiableLanguagesSorted.length; i++) {
            if (avaiableLanguagesSorted[i].equals(language)) {
                val = i;
            }
        }
        return val;
    }
}
