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
public class AnimationLostInDevice extends AnimationLostInCable {

    public AnimationLostInDevice(AnimationPanelInnerInterface animationPanelInnerInterface,
            DataLayerFacade dataLayer, PacketType packetType,
            Point defaultZoomSource, Point defaultZoomDest,
            int durationInMilliseconds, EventType eventType) {

        super(animationPanelInnerInterface, dataLayer, packetType,
                defaultZoomSource, defaultZoomDest,
                durationInMilliseconds, eventType);
    }
    
    /*
    @Override
    protected void move(double fraction) {
        // position will stop in the middle of cable
        if (fraction <= 0.5) {
            defautlZoomWidthDifference = (defaultZoomEndX - defaultZoomStartX) * fraction;
            defautlZoomHeightDifference = (defaultZoomEndY - defaultZoomStartY) * fraction;
        } else {
            defautlZoomWidthDifference = (defaultZoomEndX - defaultZoomStartX) * 0.5;
            defautlZoomHeightDifference = (defaultZoomEndY - defaultZoomStartY) * 0.5;
        }
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/
    
}
