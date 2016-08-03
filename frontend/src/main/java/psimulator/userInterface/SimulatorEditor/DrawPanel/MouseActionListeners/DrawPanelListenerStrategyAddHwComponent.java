package psimulator.userInterface.SimulatorEditor.DrawPanel.MouseActionListeners;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import psimulator.dataLayer.DataLayerFacade;
import shared.Components.HwComponentModel;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.UndoCommands.UndoableAddHwComponent;
import psimulator.userInterface.SimulatorEditor.Tools.AbstractTool;
import psimulator.userInterface.SimulatorEditor.Tools.AddDeviceTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class DrawPanelListenerStrategyAddHwComponent extends DrawPanelListenerStrategy {

    private AddDeviceTool addDeviceTool;

    public DrawPanelListenerStrategyAddHwComponent(DrawPanelInnerInterface drawPanel, UndoManager undoManager, MainWindowInnerInterface mainWindow, DataLayerFacade dataLayer) {
        super(drawPanel, undoManager, mainWindow, dataLayer);
    }

    
    
    @Override
    public void deInitialize() {
        drawPanel.setCursor(defCursor);
    }

    @Override
    public void setTool(AbstractTool tool) {
        this.addDeviceTool = (AddDeviceTool) tool;
    }

    @Override
    public void mousePressedLeft(MouseEvent e) {
        // convert
        e = convertMouseEvent(e);
        
        
        HwComponentModel hwComponentModel = dataLayer.getNetworkFacade().createHwComponentModel(addDeviceTool.getHwType(), addDeviceTool.getInterfaces(), 0, 0);
        
        // create new component
        HwComponentGraphic component = new HwComponentGraphic( dataLayer, hwComponentModel);

        component.initialize();

        // set position of new component
        component.setLocationByMiddlePoint(e.getPoint());

        // add component to graph
        drawPanel.getGraphOuterInterface().addHwComponent(component);
        
        // inform drawPanel about size change if component placed out of draw panel
        drawPanel.repaint();
        
        // add to undo manager
        undoManager.undoableEditHappened(new UndoableEditEvent(this,
                new UndoableAddHwComponent(drawPanel.getGraphOuterInterface(), component)));

        // update undo redo buttons
        mainWindow.updateUndoRedoButtons();

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        drawPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        drawPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
