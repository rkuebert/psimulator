package psimulator.userInterface.SaveLoad;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import psimulator.dataLayer.DataLayerFacade;
import shared.Serializer.SaveLoadExceptionParametersWrapper;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractSaveLoadManager {
    protected DataLayerFacade dataLayer;
    protected Component parentComponent;
    protected JFileChooser fileChooser;
    //
    protected File file;
    protected long lastSavedTimestamp;
    
    public AbstractSaveLoadManager(Component parentComponent, DataLayerFacade dataLayer) {
        this.parentComponent = parentComponent;
        this.dataLayer = dataLayer;
        
        // create file chooser
        fileChooser = new JFileChooser();

        // set texts to file chooser
        setTextsToFileChooser();
    }

    public void updateTextsOnFileChooser() {
        setTextsToFileChooser();
    }
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public void setLastSavedTimestamp() {
        setLastSavedTimestamp(System.currentTimeMillis());
    }
    
    public long getLastSavedTimestamp() {
        return lastSavedTimestamp;
    }

    public void setLastSavedTimestamp(long lastSavedTimestamp) {
        this.lastSavedTimestamp = lastSavedTimestamp;
    }

    public void setLastSavedFile(File file) {
        setFile(file);
    }
    
    protected final int showWarningPossibleDataLossDialog(String title, String message) {
        Object[] options = {dataLayer.getString("SAVE"), dataLayer.getString("DONT_SAVE"), dataLayer.getString("CANCEL")};
        int n = JOptionPane.showOptionDialog(parentComponent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, //do not use a custom Icon
                options, //the titles of buttons
                options[0]); //default button title

        return n;
    }
    
    protected final int showWarningPossibleOverwriteDialog(String title, String message) {
        Object[] options = {dataLayer.getString("YES"), dataLayer.getString("NO"), dataLayer.getString("CANCEL")};
        int n = JOptionPane.showOptionDialog(parentComponent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, //do not use a custom Icon
                options, //the titles of buttons
                options[0]); //default button title

        return n;
    }
    
    protected final void showWarningSaveLoadError(SaveLoadExceptionParametersWrapper parametersWrapper) {

        String title;

        if (parametersWrapper.isSaving()) {
            title = dataLayer.getString("ERROR_WHILE_SAVING");
        } else {
            title = dataLayer.getString("ERROR_WHILE_LOADING");
        }

        String message;

        switch (parametersWrapper.getSaveLoadExceptionType()) {
            case FILE_DOES_NOT_EXIST:
                message = dataLayer.getString("FILE_DOES_NOT_EXIST");
                break;
            case CANT_READ_FROM_FILE:
                message = dataLayer.getString("FILE_IS_LOCKED_FOR_READ");
                break;
            case ERROR_WHILE_READING:
                message = dataLayer.getString("ERROR_OCCOURED_WHILE_READING_FROM_FILE");
                break;
            case CANT_WRITE_TO_FILE:
                message = dataLayer.getString("FILE_IS_LOCKED_FOR_WRITE");
                break;
            case ERROR_WHILE_WRITING:
                message = dataLayer.getString("ERROR_OCCOURED_WHILE_WRITING_TO_FILE");
                break;
            case ERROR_WHILE_CREATING:
                message = dataLayer.getString("ERROR_OCCOURED_WHILE_CREATING_NEW_FILE");
                break;
            case CANNOT_OVERWRITE:
                return;
            default:
                message = "default in main window in method show warning save load error";
                break;
        }

        JOptionPane.showMessageDialog(parentComponent,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Sets internationalized texts to file chooser
     */
    protected final void setTextsToFileChooser() {
        UIManager.put("FileChooser.lookInLabelText", dataLayer.getString("FILE_CHOOSER_LOOK_IN"));
        UIManager.put("FileChooser.filesOfTypeLabelText", dataLayer.getString("FILE_CHOOSER_FILES_OF_TYPE"));
        UIManager.put("FileChooser.upFolderToolTipText", dataLayer.getString("FILE_CHOOSER_UP_FOLDER"));

        UIManager.put("FileChooser.fileNameLabelText", dataLayer.getString("FILE_CHOOSER_FILE_NAME"));
        UIManager.put("FileChooser.homeFolderToolTipText", dataLayer.getString("FILE_CHOOSER_HOME_FOLDER"));
        UIManager.put("FileChooser.newFolderToolTipText", dataLayer.getString("FILE_CHOOSER_NEW_FOLDER"));
        UIManager.put("FileChooser.listViewButtonToolTipTextlist", dataLayer.getString("FILE_CHOOSER_LIST_VIEW"));
        UIManager.put("FileChooser.detailsViewButtonToolTipText", dataLayer.getString("FILE_CHOOSER_DETAILS_VIEW"));
        UIManager.put("FileChooser.saveButtonText", dataLayer.getString("FILE_CHOOSER_SAVE"));
        UIManager.put("FileChooser.openButtonText", dataLayer.getString("FILE_CHOOSER_OPEN"));
        UIManager.put("FileChooser.cancelButtonText", dataLayer.getString("FILE_CHOOSER_CANCEL"));
        UIManager.put("FileChooser.updateButtonText=", dataLayer.getString("FILE_CHOOSER_UPDATE"));
        UIManager.put("FileChooser.helpButtonText", dataLayer.getString("FILE_CHOOSER_HELP"));
        UIManager.put("FileChooser.saveButtonToolTipText", dataLayer.getString("FILE_CHOOSER_SAVE"));
        UIManager.put("FileChooser.openButtonToolTipText", dataLayer.getString("FILE_CHOOSER_OPEN"));
        UIManager.put("FileChooser.cancelButtonToolTipText", dataLayer.getString("FILE_CHOOSER_CANCEL"));
        UIManager.put("FileChooser.updateButtonToolTipText", dataLayer.getString("FILE_CHOOSER_UPDATE"));
        UIManager.put("FileChooser.helpButtonToolTipText", dataLayer.getString("FILE_CHOOSER_HELP"));


        UIManager.put("FileChooser.openDialogTitleText", dataLayer.getString("FILE_CHOOSER_OPEN"));
        UIManager.put("FileChooser.saveDialogTitleText", dataLayer.getString("FILE_CHOOSER_SAVE"));
        UIManager.put("FileChooser.fileNameHeaderText", dataLayer.getString("FILE_CHOOSER_FILE_NAME"));
        UIManager.put("FileChooser.newFolderButtonText", dataLayer.getString("FILE_CHOOSER_NEW_FOLDER"));

        UIManager.put("FileChooser.renameFileButtonText", dataLayer.getString("FILE_CHOOSER_RENAME_FILE"));
        UIManager.put("FileChooser.deleteFileButtonText", dataLayer.getString("FILE_CHOOSER_DELETE_FILE"));
        UIManager.put("FileChooser.filterLabelText", dataLayer.getString("FILE_CHOOSER_FILE_TYPES"));
        UIManager.put("FileChooser.fileSizeHeaderText", dataLayer.getString("FILE_CHOOSER_SIZE"));
        UIManager.put("FileChooser.fileDateHeaderText", dataLayer.getString("FILE_CHOOSER_DATE_MODIFIED"));

        UIManager.put("FileChooser.saveInLabelText", dataLayer.getString("FILE_CHOOSER_LOOK_IN"));
        UIManager.put("FileChooser.acceptAllFileFilterText", dataLayer.getString("FILE_CHOOSER_ACCEPT_FILES"));

        // let fileChooser to update according to current look and feel = it loads texts againt
        fileChooser.updateUI();
    }
}
