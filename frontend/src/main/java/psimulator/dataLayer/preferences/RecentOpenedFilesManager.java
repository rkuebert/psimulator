package psimulator.dataLayer.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class RecentOpenedFilesManager {

    private static final String DELIMITER = ";";
    public static final int MAX_COUNT = 10;
    private List<File> recentOpenedFiles;

    public RecentOpenedFilesManager() {
    }
    
    
    public int getSize(){
        return recentOpenedFiles.size();
    }
    
    /**
     * Creates files string from list of recent opened files
     * @return 
     */
    public String createStringFromFiles(){
        String s = "";
        
        for(File file : recentOpenedFiles){
            s += file.getAbsolutePath() + ";";
        }
        
        return s;
    }
    
    /**
     * Initializes RecentOpendFilesManager with files in parameter string
     * @param filesInString 
     */
    public void parseFilesFromString(String filesInString){
        List<String> filePathsList = parseFilesStringToList(filesInString);
        recentOpenedFiles = createFilesList(filePathsList);
    }

    /**
     * Returns recent opend files list
     * @return 
     */
    public List<File> getRecentOpenedFiles() {
        return recentOpenedFiles;
    }

    /**
     * Adds file at begining of the list. Files over max count are removed.
     * @param file 
     */
    public void addFile(File file){
        File oldFileRecord = null;
        
        // find if file of this name is in the list
        for(File f : recentOpenedFiles){
            if(f.toString().equals(file.toString())){
                oldFileRecord = f;
            }
        }
        
        // if it is in the list, remove it from the list
        if(oldFileRecord != null){
            recentOpenedFiles.remove(oldFileRecord);
        }
        
        // add file to the beginning of the list
        recentOpenedFiles.add(0, file);
        
        // if list size exceeds max count, remove exceeding file
        if(recentOpenedFiles.size() > MAX_COUNT){
            int removeCount = recentOpenedFiles.size() - MAX_COUNT;
            
            for(int i = 0; i < removeCount; i++){
                recentOpenedFiles.remove(MAX_COUNT);
            }
        }
    }
    
    /**
     * Removes not existing files from list
     */
    public void clearNotExistingFiles(){
        List<File> newFiles = new LinkedList<>();
        for(File file : recentOpenedFiles){
            if (file.exists()) {
                newFiles.add(file);
            }
        }
        recentOpenedFiles = newFiles;
    }
    
    /**
     * Checks if files in parameter exists.
     * @param files
     * @return true if exists, false if some doesnt exist.
     */
    public boolean checkFilesIfExists(List<File> files){
        for(File file : files){
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Creates files list from path list
     * @param filePathsList
     * @return 
     */
    private List<File> createFilesList(List<String> filePathsList){
        List<File> files = new LinkedList<>();
        
        for(String str : filePathsList){
            if(str.isEmpty()){
                continue;
            }
            
            File tmpFile = new File(str);
            
            files.add(tmpFile);
        }
        return files;
    }

    /**
     * Parses filenames from parameter string. Ues DELIMITER.
     * @param filesInString
     * @return 
     */
    private List<String> parseFilesStringToList(String filesInString) {
        List<String> filePathsList = new ArrayList<>();

        String[] tmp = filesInString.split(DELIMITER);
        filePathsList.addAll(Arrays.asList(tmp));

        return filePathsList;
    }
}
