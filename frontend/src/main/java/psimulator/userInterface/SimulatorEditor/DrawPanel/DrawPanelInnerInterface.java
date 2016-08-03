package psimulator.userInterface.SimulatorEditor.DrawPanel;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.DrawPanelAction;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface DrawPanelInnerInterface{
    
    /**
     * returns graph
     * @return 
     */
    public GraphOuterInterface getGraphOuterInterface();
    /**
     * Gets dataLayerFacade;
     * @return 
     */
    public DataLayerFacade getDataLayerFacade();
    /**
     * Gets AbstractAction corresponding to DrawPanelAction
     * @param action
     * @return 
     */
    public abstract AbstractAction getAbstractAction(DrawPanelAction action);
    
    /**
     * Sets cursor in draw panel
     * @param cursor 
     */
    public abstract void setCursor(Cursor cursor);
    public void repaint();
    /**
     * Sets that cable is being paint. Actual zoom in end poitn is used because when zooming, the end
     * point has to be at the mouse position
     * @param lineInProgres
     * @param start - point in defaultZoom
     * @param end - point in actualZoom
     */
    public void setLineInProgras(boolean lineInProgres, Point startInDefaultZoom, Point endInActualZoom);
    /**
     * Sets transparent rectangle that is being paint
     * @param rectangleInProgress
     * @param rectangle 
     */
    public void setTransparetnRectangleInProgress(boolean rectangleInProgress, Rectangle rectangle);
    
    /**
     * Fits area of DrawPanel to area of Graph. Call whenever need to make DrawPanel
     * smaller according to Graph. There is minimum DrawPanel size.
     */
    public abstract void doFitToGraphSize();
    
    /**
     * Sets tool in EditorPanels toolBar
     * @param mainTool
     */
    public void doSetTollInEditorToolBar(MainTool mainTool);
    /**
     * Updates size of panel according to parameter. If dimension is bigger than actual
     * size of drawPanel, than size is changed.
     * @param dimension of Graph
     */
    
    public JScrollPane getJScrollPane();
}
