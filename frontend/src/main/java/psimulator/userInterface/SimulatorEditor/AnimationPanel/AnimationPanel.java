package psimulator.userInterface.SimulatorEditor.AnimationPanel;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.core.animation.timing.TimingSource.PostTickListener;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import psimulator.dataLayer.Singletons.TimerKeeperSingleton;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.Animations.AbstractAnimation;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.Animations.AnimationLostInCable;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.Animations.AnimationLostInDevice;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.Animations.AnimationSuccessful;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.CableGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.PacketImageType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;
import psimulator.userInterface.SimulatorEditor.UserInterfaceMainPanelInnerInterface;
import shared.SimulatorEvents.SerializedComponents.EventType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class AnimationPanel extends AnimationPanelOuterInterface implements AnimationPanelInnerInterface {
    //

    //private static final TimingSource f_repaintTimer = new SwingTimerTimingSource();
    private PostTickListener postTickListener;
    //
    private DataLayerFacade dataLayer;
    private Graph graph;
    //
    private List<AbstractAnimation> animations;
    //

    public AnimationPanel(MainWindowInnerInterface mainWindow, UserInterfaceMainPanelInnerInterface editorPanel,
            DataLayerFacade dataLayer, DrawPanelOuterInterface drawPanel) {

        super();
        // set timing sourcce to Animator
        //Animator.setDefaultTimingSource(f_repaintTimer);

        this.dataLayer = dataLayer;

        // set opacity
        this.setOpaque(false);

        // CopyOnWrite is good for:
        //  - reads hugely outnumber writes (paint component every 15ms)
        //  - the array is small (or writes are very infrequent)
        animations = new CopyOnWriteArrayList<>();

        // create post tick listener
        postTickListener = new TimingSource.PostTickListener() {

            @Override
            public void timingSourcePostTick(TimingSource source, long nanoTime) {
                repaint();
            }
        };

        // add jPanelAnimation as observer to preferences manager
        dataLayer.addPreferencesObserver((Observer) this);

        // add jPanelAnimation as observer to simulator manager
        dataLayer.addSimulatorObserver((Observer) this);
    }

    /**
     * Paints animations on this panel.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        Iterator<AbstractAnimation> it = animations.iterator();
        while (it.hasNext()) {
            AbstractAnimation animation = it.next();
            animation.paintComponent(g2);
        }

        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }   

    @Override
    public void update(Observable o, Object o1) {
        switch ((ObserverUpdateEventType) o1) {
            case VIEW_DETAILS:
                // no need to react, 
                break;
            case ZOOM_CHANGE:
                // no need to react, will change size from UserInterfaceLayeredPane
                break;
            case PACKET_IMAGE_TYPE_CHANGE:
                // no need to react
                break;
            case SIMULATOR_PLAYER_PLAY:
            case SIMULATOR_REALTIME_ON:
                connectToTimer();
                break;
            case SIMULATOR_PLAYER_STOP:
            case SIMULATOR_REALTIME_OFF:
                removeAllAnimations();
                disconnectFromTimer();
                break;
        }
    }

    private void connectToTimer() {
        TimerKeeperSingleton.getInstance().getTimingSource().addPostTickListener(postTickListener);
        //System.out.println("Connected to timer");
    }

    private void disconnectFromTimer() {
        TimerKeeperSingleton.getInstance().getTimingSource().removePostTickListener(postTickListener);
        //System.out.println("Disconnected from timer");
        this.repaint();
    }

    /**
     * Removes all animations from list
     */
    private void removeAllAnimations() {
        Iterator<AbstractAnimation> it = animations.iterator();
        while (it.hasNext()) {
            AbstractAnimation animation = it.next(); // convert X and Yto actual using zoom manager 
            animation.stopAnimator();
        }

        animations.clear();
    }

    /**
     * Removes concrete animation from animations list
     *
     * @param animation
     */
    @Override
    public void removeAnimation(AbstractAnimation animation) {
        animations.remove(animation);
    }

    /**
     * Gets current packet image type
     * @return 
     */
    @Override
    public PacketImageType getPacketImageType() {
        return dataLayer.getPackageImageType();
    }

    /**
     * Removes all animations and removes Graph.
     *
     * @return
     */
    @Override
    public Graph removeGraph() {
        removeAllAnimations();

        Graph tmp = graph;
        graph = null;
        return tmp;
    }

    /**
     * Sets graph and chnage bounds of this panel
     *
     * @param graph
     */
    @Override
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    /**
     * Creates animation of given timeInMiliseconds from AbstractHwComponent
     * idSource to AbstractHwComponent idDestination.
     *
     * If EventType is LOST_IN_DEVICE, than idDestination doesnt have to be set.
     * 
     * @param timeInMiliseconds
     * @param idSource
     * @param idDestination
     */
    @Override
    public void createAnimation(PacketType packetType, int timeInMiliseconds, int idSource, int idDestination, EventType eventType) {
        // points in Default zoom
        Point src;
        Point dest;
        
        // calculate start and end points of animation
        switch(eventType){
            case LOST_IN_DEVICE:
                // destination is near the component
                src = graph.getAbstractHwComponent(idSource).getCenterLocationDefaultZoom();
                dest = new Point(src.x+50,src.y+50);
                break;
            case SUCCESSFULLY_TRANSMITTED:
            case LOST_IN_CABLE:
            default:
                src = graph.getAbstractHwComponent(idSource).getCenterLocationDefaultZoom();
                dest = graph.getAbstractHwComponent(idDestination).getCenterLocationDefaultZoom();
                break;
         }

        // create animation
        AbstractAnimation animation;
        switch(eventType){
            case SUCCESSFULLY_TRANSMITTED:
                animation = new AnimationSuccessful(this, dataLayer, packetType, src, dest, 
                        timeInMiliseconds, eventType);
                break;
            case LOST_IN_CABLE:
                animation = new AnimationLostInCable(this, dataLayer, packetType, src, dest, 
                        timeInMiliseconds, eventType);
                break;
            case LOST_IN_DEVICE:
            default:
                animation = new AnimationLostInDevice(this, dataLayer, packetType, src, dest, 
                        timeInMiliseconds, eventType);
                break;
        }
        
        // add animation to animations list
        animations.add(animation);
    }

    /**
     * Counts the duration of animation im milliseconds
     *
     * @param cableId
     * @param speedCoeficient
     * @return
     */
    @Override
    public int getAnimationDuration(int cableId, int speedCoeficient) {
        // if cable not known
        if(cableId < 0){
            return 10 * speedCoeficient * 50;
        }
        
        // if we know id of cable
        CableGraphic cable = graph.getCable(cableId);

        int delay = cable.getDelay();

        speedCoeficient = speedCoeficient * 50;

        int speed = delay * speedCoeficient;

        return speed;
    }

    // do not take cursor
    @Override
    public boolean contains(int x, int y) {
        return false;
    }
}
