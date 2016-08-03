package psimulator.userInterface.SimulatorEditor.SwingComponents;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.userInterface.SimulatorEditor.Tools.AbstractTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class MenuToggleButton extends JToggleButton {
    private DataLayerFacade dataLayer;
    /**
     * arrow icon for lower right corner
     */
    private static final Icon i = new MenuArrowIcon();
    /**
     * popup menu for this menu tohhle button
     */
    protected ToolPopupMenu pop;
    /**
     * current selected tool
     */
    private AbstractTool currentTool;

    public MenuToggleButton(List<AbstractTool> tools, DataLayerFacade dataLayer) {
        super();
        
        this.dataLayer = dataLayer;

        if (tools == null || tools.isEmpty()) {
            this.setToolTipText("no tool avaiable");
            this.setEnabled(false);
        } else {
            // if more than one tool, create and add popup menu
            if (tools.size() > 1) {
                // create popup menu
                this.pop = new ToolPopupMenu(tools, this);

                // add mouse adapter for right click
                addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        MenuToggleButton b = (MenuToggleButton) e.getSource();

                        // uncomment next line for LEFT CLICK CHOOSE, RIGHT CLICK POPUP
                        if (SwingUtilities.isRightMouseButton(e)) {
                            if (pop != null) {
                                // show popup menu
                                pop.show(b, b.getWidth(), 0);
                            }
                        // uncomment next line for LEFT CLICK CHOOSE, RIGHT CLICK POPUP   
                        }
                    }
                });
            }// else { // comment else for LEFT CLICK CHOOSE, RIGHT CLICK POPUP
                // create and add action
                Action a = new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {

                        MenuToggleButton b = (MenuToggleButton) ae.getSource();

                        // if tool enabled
                        if (b.isSelected()) {
                            // enable current tool
                            setCurrentToolEnabled();
                        }
                    }
                };
                setAction(a);
            //}// comment for LEFT CLICK CHOOSE, RIGHT CLICK POPUP

            // set first tool as current tool
            setCurrentTool(tools.get(0));
        }

        setFocusable(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    /**
     * sets current tool in MenuTogglebutton  to tool in parameter
     * @param tool Chosen tool
     */
    public final void setCurrentTool(AbstractTool tool) {
        // set current tool to tool
        currentTool = tool;
        //AbstractCreationTool abstractCreationTool = (AbstractCreationTool) tool;
        // set tool tip text of this MenuToggleButton
        //this.setToolTipText(tool.getName());
        updateToolTip();
        // set Image icon of this MenuToggleButton
        this.setIcon(currentTool.getImageIcon(dataLayer));
        // enable current tool in 
        setCurrentToolEnabled();
        // set toggle button selected
        this.setSelected(true);
    }
    
    public final void updateToolTip(){
        // update tool tip of jToggleButton
        this.setToolTipText(currentTool.getToolTip(dataLayer));
        // upadte tool tips in popup menu that belongs to this jToggleButton
        if (pop != null) {
            pop.updateToolNames(dataLayer);
        }
    }
    
    public final void updateIconSize(){
        this.setIcon(currentTool.getImageIcon(dataLayer));
        
        // upadte tool tips in popup menu that belongs to this jToggleButton
        if (pop != null) {
            pop.updateIconSize(dataLayer);
        }
    }

    /**
     * enables current tool in this Menu ToggleButton 
     */
    public void setCurrentToolEnabled() {
        currentTool.setEnabled();
    }

    public AbstractTool getSelectedTool() {
        return currentTool;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dim = getSize();
        Insets ins = getInsets();

        // if there is popup menu, paint triangle in lower right corner
        if (pop != null) {
            int x = dim.width - ins.right - i.getIconWidth();
            int y = dim.width - ins.right - i.getIconHeight();
            i.paintIcon(this, g, x, y);
        }

    }
}

/**
 * Class represents small arrow for menu use
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
class MenuArrowIcon implements Icon {

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(Color.BLACK);
        g2.translate(x, y);

        int[] xPoints = {6, 2, 6};
        int[] yPoints = {2, 6, 6};

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawPolygon(xPoints, yPoints, 3);
        g2.fillPolygon(xPoints, yPoints, 3);

        g2.translate(-x, -y);
    }

    @Override
    public int getIconWidth() {
        return 9;
    }

    @Override
    public int getIconHeight() {
        return 9;
    }
}