package psimulator.dataLayer;

import java.io.File;
import java.util.Observer;
import psimulator.dataLayer.Network.NetworkFacade;
import psimulator.dataLayer.Simulator.SimulatorManagerInterface;
import psimulator.dataLayer.interfaces.LanguageInterface;
import psimulator.dataLayer.interfaces.PreferencesInterface;
import shared.Components.NetworkModel;
import shared.Serializer.SaveLoadException;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;
import shared.telnetConfig.TelnetConfig;


/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class DataLayerFacade implements PreferencesInterface, LanguageInterface{
    
    public abstract SimulatorManagerInterface getSimulatorManager();
    public abstract void addSimulatorObserver(Observer observer);

    public abstract void saveNetworkModelToFile(File file) throws SaveLoadException;
    public abstract NetworkModel loadNetworkModelFromFile(File file) throws SaveLoadException;
    
    public abstract void saveEventsToFile(SimulatorEventsWrapper simulatorEvents, File file) throws SaveLoadException;
    public abstract SimulatorEventsWrapper loadEventsFromFile(File file) throws SaveLoadException;
    
    public abstract NetworkFacade getNetworkFacade();
    
    public abstract void setTelnetConfig(TelnetConfig telnetConfig);
    public abstract TelnetConfig getTelnetConfig();
    
    //public abstract void saveProperties();
}
