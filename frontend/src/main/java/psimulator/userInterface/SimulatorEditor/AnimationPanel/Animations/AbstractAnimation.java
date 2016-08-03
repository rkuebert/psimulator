package psimulator.userInterface.SimulatorEditor.AnimationPanel.Animations;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.concurrent.TimeUnit;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.AnimationPanelInnerInterface;
import shared.SimulatorEvents.SerializedComponents.EventType;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractAnimation implements TimingTarget{
    //
    protected Animator animator;
    //
    protected AnimationPanelInnerInterface animationPanelInnerInterface;
    protected DataLayerFacade dataLayer;
    //
    protected int defaultZoomStartX;
    protected int defaultZoomStartY;
    protected int defaultZoomEndX;
    protected int defaultZoomEndY;
    //
    protected Image image;
    protected PacketType packetType;
    //
    protected boolean visible;
    //
    protected double defautlZoomWidthDifference = 0.0;
    protected double defautlZoomHeightDifference = 0.0;
    //
    protected EventType eventType;
    //
    protected double fraction;
    
    public AbstractAnimation(final AnimationPanelInnerInterface animationPanelInnerInterface,
            DataLayerFacade dataLayer,
            PacketType packetType, Point defaultZoomSource, Point defaultZoomDest,
            int durationInMilliseconds,
            EventType eventType) {

        this.dataLayer = dataLayer;
        this.animationPanelInnerInterface = animationPanelInnerInterface;
        //
        this.packetType = packetType;
        this.eventType = eventType;

        // get image
        image = ImageFactorySingleton.getInstance().getPacketImage(packetType, animationPanelInnerInterface.getPacketImageType(),
                ZoomManagerSingleton.getInstance().getPackageIconWidth());

        // get start coordinates in default zoom
        defaultZoomStartX = defaultZoomSource.x;
        defaultZoomStartY = defaultZoomSource.y;

        // get end coordinates in default zoom
        defaultZoomEndX = defaultZoomDest.x;
        defaultZoomEndY = defaultZoomDest.y;

        // create animator      
        animator = new Animator.Builder().setDuration(durationInMilliseconds, TimeUnit.MILLISECONDS).
                setStartDirection(Animator.Direction.FORWARD).
                addTarget((TimingTarget) this).build();

        animator.start();
    }
    
    
        /**
     * Stops the animation
     */
    public void stopAnimator() {
        animator.stop();
    }
    
    
    /**
     * Returns image of animation in actual zoom sizes
     *
     * @return
     */
    public Image getImage() {
        image = ImageFactorySingleton.getInstance().getPacketImage(packetType, animationPanelInnerInterface.getPacketImageType(),
                ZoomManagerSingleton.getInstance().getPackageIconWidth());
        return image;
    }

    /**
     * Gets X position of animated image in actual zoom.
     *
     * @return
     */
    public int getX() {
        return (int) ZoomManagerSingleton.getInstance().doScaleToActual(defaultZoomStartX + defautlZoomWidthDifference - (ZoomManagerSingleton.getInstance().getPackageIconWidthDefaultZoom() / 2.0));
    }

    /**
     * Gets Y position of animated image in actual zoom.
     *
     * @return
     */
    public int getY() {
        return (int) ZoomManagerSingleton.getInstance().doScaleToActual(defaultZoomStartY + defautlZoomHeightDifference - (ZoomManagerSingleton.getInstance().getPackageIconWidthDefaultZoom() / 2.0));
    }
    
    
    /**
     * Finds if visible
     *
     * @return
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Gets current fraction of animation <0,1>
     */
    public double getFraction() {
        return fraction;
    }

    /**
     * Gets if this animation should end normally, or has to end in the middle
     * because the pacekt was lost
     */
    public EventType getEventType() {
        return eventType;
    }
    
    
    // ---------- TimingTarget implementation ------------------- //
    @Override
    public void begin(Animator source) {
        // nothing to do
    }

    @Override
    public void end(Animator source) {
        // at the end of animation remove itself from animation pane
        animationPanelInnerInterface.removeAnimation(this);
    }

    @Override
    public void repeat(Animator source) {
        // nothing to do
    }

    @Override
    public void reverse(Animator source) {
        // nothing to do
    }

    @Override
    public void timingEvent(Animator source, double fraction) {
        this.fraction = fraction;
        move(fraction);
    }
    
    /**
     * Moves image coordinates according to elapsed fraction of time.
     *
     * @param fraction
     */
    protected abstract void move(double fraction);
    
    public abstract void paintComponent(Graphics2D g2);

}
