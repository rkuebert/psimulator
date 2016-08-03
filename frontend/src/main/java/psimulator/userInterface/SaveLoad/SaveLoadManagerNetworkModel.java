package psimulator.userInterface.SaveLoad;

import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.RecentlyOpenedDirectoryType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphBuilder.GraphBuilderFacade;
import shared.Components.NetworkModel;
import shared.Serializer.SaveLoadException;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SaveLoadManagerNetworkModel extends AbstractSaveLoadManager {

    public SaveLoadManagerNetworkModel(Component parentComponent, DataLayerFacade dataLayer) {
        super(parentComponent, dataLayer);
    }

    /**
     *
     * @return true if data can be lost, false if cant be lost
     */
    public boolean doCheckIfPossibleDataLoss(Graph graph) {
        if (graph == null) {
            return false;
        }

        // if timestamps say graph was not modified
        if (graph.getLastEditTimestamp() <= getLastSavedTimestamp()) {
            return false;
        }

        return true;
    }

    /**
     * asks user what to do
     *
     * @param graph
     * @return
     */
    public SaveLoadManagerUserReaction doAskUserIfSave(Graph graph) {
        //save config data
        int i = showWarningPossibleDataLossDialog(dataLayer.getString("WINDOW_TITLE"), dataLayer.getString("CLOSING_NOT_SAVED_PROJECT"));

        // if canceled
        if (i == 2 || i == -1) {
            // do nothing
            //return false;
            return SaveLoadManagerUserReaction.CANCEL;
        }

        // if do not save
        if (i == 1) {
            return SaveLoadManagerUserReaction.DO_NOT_SAVE;
        }

        // if YES -> save
        if (i == 0) {
            //boolean result = doSaveGraphAction();
            return SaveLoadManagerUserReaction.DO_SAVE;
        }

        // should never happen
        return SaveLoadManagerUserReaction.CANCEL;
    }

    /**
     * Shows save dialog.
     *
     */
    public boolean doSaveAsGraphAction() {
        try {
            // save as
            return saveAs();
        } catch (SaveLoadException ex) {
            showWarningSaveLoadError(ex.getParametersWrapper());
            return false;
        }
    }

    /**
     * saves without dialog Returns true if succesfull
     *
     * @throws SaveLoadException
     */
    public boolean doSaveGraphAction() {
        File selectedFile = getFile();

        try {
            // same as save as but do not ask the user
            if (selectedFile != null) {
                // save
                save(selectedFile);
            } else { // same as save as
                // save as
                return saveAs();
            }
        } catch (SaveLoadException ex) {
            showWarningSaveLoadError(ex.getParametersWrapper());
            return false;
        }
        return true;
    }

    /**
     * Opens dialog and loads model. If exception occours, warning dialog is shown.
     * @return 
     */
    public NetworkModel doLoadNetworkModel() {
        try {
            return open();
        } catch (SaveLoadException ex) {
            showWarningSaveLoadError(ex.getParametersWrapper());
            return null;
        }
    }

    /**
     * Opens netwrok from file. If exception occours, warning dialog is shown.
     * @param filePath
     * @return 
     */
    public NetworkModel doLoadNetworkModel(String filePath) {
        try {
            File fileToOpen = new File(filePath);
            return open(fileToOpen);
        } catch (SaveLoadException ex) {
            showWarningSaveLoadError(ex.getParametersWrapper());
            return null;
        }
    }

    /**
     * Builds graph from networkModel.
     * @param networkModel
     * @return 
     */
    public Graph buildGraphFromNetworkModel(NetworkModel networkModel) {
        GraphBuilderFacade graphBuilderFacade = new GraphBuilderFacade();
        Graph graph = graphBuilderFacade.buildGraph(networkModel);

        return graph;
    }

    private NetworkModel open() throws SaveLoadException {
        File recentDir = dataLayer.getRecentDirectory(RecentlyOpenedDirectoryType.NETWORKS_DIR);
        if(recentDir != null){
            fileChooser.setCurrentDirectory(recentDir);
        }
        
        int returnVal = fileChooser.showOpenDialog(parentComponent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selctedFile = fileChooser.getSelectedFile();

            // save current directory
            recentDir = fileChooser.getCurrentDirectory();
            dataLayer.setRecentDirectory(RecentlyOpenedDirectoryType.NETWORKS_DIR, recentDir);

            return open(selctedFile);
        }
        return null;
    }

    private NetworkModel open(File fileToOpen) throws SaveLoadException {
        // set wait cursor
        parentComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
        
        // load network model
        NetworkModel networkModel = dataLayer.loadNetworkModelFromFile(fileToOpen);

        // set saved timestamp and file name
        setLastSavedFile(fileToOpen);

        // add to recently opened
        dataLayer.addRecentOpenedFile(file);

        // save preferences
        dataLayer.savePreferences();

        return networkModel;
    }

    /**
     * Returns true if success
     *
     * @param graph
     * @return
     * @throws SaveLoadException
     */
    private boolean saveAs() throws SaveLoadException {
        File recentDir = dataLayer.getRecentDirectory(RecentlyOpenedDirectoryType.NETWORKS_DIR);
        if(recentDir != null){
            fileChooser.setCurrentDirectory(recentDir);
        }
        
        int returnVal = fileChooser.showSaveDialog(parentComponent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selctedFile = fileChooser.getSelectedFile();
            
            // save current directory
            recentDir = fileChooser.getCurrentDirectory();
            dataLayer.setRecentDirectory(RecentlyOpenedDirectoryType.NETWORKS_DIR, recentDir);

            // check if overwrite
            if (selctedFile.exists()) {
                int i = showWarningPossibleOverwriteDialog(dataLayer.getString("WINDOW_TITLE"), dataLayer.getString("DO_YOU_WANT_TO_OVERWRITE"));

                // if OK, save dialog
                if (i == JOptionPane.OK_OPTION) {
                    // save
                    save(selctedFile);
                    
                    // add to recently opened
                    dataLayer.addRecentOpenedFile(file);
                    
                    return true;
                }

                // if CANCEL, show dialog again
                if (i == JOptionPane.NO_OPTION) {
                    return saveAs();
                }

                // cancel or quit dialog
                return false;
            } else {
                // save
                save(selctedFile);
                
                // add to recently opened
                dataLayer.addRecentOpenedFile(file);
                    
                return true;
            }
        }
        return false;
    }

    private void save(File file) throws SaveLoadException {
        // set wait cursor
        parentComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // save graph
        //dataLayer.saveGraphToFile(graph, file);
        dataLayer.saveNetworkModelToFile(file);

        // set saved timestamp
        setLastSavedFile(file);
        setLastSavedTimestamp();
    }
}
