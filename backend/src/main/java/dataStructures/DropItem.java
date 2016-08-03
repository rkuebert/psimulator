/*
 * created 29.3.2012
 */

package dataStructures;

import dataStructures.packets.L4Packet;
import dataStructures.packets.L3Packet;
import dataStructures.packets.L2Packet;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * Represents event: dropping packet
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class DropItem {

	public final L2Packet l2packet;
	public final L3Packet l3packet;
	public final L4Packet l4packet;
	public final int deviceID;

	public DropItem(L2Packet l2packet, int deviceID) {
		this.l2packet = l2packet;
		this.deviceID = deviceID;
		this.l3packet = null;
		this.l4packet = null;
	}

	public DropItem(L3Packet l3packet, int deviceID) {
		this.l3packet = l3packet;
		this.deviceID = deviceID;
		this.l2packet = null;
		this.l4packet = null;
	}

	public DropItem(L4Packet l4packet, int deviceID) {
		this.l4packet = l4packet;
		this.deviceID = deviceID;
		this.l2packet = null;
		this.l3packet = null;
	}

	@Override
	public String toString() {
		String s = "";
		if (l3packet != null) {
			return l3packet.getEventDesc();
		}
		if (l2packet != null) {
			return l2packet.getEventDesc();
		}
		if (l4packet != null) {
			return l4packet.getEventDesc();
		}
		return "* TODO *";
	}

	public PacketType getPacketType() {
		if (l3packet != null) {
			return l3packet.getPacketEventType();
		}
		if (l2packet != null) {
			return l2packet.getPacketEventType();
		}
		if (l4packet != null) {
			return l4packet.getPacketEventType();
		}
		return null; // nemelo by nikdy nastat!
	}
}
