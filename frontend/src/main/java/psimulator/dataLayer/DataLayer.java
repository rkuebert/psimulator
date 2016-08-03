package psimulator.dataLayer;

import java.io.File;
import java.util.List;
import java.util.Observer;
import psimulator.dataLayer.Enums.LevelOfDetailsMode;
import psimulator.dataLayer.Enums.RecentlyOpenedDirectoryType;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.dataLayer.Enums.ViewDetailsType;
import psimulator.dataLayer.Network.NetworkFacade;
import psimulator.dataLayer.Simulator.SimulatorManager;
import psimulator.dataLayer.Simulator.SimulatorManagerInterface;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;
import psimulator.dataLayer.language.LanguageManager;
import psimulator.dataLayer.preferences.PreferencesManager;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.PacketImageType;
import shared.Components.NetworkModel;
import shared.Serializer.AbstractNetworkSerializer;
import shared.Serializer.NetworkModelSerializerXML;
import shared.Serializer.SaveLoadException;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;
import shared.SimulatorEvents.Serializer.AbstractSimulatorEventsSaveLoadInterface;
import shared.SimulatorEvents.Serializer.SimulatorEventsSerializerXML;
import shared.telnetConfig.TelnetConfig;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class DataLayer extends DataLayerFacade {

    private LanguageManager languageManager;
    private PreferencesManager preferencesManager;
    private SimulatorManager simulatorManager; 
    //
    private AbstractNetworkSerializer abstractNetworkSerializer;
    //
    private AbstractSimulatorEventsSaveLoadInterface simulatorEventsSerializer;
    //
    private NetworkFacade networkFacade;
    //
    private TelnetConfig telnetConfig;
    
    

    public DataLayer() {
        networkFacade = new NetworkFacade();
        //
        preferencesManager = new PreferencesManager();
        languageManager = new LanguageManager();
        simulatorManager = new SimulatorManager((DataLayerFacade)this);
        
        abstractNetworkSerializer = new NetworkModelSerializerXML();
        //abstractNetworkSerializer = new NetworkModelSerializer();

        //simulatorEventsSerializer = new SimulatorEventsSerializer();
        simulatorEventsSerializer = new SimulatorEventsSerializerXML();
        
        // get instance to init imageFactory
        ImageFactorySingleton.getInstance();
        
        //
        
    }
    
    
    /**
     * Returns current icon toolbar size.
     * @return 
     */
    @Override
    public ToolbarIconSizeEnum getToolbarIconSize() {
        return preferencesManager.getToolbarIconSize();
    }

    /**
     * Sets toolbar icon size in preferences.
     * @param size 
     */
    @Override
    public void setToolbarIconSize(ToolbarIconSizeEnum size) {
        preferencesManager.setToolbarIconSize(size);
    }

    /**
     * Saves aplication preferences.
     * 
     * Call before program exit.
     */
    @Override
    public void savePreferences() {
        preferencesManager.savePreferences();
        languageManager.savePreferences();
        ZoomManagerSingleton.getInstance().savePreferences();
    }

    
    @Override
    public void setCurrentLanguage(int languagePosition) {
        languageManager.setCurrentLanguage(languagePosition);
    }

    @Override
    public Object[] getAvaiableLanguageNames() {
        return languageManager.getAvaiableLanguageNames();
    }

    @Override
    public int getCurrentLanguagePosition() {
        return languageManager.getCurrentLanguagePosition();
    }

    @Override
    public String getString(String string) {
        return languageManager.getString(string);
    }

    @Override
    public void addLanguageObserver(Observer observer) {
        languageManager.addObserver(observer);
    }

    @Override
    public void addPreferencesObserver(Observer observer) {
        preferencesManager.addObserver(observer);
    }

    @Override
    public void deletePreferencesObserver(Observer observer) {
        preferencesManager.deleteObserver(observer);
    }

    @Override
    public void deleteLanguageObserver(Observer observer) {
        languageManager.deleteObserver(observer);
    }

    @Override
    public void addSimulatorObserver(Observer observer) {
        simulatorManager.addObserver(observer);
    }

    @Override
    public SimulatorManagerInterface getSimulatorManager() {
        return simulatorManager;
    }
    
    @Override
    public void saveNetworkModelToFile(File file) throws SaveLoadException {
        abstractNetworkSerializer.saveNetworkModelToFile(networkFacade.getNetworkModel(), file);
    }

    @Override
    public NetworkModel loadNetworkModelFromFile(File file) throws SaveLoadException {
        NetworkModel networkModel = abstractNetworkSerializer.loadNetworkModelFromFile(file);
        return networkModel;
    }
  
    @Override
    public void saveEventsToFile(SimulatorEventsWrapper simulatorEvents, File file) throws SaveLoadException {
        simulatorEventsSerializer.saveEventsToFile(simulatorEvents, file);
    }

    @Override
    public SimulatorEventsWrapper loadEventsFromFile(File file) throws SaveLoadException {
        return simulatorEventsSerializer.loadEventsFromFile(file);
    }

    @Override
    public LevelOfDetailsMode getLevelOfDetails() {
        return preferencesManager.getLevelOfDetails();
    }

    @Override
    public void setLevelOfDetails(LevelOfDetailsMode levelOfDetails) {
        preferencesManager.setLevelOfDetails(levelOfDetails);
    }

    @Override
    public PacketImageType getPackageImageType() {
        return preferencesManager.getPackageImageType();
    }

    @Override
    public void setPackageImageType(PacketImageType packageImageType) {
        preferencesManager.setPackageImageType(packageImageType);
    }

    @Override
    public String getConnectionIpAddress() {
        return preferencesManager.getConnectionIpAddress();
    }

    @Override
    public void setConnectionIpAddress(String connectionIpAddress) {
        preferencesManager.setConnectionIpAddress(connectionIpAddress);
    }

    @Override
    public String getConnectionPort() {
        return preferencesManager.getConnectionPort();
    }

    @Override
    public void setConnectionPort(String connectionPort) {
        preferencesManager.setConnectionPort(connectionPort);
    }

    @Override
    public NetworkFacade getNetworkFacade() {
        return networkFacade;
    }

    @Override
    public List<File> getRecentOpenedFiles() {
        return preferencesManager.getRecentOpenedFiles();
    }

    @Override
    public void addRecentOpenedFile(File file) {
        preferencesManager.addRecentOpenedFile(file);
    }

    @Override
    public void setTelnetConfig(TelnetConfig telnetConfig) {
        this.telnetConfig = telnetConfig;
    }

    @Override
    public TelnetConfig getTelnetConfig() {
        return telnetConfig;
    }

    @Override
    public void setViewDetails(ViewDetailsType viewDetailsType, boolean value) {
        preferencesManager.setViewDetails(viewDetailsType, value);
    }

    @Override
    public boolean isViewDetails(ViewDetailsType viewDetailsType) {
        return preferencesManager.isViewDetails(viewDetailsType);
    }

    @Override
    public void setRecentDirectory(RecentlyOpenedDirectoryType directoryType, File file) {
        preferencesManager.setRecentDirectory(directoryType, file);
    }

    @Override
    public File getRecentDirectory(RecentlyOpenedDirectoryType directoryType) {
        return preferencesManager.getRecentDirectory(directoryType);
    }
}
