package psimulator.userInterface.SimulatorEditor.AnimationPanel.Animations;

import java.awt.*;
import java.awt.geom.GeneralPath;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.AnimationPanelInnerInterface;
import shared.SimulatorEvents.SerializedComponents.EventType;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class AnimationLostInCable extends AbstractAnimation {

    public AnimationLostInCable(AnimationPanelInnerInterface animationPanelInnerInterface,
            DataLayerFacade dataLayer, PacketType packetType,
            Point defaultZoomSource, Point defaultZoomDest,
            int durationInMilliseconds, EventType eventType) {

        super(animationPanelInnerInterface, dataLayer, packetType,
                defaultZoomSource, defaultZoomDest,
                durationInMilliseconds, eventType);
    }

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
        // if packet is lost and reached half-way
        if (getFraction() > 0.5) {
            Composite tmpComposite = g2.getComposite();

            int rule = AlphaComposite.SRC_OVER;
            //float alpha = (float)animation.getFraction()*2;
            float alpha = (float) (-2 * getFraction() + 2);
            if (alpha > 1f) {
                alpha = 1f;
            }
            if (alpha < 0f) {
                alpha = 0f;
            }

            // set antialiasing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = (int) (getImage().getWidth(null) * 0.7);
            int height = (int) (getImage().getHeight(null) * 0.7);

            int x = (int) (getX() + getImage().getWidth(null) * 0.15);
            int y = (int) (getY() + getImage().getHeight(null) * 0.15);

            // create cross shape
            GeneralPath shape = new GeneralPath();
            shape.moveTo(x, y);
            shape.lineTo(x + width, y + height);
            shape.moveTo(x, y + height);
            shape.lineTo(x + width, y);

            // create stroke
            float strokeWidth = getImage().getWidth(null) / 5;
            BasicStroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

            Composite comp = AlphaComposite.getInstance(rule, alpha);
            // set transparency
            g2.setComposite(comp);
            // paint image
            g2.drawImage(getImage(), getX(), getY(), null);
            // set original transparency
            g2.setComposite(tmpComposite);

            // save old stroke and color
            Stroke tmpStroke = g2.getStroke();
            Color tmpColor = g2.getColor();

            // set stroke and color
            g2.setStroke(stroke);
            g2.setColor(Color.RED);

            // paint red cross
            g2.draw(shape);

            // restore old stroke and color
            g2.setColor(tmpColor);
            g2.setStroke(tmpStroke);
        } else {
            g2.drawImage(getImage(), getX(), getY(), null);
        }
    }
}
