/*
 * created 13.3.2012
 */

package networkModule.L3;

import dataStructures.packets.ArpPacket;
import dataStructures.DropItem;
import dataStructures.packets.IpPacket;
import dataStructures.packets.L4Packet;
import dataStructures.PacketItem;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.Objects;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;
import networkModule.L3.RoutingTable.Record;
import networkModule.IpNetworkModule;

/**
 * Cisco-specific IPLayer.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoIPLayer extends IPLayer {

	public final CiscoWrapperRT wrapper;

	public CiscoIPLayer(IpNetworkModule netMod) {
		super(netMod);
		this.ttl = 255;
		wrapper = new CiscoWrapperRT(netMod.getDevice(), this);
	}

	@Override
	protected void handleReceiveArpPacket(ArpPacket packet, EthernetInterface iface) {
		// tady se bude resit update cache + reakce
		switch (packet.operation) {
			case ARP_REQUEST:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "ARP request received.", packet);

				// ulozit si odesilatele
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);

				// posli ARP REPLY ((jsem to ja && mam routu na tazatele, tak odpovez!) || (nejsem to ja && mam routu na tazatele && vim kam mam ten paket dal poslat))
				if ((isItMyIpAddress(packet.targetIpAddress) && haveRouteFor(packet.senderIpAddress))
						|| (!isItMyIpAddress(packet.targetIpAddress) && haveRouteFor(packet.senderIpAddress) && haveRouteFor(packet.targetIpAddress))) {

					// poslat ARP reply
					ArpPacket arpPacket = new ArpPacket(packet.targetIpAddress, iface.getMac(), packet.senderIpAddress, packet.senderMacAddress);
					Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Reacting on ARP request: sending REPLY to " + packet.senderIpAddress, arpPacket);
					netMod.ethernetLayer.sendPacket(arpPacket, iface, packet.senderMacAddress);
				} else {
					Logger.log(this, Logger.DEBUG, LoggingCategory.ARP, "ARP request received, but I am not a target - doing nothing.", packet);
				}
				break;

			case ARP_REPLY:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "ARP reply received.", packet);
				// ulozit si target
				// kdyz uz to prislo sem, tak je jasne, ze ta odpoved byla pro me (protoze odpoved se posila jen odesilateli a ne na broadcast), takze si ji muzu ulozit a je to ok
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);
				shouldHandleStoreBuffer = true;
				worker.wake();
				break;

			default:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Dropping packet: Unknown ARP type packet received.", packet);
				Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
		}
	}

	/**
	 * Na ciscu, kdyz se odesila novy packet (ze shora), tak se nejdrive kontroluje RT, pokud neni zadny zaznam,
	 * tak se clovek ani nedopingne na sve iface s IP.
	 *
	 * @param packet
	 * @param src the value of src
	 * @param dst
	 */
	@Override
	protected void handleSendPacket(L4Packet packet, IpAddress src, IpAddress dst, int ttl) {

		Record record = routingTable.findRoute(dst);
		if (record == null) { // kdyz nemam zaznam na v RT, tak zahodim
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Dropping packet: unroutable.", packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
			return;
		}

		IpPacket p = new IpPacket(src == null ? record.iface.getIpAddress().getIp() : src, dst, ttl, packet);

		if (isItMyIpAddress(dst)) {
			EthernetInterface iface = findInterfaceForIpAddress(dst);
			handleReceivePacket(p, iface); // rovnou ubsluz v mem vlakne
			return;
		}

		processPacket(p, record, null);
	}

	/**
	 * Handles packet: is it for me?, routing, decrementing TTL, postrouting, MAC address finding.
	 *
	 * @param packet
	 * @param iface incomming EthernetInterface, can be null
	 */
	@Override
	protected void handleReceiveIpPacket(IpPacket packet, EthernetInterface iface) {

		NetworkInterface ifaceIn = findIncommingNetworkIface(iface);

		// kdyz je to vuci prichozimu rozhrani broadcast, tak to poslu nahoru (je to pro me) - kvuli DHCP!
		if (ifaceIn != null && ifaceIn.getIpAddress() != null && ifaceIn.getIpAddress().getBroadcast().equals(packet.dst)) {
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Received IP packet which was sent as broadcast for this interface.", packet);
			netMod.transportLayer.receivePacket(new PacketItem(packet,ifaceIn));
			return;
		}

		// odnatovat
		packet = packetFilter.preRouting(packet, ifaceIn);
		if (packet == null) { // packet dropped, ..
			return;
		}

		// je pro me?
		if (isItMyIpAddress(packet.dst)) { // TODO: cisco asi pravdepovodne se nejdriv podiva do RT, a asi tam bude muset byt zaznam na svoji IP, aby se to dostalo nahoru..
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Received IP packet destined to be mine.", packet);
			netMod.transportLayer.receivePacket(new PacketItem(packet,ifaceIn));
			return;
		}

		// osetri TTL
		if (packet.ttl == 1) {
			// posli TTL expired a zaloguj zahozeni paketu
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Dropping packet: TTL expired.", packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
			getIcmpHandler().sendTimeToLiveExceeded(packet.src, packet);
			return;
		}

		// zaroutuj
		Record record = routingTable.findRoute(packet.dst);
		if (record == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Dropping packet: IP packet received, but packet is unroutable - no record for "+packet.dst+". Will send Destination Host Unreachable.", packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
			getIcmpHandler().sendHostUnreachable(packet.src, packet); // cisco na skolnich routerech odesi DHU a nebo DNU jako linux
			return;
		}

		// vytvor novy paket a zmensi TTL (kdyz je packet.src null, tak to znamena, ze je odeslan z toho sitoveho device
		//		a tedy IP adresa se musi vyplnit dle iface, ze ktereho to poleze ven
		IpPacket p = new IpPacket(packet.src, packet.dst, packet.ttl - 1, packet.data);

		Logger.log(this, Logger.INFO, LoggingCategory.NET, "IP packet received from interface: "+(ifaceIn == null ? "null" : ifaceIn.name), packet);
		processPacket(p, record, ifaceIn);
	}

	@Override
	public void changeIpAddressOnInterface(NetworkInterface iface, IPwithNetmask ipAddress) {
		super.changeIpAddressOnInterface(iface, ipAddress);
		wrapper.update();
	}

	/**
	 * Returs true iff routing table has a valid record for given IP.
	 * @param ip
	 * @return
	 */
	private boolean haveRouteFor(IpAddress ip) {
		Record record = routingTable.findRoute(ip);
		if (record == null) {
			return false;
		}
		return true;
	}

	/**
	 * Just for Loader's: private Map<CiscoIPLayer, RoutingTableConfig> ciscoSettings = new HashMap<>().
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CiscoIPLayer other = (CiscoIPLayer) obj;
		if (!Objects.equals(this.getNetMod().getDevice().getName(), other.getNetMod().getDevice().getName())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 52465;
		hash = 71 * hash + Objects.hashCode(getNetMod().getDevice().getName());
		return hash;
	}
}
