package psimulator.userInterface.SimulatorEditor.DrawPanel.UndoCommands;

import javax.swing.undo.AbstractUndoableEdit;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class UndoableAddHwComponent extends AbstractUndoableEdit {
    protected GraphOuterInterface graph;
    protected HwComponentGraphic component;
    
    public UndoableAddHwComponent(GraphOuterInterface graph, HwComponentGraphic component){
        super();
        this.component = component;
        this.graph = graph;
    }

    @Override
    public String getPresentationName() {
      return "HW component add/remove";
    }

    @Override
    public void undo() {
      super.undo();
      graph.doUnmarkAllComponents();
      
      graph.removeHwComponent(component);
    }

    @Override
    public void redo() {
      super.redo();
      graph.doUnmarkAllComponents();
      
      graph.addHwComponent(component);
    }
}
