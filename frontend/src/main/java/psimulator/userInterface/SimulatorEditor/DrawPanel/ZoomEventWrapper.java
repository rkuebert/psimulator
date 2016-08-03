package psimulator.userInterface.SimulatorEditor.DrawPanel;

import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.ZoomType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ZoomEventWrapper {
    
    private double oldScale;
    private double newScale;
    
    private int mouseXInOldZoom;
    private int mouseYInOldZoom;
    
    private ZoomType zoomType;

    public ZoomEventWrapper(double oldScale, double newScale, int mouseXInOldZoom, int mouseYInOldZoom, ZoomType zoomType) {
        this.oldScale = oldScale;
        this.newScale = newScale;
        this.mouseXInOldZoom = mouseXInOldZoom;
        this.mouseYInOldZoom = mouseYInOldZoom;
        this.zoomType = zoomType;
    }

    public int getMouseXInOldZoom() {
        return mouseXInOldZoom;
    }

    public int getMouseYInOldZoom() {
        return mouseYInOldZoom;
    }

    public double getNewScale() {
        return newScale;
    }

    public double getOldScale() {
        return oldScale;
    }

    public ZoomType getZoomType() {
        return zoomType;
    }
}
