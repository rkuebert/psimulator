package shared.SimulatorEvents.Serializer;

import shared.Serializer.SaveLoadException;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;
import java.io.File;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface AbstractSimulatorEventsSaveLoadInterface {
    /**
     * Saves simulator events to specified file. If error occours, Exception is thrown.
     * @param simulatorEvents
     * @param file File to save events to
     * @throws SaveLoadException 
     */
     public void saveEventsToFile(SimulatorEventsWrapper simulatorEvents, File file) throws SaveLoadException;
     
     /**
      * Loads simulator events from specified file. If error occours, Exception is thrown.
      * @param file File to load events from
      * @return
      * @throws SaveLoadException 
      */
     public SimulatorEventsWrapper loadEventsFromFile(File file) throws SaveLoadException;
}
