package psimulator.userInterface.SimulatorEditor.DrawPanel.Support;

import java.util.Observable;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class CustomObservable extends Observable{
    
    
    public void notifyAllObservers(ObserverUpdateEventType updateEventType){
        setChanged();
        notifyObservers(updateEventType);
    }
    
}
