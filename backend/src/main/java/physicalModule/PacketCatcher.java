/*
 * Erstellt am 20.3.2012.
 */

package physicalModule;

import dataStructures.packets.IpPacket;
import dataStructures.packets.L4Packet;
import dataStructures.packets.L3Packet;
import dataStructures.packets.IcmpPacket;
import dataStructures.packets.EthernetPacket;
import dataStructures.packets.ArpPacket;
import dataStructures.*;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.*;
import java.util.Arrays;
import java.util.logging.Level;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.JProtocol;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Arp;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.network.Icmp.Echo;
import org.jnetpcap.protocol.network.Ip4;

/**
 * Class for capturing packets from real network.
 *
 * Trida na chytani paketu z realny site. Bezi v uplne vlastim vlakne!
 * @author Tomas Pitrinec
 */
public class PacketCatcher implements Runnable, Loggable {

	Pcap pcap;
	RealSwitchport swport;
	Thread myThread;


	public PacketCatcher(Pcap pcap, RealSwitchport swport) {
		this.pcap = pcap;
		this.swport = swport;

		// startovani, vse uz musi byt nacteno!
		myThread = new Thread(this, getDescription());
		log(Logger.DEBUG, "Jdu startovat.", null);
		myThread.start();
	}

	/**
	 * Metoda hlavni smycky, chyta pakety a nechava je zpracovat.
	 */
	@Override
	public void run() {

		// vytvorim jednoduchej packet handler, kterej vola moji fci:
		PcapPacketHandler packetHandler = new PcapPacketHandler() {
			@Override
			public void nextPacket(PcapPacket packet, Object user) {
				translatePacket(packet);
			}
		};

		// spustim nekonecnou smycku:
		pcap.loop(-1, packetHandler, null);

		log(Logger.DEBUG,"Vlakno catcheru konci.",null); // sem se to muze dostat jedine po zavolani pcap.close()
	}

	private void translatePacket(JPacket packet){
		L2Packet l2p = processLinkLayer(packet);
		if(l2p!=null){
			//poslu paket:
			swport.receivePacket(l2p);
		}
	}


	/**
	 * Translates ethernet II packets, than calls the translation of network layer and gives the L2Packet to the swport.
	 * @param packet
	 */
	private L2Packet processLinkLayer(JPacket packet){

		if (packet.hasHeader(JProtocol.ETHERNET_ID)) { // kdyz to ma ethernetovou hlavicku
			// nactu si hlavicku
			Ethernet ethHeader;
			ethHeader = new Ethernet();
			packet.getHeader(ethHeader);

			// nactu si mac adresy:
			MacAddress src=new MacAddress(ethHeader.source());
			MacAddress dst=new MacAddress(ethHeader.destination());

			// necham zpracovat paket vyssi vrstvy:
			L3Packet data = processNetLayer(packet);

			//vytvorim paket:
			if(data!=null){
				return new EthernetPacket (src,dst,data.getType(), data);	// typ se dava podle skutecnosti, ne podle toho, co bylo zadano v puvodnim paketu
			}

		} else {	// je to nejakej neznamej typ, loguju to jako info
			log(Logger.INFO, "Packet with unknown header on link layer catched and dropped.", null);
		}
		return null;
	}

	/**
	 * Translates the packets on net layer, for now translates the ARP and IPv4 packets.
	 * @param packet
	 * @return
	 */
	private L3Packet processNetLayer(JPacket packet){

		if (packet.hasHeader(JProtocol.ARP_ID)) {
			return processArp(packet);
		} else if (packet.hasHeader(JProtocol.IP4_ID)) {
			return processIp(packet);
		} else {
			// Here you can add translations of other headers on network layer.

			log(Logger.INFO, "Chytil jsem paket s neznamou hlavickou na sitovy vrstve.", null);
			return null;
		}
	}


	/**
	 * Translates ARP packet.
	 * @param packet
	 * @return null if packet doesn't contain ARP header
	 */
	private ArpPacket processArp(JPacket packet){

		if (packet.hasHeader(JProtocol.ARP_ID)) { // for sure

			// nactu hlavicku:
			Arp arpHeader = new Arp();
			packet.getHeader(arpHeader);

			// zjistim parametry:
			Arp.OpCode opCode = arpHeader.operationEnum();	// typ (reply, request)

			IpAddress senderIP = new IpAddress(arpHeader.spa());	// vykoukal jsem to z nejakejch anotaci v javadocu
			MacAddress senderMac = new MacAddress(arpHeader.sha());
			IpAddress targetIP = new IpAddress(arpHeader.tpa());
			MacAddress targetMac = new MacAddress(arpHeader.tha());

			//vytvorim paket:
			if(opCode==Arp.OpCode.REPLY){
				return new ArpPacket(senderIP, senderMac, targetIP, targetMac);
			} else {
				return new ArpPacket(senderIP,senderMac,targetIP);
			}

		} else {
			return null;
		}
	}

	/**
	 * Finds IP header in JPacket and returns complete IpPacket (with all data).
	 * @param packet
	 * @return null if packet doesn't contain IP header
	 */
	private IpPacket processIp(JPacket packet){

		if (! packet.hasHeader(JProtocol.IP4_ID)) { // for sure
			return null;
		} else {
			// nactu hlavicku:
			Ip4 ipHeader = new Ip4();
			packet.getHeader(ipHeader);

			// nactu dulezity data
			IpAddress src = IpAddress.createIpFromBits(ipHeader.sourceToInt());
			IpAddress dst = IpAddress.createIpFromBits(ipHeader.destinationToInt());
			int ttl = ipHeader.ttl();

			//necham zpracovat protokol vyssi vrstvy:
			L4Packet  data = processTransportLayer(packet);

			if(data==null){ //unknown type of transport layer header
				log(Logger.INFO, "Packet with unknown transport layer header catched and dropped.", null);
				return null;
			} else {
				// nakonec ten paket vytvorim
				return new IpPacket(src, dst, ttl, data);
			}
		}
	}


	/**
	 * Translates the packets on transport layer, for now translates only ICMP echo/reply packets.
	 * @param packet
	 * @return
	 */
	private L4Packet processTransportLayer(JPacket packet) {
		if(packet.hasHeader(JProtocol.ICMP_ID)){
			return processIcmp(packet);
		} else {
			// Here you can add translations of other headers on transport layer.

		}

		return null;
	}

	private IcmpPacket processIcmp(JPacket packet){

		IcmpPacket p = null;

		if(packet.hasHeader(JProtocol.ICMP_ID)){	// for sure
			// ziskam hlavicku
			Icmp icmp = new Icmp();
			packet.getHeader(icmp);

			// ziskam typ a kod:
			int type = icmp.type();
			int code = icmp.code();

			if (icmp.hasSubHeader(Icmp.IcmpType.ECHO_REPLY_ID) || icmp.hasSubHeader(Icmp.IcmpType.ECHO_REQUEST_ID)) {	// je to request nebo reply
				Echo icmpEcho;
				if (icmp.hasSubHeader(Icmp.IcmpType.ECHO_REPLY_ID))	// i kdyz je to jedno, tak to musim takhle rozlisovat
					icmpEcho = new Icmp.EchoReply();
				else
					icmpEcho = new Icmp.EchoRequest();
				icmp.getSubHeader(icmpEcho);	// nactu si podhlavicku
				int identifier = icmpEcho.id();
				int sequence = icmpEcho.sequence();
				byte [] data = icmp.getPayload();
				try {
					p = new IcmpPacket(type, code, identifier, sequence, data);
				} catch (Exception ex) {
					log(Logger.WARNING,ex.getMessage(), ex);
				}
			} else { // neni to request ani reply
				// Here you can add the translation of other ICMP packets

				// tady potrebuju vypsat ty data:
				JPacket innerPacket = new JMemoryPacket(JProtocol.IP4_ID, icmp.getPayload());

				IcmpPacket innerIcmp = processIcmp(innerPacket);	// translated inner packet
				if (innerIcmp != null) {
					try {
						// inner packet is ICMP
						p = new IcmpPacket(type, code, innerIcmp.id, innerIcmp.seq, innerIcmp.payload);
					} catch (Exception ex) {
						log(Logger.WARNING,ex.getMessage(), ex);
					}
				}

			}
		}

		return p;
	}



	@Override
	public String getDescription() {
		return swport.getDescription()+": cather";
	}

	private void log(int logLevel, String msg, Object obj){
		Logger.log(this, logLevel, LoggingCategory.REAL_NETWORK, msg, obj);
	}



}
