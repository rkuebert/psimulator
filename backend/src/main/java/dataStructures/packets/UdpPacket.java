/*
 * Erstellt am 31.3.2012.
 */

package dataStructures.packets;

import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * Representation of UDP packet.
 * @author Tomas Pitrinec
 */
public class UdpPacket extends TcpUdpPacket {

	private static int headerLength = 8; // dylka hlavicky v bajtech; zdroj: http://cs.wikipedia.org/wiki/User_Datagram_Protocol

	private PacketData data;

	private final int size;	// size of whole packet - header plus data, compute in constructor

	public UdpPacket(int srcPort, int dstPort, PacketData data) {
		super(srcPort, dstPort);
		this.data = data;
		size = headerLength + (data != null ? data.getSize() : 0);
	}

	public PacketData getData() {
		return data;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public L4PacketType getType() {
		return L4PacketType.UDP;
	}

	@Override
	public L4Packet getCopyWithDifferentSrcPort(int port) {
		return new UdpPacket(port, dstPort, data);
	}

	@Override
	public L4Packet getCopyWithDifferentDstPort(int port) {
		return new UdpPacket(srcPort, port, data);
	}


// z rozhrani EventDescriptive:

	@Override
	public String getEventDesc() {
		String s = "=== UDP === \n";
		s += "srcport: " + srcPort + " ";
		s += "dstport: " + dstPort + " ";
		s += "size: " + size;
		if (data != null) {
			s += "\n" + data.getEventDesc();
		}
		return s;
	}

	@Override
	public PacketType getPacketEventType() {
		return PacketType.UDP;
	}

}
