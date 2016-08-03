/*
 * Erstellt am 4.4.2012.
 */

package dataStructures.packets;

import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * Implementace dhcp paketu, jsou tu jen veci, ktery jsou v simulatrou opravdu potreba.
 * @author Tomas Pitrinec
 */
public class DhcpPacket implements PacketData {

	public final DhcpType type;
	public final int transaction_id;
	public final IpAddress serverIdentifier;

	public final IPwithNetmask ipToAssign;
	public final IpAddress broadcast;
	public final IpAddress router;
	public final MacAddress clientMac;

	public DhcpPacket(DhcpType type, int transaction_id, IpAddress serverIdentifier, IPwithNetmask ipToAssign,
			IpAddress broadcast, IpAddress router, MacAddress clientMac) {
		this.type = type;
		this.transaction_id = transaction_id;
		this.serverIdentifier = serverIdentifier;
		this.ipToAssign = ipToAssign;
		this.broadcast = broadcast;
		this.clientMac = clientMac;
		this.router = router;
	}

	@Override
	public int getSize() {
		return 300; // vyzkoumano wiresharkem
	}

	@Override
	public String getEventDesc() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public PacketType getPacketEventType() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public enum DhcpType{
		// client:
		DISCOVER,
		REQUEST,

		// server:
		OFFER,
		ACK,
		NAK,

		// ostatni zatim nedelam
	}

}
