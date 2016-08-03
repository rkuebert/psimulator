package psimulator.userInterface.SimulatorEditor.DrawPanel;

import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.SimulatorEditor.DrawPanel.MouseActionListeners.DrawPanelListenerStrategy;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface DrawPanelToolChangeOuterInterface {
 
    /**
     * Removes current active mouse listener.
     */
    public void removeCurrentMouseListener();
    
    /**
     * Returns mouse listener according to tool in parameter.
     * @param tool 
     * @return MouseListener - listener that corresponds MainTool
     */
    public DrawPanelListenerStrategy getMouseListener(MainTool tool);
    
    /**
     * Sets active mouse listener to listener in pamrameter
     * @param mouseListener 
     */
    public void setCurrentMouseListener(DrawPanelListenerStrategy mouseListener);
    
    /**
     * Sets active mouse listener to simulator mouse listener
     */
    public void setCurrentMouseListenerSimulator();
}
