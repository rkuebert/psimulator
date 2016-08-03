package psimulator.userInterface.SimulatorEditor.AnimationPanel;

import psimulator.userInterface.SimulatorEditor.AnimationPanel.Animations.AbstractAnimation;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.PacketImageType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface AnimationPanelInnerInterface {
    /**
     * Removes animation from animation list.
     * @param animation 
     */
    public void removeAnimation(AbstractAnimation animation);
    
    /**
     * Gets package image type.
     * @return 
     */
    public PacketImageType getPacketImageType();
}
