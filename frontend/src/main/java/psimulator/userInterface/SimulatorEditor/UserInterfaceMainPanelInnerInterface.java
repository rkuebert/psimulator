package psimulator.userInterface.SimulatorEditor;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface UserInterfaceMainPanelInnerInterface {

    /**
     * Sets mainTool active in toolbar.
     * @param mainTool 
     */
    public void doSetToolInToolBar(MainTool mainTool);
    
    /**
     * Gets scroll pane
     * @return 
     */
    public JScrollPane getJScrollPane();
    
    /**
     * Gets view port.
     * @return 
     */
    public JViewport getJViewport();
}
