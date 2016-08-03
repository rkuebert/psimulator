/*
 * created 1.5.2012
 */

package networkModule.L4;

import dataStructures.PacketItem;
import dataStructures.packets.TcpUdpPacket;
import logging.Loggable;
import utils.Util;

/**
 * Forwards incomming UDP packets to application.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class UdpHandler implements Loggable {

	private final TransportLayer transportLayer;

	public UdpHandler(TransportLayer transportLayer) {
		this.transportLayer = transportLayer;
	}

	public void handleReceivedUdpPacket(PacketItem m) {
		TcpUdpPacket p = (TcpUdpPacket) m.packet.data;

		transportLayer.forwardPacketToApplication(m, p.dstPort);
	}

	@Override
	public String getDescription() {
		return Util.zarovnej(transportLayer.netMod.getDevice().getName(), Util.deviceNameAlign)+" UdpHandler";
	}
}
