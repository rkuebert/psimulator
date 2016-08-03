package psimulator.userInterface.SimulatorEditor.DrawPanel.Components;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import shared.Components.HwTypeEnum;
import psimulator.dataLayer.DataLayerFacade;
import shared.Components.Identifiable;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractComponentGraphic extends JComponent implements Markable, Identifiable {

    //
    protected DataLayerFacade dataLayer;
    //

    private boolean marked = false;
    //

    /**
     * Use when creating graph by user actions.
     * @param dataLayer
     */
    public AbstractComponentGraphic(DataLayerFacade dataLayer){
        this.dataLayer = dataLayer;
    }
    
    /**
     * Use when building graph from Network.
     */
    public AbstractComponentGraphic(){
    }
    
    /**
     * Use when building graph from Network.
     * @param dataLayer
     * @param imageFactory
     */
    public void setInitReferences(DataLayerFacade dataLayer){
        this.dataLayer = dataLayer;
    }

    @Override
    public boolean isMarked() {
        return marked;
    }

    @Override
    public void setMarked(boolean marked) {
        this.marked = marked;
    }
    
    public abstract HwTypeEnum getHwType();

    public abstract boolean intersects(Point p);

    public abstract boolean intersects(Rectangle r);

    public abstract void doUpdateImages();

    public abstract void initialize();

    /**
     * Creates images for givent texts
     *
     * @param texts
     * @return
     */
    protected List<BufferedImage> getTextsImages(List<String> texts, int fontSize) {
        // create font
        Font font = new Font("SanSerif", Font.PLAIN, fontSize); //zoomManager.getCurrentFontSize()

        // get font metrics
        FontMetrics fm = ImageFactorySingleton.getInstance().getFontMetrics(font);

        List<BufferedImage> images = new ArrayList<BufferedImage>();

        for (String text : texts) {
            images.add(getImageForText(fm, text, font));
        }
    return images;
    }

    /**
     * Creates image for text in font with given FontMetrics
     *
     * @param fm
     * @param text
     * @param font
     * @return
     */
    protected BufferedImage getImageForText(FontMetrics fm, String text, Font font) {
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent() + fm.getDescent();

        return ImageFactorySingleton.getInstance().getImageWithText(text, font, textWidth, textHeight, fm.getMaxAscent());
    }

    /**
     * Creates images for givent texts
     *
     * @param texts
     * @param g2
     * @return
     */
    protected BufferedImage getTextImage(String text, int fontSize) {
        // create font
        Font font = new Font("SanSerif", Font.PLAIN, fontSize); //zoomManager.getCurrentFontSize()

        // get font metrics
        FontMetrics fm = ImageFactorySingleton.getInstance().getFontMetrics(font);

        return getImageForText(fm, text, font);
    }
}
