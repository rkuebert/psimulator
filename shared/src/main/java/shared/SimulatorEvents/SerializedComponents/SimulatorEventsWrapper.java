package shared.SimulatorEvents.SerializedComponents;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used for save/load of SimulatorEvent objects
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
@XmlRootElement
public class SimulatorEventsWrapper implements Serializable{
    
    private List<SimulatorEvent> simulatorEvents;

    public SimulatorEventsWrapper(List<SimulatorEvent> simulatorEvents) {
        this.simulatorEvents = simulatorEvents;
    }

    public SimulatorEventsWrapper() {
    }
    
    

    public List<SimulatorEvent> getSimulatorEvents() {
        return simulatorEvents;
    }

    public void setSimulatorEvents(List<SimulatorEvent> simulatorEvents) {
        this.simulatorEvents = simulatorEvents;
    }
}
