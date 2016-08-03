/*
 * Erstellt am 26.10.2011.
 */
package dataStructures.packets;

import dataStructures.EventDescriptive;

/**
 *
 * @author neiss
 */
public abstract class L2Packet implements EventDescriptive {

    public final L3Packet data;

	// asi nebude potreba, nevim
	public L2Packet() {
		this.data = null;
	}

	public L2Packet(L3Packet data) {
		this.data = data;
	}

	public enum L2PacketType{
		ethernetII;
	}

    /**
	 * Returns size of this packet, count the size of data, too.
	 * Cachovat, jinak bude pekne narocnej
	 */
    public abstract int getSize();

	public abstract L2PacketType getType();

	@Override
	public String toString(){
		return "L2Packet: generic packet on Layer 2.";
	}

	public String toStringWithData() {
		return toString() + " " + (data == null ? "" : data.toString());
	}
}
