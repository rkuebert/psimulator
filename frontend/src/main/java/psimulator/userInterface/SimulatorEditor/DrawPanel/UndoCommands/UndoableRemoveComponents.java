package psimulator.userInterface.SimulatorEditor.DrawPanel.UndoCommands;

import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.CableGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class UndoableRemoveComponents extends AbstractUndoableEdit {

    protected List<HwComponentGraphic> components;
    protected List<CableGraphic> cables;
    protected GraphOuterInterface graph;

    public UndoableRemoveComponents(GraphOuterInterface graph, List<HwComponentGraphic> components, List<CableGraphic> cables) {
        super();

        this.components = components;
        this.graph = graph;
        this.cables = cables;
    }

    @Override
    public String getPresentationName() {
        return "HW component add/remove";
    }

    @Override
    public void undo() {
        super.undo();
        // set all components unmarked, they could be marked in time between undo and redo
        graph.doUnmarkAllComponents();

        //System.out.println("Undo - Adding "+components.size()+ " components and "+cables.size()+" cables" );
        graph.addHwComponents(components);
        graph.addCables(cables);

    }

    @Override
    public void redo() {
        super.redo();
        // set all components unmarked, they could be marked in time between undo and redo
        graph.doUnmarkAllComponents();


        //System.out.println("Redo - Removing "+components.size()+ " components and "+cables.size()+" cables" );
        graph.removeHwComponents(components);
        graph.removeCables(cables);
    }

}
