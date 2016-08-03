/*
 * Erstellt am 9.5.2012.
 * Tuhle třídu jsem jenom odnekud presunul.
 */
package dataStructures;

import dataStructures.packets.L2Packet;

/**
 *
 * @author Stanislav Rehak
 */
public class CableItem {

	public final L2Packet packet;
	public final int sourceID;
	public final int destinationID;
	public final int cableID;

	public CableItem(L2Packet packet, int source_ID, int destination_ID, int cabel_ID) {
		this.packet = packet;
		this.sourceID = source_ID;
		this.destinationID = destination_ID;
		this.cableID = cabel_ID;
	}

	@Override
	public String toString() {
		return String.format("sourceID=%d, destinationID=%d, cableID=%d, packet=", sourceID, destinationID, cableID) + packet.toStringWithData();
	}
}
