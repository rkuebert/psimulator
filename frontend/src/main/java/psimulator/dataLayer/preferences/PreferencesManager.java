package psimulator.dataLayer.preferences;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.prefs.Preferences;
import psimulator.dataLayer.Enums.*;
import psimulator.dataLayer.interfaces.SaveableInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.PacketImageType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class PreferencesManager extends Observable implements SaveableInterface {

    // strings for saving
    private static final String TOOLBAR_ICON_SIZE = "TOOLBAR_ICON_SIZE";
    private static final String PACKAGE_IMAGE_TYPE = "PACKAGE_IMAGE_TYPE";
    private static final String VIEW_DEVICE_NAMES = "VIEW_DEVICE_NAMES";
    private static final String VIEW_DEVICE_TYPES = "VIEW_DEVICE_TYPES";
    private static final String VIEW_INTERFACE_NAMES = "VIEW_INTERFACE_NAMES";
    private static final String VIEW_CABLE_DELAY = "VIEW_CABLE_DELAY";
    private static final String VIEW_IP_ADDRESSES = "VIEW_IP_ADDRESSES";
    private static final String VIEW_MAC_ADDRESSES = "VIEW_MAC_ADDRESSES";
    private static final String AUTO_LEVEL_OF_DETAILS = "AUTO_LEVEL_OF_DETAILS";
    //
    private static final String VIEW_NETWORK_BOUNDS = "VIEW_NETWORK_BOUNDS";
    //
    private static final String CONNECTION_IP_ADDRESS = "CONNECTION_IP_ADDRESS";
    private static final String CONNECTION_PORT = "CONNECTION_PORT";
    //
    private static final String RECENTLY_OPENED_FILES = "RECENTLY_OPENED_FILES";
    private static final String RECENTLY_OPENED_NETWORK_DIR = "RECENTLY_OPENED_NETWORK_DIR";
    private static final String RECENTLY_OPENED_EVENT_DIR = "RECENTLY_OPENED_EVENT_DIR";
    // 
    private Preferences prefs;
    // set to default
    private ToolbarIconSizeEnum toolbarIconSize = ToolbarIconSizeEnum.MEDIUM;
    private PacketImageType packageImageType = PacketImageType.ENVELOPE;
    private LevelOfDetailsMode levelOfDetails = LevelOfDetailsMode.MANUAL;
    //
    private boolean viewDeviceNames = true;
    private boolean viewDeviceTypes = true;
    private boolean viewInterfaceNames = true;
    private boolean viewCableDelay = true;
    private boolean viewIpAddresses = true;
    private boolean viewMacAddresses = true;
    //
    private boolean viewNetworkBounds = false;
    //
    private String connectionIpAddress = "127.0.0.1";
    private String connectionPort = "12000";
    private String recentlyOpenedFiles = "";
    private String recentlyOpenedNetworkDir = "";
    private String recentlyOpenedEventDir = "";
    //
    private RecentOpenedFilesManager recentOpenedFilesManager;
    //

    public PreferencesManager() {
        // initialize preferences store
        prefs = Preferences.userNodeForPackage(this.getClass());

        // create recent opened files manager
        recentOpenedFilesManager = new RecentOpenedFilesManager();

        // load preferences
        loadPreferences();
    }

    /**
     * Saves preferences to preferences store.
     */
    @Override
    public void savePreferences() {
        // save toolbar icon size
        prefs.put(TOOLBAR_ICON_SIZE, toolbarIconSize.toString());
        prefs.put(PACKAGE_IMAGE_TYPE, packageImageType.toString());

        prefs.putBoolean(VIEW_DEVICE_NAMES, viewDeviceNames);
        prefs.putBoolean(VIEW_DEVICE_TYPES, viewDeviceTypes);
        prefs.putBoolean(VIEW_INTERFACE_NAMES, viewInterfaceNames);
        prefs.putBoolean(VIEW_CABLE_DELAY, viewCableDelay);
        prefs.putBoolean(VIEW_IP_ADDRESSES, viewIpAddresses);
        prefs.putBoolean(VIEW_MAC_ADDRESSES, viewMacAddresses);

        prefs.putBoolean(VIEW_NETWORK_BOUNDS, viewNetworkBounds);

        prefs.put(AUTO_LEVEL_OF_DETAILS, levelOfDetails.toString());

        prefs.put(CONNECTION_IP_ADDRESS, connectionIpAddress);
        prefs.put(CONNECTION_PORT, connectionPort);

        // get string with filenames from recentOpenedFilesManager
        recentlyOpenedFiles = recentOpenedFilesManager.createStringFromFiles();

        prefs.put(RECENTLY_OPENED_FILES, recentlyOpenedFiles);

        // save last directory
        prefs.put(RECENTLY_OPENED_NETWORK_DIR, recentlyOpenedNetworkDir);
        prefs.put(RECENTLY_OPENED_EVENT_DIR, recentlyOpenedEventDir);
    }

    /**
     * Loads preferences from preferences store.
     */
    @Override
    public void loadPreferences() {
        // load toolbar icon size
        toolbarIconSize = ToolbarIconSizeEnum.valueOf(prefs.get(TOOLBAR_ICON_SIZE, toolbarIconSize.toString()));
        packageImageType = PacketImageType.valueOf(prefs.get(PACKAGE_IMAGE_TYPE, packageImageType.toString()));

        viewDeviceNames = prefs.getBoolean(VIEW_DEVICE_NAMES, viewDeviceNames);
        viewDeviceTypes = prefs.getBoolean(VIEW_DEVICE_TYPES, viewDeviceTypes);
        viewInterfaceNames = prefs.getBoolean(VIEW_INTERFACE_NAMES, viewInterfaceNames);
        viewCableDelay = prefs.getBoolean(VIEW_CABLE_DELAY, viewCableDelay);
        viewIpAddresses = prefs.getBoolean(VIEW_IP_ADDRESSES, viewIpAddresses);
        viewMacAddresses = prefs.getBoolean(VIEW_MAC_ADDRESSES, viewMacAddresses);

        viewNetworkBounds = prefs.getBoolean(VIEW_NETWORK_BOUNDS, viewNetworkBounds);

        levelOfDetails = LevelOfDetailsMode.valueOf(prefs.get(AUTO_LEVEL_OF_DETAILS, levelOfDetails.toString()));

        connectionIpAddress = prefs.get(CONNECTION_IP_ADDRESS, connectionIpAddress);
        connectionPort = prefs.get(CONNECTION_PORT, connectionPort);

        recentlyOpenedFiles = prefs.get(RECENTLY_OPENED_FILES, recentlyOpenedFiles);

        // let Recent opened files manager parse the saved files
        recentOpenedFilesManager.parseFilesFromString(recentlyOpenedFiles);

        // clear non existing files
        recentOpenedFilesManager.clearNotExistingFiles();

        // load last directory
        recentlyOpenedNetworkDir = prefs.get(RECENTLY_OPENED_NETWORK_DIR, recentlyOpenedNetworkDir);
        recentlyOpenedEventDir = prefs.get(RECENTLY_OPENED_EVENT_DIR, recentlyOpenedEventDir);
    }

    // GETTERS AND SETTERS
    
    /**
     * Gets current toolbar icon size.
     * @return 
     */
    public ToolbarIconSizeEnum getToolbarIconSize() {
        return toolbarIconSize;
    }

    /**
     * Sets current toolbar icon size and notifies observers.
     * ICON_SIZE
     * @param toolbarIconSize 
     */
    public void setToolbarIconSize(ToolbarIconSizeEnum toolbarIconSize) {
        this.toolbarIconSize = toolbarIconSize;

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.ICON_SIZE);
    }

    /**
     * Gets current packet image type.
     * @return 
     */
    public PacketImageType getPackageImageType() {
        return packageImageType;
    }

    /**
     * Sets packet image type and notifies observers.
     * PACKET_IMAGE_TYPE_CHANGE
     * @param packageImageType 
     */
    public void setPackageImageType(PacketImageType packageImageType) {
        this.packageImageType = packageImageType;

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.PACKET_IMAGE_TYPE_CHANGE);
    }

    /**
     * Gets current level of details mode.
     * @return 
     */
    public LevelOfDetailsMode getLevelOfDetails() {
        return levelOfDetails;
    }

    /**
     * Sets level of details mode and notifies the observers.
     * VIEW_DETAILS
     * @param levelOfDetails 
     */
    public void setLevelOfDetails(LevelOfDetailsMode levelOfDetails) {
        this.levelOfDetails = levelOfDetails;

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.VIEW_DETAILS);
    }

    /**
     * Gets IP address to psimulator server.
     * @return 
     */
    public String getConnectionIpAddress() {
        return connectionIpAddress;
    }

    /**
     * Sets IP address to psimulator server
     * @param connectionPort 
     */
    public void setConnectionIpAddress(String connectionIpAddress) {
        this.connectionIpAddress = connectionIpAddress;
    }

    /**
     * Gets connection port to psimulator server.
     * @return 
     */
    public String getConnectionPort() {
        return connectionPort;
    }

    /**
     * Sets connection port to psimulator server
     * @param connectionPort 
     */
    public void setConnectionPort(String connectionPort) {
        this.connectionPort = connectionPort;
    }

    /**
     * Gets recently opened files.
     * @return 
     */
    public List<File> getRecentOpenedFiles() {
        return recentOpenedFilesManager.getRecentOpenedFiles();
    }

    /**
     * Adds file to recently opened and notifies observers.
     * RECENT_OPENED_FILES_CHANGED
     * @param file 
     */
    public void addRecentOpenedFile(File file) {
        recentOpenedFilesManager.addFile(file);

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.RECENT_OPENED_FILES_CHANGED);
    }

    /**
     * Saves directory of type into preferences
     * @param directoryType
     * @param file 
     */
    public void setRecentDirectory(RecentlyOpenedDirectoryType directoryType, File file) {
        switch (directoryType) {
            case NETWORKS_DIR:
                recentlyOpenedNetworkDir = file.getAbsolutePath();
                break;
            case EVENTS_DIR:
                recentlyOpenedEventDir = file.getAbsolutePath();
                break;
            default:
                break;
        }

    }

    /**
     * Gets recent  directory of directoryType from preferences
     * @param directoryType
     * @return directory File, or NULL if file does not exist....
     */
    public File getRecentDirectory(RecentlyOpenedDirectoryType directoryType) {
        File file = null;
        switch (directoryType) {
            case NETWORKS_DIR:
                if (recentlyOpenedNetworkDir.equals("")) {
                    return null;
                }
                file = new File(recentlyOpenedNetworkDir);
                break;
            case EVENTS_DIR:
                if (recentlyOpenedEventDir.equals("")) {
                    return null;
                }
                file = new File(recentlyOpenedEventDir);
                break;
            default:
                break;
        }
        
        if (file.exists() && file.isDirectory() && file.canRead() && file.canWrite()) {
            return file;
        }

        return null;
    }

    /**
     * Gets value of specified view details type.
     * @param viewDetailsType
     * @return 
     */
    public boolean isViewDetails(ViewDetailsType viewDetailsType) {
        switch (viewDetailsType) {
            case CABLE_DELAYS:
                return viewCableDelay;
            case DEVICE_NAMES:
                return viewDeviceNames;
            case DEVICE_TYPES:
                return viewDeviceTypes;
            case INTERFACE_NAMES:
                return viewInterfaceNames;
            case IP_ADDRESS:
                return viewIpAddresses;
            case MAC_ADDRESS:
                return viewMacAddresses;
            case NETWORK_BOUNDS:
                return viewNetworkBounds;
            default:
                return false;
        }
    }

    /**
     * Set specified view details to value.
     * @param viewDetailsType
     * @param value 
     */
    public void setViewDetails(ViewDetailsType viewDetailsType, boolean value) {
        switch (viewDetailsType) {
            case CABLE_DELAYS:
                viewCableDelay = value;
                break;
            case DEVICE_NAMES:
                viewDeviceNames = value;
                break;
            case DEVICE_TYPES:
                viewDeviceTypes = value;
                break;
            case INTERFACE_NAMES:
                viewInterfaceNames = value;
                break;
            case IP_ADDRESS:
                viewIpAddresses = value;
                break;
            case MAC_ADDRESS:
                viewMacAddresses = value;
                break;
            case NETWORK_BOUNDS:
                viewNetworkBounds = value;
                break;
            default:
                break;
        }
    }
}
