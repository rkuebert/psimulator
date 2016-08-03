/*
 * Erstellt am 5.4.2012.
 */

package dataStructures;

import dataStructures.packets.IpPacket;
import networkModule.L3.NetworkInterface;

/**
 * Item for supplying packets with additional informations in network module and applications.
 * Slouzi k predavani paketu s dodatecnejma informacema v ramci sitovyho modulu a aplikaci.
 * @author Tomas Pitrinec
 */
public class PacketItem {

	public final IpPacket packet;
	public final NetworkInterface iface;

	public PacketItem(IpPacket packet, NetworkInterface iface) {
		this.packet = packet;
		this.iface = iface;
	}


}
