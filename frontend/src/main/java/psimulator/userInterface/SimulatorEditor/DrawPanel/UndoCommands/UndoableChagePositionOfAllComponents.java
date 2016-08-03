package psimulator.userInterface.SimulatorEditor.DrawPanel.UndoCommands;

import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.undo.AbstractUndoableEdit;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class UndoableChagePositionOfAllComponents extends AbstractUndoableEdit {

    protected GraphOuterInterface graph;
    protected HashMap<HwComponentGraphic, Dimension> map;
    
    public UndoableChagePositionOfAllComponents(GraphOuterInterface graph, HashMap<HwComponentGraphic, Dimension> map) {
        super();
        this.graph = graph;
        this.map = map;
    }

    @Override
    public void undo() {
        super.undo();

   
        for (HwComponentGraphic component : map.keySet()) {
            graph.doChangePositionOfAbstractHwComponent(component, map.get(component), true);
        }

    }

    @Override
    public void redo() {
        super.redo();

        for (HwComponentGraphic component : map.keySet()) {
            graph.doChangePositionOfAbstractHwComponent(component, map.get(component), false);
        }

    }
}
