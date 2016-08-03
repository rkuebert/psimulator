package psimulator.userInterface.SimulatorEditor.Tools;

import javax.swing.ImageIcon;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelToolChangeOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ManipulationTool extends AbstractTool{

    public ManipulationTool(MainTool tool, String path, DrawPanelToolChangeOuterInterface toolChangeInterface) {
        super(tool, path, toolChangeInterface);
    }

    @Override
    public String getToolTip(DataLayerFacade dataLayer) {
        return "bla";
    }

    @Override
    public String getTranslatedName(DataLayerFacade dataLayer) {
        switch(tool){
            case HAND:
                return dataLayer.getString("HAND");
            case DRAG_MOVE:
                return dataLayer.getString("DRAG_MOVE");
        }
        return "";
    }
    
    
}
