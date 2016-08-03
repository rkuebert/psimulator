package psimulator.userInterface.SimulatorEditor.Tools;

import javax.swing.ImageIcon;
import shared.Components.HwTypeEnum;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelToolChangeOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class CreateCableTool extends AbstractCreationTool{

    protected int delay;
    
    public CreateCableTool(MainTool tool, String path, DrawPanelToolChangeOuterInterface toolChangeInterface, HwTypeEnum hwType, int delay) {
        super(tool, path, toolChangeInterface, hwType);
        
        this.delay = delay;
    }
  
    public int getDelay(){
        return delay;
    }

    @Override
    public String getParameterLabel() {
        return " - Delay: ";
    }

    @Override
    public int getParameter() {
        return delay;
    }

    @Override
    public String getToolTip(DataLayerFacade dataLayer) {
        return getTranslatedName(dataLayer) + " - "+dataLayer.getString("DELAY") +": " + getParameter();
    }
    
}
