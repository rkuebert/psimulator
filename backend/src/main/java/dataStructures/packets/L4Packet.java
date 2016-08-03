/*
 * Erstellt am 27.10.2011.
 */

package dataStructures.packets;

import dataStructures.EventDescriptive;

/**
 *
 * @author neiss
 */
public abstract class L4Packet implements EventDescriptive {



	public abstract int getSize();

	public enum L4PacketType{
		ICMP,
		TCP,
		UDP,
	}

	public abstract L4PacketType getType();

	/*
	 * Veci pro NAT - je to pripraveno i pro TCP/UDP pakety.
	 */
	public abstract int getPortSrc();
	public abstract int getPortDst();
	public abstract L4Packet getCopyWithDifferentSrcPort(int port);
	public abstract L4Packet getCopyWithDifferentDstPort(int port);
}
