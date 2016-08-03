package psimulator.userInterface.SimulatorEditor.SwingComponents;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.SimulatorEditor.Tools.AbstractCreationTool;
import psimulator.userInterface.SimulatorEditor.Tools.AbstractTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ToolPopupMenu extends JPopupMenu{
    
    private List<AbstractTool> tools;
    private List<JRadioButtonMenuItem> jMenuItems;
    private MenuToggleButton toggleButton;
    private ButtonGroup toolsButtonGroup;
    
    public ToolPopupMenu(List<AbstractTool> tools, final MenuToggleButton toggleButton){
        super();
        
        this.toggleButton = toggleButton;
        this.tools = tools;
     
        jMenuItems = new ArrayList<>();
        toolsButtonGroup = new ButtonGroup();
        
        for (final AbstractTool tool : tools) {
            //AddDeviceTool tmp = (AddDeviceTool)tool;
            AbstractCreationTool tmp = (AbstractCreationTool)tool;

            // create menu item for tool
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem();
            
            jMenuItems.add(mi);
            
            mi.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    // set current tool to this tool
                    toggleButton.setCurrentTool(tool);
                }
            });
            this.add(mi);
            toolsButtonGroup.add(mi);
        }
        
        // set the first selected
        jMenuItems.get(0).setSelected(true);
    }
    
    @Override
    public void show(Component cmpnt, int x , int y){
        // show
        super.show(cmpnt, x, y);
    }

    public void updateToolNames(DataLayerFacade dataLayer){
        for (int i=0;i<jMenuItems.size();i++) {
            jMenuItems.get(i).setText(tools.get(i).getToolTip(dataLayer));
        }
    }
    
    public final void updateIconSize(DataLayerFacade dataLayer){
        for (int i=0;i<jMenuItems.size();i++) {
            ImageIcon icon = tools.get(i).getImageIconForPopup(dataLayer);
            jMenuItems.get(i).setIcon(icon);
        }
    }
    
}
