package psimulator.userInterface.SimulatorEditor.DrawPanel.Actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.UndoCommands.UndoableChagePositionOfAllComponents;
import psimulator.userInterface.MainWindowInnerInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ActionAlignComponentsToGrid extends AbstractDrawPanelAction {

    public ActionAlignComponentsToGrid(UndoManager undoManager, DrawPanelInnerInterface drawPanel, MainWindowInnerInterface mainWindow) {
        super(undoManager, drawPanel, mainWindow);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        // align components to grid
        HashMap<HwComponentGraphic, Dimension> movedComponentsMap;
        
        GraphOuterInterface graph = drawPanel.getGraphOuterInterface();
        
        if(graph.getMarkedAbstractHWComponentsCount() > 0){
            movedComponentsMap = graph.doAlignMarkedComponentsToGrid();
        }else{
            movedComponentsMap = graph.doAlignComponentsToGrid();
        }
        
        graph.doUnmarkAllComponents();
        
        // if map not empty set undoable edit
        if (!movedComponentsMap.isEmpty()) {
            // add to undo manager
            undoManager.undoableEditHappened(new UndoableEditEvent(this,
                    new UndoableChagePositionOfAllComponents(graph, movedComponentsMap)));

            // update Undo and Redo buttons
            mainWindow.updateUndoRedoButtons();

            
        }
        // repaint draw Panel
        drawPanel.repaint();
        
    }
}
