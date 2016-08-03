package psimulator.userInterface.SimulatorEditor.AnimationPanel.Animations;

import java.awt.Graphics2D;
import java.awt.Point;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.AnimationPanelInnerInterface;
import shared.SimulatorEvents.SerializedComponents.EventType;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class AnimationSuccessful extends AbstractAnimation{

    public AnimationSuccessful(AnimationPanelInnerInterface animationPanelInnerInterface, 
            DataLayerFacade dataLayer, PacketType packetType, 
            Point defaultZoomSource, Point defaultZoomDest, 
            int durationInMilliseconds, EventType eventType) {
        super(animationPanelInnerInterface, dataLayer, packetType, 
                defaultZoomSource, defaultZoomDest, 
                durationInMilliseconds, eventType);
    }

    @Override
    protected void move(double fraction) {
        defautlZoomWidthDifference = (defaultZoomEndX - defaultZoomStartX) * fraction;
        defautlZoomHeightDifference = (defaultZoomEndY - defaultZoomStartY) * fraction;
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        g2.drawImage(getImage(), getX(), getY(), null);
    }
    
}
