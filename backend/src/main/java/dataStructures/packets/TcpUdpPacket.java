/*
 * created 17.3.2012
 */

package dataStructures.packets;

import dataStructures.packets.L4Packet;

/**
 * Represents TCP and UDP packet.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class TcpUdpPacket extends L4Packet {

	public final int srcPort;
	public final int dstPort;

	public TcpUdpPacket(int srcPort, int dstPort) {
		this.srcPort = srcPort;
		this.dstPort = dstPort;
	}

	@Override
	public int getPortSrc() {
		return srcPort;
	}

	@Override
	public int getPortDst() {
		return dstPort;
	}
}
