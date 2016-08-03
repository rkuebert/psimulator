package psimulator.userInterface.SimulatorEditor.DrawPanel.MouseActionListeners;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.undo.UndoManager;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.AbstractComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.BundleOfCablesGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanel;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.ZoomType;
import psimulator.userInterface.SimulatorEditor.Tools.AbstractTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class DrawPanelListenerStrategy extends MouseInputAdapter implements MouseWheelListener {

    protected DrawPanelInnerInterface drawPanel;
    protected MainWindowInnerInterface mainWindow;
    protected UndoManager undoManager;
    protected DataLayerFacade dataLayer;
    
    protected JComponent comp;
    protected JViewport vport;
    
    protected final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    protected final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    
    
    /**
     * list for all marked components
     */
    //protected List<Markable> markedComponents = new ArrayList<Markable>();

    public DrawPanelListenerStrategy(DrawPanelInnerInterface drawPanel, UndoManager undoManager, 
            MainWindowInnerInterface mainWindow, DataLayerFacade dataLayer) {
        super();

        this.drawPanel = drawPanel;
        this.undoManager = undoManager;
        this.mainWindow = mainWindow;
        this.dataLayer = dataLayer;
    }

    public void initialize(){
        drawPanel.setCursor(defCursor);
        
        comp = (DrawPanel) drawPanel;
        vport = drawPanel.getJScrollPane().getViewport();
    }
    
    public abstract void deInitialize();

    public abstract void setTool(AbstractTool tool);

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            //System.out.println("Point:"+convertPoint(e.getPoint());
            // scroll down
            if (e.getWheelRotation() == -1) {
                ZoomManagerSingleton.getInstance().zoomIn(convertPoint(e.getPoint()), ZoomType.MOUSE);
                //scroll up    
            } else if (e.getWheelRotation() == 1) {
                ZoomManagerSingleton.getInstance().zoomOut(convertPoint(e.getPoint()), ZoomType.MOUSE);
            }
        }
    }

    @Override
    public final void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            mouseClickedLeft(e);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            mouseClickedRight(e);
        }
    }

    @Override
    public final void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            mousePressedLeft(e);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            mousePressedRight(e);
        }
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            mouseReleasedLeft(e);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            mouseReleasedRight(e);
        }
    }

    @Override
    public final void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            mouseDraggedLeft(e);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            mouseDraggedRight(e);
        }
    }
    
    public void mousePressedRight(MouseEvent e) {
        drawPanel.doSetTollInEditorToolBar(MainTool.HAND);
    }

    public void mouseClickedLeft(MouseEvent e) {
    }

    public void mouseClickedRight(MouseEvent e) {
    }

    public void mousePressedLeft(MouseEvent e) {
    }

    public void mouseReleasedLeft(MouseEvent e) {
    }

    public void mouseReleasedRight(MouseEvent e) {
    }

    public void mouseDraggedLeft(MouseEvent e) {
    }

    public void mouseDraggedRight(MouseEvent e) {
    }

    protected Point convertPoint(Point point){
        return SwingUtilities.convertPoint(vport,point,comp);
    }
    
    protected MouseEvent convertMouseEvent(MouseEvent mouseEvent){
        return SwingUtilities.convertMouseEvent(vport,mouseEvent,comp);
    }
    
    /**
     * Return component at point
     * @param point
     * @return component clicked
     */
    protected AbstractComponentGraphic getClickedItem(Point point) {
        // search HwComponents
        AbstractComponentGraphic clickedComponent = getClickedAbstractHwComponent(point);

        if (clickedComponent != null) {
            return clickedComponent;
        }

        // create small rectangle arround clicked point
        
        // search cables
        for (BundleOfCablesGraphic boc : drawPanel.getGraphOuterInterface().getBundlesOfCables()) {
            clickedComponent = boc.getIntersectingCable(point);
            if(clickedComponent != null){
               return clickedComponent; 
            }
        }

        return clickedComponent;
    }
    
    /**
     * Get clicked AbstractHWComponent at point
     * @param point
     * @return 
     */
    protected HwComponentGraphic getClickedAbstractHwComponent(Point point) {
        HwComponentGraphic clickedComponent = null;

        // search HwComponents
        for (HwComponentGraphic c : drawPanel.getGraphOuterInterface().getHwComponents()) {
            if (c.intersects(point)) {
                clickedComponent = c;
                break;
            }
        }

        return clickedComponent;
    }
    
}
