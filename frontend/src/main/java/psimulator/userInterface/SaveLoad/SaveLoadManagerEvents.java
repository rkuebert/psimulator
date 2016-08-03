package psimulator.userInterface.SaveLoad;

import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.RecentlyOpenedDirectoryType;
import shared.Serializer.SaveLoadException;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SaveLoadManagerEvents extends AbstractSaveLoadManager{
    
    public SaveLoadManagerEvents(Component parentComponent, DataLayerFacade dataLayer) {
        super(parentComponent, dataLayer);
    }
    
    /**
     * Shows save dialog.
     *
     */
    public boolean doSaveAsEventsAction(SimulatorEventsWrapper simulatorEvents) {
        try {
            // save as
            return saveAsEvents(simulatorEvents);
        } catch (SaveLoadException ex) {
            showWarningSaveLoadError(ex.getParametersWrapper());
            return false;
        }
    }
    
    /**
     * Shows open dialog
     */
    public SimulatorEventsWrapper doLoadEventsAction(){
        try {
            return load();
        } catch (SaveLoadException ex) {
            showWarningSaveLoadError(ex.getParametersWrapper());
            return null;
        }
    }
    
    
    private SimulatorEventsWrapper load() throws SaveLoadException {
        File recentDir = dataLayer.getRecentDirectory(RecentlyOpenedDirectoryType.EVENTS_DIR);
        if(recentDir != null){
            fileChooser.setCurrentDirectory(recentDir);
        }
        
        int returnVal = fileChooser.showOpenDialog(parentComponent);

        // save current directory
        recentDir = fileChooser.getCurrentDirectory();
        dataLayer.setRecentDirectory(RecentlyOpenedDirectoryType.EVENTS_DIR, recentDir);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selctedFile = fileChooser.getSelectedFile();
            
            // set wait cursor
            parentComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // load events from file
            SimulatorEventsWrapper simulatorEvents= dataLayer.loadEventsFromFile(selctedFile);

            // set saved timestamp and file name
            setLastSavedFile(selctedFile);
            
            //
            setLastSavedTimestamp();

            return simulatorEvents;
        }
        return null;
    }
    
    /**
     * Returns true if success
     */
    private boolean saveAsEvents(SimulatorEventsWrapper simulatorEvents) throws SaveLoadException {
        File recentDir = dataLayer.getRecentDirectory(RecentlyOpenedDirectoryType.EVENTS_DIR);
        if(recentDir != null){
            fileChooser.setCurrentDirectory(recentDir);
        }
        
        int returnVal = fileChooser.showSaveDialog(parentComponent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selctedFile = fileChooser.getSelectedFile();

            // save current directory
            recentDir = fileChooser.getCurrentDirectory();
            dataLayer.setRecentDirectory(RecentlyOpenedDirectoryType.EVENTS_DIR, recentDir);
            
            // check if overwrite
            if (selctedFile.exists()) {
                int i = showWarningPossibleOverwriteDialog(dataLayer.getString("WINDOW_TITLE"), dataLayer.getString("DO_YOU_WANT_TO_OVERWRITE"));
                
                // if OK, save dialog
                if(i == JOptionPane.OK_OPTION){
                    // save
                    saveEvents(selctedFile, simulatorEvents);
                    return true;
                }
                
                // if CANCEL, show dialog again
                if(i == JOptionPane.NO_OPTION){
                    return saveAsEvents(simulatorEvents);
                }
                
                // cancel or quit dialog
                return false;
            }else{
                // save
                saveEvents(selctedFile, simulatorEvents);
                return true;
            }
        }
        return false;
    }
    
    private void saveEvents(File file, SimulatorEventsWrapper simulatorEvents) throws SaveLoadException {
        // set wait cursor
        parentComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // save events
        dataLayer.saveEventsToFile(simulatorEvents, file);

        // set saved timestamp
        setLastSavedFile(file);
        setLastSavedTimestamp();
    }
    
    
}
