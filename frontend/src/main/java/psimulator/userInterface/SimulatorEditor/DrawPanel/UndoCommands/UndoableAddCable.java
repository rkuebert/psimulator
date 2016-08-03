package psimulator.userInterface.SimulatorEditor.DrawPanel.UndoCommands;

import javax.swing.undo.AbstractUndoableEdit;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.CableGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class UndoableAddCable extends AbstractUndoableEdit {
    protected CableGraphic cable;
    protected GraphOuterInterface graph;
    
    public UndoableAddCable(CableGraphic cable, GraphOuterInterface graph){
        super();
        this.cable = cable;
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
      
      graph.removeCable(cable);
    }

    @Override
    public void redo() {
      super.redo();
      graph.doUnmarkAllComponents();
      
      graph.addCable(cable);
    }
}