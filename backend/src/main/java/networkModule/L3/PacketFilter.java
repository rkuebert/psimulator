/*
 * created 1.2.2012
 */

package networkModule.L3;

import dataStructures.packets.IpPacket;
import networkModule.L3.nat.NatTable;

/**
 * Represents packet filter, implements network address translation.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PacketFilter {

	private final NatTable natTable;

	public PacketFilter(IPLayer ipLayer) {
		this.natTable = new NatTable(ipLayer);
	}

	public NatTable getNatTable() {
		return natTable;
	}

	public IpPacket preRouting(IpPacket packet, NetworkInterface in) {

		packet = natTable.backwardTranslate(packet, in);


		return packet;
	}

	public IpPacket postRouting(IpPacket packet, NetworkInterface in, NetworkInterface out) {

		packet = natTable.translate(packet, in, out);

		return packet;
	}

	// cisco NAT
	// http://www.cisco.com/en/US/tech/tk648/tk361/technologies_tech_note09186a0080133ddd.shtml
}
