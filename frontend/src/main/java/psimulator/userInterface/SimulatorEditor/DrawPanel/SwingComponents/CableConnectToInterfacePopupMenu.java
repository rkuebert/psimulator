package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import shared.Components.EthInterfaceModel;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.MouseActionListeners.ChooseEthInterfaceInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class CableConnectToInterfacePopupMenu extends JPopupMenu {

    // graphical components
    private ButtonGroup interfaceGroup;
    private ItemHandler handler = new ItemHandler();
    private JComponent drawPanel;
    private JMenuItem items[];
    // END graphical components
    
    private HwComponentGraphic component;
    private ChooseEthInterfaceInterface chooseEthInterfaceInterface;
   
    public CableConnectToInterfacePopupMenu(DrawPanelInnerInterface drawPanel, PopupMenuListener popupMenuListener, ChooseEthInterfaceInterface chooseEthInterfaceInterface) {
        this.drawPanel = (JComponent) drawPanel;
        this.addPopupMenuListener(popupMenuListener);
        this.chooseEthInterfaceInterface = chooseEthInterfaceInterface;
    }

    /**
     * Shows JPopupMenu for HwComponentGraphic component at x and y coordinates. Parent is a drawPanel
     * @param component
     * @param x Coordinate
     * @param y Coordinate
     */
    public void showPopupInterfaceChoose(HwComponentGraphic component, int x, int y) {
        // init data structures
        interfaceGroup = new ButtonGroup();
        this.removeAll();
        this.component = component;
        
        // create new array with length = number of interfaces
        items = new JMenuItem[component.getInterfaces().size()];
        
        int i = 0;
        
        Icon icon = ImageFactorySingleton.getInstance().getImageIcon(ImageFactorySingleton.ICON_ETH_INTERFACE_16_PATH);
        
        // create menu items
        for(EthInterfaceModel ei : component.getInterfaces()){
            // new menu item
            items[i] = new JMenuItem(ei.getName(), icon);
            // if EthInterface in use, marked as disabled
            if(ei.hasCable()){
                items[i].setEnabled(false);
            }
            // add menu item to PopupMenu
            this.add(items[ i]);
            // add menu item to interface group
            interfaceGroup.add(items[ i]);
            // add action listener to item
            items[i].addActionListener(handler);
            
            // increase counter
            i++;
        }
        
        // show draw panel
        this.show(drawPanel, x, y);
    }

    private class ItemHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // determine which menu item was selected
            for (int i = 0; i < items.length; i++) {
                if (e.getSource() == items[i]) {
                    // set chosen interface in ChooseEthInterfaceInterface
                    chooseEthInterfaceInterface.setChosenInterface(component.getInterfaces().get(i));
                    return;
                }
            }
        }
    }

}
