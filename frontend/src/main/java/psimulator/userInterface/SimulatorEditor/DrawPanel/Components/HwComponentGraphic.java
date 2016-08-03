package psimulator.userInterface.SimulatorEditor.DrawPanel.Components;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.LevelOfDetailsMode;
import shared.Components.EthInterfaceModel;
import shared.Components.HwComponentModel;
import shared.Components.HwTypeEnum;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;
import psimulator.dataLayer.Enums.ViewDetailsType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Support.GraphicUtils;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class HwComponentGraphic extends AbstractComponentGraphic {

    protected HwComponentModel hwComponentModel;
    
    // position in 1:1 zoom
    protected int defaultZoomWidth;
    protected int defaultZoomHeight;
    // position of textRectangle in 1:1 zoom
    protected int defaultZoomTextWidth;
    protected int defaultZoomTextHeight;
    //
    private List<BundleOfCablesGraphic> bundlesOfCables = new ArrayList<>();
    //
    protected BufferedImage imageUnmarked;
    protected BufferedImage imageMarked;
    protected List<BufferedImage> textImages;
    //
    
    /**
     * Use when creating graph by user actions.
     * @param imageFactory
     * @param dataLayer
     * @param hwType 
     */
    public HwComponentGraphic(DataLayerFacade dataLayer, HwComponentModel hwComponentModel){//, int interfacesCount) {
        super(dataLayer);
        
        this.hwComponentModel = hwComponentModel;
    }
    
    /**
     * Use when building graph from Network.
     * @param id
     * @param hwType 
     */
    public HwComponentGraphic(HwComponentModel hwComponentModel){
        super();
        
        this.hwComponentModel = hwComponentModel;
    }

    public HwComponentModel getHwComponentModel() {
        return hwComponentModel;
    }
    
    
    @Override
    public HwTypeEnum getHwType() {
        return hwComponentModel.getHwType();
    }

    @Override
    public Integer getId() {
        return hwComponentModel.getId();
    }
    
    /**
     * Adds new interface to the component
     */
    public void addInterface(EthInterfaceModel ethInterface){
        // add interface
        hwComponentModel.addInterface(ethInterface);
    }
    
    /**
     * Removes last interface from component if interface does not have cable and has more
     * interfaces than minimum
     */
    public void removeInterface(EthInterfaceModel ethInterface){
        // remove interface
        getHwComponentModel().removeInterface(ethInterface);    
    }
    
    /**
     * Changes position by offset in parameter adding it to default zoom
     *
     * @param offsetInDefaultZoom
     */
    public void doChangePosition(Dimension offsetInDefaultZoom, boolean positive) {
        Point defaultZoomPoint;
        // count point to move component
        if (positive) {
            defaultZoomPoint = new Point(hwComponentModel.getDefaultZoomXPos() + offsetInDefaultZoom.width,
                    hwComponentModel.getDefaultZoomYPos() + offsetInDefaultZoom.height);
        } else {
            defaultZoomPoint = new Point(hwComponentModel.getDefaultZoomXPos() - offsetInDefaultZoom.width,
                    hwComponentModel.getDefaultZoomYPos() - offsetInDefaultZoom.height);
        }
        // set new postion
        setLocation(defaultZoomPoint.x, defaultZoomPoint.y);
    }

    /**
     * Sets position with center of image in the middlePoint
     *
     * @param middlePoint Center of image in actual zoom
     */
    public void setLocationByMiddlePoint(Point middlePoint) {
        setLocation(ZoomManagerSingleton.getInstance().doScaleToDefault(middlePoint.x) - (defaultZoomWidth / 2),
                ZoomManagerSingleton.getInstance().doScaleToDefault(middlePoint.y) - (defaultZoomHeight / 2));
    }

    @Override
    public void setLocation(int defaultZoomXPos, int defaultZoomYPos) {
        //System.out.println("tady");

        if (defaultZoomXPos < 0) {
            defaultZoomXPos = 0;
        }
        if (defaultZoomYPos < 0) {
            defaultZoomYPos = 0;
        }

        // update defautl position (without zoom)
        this.hwComponentModel.setDefaultZoomXPos(defaultZoomXPos);
        this.hwComponentModel.setDefaultZoomYPos(defaultZoomYPos);
    }

    @Override
    public boolean intersects(Point p) {
        if ((p.x >= getX() && p.x <= getX() + imageUnmarked.getWidth())
                && (p.y >= getY() && p.y <= getY() + imageUnmarked.getHeight())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean intersects(Rectangle r) {
        Rectangle rect = new Rectangle(getX(), getY(), imageUnmarked.getWidth(), imageUnmarked.getHeight());
        //Rectangle rect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        return r.intersects(rect);
    }
    
    /**
     * Calculates intersecting point of this component and line made from inside and outside point.
     * Inside point is in this component. Outside point is out of the component
     * 
     * @param insidePoint Point in actual zoom
     * @param outsidePoint Point in actual zoom
     * @return Point in actual zoom
     */
    public Point getIntersectingPoint(Point insidePoint, Point outsidePoint) {
        Point intersection;
        
        Rectangle rectangle;
        Line2D line = new Line2D.Float();
        
        for(int i = textImages.size()-1;i>=0;i--){
            BufferedImage image = textImages.get(i);
            
            int x,y,w,h;
            
            x=(int) (ZoomManagerSingleton.getInstance().doScaleToActual(hwComponentModel.getDefaultZoomXPos()) - ((image.getWidth() - ZoomManagerSingleton.getInstance().doScaleToActual(defaultZoomWidth))/2.0));
            y= ZoomManagerSingleton.getInstance().doScaleToActual(hwComponentModel.getDefaultZoomYPos()) + ZoomManagerSingleton.getInstance().doScaleToActual(defaultZoomHeight) + i*image.getHeight();
            w = image.getWidth();
            h = image.getHeight();
            
            rectangle = new Rectangle(x,y,w,h);
            
            line.setLine(insidePoint, outsidePoint);
 
            if(line.intersects(rectangle)){
                intersection = GraphicUtils.getIntersectingPoint(rectangle, insidePoint, outsidePoint);
                return intersection;
            }
        }
        
        // no text rectangle intersects line
        
        rectangle = new Rectangle(getX(), getY(), getWidth(), getHeight());
        intersection = GraphicUtils.getIntersectingPoint(rectangle, insidePoint, outsidePoint);
        
        return intersection;
    }

    //----------- GETTERS AND SETTERS
    /**
     * returns all EthInterfaces
     *
     * @return
     */
    public List<EthInterfaceModel> getInterfaces() {
        return new ArrayList<>(hwComponentModel.getEthInterfaces());
    }

    public Object[] getInterfacesNames() {
        return hwComponentModel.getInterfacesNames();
    }

    public EthInterfaceModel getEthInterface(Integer id) {
        return hwComponentModel.getEthInterface(id);
    }

    public EthInterfaceModel getEthInterfaceAtIndex(int index) {
        return hwComponentModel.getEthInterfaceAtIndex(index);
    }

    /**
     * gets first avaiable ethInterface, if no avaiable null is renturned
     *
     * @return
     */
    public EthInterfaceModel getFirstFreeInterface() {
        return hwComponentModel.getFirstFreeInterface();
    }

    /**
     * finds out whether component has any free EthInterface
     *
     * @return
     */
    public boolean hasFreeInterace() {
        return hwComponentModel.hasFreeInterace();
    }

    /**
     * gets all bundles of cables
     *
     * @return
     */
    public List<BundleOfCablesGraphic> getBundleOfCableses() {
        return bundlesOfCables;
    }

    /**
     * adds bundle of cables to component
     *
     * @param boc
     */
    public void addBundleOfCables(BundleOfCablesGraphic boc) {
        bundlesOfCables.add(boc);
    }

    /**
     * removes bundle of cables from this component
     *
     * @param boc
     */
    public void removeBundleOfCables(BundleOfCablesGraphic boc) {
        bundlesOfCables.remove(boc);
    }

    /**
     * gets center of this component
     *
     * @return
     */
    public Point getCenterLocation() {
        return new Point(getX() + getWidth() / 2, getY() + getHeight() / 2);
    }
    
    /**
     * gets center of this component
     *
     * @return
     */
    public Point getCenterLocationDefault() {
        return new Point(hwComponentModel.getDefaultZoomXPos() + defaultZoomWidth / 2, 
                hwComponentModel.getDefaultZoomYPos() + defaultZoomTextHeight / 2);
    }

    /**
     * gets center of this component
     *
     * @return
     */
    public Point getCenterLocationDefaultZoom() {
        return new Point(hwComponentModel.getDefaultZoomXPos() + defaultZoomWidth / 2,
                hwComponentModel.getDefaultZoomYPos() + defaultZoomHeight / 2);
    }

    /**
     * Gets Point in actual scale of lower right corner of component
     *
     * @return Actual-scale ponint
     
    public Point getLowerRightCornerLocation1() {
        return new Point(getX() + getWidth(), getY() + getHeight());
    }*/
    
    /**
     * Gets Point in actual scale of lower right corner of component including text labels
     *
     * @return Actual-scale ponint
     */
    public Point getLowerRightCornerLocation() {
        int x = getDefaultZoomXPos() + getDefaultZoomWidth();
        if(getDefaultZoomTextWidth()> getDefaultZoomWidth()){
            x += ((getDefaultZoomTextWidth() - getDefaultZoomWidth())/2.0);
        }
        
        int y = getDefaultZoomYPos() + getDefaultZoomHeight() + getDefaultZoomTextHeight();
        
        return new Point(ZoomManagerSingleton.getInstance().doScaleToActual(x), ZoomManagerSingleton.getInstance().doScaleToActual(y));
    }

    @Override
    public int getWidth() {
        return ZoomManagerSingleton.getInstance().doScaleToActual(defaultZoomWidth);
    }

    @Override
    public int getHeight() {
        return ZoomManagerSingleton.getInstance().doScaleToActual(defaultZoomHeight);// + getTextHeight();
    }

    @Override
    public int getX() {
        return ZoomManagerSingleton.getInstance().doScaleToActual(hwComponentModel.getDefaultZoomXPos());
    }

    @Override
    public int getY() {
        return ZoomManagerSingleton.getInstance().doScaleToActual(hwComponentModel.getDefaultZoomYPos());
    }

    public String getDeviceName() {
        return hwComponentModel.getName();
    }

    public void setDeviceName(String deviceName) {
        hwComponentModel.setName(deviceName);
    }

    public int getInterfaceCount() {
        return hwComponentModel.getEthInterfaceCount();
    }

    public void setDefaultZoomXPos(int defaultZoomXPos) {
        this.hwComponentModel.setDefaultZoomXPos(defaultZoomXPos);
    }

    public void setDefaultZoomYPos(int defaultZoomYPos) {
        this.setDefaultZoomYPos(defaultZoomYPos);
    }

    public int getDefaultZoomXPos() {
        return hwComponentModel.getDefaultZoomXPos();
    }

    public int getDefaultZoomYPos() {
        return hwComponentModel.getDefaultZoomYPos();
    }

    public int getDefaultZoomHeight() {
        return defaultZoomHeight;
    }

    public int getDefaultZoomWidth() {
        return defaultZoomWidth;
    }
    
    public int getTextsWidth(){
        return ZoomManagerSingleton.getInstance().doScaleToActual(defaultZoomTextWidth);
    }
    
    public int getTextsHeight(){
        return ZoomManagerSingleton.getInstance().doScaleToActual(defaultZoomTextHeight);
    }

    public int getDefaultZoomTextHeight() {
        return defaultZoomTextHeight;
    }

    public int getDefaultZoomTextWidth() {
        return defaultZoomTextWidth;
    }
    
    @Override
    public void initialize() {
        doUpdateImages();

        // set image width and height in default zoom
        defaultZoomWidth = ZoomManagerSingleton.getInstance().doScaleToDefault(imageUnmarked.getWidth());
        defaultZoomHeight = ZoomManagerSingleton.getInstance().doScaleToDefault(imageUnmarked.getHeight());
    }

    @Override
    public final void doUpdateImages() {
        // get new images of icons
        imageUnmarked = ImageFactorySingleton.getInstance().getImage(getHwType(), ZoomManagerSingleton.getInstance().getIconWidth(), false);
        imageMarked = ImageFactorySingleton.getInstance().getImage(getHwType(), ZoomManagerSingleton.getInstance().getIconWidth(), true);
        
        // get texts that have to be painted
        List<String> texts = getTexts();
        textImages = getTextsImages(texts, ZoomManagerSingleton.getInstance().getCurrentFontSize());
        
        // set text images width and height
        int textW = 0;
        int textH = 0;

        for (BufferedImage image : textImages) {
            if (image.getWidth() > textW) {
                textW = image.getWidth();
            }

            textH = textH + image.getHeight();
        }

        defaultZoomTextWidth = ZoomManagerSingleton.getInstance().doScaleToDefault(textW);
        defaultZoomTextHeight = ZoomManagerSingleton.getInstance().doScaleToDefault(textH);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (isMarked()) {
            // paint image
            g2.drawImage(imageMarked, getX(), getY(), null);
        } else {
            // paint image
            g2.drawImage(imageUnmarked, getX(), getY(), null);
        }
        
        // paint texts
        if (textImages != null) {
            paintTextsUnderImage(g2);
        }
    }

    private void paintTextsUnderImage(Graphics2D g2) {
        paintTexts(g2, textImages);
    }

    /**
     * Paint imagess of texts centered in Y_axes under component image
     *
     * @param g2
     * @param images
     */
    private void paintTexts(Graphics2D g2, List<BufferedImage> images) {
        int x;
        int y = getY() + getHeight() + 1;

        for (BufferedImage image : images) {
            x = (int) (getX() - ((image.getWidth() - getWidth()) / 2.0));

            g2.drawImage(image, x, y, null);
            
            y = y + image.getHeight();
        }
    }
    
    
    /**
     * Gets text that have to be displayed with this component.
     *
     * @return
     */
    private List<String> getTexts() {
        boolean paintType = false;
        boolean paintName = false;

        // if LOD active
        if (dataLayer.getLevelOfDetails() == LevelOfDetailsMode.AUTO) {
            switch (ZoomManagerSingleton.getInstance().getCurrentLevelOfDetails()) {
                case LEVEL_1:
                    break;
                case LEVEL_2:
                    paintName = true;
                    break;
                case LEVEL_3:
                case LEVEL_4:
                default:
                    paintType = true;
                    paintName = true;
                    break;
            }
        } else { // if LOD not active
            paintName = dataLayer.isViewDetails(ViewDetailsType.DEVICE_NAMES);
            paintType = dataLayer.isViewDetails(ViewDetailsType.DEVICE_TYPES);
        }

        /*
         * if (paintName == false && paintType == false) { return null; }
         */

        // list for texts
        List<String> texts = new ArrayList<String>();

        if (paintType) {
            texts.add(dataLayer.getString(getHwType().toString()));
        }

        if (paintName) {
            texts.add(getDeviceName());
        }


        return texts;
    }

}
