package psimulator.userInterface.SimulatorEditor.DrawPanel.Actions;

import javax.swing.AbstractAction;
import javax.swing.undo.UndoManager;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractDrawPanelAction extends AbstractAction{
 
    protected UndoManager undoManager;
    protected DrawPanelInnerInterface drawPanel;
    protected MainWindowInnerInterface mainWindow;
    
    public AbstractDrawPanelAction(UndoManager undoManager, DrawPanelInnerInterface drawPanel, MainWindowInnerInterface mainWindow) {
        this.undoManager = undoManager;
        this.drawPanel = drawPanel;
        this.mainWindow = mainWindow;
    }
}
