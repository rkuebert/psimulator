package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph;

import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.CableGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface GraphBuilderInterface {
    public void addHwComponentWithoutGraphSizeChange(HwComponentGraphic component);
    public void addCableOnGraphBuild(CableGraphic cable);
    public HwComponentGraphic getAbstractHwComponent(int id);
}
