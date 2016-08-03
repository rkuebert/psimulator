package psimulator.dataLayer.Singletons;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Observable;
import java.util.prefs.Preferences;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.interfaces.SaveableInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.LevelOfDetail;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.ZoomType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.ZoomEventWrapper;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class ZoomManagerSingleton extends Observable implements SaveableInterface{
    private static final String ZOOM_SCALE = "ZOOM_SCALE";
    private Preferences prefs;
    //
    private int hwIconWidth = 140;
    private int packageIconWidth = 80;
    //
    private int defaultScale = 8;
    private int scale = defaultScale;
    private int zoomInc = 1;
    private int minScale = 1;
    private int maxScale = 10;
    //
    private float basicStrokeWidth = 5f;
    //
    private int defaultFontSize = 24;
    
    // Wrapper prepared when notifyObservers called
    private ZoomEventWrapper zoomEventWrapper;
    
    private ZoomManagerSingleton() {
        // initialize preferences store
        prefs = Preferences.userNodeForPackage(this.getClass());
        
        // load preferences
        loadPreferences();    
    }
    
    public static ZoomManagerSingleton getInstance() {
        return ZoomManagerSingletonHolder.INSTANCE;
    }

    /**
     * Saves zoom scale into preferences
     */
    @Override
    public void savePreferences() {
        prefs.putInt(ZOOM_SCALE, scale);
    }

    /**
     * Loads zoom scale from preferenes
     */
    @Override
    public void loadPreferences() {
        scale = prefs.getInt(ZOOM_SCALE, scale);
        
        if(scale< minScale || scale > maxScale){
            scale = defaultScale;
        }
    }
    
    private static class ZoomManagerSingletonHolder {
        private static final ZoomManagerSingleton INSTANCE = new ZoomManagerSingleton();
    }
    
    /**
     * Gets zoom event wrapper created when zoom last changed
     * @return 
     */
    public ZoomEventWrapper getZoomEventWrapper() {
        return zoomEventWrapper;
    }
       
    /**
     * returns Icon size according to scale and default icon size
     * @return 
     */
    public int getIconWidth() {
        return (int) (getCurrentScale() * hwIconWidth);
    }

    /**
     * returns Icon size according to default zoom  scale
     */
    public int getIconWidthDefaultZoom() {
        return (int) (hwIconWidth);
    }
    
    /**
     * returns Icon size according to scale and default icon size
     * @return 
     */
    public int getPackageIconWidth() {
        return (int) (getCurrentScale() * packageIconWidth);
    }

    /**
     * returns Icon size according to default zoom  scale
     */
    public int getPackageIconWidthDefaultZoom() {
        return (int) (packageIconWidth);
    }

    /**
     * returns stroke width according to current scale
     * @return 
     */
    public float getStrokeWidth() {
        return Math.max((float) (basicStrokeWidth * getCurrentScale()), 0.8f);
    }

    /**
     * Gets current scale
     * @return Current scale
     */
    public double getCurrentScale() {
        return getScale(scale);
    }

    /**
     * Zooms in if possible and notifies all observers
     */
    public void zoomIn() {
        zoomIn(new Point(0, 0), ZoomType.CENTER);
    }

    public void zoomIn(Point mousePostition, ZoomType zoomType) {
        if (canZoomIn()) {
            double oldZoom = getScale(scale);
            
            scale += zoomInc;
            
            double newZoom = getScale(scale);
            
            // notify all observers
            notifyAllObservers(mousePostition, oldZoom, newZoom, zoomType);
        }
    }

    /**
     * Zooms out if possible and notifies all observers
     */
    public void zoomOut() {
        zoomOut(new Point(0, 0), ZoomType.CENTER);
    }

    /**
     * Zooms out if possible and notifies all observers
     */
    public void zoomOut(Point mousePostition, ZoomType zoomType) {
        if (canZoomOut()) {
            double oldZoom = getScale(scale);
            
            scale -= zoomInc;
            
            double newZoom = getScale(scale);
            
            // notify all observers
            notifyAllObservers(mousePostition, oldZoom, newZoom, zoomType);
        }
    }

    /**
     * Resets zoom to default and notifies all observers
     */
    public void zoomReset() {
        double oldZoom = getScale(scale);
        
        // scale set to default
        scale = defaultScale;
        
        double newZoom = getScale(scale);
        
        // notify all observers
        notifyAllObservers(new Point(0, 0), oldZoom, newZoom, ZoomType.CENTER);
    }

    /**
     * Finds whether is possible to zoom out
     * @return true if possible, otherwise false
     */
    public boolean canZoomOut() {
        // if maximum zoom not reached
        if (scale >= minScale + zoomInc) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Finds whether is possible to zoom in
     * @return true if possible, otherwise false
     */
    public boolean canZoomIn() {
        // if maximum zoom reached
        if (scale <= maxScale - zoomInc) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Scales point in default scale to actual scale point
     * @param defaultScalePoint Point in default scale
     * @return Scaled point in actualScale
     */
    public Point doScaleToActual(Point defaultScalePoint) {
        return new Point((int) (defaultScalePoint.x * getScale(scale)), (int) (defaultScalePoint.y * getScale(scale)));
    }

    /**
     * Scales dimension in default scale to actual scale point
     * @param defaultScaleDimension Dimension in actual scale
     * @return Scaled dimension in actual scale
     */
    public Dimension doScaleToActual(Dimension defaultScaleDimension) {
        return new Dimension((int) (defaultScaleDimension.width * getScale(scale)), (int) (defaultScaleDimension.height * getScale(scale)));
    }

    /**
     * Scales defaultScale number to actualScale number
     * @param defaultScale Number in default scale
     * @return Number in actual scale
     */
    public int doScaleToActual(int defaultScale) {
        return ((int) (defaultScale * getScale(scale)));
    }
    
    /**
     * Scales defaultScale number to actualScale number
     * @param defaultScale Number in default scale
     * @return Number in actual scale
     */
    public double doScaleToActual(double defaultScale) {
        return (defaultScale * getScale(scale));
    }

    /**
     * Scales point in actual scale to default scale point
     * @param actualScalePoint Point in actual scale
     * @return Scaled point in default scale
     */
    public Point doScaleToDefault(Point actualScalePoint) {
        return new Point((int) (actualScalePoint.x / getScale(scale)), (int) (actualScalePoint.y / getScale(scale)));
    }

    /**
     * Scales dimension in actual scale to default scale point
     * @param actualScaleDimension Dimension in actual scale
     * @return Scaled dimension in default scale
     */
    public Dimension doScaleToDefault(Dimension actualScaleDimension) {
        return new Dimension((int) (actualScaleDimension.width / getScale(scale)), (int) (actualScaleDimension.height / getScale(scale)));
    }

    /**
     * Scales actualScale number todefaultScale number
     * @param actualScale Number in actual scale 
     * @return Number in default scale
     */
    public int doScaleToDefault(int actualScale) {
        return ((int) (actualScale / getScale(scale)));
    }

    
    
    /**
     * Returns font size to use in current zoom.
     * @return Size of font in int.
     */
    public int getCurrentFontSize(){
        return (int) (defaultFontSize * getScale(scale));
    }
    
    /**
     * Gets Level of detail according to current zoom.
     * @return 
     */
    public LevelOfDetail getCurrentLevelOfDetails(){
        if(getScale(scale) <=0.3){
            return LevelOfDetail.LEVEL_1;
        }else if(getScale(scale) < 0.6){
            return LevelOfDetail.LEVEL_2;
        }else if(getScale(scale) < 0.8){
            return LevelOfDetail.LEVEL_3;   
        }else{
            return LevelOfDetail.LEVEL_4;   
        }
    }

    /**
     * Converts scale to double. (Divide by 10)
     * @param scale
     * @return 
     */
    private double getScale(int scale){
        return (double)(scale / 10.0);
    }  
    
    /**
     * calls setChanged and notifyObservers
     */
    private void notifyAllObservers(Point mousePostition, double odlScale, double newScale, ZoomType zoomType) {
        this.zoomEventWrapper = new ZoomEventWrapper(odlScale, newScale, mousePostition.x, mousePostition.y, zoomType);
        
        setChanged();
        //notifyObservers(new ZoomEventWrapper(false, mousePostition.x, mousePostition.y, 0.0));
        notifyObservers(ObserverUpdateEventType.ZOOM_CHANGE);
    }
}
