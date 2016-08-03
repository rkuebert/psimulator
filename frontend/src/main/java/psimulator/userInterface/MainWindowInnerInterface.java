package psimulator.userInterface;

import java.awt.Component;
import javax.swing.JRootPane;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface MainWindowInnerInterface {
    /**
     * Updates Undo and Redo APP buttons according to undo manager
     */
    public void updateUndoRedoButtons();
    /**
     * Updates ZoomIn and ZoomOut APP buttons according to zoom manager
     */
    public void updateZoomButtons();
    /**
     * Updates icons in toolbar according to size 
     * @param size Size to update to
     */
    public void updateToolBarIconsSize(ToolbarIconSizeEnum size);
    
    /**
     * Get root pane from main winfow
     * @return 
     */
    public JRootPane getRootPane();

    /**
     * Get main window component. Use for creating dialogs which has to have mainWindow
     * as parent.
     * @return 
     */
    public Component getMainWindowComponent();
    
    /**
     * Call when user wants to save events.
     * @param simulatorEventsWrapper 
     */
    public void saveEventsAction(SimulatorEventsWrapper simulatorEventsWrapper);
    
    /**
     * Call when user wants to load events.
     * @return 
     */
    public SimulatorEventsWrapper loadEventsAction();

}
