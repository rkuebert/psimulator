package psimulator.userInterface.SimulatorEditor.DrawPanel.MouseActionListeners;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.undo.UndoManager;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.PopupMenuSimulatorComponent;
import psimulator.userInterface.SimulatorEditor.Tools.AbstractTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class DrawPanelListenerStrategySimulator extends DrawPanelListenerStrategyDragMove{

    
    public DrawPanelListenerStrategySimulator(DrawPanelInnerInterface drawPanel, UndoManager undoManager, MainWindowInnerInterface mainWindow, DataLayerFacade dataLayer) {
        super(drawPanel, undoManager,mainWindow, dataLayer);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        //System.out.println("Simulator mouse listener init");
        drawPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void deInitialize() {
        drawPanel.repaint();
    }

    @Override
    public void setTool(AbstractTool tool) {
        // should never happen
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * When HwComponentGraphic clicked, the popup menu for component in Simulator is opened
     * @param e 
     */
    @Override
    public void mousePressedRight(MouseEvent e) {
        // open popup menu
        
        // convert
        e = convertMouseEvent(e);

        // get clicked component
        HwComponentGraphic clickedComponent = getClickedAbstractHwComponent(e.getPoint());

         // if there is one marked component or no marked component and a component was clicked
        if (clickedComponent != null) {

            // show popup
            PopupMenuSimulatorComponent popup = new PopupMenuSimulatorComponent(mainWindow, drawPanel, dataLayer, clickedComponent);
            
            popup.show(drawPanel, e.getPoint().x, e.getPoint().y);
            
            return;
        }
    }
    
}
