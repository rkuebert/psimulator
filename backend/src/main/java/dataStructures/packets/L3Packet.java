/*
 * Erstellt am 27.10.2011.
 */

package dataStructures.packets;

import dataStructures.EventDescriptive;

/**
 *
 * @author neiss
 */
public abstract class L3Packet implements EventDescriptive {

    public final L4Packet data;
	protected int size;

	public L3Packet(L4Packet data) {
		this.data = data;
	}

	public enum L3PacketType{
		IPv4,
		ARP,
		UNKNOWN,	// nejakej neznamej typ
	}

	protected int getDataSize() {
		return (data != null ? data.getSize() : 0);
	}

	/**
	 * Call in constructor in your classes.
	 * @return
	 */
	protected abstract void countSize();

	public int getSize() {
		return size;
	}

	public abstract L3PacketType getType();

	@Override
	public String toString(){
		return "L3Packet: generic packet on Layer 3.";
	}
}
