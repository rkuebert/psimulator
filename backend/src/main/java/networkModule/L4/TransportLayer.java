/*
 * created 1.2.2012
 */
package networkModule.L4;

import applications.Application;
import dataStructures.DropItem;
import dataStructures.PacketItem;
import java.util.HashMap;
import java.util.Map;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.IpNetworkModule;
import networkModule.L3.IPLayer;
import utils.Util;

/**
 * Implementace transportni vrstvy sitovyho modulu. <br />
 * Nebezi v vlastnim vlakne, je to vlastne jen rozhrani mezi aplikacema a 3. vrstvou.
 *
 * Pozor: Porty jsou sdileny napric protokoly (ICMP, TCP, UDP), proto nelze poslouchat na 80 pres TCP, UDP a ICMP najednout.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class TransportLayer implements Loggable {

	public final IpNetworkModule netMod;
	public final IcmpHandler icmpHandler;
	public final UdpHandler tcpudpHandler;
	/**
	 * List of registred ports of applications. <br />
	 * Key - port or session number <br />
	 * Value - listening application
	 *
	 */
	private final Map<Integer, Application> applications = new HashMap<>();
	private int portCounter = 1025;
	private static final int portMAX = 65_535;

	public TransportLayer(IpNetworkModule netMod) {
		this.netMod = netMod;
		this.icmpHandler = new IcmpHandler(this);
		this.tcpudpHandler = new UdpHandler(this);
	}

	public IPLayer getIpLayer() {
		return netMod.ipLayer;
	}

	/**
	 * This method should be called from L3.
	 * @param packetItem
	 */
	public void receivePacket(PacketItem packetItem) {
		if (packetItem.packet.data == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.TRANSPORT, "Dropping packet: Received packet with no L4 data.", packetItem.packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packetItem.packet, getIpLayer().getNetMod().getDevice().configID));
			return;
		}

		switch (packetItem.packet.data.getType()) {
			case ICMP:
				Logger.log(this, Logger.INFO, LoggingCategory.TRANSPORT, "Incomming ICMP paket", packetItem);
				icmpHandler.handleReceivedIcmpPacket(packetItem);
				break;

			case TCP:
				Logger.log(this, Logger.IMPORTANT, LoggingCategory.TRANSPORT, "Dropping packet: TCP handler is not yet implemented.", packetItem.packet);
				Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packetItem.packet, getIpLayer().getNetMod().getDevice().configID));
				break;

			case UDP:
				Logger.log(this, Logger.INFO, LoggingCategory.TRANSPORT, "Incomming UDP paket", packetItem);
				tcpudpHandler.handleReceivedUdpPacket(packetItem);
				break;
			default:
				Logger.log(this, Logger.WARNING, LoggingCategory.TRANSPORT, "Dropping packet: Received packet with unknown L4 type.", packetItem.packet);
				Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packetItem.packet, getIpLayer().getNetMod().getDevice().configID));
		}
	}

	/**
	 * Forward incomming packet to application listening on given port.
	 *
	 * @param packetItem
	 * @param port
	 */
	protected void forwardPacketToApplication(PacketItem packetItem, int port) {
		Application app = applications.get(port);
		if (app != null) {
			app.receivePacket(packetItem);
		} else {
			Logger.log(this, Logger.INFO, LoggingCategory.TRANSPORT, "Dropping packet: There is now app listening on this port: "+port+ ", sending port unreachable to: "+packetItem.packet.src, packetItem.packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packetItem.packet, getIpLayer().getNetMod().getDevice().configID));
//			icmphandler.sendPortUnreachable(packet.src, packet); // zatim posilat nebudeme, protoze by se nam toho tam posilalo moc
		}
	}

	/**
	 * Register application to listen and accept packets.
	 *
	 * @param app to register
	 * @param port listen on this port - if null given it will assign free port
	 * @return assigned port or -1 iff given port is already in use (than app is for obvious reason not registered)
	 */
	public int registerApplication(Application app, Integer port) {
		if (port == null) {
			port = getFreePort();
		} else {
			Application a = applications.get(port);
			if (a != null) {
				Logger.log(this, Logger.IMPORTANT, LoggingCategory.TRANSPORT, "Given port is already is use: " + port + " by " + a.name, null);
				return -1;
			}
		}

		applications.put(port, app);
		return port;
	}

	/**
	 * Unregister application from transport layer. <br />
	 * Application is specified by port number.
	 *
	 * @param port
	 */
	public void unregisterApplication(int port) {
		applications.remove(port);
	}

	@Override
	public String getDescription() {
		return Util.zarovnej(netMod.getDevice().getName(), Util.deviceNameAlign) + "TcpIpLayer";
	}

	/**
	 * Returns unused port number.
	 *
	 * @return
	 */
	private Integer getFreePort() {
		if (portCounter > portMAX) {
			portCounter = portCounter - portMAX + 1024;
			Logger.log(this, Logger.INFO, LoggingCategory.TRANSPORT, "Resetting portCounter.", null);
		}
		if (applications.containsKey(portCounter)) {
			portCounter++;
			return getFreePort();
		} else {
			return portCounter++;
		}
	}
}
