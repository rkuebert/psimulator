package psimulator.dataLayer.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class LanguageLoader {

    private List<File> listOfFiles;

    /**
     * Returns all resource bundles from directoryPath that contains all keys from referenceBundles.
     * Result is in HashMap with language name as key and ResourceBundle as value.
     * 
     * @param directoryPath Path to directory where to look for bundles
     * @param referenceBundle Bundle with reference keys
     * @return HashMap with language name as key and ResourceBundle as value.
     */
    public HashMap<String, ResourceBundle> getAllResouceBundles(String directoryPath, ResourceBundle referenceBundle) {
        HashMap<String, ResourceBundle> bundles = new HashMap<String, ResourceBundle>();
        
        File folder = new File(directoryPath);
        
        // if folder does not exist, or isnt directory or cant be red
        if(!(folder.exists() && folder.isDirectory() && folder.canRead())){
            return bundles;
        }
        
        listOfFiles = getAllFilesInDirectory(folder);    // "./Languages"
        
        Iterator<File> it = listOfFiles.iterator();

        while (it.hasNext()) {
            File file = it.next();
            
            // load bundle from file
            ResourceBundle bundle = loadBundle(file);
            
            // chceck consistency of bundle
            if(!isBundleValid(referenceBundle, bundle)){
                // id bundle not valid, than test next bundle
                bundle = null;
                continue;
            }
            
            // save bundle into HashMap
            String languageName = bundle.getString("BUNDLE_LANGUAGE_NAME");
            bundles.put(languageName, bundle);
            
        }
        listOfFiles = null;
        
        // return hash map
        return bundles;
    }

    /**
     * Test whether test bundle contains all keys as reference bundle
     * 
     * @param referenceBundle
     * @param testBundle
     * @return 
     */
    private boolean isBundleValid(ResourceBundle referenceBundle, ResourceBundle testBundle) {
        Set refKeys = referenceBundle.keySet();

        Iterator<String> it = refKeys.iterator();

        while (it.hasNext()) {
            String key = it.next();
            
            // if tested bundle does not contain key, than bundle is not valid
            if(!testBundle.containsKey(key)){
                return false;
            }
            if(testBundle.getString(key).isEmpty()){
                return false;
            }
        }

        return true;
    }

    /**
     * loads resource bundle from File file
     * @param file File to be loaded
     * @return resourceBundle from file
     */
    private ResourceBundle loadBundle(File file) {
        ResourceBundle bundle = null;

        InputStreamReader reader = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            reader = new InputStreamReader(fis, Charset.forName("UTF-8"));
            bundle = new PropertyResourceBundle(reader);

        } catch (IOException ex) {
            // nothing to do
        } finally {
            try {
                fis.close();
                reader.close();
            } catch (IOException ex) {
                // nothing to do
            }
        }
        return bundle;
    }

    /**
     * Finds all files in given directory with .properties extension that can be read
     * 
     * @param directoryPath Path where to look for language files
     * @return LinkedList<File> with all files
     */
    private List<File> getAllFilesInDirectory(File folder) {

        listOfFiles = new LinkedList(Arrays.asList(folder.listFiles()));

        Iterator<File> it = listOfFiles.iterator();

        while (it.hasNext()) {
            File file = it.next();

            if (file.isFile() && file.canRead() && file.getName().endsWith(".properties") && file.length()<=10000L) {
                //System.out.println(file.length());
            } else {
                it.remove();
                continue;
            }
        }
        return listOfFiles;
    }
}
