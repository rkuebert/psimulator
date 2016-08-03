package psimulator.userInterface.SimulatorEditor.Tools;

import java.awt.Image;
import javax.swing.ImageIcon;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelToolChangeOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.SimulatorEditor.DrawPanel.MouseActionListeners.DrawPanelListenerStrategy;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractTool {
    
    protected MainTool tool;
    protected String path;
    protected DrawPanelToolChangeOuterInterface toolChangeInterface;
    
    public AbstractTool(MainTool tool, String path, DrawPanelToolChangeOuterInterface toolChangeInterface) {
        this.tool = tool;
        this.path = path;
        this.toolChangeInterface = toolChangeInterface;
    }

    public abstract String getTranslatedName(DataLayerFacade dataLayer);
    
    public abstract String getToolTip(DataLayerFacade dataLayer);

    public MainTool getTool() {
        return tool;
    }
    
    public ImageIcon getImageIcon(DataLayerFacade dataLayer) {
        ToolbarIconSizeEnum iconSize = dataLayer.getToolbarIconSize();
        return ImageFactorySingleton.getInstance().getImageIconForToolbar(tool, path, iconSize, false);
    }
    
    public ImageIcon getImageIconForPopup(DataLayerFacade dataLayer) {
        ToolbarIconSizeEnum iconSize = dataLayer.getToolbarIconSize();
        return ImageFactorySingleton.getInstance().getImageIconForToolbar(tool, path, iconSize, true);
    }
 
    /**
     * Sets proper DrawPanelListenerStrategy in toolChangeInterface
     */
    public void setEnabled(){
        // remove current mouse listener
        toolChangeInterface.removeCurrentMouseListener();
        // get mouse listener for needed tool
        DrawPanelListenerStrategy listener =  toolChangeInterface.getMouseListener(tool);
        // tell mouse listener about tool change
        listener.setTool(this);
        // add mouse listener to toolChangeInterface
        toolChangeInterface.setCurrentMouseListener(listener);
    }
}
