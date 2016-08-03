package psimulator.userInterface.SimulatorEditor.DrawPanel.Actions;

import java.util.List;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.CableGraphic;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class RemovedComponentsWrapper {
    private List<HwComponentGraphic> removedComponents;
    private List<CableGraphic> removedCables;

    public RemovedComponentsWrapper(List<HwComponentGraphic> removedComponents, List<CableGraphic> removedCables) {
        this.removedComponents = removedComponents;
        this.removedCables = removedCables;
    }

    public List<CableGraphic> getRemovedCables() {
        return removedCables;
    }

    public List<HwComponentGraphic> getRemovedComponents() {
        return removedComponents;
    }   
}
