package psimulator.userInterface.SimulatorEditor.UserInterfaceLayeredPane;

import javax.swing.AbstractAction;
import javax.swing.JLayeredPane;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.AnimationPanelOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelToolChangeOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.DrawPanelAction;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class UserInterfaceLayeredPaneOuterInterface extends JLayeredPane implements DrawPanelToolChangeOuterInterface{
    
    // USED BY EDITOR PANEL
    /**
     * Finds if UNDO can be performed
     * @return true if yes, false if no
     */
    public abstract boolean canUndo();
    /**
     * Finds if REDO can be performed
     * @return true if yes, false if no
     */
    public abstract boolean canRedo();
    /**
     * Calls UNDO operation
     */
    public abstract void undo();
    /**
     * Calls REDO operation
     */
    public abstract void redo();
    /**
     * Gets Action corresponding to DrawPanelAction in parameter
     * @param action
     * @return AbstractAction
     */
    public abstract AbstractAction getAbstractAction(DrawPanelAction action);
  
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
    
    /**
     * finds whether has graph
     * @return 
     */
    public abstract boolean hasGraph();
    
    public abstract Graph getGraph();
    
    public abstract AnimationPanelOuterInterface getAnimationPanelOuterInterface();
    
    //public abstract void updateSize();
}
