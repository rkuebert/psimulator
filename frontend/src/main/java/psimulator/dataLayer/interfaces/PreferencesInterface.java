package psimulator.dataLayer.interfaces;

import java.io.File;
import java.util.List;
import java.util.Observer;
import psimulator.dataLayer.Enums.LevelOfDetailsMode;
import psimulator.dataLayer.Enums.RecentlyOpenedDirectoryType;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.dataLayer.Enums.ViewDetailsType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.PacketImageType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface PreferencesInterface {
    /**
     * Gets current toolbar icon size.
     * @return 
     */
    public ToolbarIconSizeEnum getToolbarIconSize();
    /**
     * Sets current toolbar icon size.
     * @param size 
     */
    public void setToolbarIconSize(ToolbarIconSizeEnum size); 
    /**
     * Gets level of details mode.
     * @return 
     */
    public LevelOfDetailsMode getLevelOfDetails();
    /**
     * Sets current level of details mode.
     * @param levelOfDetails 
     */
    public void setLevelOfDetails(LevelOfDetailsMode levelOfDetails);
    
    
    /**
     * Gets if viewDetailsType in parameter is set to true.
     * @param viewDetailsType
     * @return 
     */
    public boolean isViewDetails(ViewDetailsType viewDetailsType);
    /**
     * Sets view details type in parameter to value.
     * @param viewDetailsType
     * @param value 
     */
    public void setViewDetails(ViewDetailsType viewDetailsType, boolean value);

    /**
     * Gets current package image type
     * @return 
     */
    public PacketImageType getPackageImageType();
    /**
     * Sets current package image type
     * @param packageImageType 
     */
    public void setPackageImageType(PacketImageType packageImageType);
    
    /**
     * Gets current IP address of psimulator server.
     * @return 
     */
    public String getConnectionIpAddress();
    /**
     * Sets current IP address of psimulator server
     * @param connectionIpAddress 
     */
    public void setConnectionIpAddress(String connectionIpAddress);
    /**
     * Gets port of psimulator server.
     * @return 
     */
    public String getConnectionPort();
    /**
     * Sets port of psimulator server.
     * @param connectionPort 
     */
    public void setConnectionPort(String connectionPort);
    
    /**
     * Saves preferences to preferences store.
     */
    public void savePreferences();
    
    /**
     * Adds observer to prefereneces changes.
     * @param observer 
     */
    public void addPreferencesObserver(Observer observer);
    /**
     * Removes observer from preferences.
     * @param observer 
     */
    public void deletePreferencesObserver(Observer observer);
    
    /**
     * Gets list of recently opened files.
     * @return 
     */
    public List<File> getRecentOpenedFiles();
    /**
     * Adds file to recently opened.
     * @param file 
     */
    public void addRecentOpenedFile(File file);
    
    /**
     * Sets recent directory of directory type.
     * @param directoryType
     * @param file 
     */
    public void setRecentDirectory(RecentlyOpenedDirectoryType directoryType, File file);
    /**
     * Gets recent directory of directory type.
     * @param directoryType
     * @return 
     */
    public File getRecentDirectory(RecentlyOpenedDirectoryType directoryType);
}
