package psimulator.userInterface.SimulatorEditor.Tools;

import shared.Components.HwTypeEnum;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelToolChangeOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractCreationTool extends AbstractTool{
    protected HwTypeEnum hwType;

    public AbstractCreationTool(MainTool tool, String path, DrawPanelToolChangeOuterInterface toolChangeInterface, HwTypeEnum hwType) {
        super(tool, path, toolChangeInterface);
        this.hwType = hwType;
    }

    public HwTypeEnum getHwType() {
        return hwType;
    }

    public String getImagePath() {
        return path;
    }
    
    @Override
    public String getTranslatedName(DataLayerFacade dataLayer){
        return dataLayer.getString(hwType.toString());
    }
    
    @Override
    public abstract String getToolTip(DataLayerFacade dataLayer);
    
    public abstract String getParameterLabel();
    
    public abstract int getParameter();
}
