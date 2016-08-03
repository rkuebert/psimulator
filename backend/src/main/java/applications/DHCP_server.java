/*
 * Erstellt am 3.4.2012.
 */
package applications;

import dataStructures.packets.IpPacket;
import dataStructures.PacketItem;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.DhcpPacket;
import dataStructures.packets.UdpPacket;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetLayer;
import networkModule.L3.NetworkInterface;
import networkModule.SwitchNetworkModule;

/**
 * Implementace DHCP serveru. Zatim jen takova dost pofiderni implementace, vubec se neukladaj prirazeny adresy, na
 * kazdej discover se vyplaca nova adresa, na request se priradi ta vyzadana. Asi by to chtelo casem predelat.
 *
 * @author Tomas Pitrinec
 */
public class DHCP_server extends Application {

	private final static int ttl = 64;	// ttl, se kterym se budoui odesilat pakety
	public final static int server_port = 67;
	public final static int client_port = 68;

	/**
	 * Konfigurace.
	 */
	DhcpServerConfiguration config;
	EthernetLayer ethLayer;


	public DHCP_server(Device device) {
		super("dhcpd", device);	// jmeno jako u me na linuxu
		port = server_port;
		ethLayer = ((SwitchNetworkModule) device.getNetworkModule()).ethernetLayer;
	}

	@Override
	public void doMyWork() {
		if (!buffer.isEmpty()) {
			handlePacket(buffer.remove(0));
		}
	}

	private void handlePacket(PacketItem item) {
		// nactu zakladni konfiguraci:
		NetworkInterface iface = item.iface;
		IpPacket recIp = item.packet;

		// deklarace prijaktejch:
		UdpPacket recUdp;
		DhcpPacket recDhcp;

		// najednou nactu vsechny pakety, kdyby neco bylo null nebo neslo pretypovat, hodilo by to vyjimku - v tom pripade koncim
		try {
			recUdp = (UdpPacket) recIp.data;
			recDhcp = (DhcpPacket) recUdp.getData();
			recDhcp.getSize();	// abych si overil, ze to neni null
		} catch (Exception ex) {
			log(Logger.INFO, "DHCP serveru prisel spatnej paket.", recIp);
			return;
		}

		// deklarace odpovedi:
		boolean odeslat = true;
		DhcpPacket.DhcpType replyType=null;
		IPwithNetmask adrm = null;

		//resim jednotlivy pripady:
		if (recDhcp.type == DhcpPacket.DhcpType.DISCOVER) { // prisel discover, poslu offer
			replyType = DhcpPacket.DhcpType.OFFER;
			adrm  = config.getNextAddress();

		} else if (recDhcp.type == DhcpPacket.DhcpType.REQUEST) { // prisel request, poslu ack
			replyType = DhcpPacket.DhcpType.ACK;
			adrm = recDhcp.ipToAssign;
		} else { // nic jinyhos server zatim neumi
			odeslat = false;
			log(Logger.WARNING, "Prisel spatnej typ DHCP.", recIp);
		}

		// kdyz je vsechno v poradku, odeslu odpoved:
		if(odeslat){
			// nactu, co mu poslu:
			IpAddress serverAddress = iface.getIpAddress().getIp();
			// sestavim pakety:
			DhcpPacket replyDhcp = new DhcpPacket(replyType, recDhcp.transaction_id, serverAddress,
					adrm, adrm.getBroadcast(),config.routers, recDhcp.clientMac);
			UdpPacket replyUdp = new UdpPacket(server_port, recUdp.srcPort, replyDhcp);
			IpPacket replyIp = new IpPacket(serverAddress, adrm.getIp(), ttl, replyUdp);
			// nakonec to poslu pomoci ethernetovy vrstvy:
			ethLayer.sendPacket(replyIp, iface.ethernetInterface, recDhcp.clientMac);
		}


	}

	@Override
	protected void atStart() {
		// tady bude muset bejt nejaky nacitani konfigurace
	}

	@Override
	protected void atExit() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void atKill() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private void log(int logLevel, String msg, Object obj) {
		Logger.log(this, logLevel, LoggingCategory.DHCP, msg, obj);
	}

	@Override
	public String getDescription() {
		return device.getName() + "_DHCP_server";
	}

	public class DhcpServerConfiguration {

		IPwithNetmask subnetAndNetmask;
		/**
		 * First address, that can be assigned.
		 */
		IpAddress rangeStart;
		/**
		 * Last address, that can be assigned.
		 */
		IpAddress rangeEnd;
		IpAddress routers;
		IpAddress broadcast;
		IpAddress nextAddress;

		public DhcpServerConfiguration(IPwithNetmask subnetAndNetmask, IpAddress rangeStart,
				IpAddress rangeEnd, IpAddress routers, IpAddress broadcast) {
			this.subnetAndNetmask = subnetAndNetmask;
			this.rangeStart = rangeStart;
			this.rangeEnd = rangeEnd;
			this.routers = routers;
			this.broadcast = broadcast;
			nextAddress = rangeStart;
		}

		public IPwithNetmask getNextAddress() {
			IPwithNetmask vratit = new IPwithNetmask(nextAddress, subnetAndNetmask.getMask());
			if (nextAddress.equals(rangeEnd)) {	// TODO tady bych mel zjistit, jak to opravdu funguje
				log(Logger.INFO, "Vycerpan rozsah ip adres.", null);
				nextAddress = rangeStart;
			} else {
				nextAddress = IpAddress.nextAddress(nextAddress);
			}
			return vratit;
		}

	}
}
