package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphBuilder;

import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphBuilderInterface;
import shared.Components.CableModel;
import shared.Components.HwComponentModel;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractGraphBuilder {
    
    public abstract GraphBuilderInterface getResult();
    
    public abstract void buildGraph();
    
    public abstract void buildHwComponent(HwComponentModel hwComponentModel);
    
    public abstract void buildCable(CableModel cable);

}