package psimulator.userInterface.SimulatorEditor.DrawPanel.UndoCommands;

import java.awt.Dimension;
import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class UndoableMoveComponent extends AbstractUndoableEdit {
    
    protected GraphOuterInterface graph;
    
    protected List<HwComponentGraphic> components;
    protected Dimension offsetInDefaultZoom;
    
    
    public UndoableMoveComponent(GraphOuterInterface graph, List<HwComponentGraphic> components, Dimension offsetInDefaultZoom) {
        super();
        this.components = components;
        this.offsetInDefaultZoom = offsetInDefaultZoom;
        this.graph = graph;
    }
 
    @Override
    public String getPresentationName() {
      return "Component move";
    }

    @Override
    public void undo() {
      super.undo();
      
      graph.doChangePositionOfAbstractHwComponents(components, offsetInDefaultZoom, false);
      
      /*
      for(HwComponentGraphic component : components){
          component.doChangePosition(offsetInDefaultZoom, false);
      }
      
      // panel could be resized before undo, so we need to update its size
     drawPanel.updateSize(drawPanel.getGraph().getGraphLowerRightBound());
     */
    }

    @Override
    public void redo() {
      super.redo();
      
      graph.doChangePositionOfAbstractHwComponents(components, offsetInDefaultZoom, true);
      
      /*
      for(HwComponentGraphic component : components){
          component.doChangePosition(offsetInDefaultZoom, true);
      }
      // panel could be resized before redo, so we need to update its size
      drawPanel.updateSize(drawPanel.getGraph().getGraphLowerRightBound());
       */
    }
  }