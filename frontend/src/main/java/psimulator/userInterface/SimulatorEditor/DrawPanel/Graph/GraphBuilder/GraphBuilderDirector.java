package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphBuilder;

import java.util.Collection;
import java.util.Iterator;
import shared.Components.CableModel;
import shared.Components.HwComponentModel;
import shared.Components.NetworkModel;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class GraphBuilderDirector {

    private AbstractGraphBuilder abstractGraphBuilder;
    private NetworkModel networkModel;

    public GraphBuilderDirector(AbstractGraphBuilder abstractGraphBuilder, NetworkModel networkModel) {
        this.abstractGraphBuilder = abstractGraphBuilder;
        this.networkModel = networkModel;
    }

    public void construct() {
        // create graph
        abstractGraphBuilder.buildGraph();

        Collection <HwComponentModel> devices = networkModel.getHwComponents();
        Iterator<HwComponentModel> ith = devices.iterator();
        
        // build all hw components
        while(ith.hasNext()){
            HwComponentModel hwComp = ith.next();
            abstractGraphBuilder.buildHwComponent(hwComp);
        }

        Collection <CableModel> cables = networkModel.getCables();
        Iterator<CableModel> itc = cables.iterator();
        
        // build all cables
        while(itc.hasNext()){
            CableModel cableModel = itc.next();
            abstractGraphBuilder.buildCable(cableModel);
        }
    }
}
