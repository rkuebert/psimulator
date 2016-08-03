package psimulator.userInterface.SimulatorEditor.AnimationPanel;

import java.util.Observer;
import javax.swing.JComponent;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;
import shared.SimulatorEvents.SerializedComponents.EventType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AnimationPanelOuterInterface extends JComponent implements Observer{
    
    
    
    
    /**
     * removes graph from draw panel a resets state of draw panel
     * @return 
     */
    public abstract Graph removeGraph();
    /**
     * Sets graph 
     * @param graph 
     */
    public abstract void setGraph(Graph graph);
    
    // TEMP METHOD
    public abstract Graph getGraph();
    
    //@Override
    //public abstract Dimension getPreferredSize();
    
    /**
     * Creates animation with desired parameters.
     * @param packetType
     * @param timeInMiliseconds
     * @param idSource
     * @param idDestination
     * @param eventType 
     */
    public abstract void createAnimation(PacketType packetType, int timeInMiliseconds, int idSource, int idDestination, EventType eventType);
    
    /**
     * Gets animation duration for cable with given speed coeficient.
     * @param cableId
     * @param speedCoeficient
     * @return 
     */
    public abstract int getAnimationDuration(int cableId, int speedCoeficient);
}
