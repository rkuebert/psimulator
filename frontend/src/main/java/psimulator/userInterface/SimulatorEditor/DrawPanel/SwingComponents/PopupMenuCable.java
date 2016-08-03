package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.DrawPanelAction;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class PopupMenuCable extends JPopupMenu{
    
    private DataLayerFacade dataLayer;
    private DrawPanelInnerInterface drawPanel;
    
    private JMenuItem jItemCableProperties;
    private JMenuItem jItemDeleteCable;
    
    public PopupMenuCable(DrawPanelInnerInterface drawPanel, DataLayerFacade dataLayer, int cables){
        this.dataLayer = dataLayer;
        this.drawPanel = drawPanel;
        
        jItemCableProperties = new JMenuItem(dataLayer.getString("PROPERTIES"));
        jItemDeleteCable = new JMenuItem();
        
        if(cables == 1){
            this.add(jItemCableProperties);
            jItemDeleteCable.setText(dataLayer.getString("DELETE_CABLE"));
        }else{
            jItemDeleteCable.setText(dataLayer.getString("DELETE_CABLES"));
        }
        
        jItemCableProperties.addActionListener(drawPanel.getAbstractAction(DrawPanelAction.PROPERTIES));
        jItemDeleteCable.addActionListener(drawPanel.getAbstractAction(DrawPanelAction.DELETE));
        
        
        // add icons 
        jItemCableProperties.setIcon(ImageFactorySingleton.getInstance().getImageIcon(ImageFactorySingleton.ICON_CONFIGURE_16_PATH));
        jItemDeleteCable.setIcon(ImageFactorySingleton.getInstance().getImageIcon(ImageFactorySingleton.ICON_CANCEL_16_PATH));
        
        // add buttons for operations 
        this.add(jItemDeleteCable);
     
    }
    

    public void show(DrawPanelInnerInterface drawPanel, int x, int y){
        super.show((JComponent)drawPanel, x, y);
    }
}
