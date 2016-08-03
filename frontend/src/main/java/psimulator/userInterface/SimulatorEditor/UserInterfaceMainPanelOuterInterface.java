package psimulator.userInterface.SimulatorEditor;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.AnimationPanelOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class UserInterfaceMainPanelOuterInterface extends JPanel {

    public UserInterfaceMainPanelOuterInterface(BorderLayout borderLayout) {
        super(borderLayout);
    }

    /**
     * Finds if UNDO can be performed
     *
     * @return true if yes, false if no
     */
    public abstract boolean canUndo();

    /**
     * Finds if REDO can be performed
     *
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
     * Removes graph and returns it in parameter
     *
     * @return
     */
    public abstract Graph removeGraph();

    /**
     * Sets graph
     *
     * @param graph
     */
    public abstract void setGraph(Graph graph);

    /**
     * Finds if has active graph.
     *
     * @return
     */
    public abstract boolean hasGraph();

    /**
     * Gets graph from UI.
     *
     * @return
     */
    public abstract Graph getGraph();
    /**
     * Removes all simulator events
     */
    public abstract void removeAllSimulatorEvents();

    /**
     * Changes mode of user interface
     * @param userInterfaceState 
     */
    public abstract void doChangeMode(UserInterfaceMainPanelState userInterfaceState);

    /**
     * Stop all simulator activities.
     */
    public abstract void stopSimulatorActivities();

    /**
     * Gets state of user interface.
     * @return 
     */
    public abstract UserInterfaceMainPanelState getUserInterfaceState();

    public abstract void addNewProjectActionListener(ActionListener listener);

    public abstract void addOpenProjectActionListener(ActionListener listener);

    public abstract AnimationPanelOuterInterface getAnimationPanelOuterInterface();
}
