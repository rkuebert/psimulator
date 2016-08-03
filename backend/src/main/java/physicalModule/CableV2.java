/*
 * created 2.5.2012
 */

package physicalModule;

import dataStructures.CableItem;
import dataStructures.packets.L2Packet;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Represents simple cable which only forwards packets to the other side.
 *
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CableV2 extends AbstractCable {

	/**
	 * Network speed in mb/s.
	 */
	public int networkSpeed = 100; // 100 mb/s
	/**
	 * Speed of sending packets via network.
	 * 8 / (100 * 1024^2 ) = 0.000 000 076 [s] = 76 [ns]
	 */
	public int sendingSpeed;

	public CableV2(int configID) {
		super(configID);
		sendingSpeed = (int) (1_000_000_000 * (double) 8 / (double) (networkSpeed * 1024 * 1024));
	}

	/**
	 * Transmits packet to the other end of cable.
	 * @param packet data to send
	 * @param dest destination
	 */
	public void transmit(L2Packet packet, AbstractSimulatorSwitchport src, AbstractSimulatorSwitchport dest) {
		Logger.log(this, Logger.INFO, LoggingCategory.CABEL_SENDING, "Sending packet through cabel..", new CableItem(packet, src.deviceID, dest.deviceID, configID));
		dest.receivePacket(packet);
	}

	/*
	 * Sets first connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public void setFirstSwitchport(SimulatorSwitchportV2 swport) {
		swport.cable = this;
		this.firstCon = swport;
	}

	/*
	 * Sets second connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public void setSecondSwitchport(SimulatorSwitchportV2 swport) {
		swport.cable = this;
		this.secondCon = swport;
	}

	@Override
	public String getDescription() {
		return "CableV2: 1_ID=" + getFirstIdDevice() + " " + "2_ID=" + getSecondIdDevice();
	}
}
