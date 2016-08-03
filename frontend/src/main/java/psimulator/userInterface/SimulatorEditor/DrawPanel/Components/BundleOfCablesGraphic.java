package psimulator.userInterface.SimulatorEditor.DrawPanel.Components;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import psimulator.dataLayer.DataLayerFacade;
import shared.Components.HwTypeEnum;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class BundleOfCablesGraphic extends AbstractComponentGraphic{

    private HwComponentGraphic component1;
    private HwComponentGraphic component2;
    
    private List<CableGraphic> cables;
    
    private static final int LINE_WIDTH = 2;
    
    
    public BundleOfCablesGraphic(HwComponentGraphic component1, HwComponentGraphic component2){
        super();
        
        cables = new ArrayList<>();
        
        this.component1 = component1;
        this.component2 = component2;
    }
    
    
    @Override
    public HwTypeEnum getHwType() {
        return HwTypeEnum.BUNDLE_OF_CABLES;
    }

    @Override
    public Integer getId() {
        return new Integer(-1);
    }
    
    /**
     * Use when building graph from Network.
     * @param dataLayer
     * @param imageFactory
     */
    @Override
    public void setInitReferences(DataLayerFacade dataLayer){
        this.dataLayer = dataLayer;
        
        /*
        for(CableGraphic c : cables){
            c.setInitReferences(dataLayer);
        }*/
    }
    
    @Override
    public void doUpdateImages() {
        for (CableGraphic c : cables) {
            c.doUpdateImages();
        }
    }
    
    public HwComponentGraphic getComponent1(){
        return component1;
    }
    
    public HwComponentGraphic getComponent2(){
        return component2;
    }
    
    public CableGraphic getIntersectingCable(Point p){
        //throw new UnsupportedOperationException("Not supported yet.");
        Rectangle r = doCreateRectangleAroundPoint(p);
        
        for(CableGraphic c : cables){
            if(c.intersects(r)){
                return c;
            }
        }
        return null;
    }

    public List<CableGraphic> getCables() {
        return cables;
    }
 
    public int getCablesCount(){
        return cables.size();
    }
    
    /**
     * adds cable to bundle
     * @param c 
     */
    public void addCable(CableGraphic c){
        cables.add(c);
    }
    
    /**
     * remove cable from bundle
     * @param c 
     */
    public void removeCable(CableGraphic c){
        cables.remove(c);
    }
     
    @Override
    public void paintComponent(Graphics g) {
        
        // two points of line in the middle
        int x1 = component1.getCenterLocation().x;
        int y1 = component1.getCenterLocation().y;
        int x2 = component2.getCenterLocation().x;
        int y2 = component2.getCenterLocation().y;
        
        
        double L = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
        
        // count difference between cables that will be applied
        double maxDiffernce = (ZoomManagerSingleton.getInstance().getIconWidth()/1.5) / cables.size();
        double optimalDifference = 12.0 * ZoomManagerSingleton.getInstance().getCurrentScale();
        double difference = Math.min(maxDiffernce, optimalDifference);
        
        // set offset to start with
        double offsetPixels = -(difference * (cables.size()-1) /2.0);
               
        // for all cables
        for(CableGraphic c : cables){
            // count starting point
            int x1p = (int)(x1 + offsetPixels * (y2-y1) / L);
            int y1p = (int)(y1 + offsetPixels * (x1-x2) / L);
            
            // count finishing point
            int x2p = (int)(x2 + offsetPixels * (y2-y1) / L);
            int y2p = (int)(y2 + offsetPixels * (x1-x2) / L);
            
            // paint cable
            c.paintComponent(g, x1p, y1p, x2p, y2p);
            
            // change offset for next cable
            offsetPixels += difference;
        }
    }
    
    @Override
    public int getWidth() {
        //return Math.abs(getX1()-getX2());
        return LINE_WIDTH;
    }

    @Override
    public int getHeight() {
        //return Math.abs(getY1()-getY2());
        return LINE_WIDTH;
    }

    @Override
    public int getX() {
        if(getX1()<=getX2()){
            return getX1();
        }
        return getX2();
    }

    @Override
    public int getY() {
        if(getY1()<=getY2()){
            return getY1();
        }
        return getY2();
    }

    
    @Override
    public boolean intersects(Point p) {
        Rectangle r = doCreateRectangleAroundPoint(p);
        return intersects(r);
    }
    
    @Override
    public boolean intersects(Rectangle r) {
        for(CableGraphic c : cables){
            if(c.intersects(r)){
                return true;
            }
        }
        
        return false;
    }

    public int getX1() {
        return getComponent1().getCenterLocation().x;
    }

    public int getY1() {
        return getComponent1().getCenterLocation().y;
    }

    public Point2D getP1() {
        return getComponent1().getCenterLocation();
    }

    public int getX2() {
        return getComponent2().getCenterLocation().x;
    }

    public int getY2() {
        return getComponent2().getCenterLocation().y;
    }

    public Point2D getP2() {
        return getComponent2().getCenterLocation();
    }
    
    
    private Rectangle doCreateRectangleAroundPoint(Point p){
        // count difference on both sides from p
        int difference = Math.max((int) (ZoomManagerSingleton.getInstance().getStrokeWidth()), 1);
        
        // create rectangle around point
        Rectangle r = new Rectangle(p.x - difference, p.y - difference,
                2 * difference, 2 * difference);
        return r;
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
