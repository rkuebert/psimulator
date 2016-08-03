/*
 * created 1.2.2012
 */

package dataStructures.packets;

import dataStructures.packets.L4Packet;
import dataStructures.packets.L3Packet;
import dataStructures.ipAddresses.IpAddress;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import utils.Util;

/**
 * Represents IPv4 packet.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpPacket extends L3Packet {

	public final IpAddress src;
	public final IpAddress dst;
	public final int ttl;

	public IpPacket(IpAddress src, IpAddress dst, int ttl, L4Packet data) {
		super(data);
		this.src = src;
		this.dst = dst;
		this.ttl = ttl;
		countSize();
	}

	@Override
	public L3PacketType getType() {
		return L3PacketType.IPv4;
	}

	@Override
	public String toString() {
		return "IpPacket: src: " + src + " dst: " + dst + " " + "ttl: " + ttl + " " + Util.zarovnej(getType().toString(), 4) + (data == null ? "" : " | " + data.toString());
	}

	@Override
	protected final void countSize() { // IP header has 20 or 24 bytes (http://en.wikipedia.org/wiki/IPv4)
		this.size = 20 + getDataSize();
	}

	@Override
	public String getEventDesc() {
		String s = "=== IP === \n";
		s += "src: "+src+"  ";
		s += "dst: "+dst+"  ";
		s += "ttl: "+ttl+"  ";
		s += "size: "+size+"  ";
		if (data != null) {
			s += "\n" + data.getEventDesc();
		}
		return s;
	}

	@Override
	public PacketType getPacketEventType() {
		if (data != null) {
			return data.getPacketEventType();
		}
		return PacketType.IP;
	}
}
