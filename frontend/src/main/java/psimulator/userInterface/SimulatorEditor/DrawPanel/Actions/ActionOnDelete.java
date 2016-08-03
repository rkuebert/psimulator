package psimulator.userInterface.SimulatorEditor.DrawPanel.Actions;

import java.awt.event.ActionEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.UndoCommands.UndoableRemoveComponents;
import psimulator.userInterface.MainWindowInnerInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ActionOnDelete extends AbstractDrawPanelAction {

    public ActionOnDelete(UndoManager undoManager, DrawPanelInnerInterface drawPanel, MainWindowInnerInterface mainWindow) {
        super(undoManager, drawPanel, mainWindow);
    }

    /**
     * Removes all marked AbstractComponents from graph and all cables connecting those AbstractComponents
     * @param ae 
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        GraphOuterInterface graph = drawPanel.getGraphOuterInterface();
        
        // remove marked components from graph
        RemovedComponentsWrapper removedComponents = graph.doRemoveMarkedComponents();
        
        // if no component removed
        if(removedComponents == null){
            // do nothing
            return;
        }

        //System.out.println("Removing "+markedComponents.size()+ " components and "+cablesToRemove.size()+" cables" );
        undoManager.undoableEditHappened(
                new UndoableEditEvent(this,
                new UndoableRemoveComponents(graph, removedComponents.getRemovedComponents(), removedComponents.getRemovedCables())));

        // update undo redo buttons
        mainWindow.updateUndoRedoButtons();
       
        // reapaint draw panel
        drawPanel.repaint();
    }
}
