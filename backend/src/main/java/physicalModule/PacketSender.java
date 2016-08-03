/*
 * Erstellt am 20.3.2012.
 */

package physicalModule;

import dataStructures.packets.IpPacket;
import dataStructures.packets.L4Packet;
import dataStructures.packets.L2Packet;
import dataStructures.packets.IcmpPacket;
import dataStructures.packets.EthernetPacket;
import dataStructures.*;
import dataStructures.packets.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import utils.SmartRunnable;
import utils.WorkerThread;
import static dataStructures.packets.L3Packet.L3PacketType.*;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.protocol.JProtocol;
import static dataStructures.packets.ArpPacket.ArpOperation.*;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;

/**
 * Class for sending packets to real network.
 * @author Tomas Pitrinec
 */
public class PacketSender implements SmartRunnable, Loggable {

	Pcap pcap;
	RealSwitchport swport;
	WorkerThread worker;
	private final List<L2Packet> buffer = Collections.synchronizedList(new LinkedList<L2Packet>());


// konstruktory: ----------------------------------------------------------------------------------------------------

	public PacketSender(Pcap pcap, RealSwitchport swport) {
		this.pcap = pcap;
		this.swport = swport;
		log(Logger.DEBUG, "Jdu startovat", null);
		worker = new WorkerThread(this);
	}


// do my work - hlavni prace senderu: -------------------------------------------------------------------------------

	/**
	 * The main work of PacketSender. Should be called only from worker.
	 */
	@Override
	public void doMyWork() {
		while (!buffer.isEmpty()){
			sendOnRealIface(buffer.remove(0));
		}
	}


// verejny metody pro prijimani paketu do bufferu od jinejch vlaken: -------------------------------------------------

	/**
	 * The metohod to send packet. Only adds the packet do buffer and wakes my thread.
	 * @param packet
	 */
	public void sendPacket(L2Packet packet){
		buffer.add(packet);
		worker.wake();
	}

	public void stop(){
		pcap = null;
		worker.die();
	}



// privatni metody pro sitovou komunikaci: -----------------------------------------------------

	/**
	 * Send the simulator packet to real interface. Uziva nasledujici metody.
	 * @param packet
	 */
	private void sendOnRealIface(L2Packet packet){

		JPacket jPacket = createPacket(packet);

		if (packet == null) {
			log(Logger.WARNING, "Can't send packet, because packet cannot be translated.", packet);
			return;
		}

		if (pcap.sendPacket(jPacket) != Pcap.OK) {
			log(Logger.WARNING, "Error while sending packet.", packet);
		} else {
			log(Logger.DEBUG, "Packet sent.", packet);
		}

	}

	/**
	 * Create packet. Translates ethernet packets and calls translations of packets on other layers.
	 * @param paket
	 * @return
	 */
	private JPacket createPacket(L2Packet paket) {
		JPacket jPacket = null;

		if (paket.getType() == L2Packet.L2PacketType.ethernetII) {
			// prelozim ethernet paket:
			EthernetPacket p = (EthernetPacket) paket;
			byte[] ethHeader = new byte[14]; // vytvarim si misto
			copy(p.dst.getByteArray(), ethHeader, 0);
			copy(p.src.getByteArray(), ethHeader, 6);
			copy(translateEthertype(p.getEthertype()), ethHeader, 12);

			// prelozim vyssi vrstvu a vytvorim paket:
			byte[] data = translateNetLayer(p.data);
			jPacket = new JMemoryPacket(JProtocol.ETHERNET_ID,concat(ethHeader, data));

		} else {
			log(Logger.INFO, "Can't translate packet on link layer, " + paket.getType() + " packet can't be translated.", paket);
			// Here you can add translations of other headers on link layer.
		}

		if(jPacket!=null){
			calculateCecksums(jPacket);
		}


		return jPacket;
	}

	/**
	 * Translates headers on net layer. For now translates only ARP and IPv4 headers.
	 * @param packet
	 * @return
	 */
	private byte[] translateNetLayer(L3Packet packet) {
		byte[]v=null;

		if(packet.getType()==ARP){

			ArpPacket p = (ArpPacket) packet;
			byte[]begin = new byte[]{0,1,8,0,6,4}; // 2 bajty hardware type (ethernet), 2 bajty protocol type (IP), velikost mac adresy, velikost IP adresy
			byte[]opcode;
			if(p.operation==ARP_REQUEST){
				opcode = new byte[]{0,1};
			} else{ //je to reply
				opcode = new byte[]{0,2};
			}

			v=new byte[begin.length+opcode.length+6+4+6+4]; // zacatek, opcode, sender mac, sender ip, target mac, target ip

			copy(begin,v,0);
			copy(opcode,v,begin.length);
			int dylkaHlavicky=begin.length+opcode.length;
			copy(p.senderMacAddress.getByteArray(),v,dylkaHlavicky);
			copy(p.senderIpAddress.getByteArray(),v,dylkaHlavicky+6);
			copy(p.targetMacAddress.getByteArray(),v,dylkaHlavicky+10);
			copy(p.targetIpAddress.getByteArray(),v,dylkaHlavicky+16);


		} else if(packet.getType()==IPv4){

			IpPacket p = (IpPacket) packet;

			// zacatek:
			byte [] begin = new byte[] {0x45,0}; // prvni bajt: verze (4) a dylka hlavicky(20), druhej TOS, vsude to bylo 0

			// vyplnovani dylky - je to tam zapsano v klasickym poradi; a identifikace -  ta je nahodna
			byte[] lengthAndIdentification = new byte[4];
			byte [] length = intToByteArray(p.getSize());
			copy(length ,lengthAndIdentification,0); // length
			lengthAndIdentification[2] = (byte) (Math.random()*256);	//identifikace je nahodna
			lengthAndIdentification[3] = (byte) (Math.random()*256);

			byte [] flagsAndFragmentOffset = new byte[]{0x40,0};	// pro jistotu davam don't fragment, i kdyz ICMP ho nema, tcp ale ano

			byte[] vp = new byte[20];
			copy(begin, vp, 0);
			copy(lengthAndIdentification, vp, begin.length);
			copy(flagsAndFragmentOffset, vp, begin.length + lengthAndIdentification.length);

			vp[8]=(byte)p.ttl; // nastaveni ttl

			// vyplneni protokolu:
			if(p.data==null){
				vp[9] =	(byte)0x8D; // neprirazeny cislo - tohle by stejne nemelo nastat
			} else if(p.data.getType()==L4Packet.L4PacketType.ICMP){
				vp[9]=1;
			}else if(p.data.getType()==L4Packet.L4PacketType.TCP){
				vp[9]=6;
			}else if(p.data.getType()==L4Packet.L4PacketType.ICMP){
				vp[9]=	0x11;
			}
			vp[10]=0;vp[11]=0;	// tady se pak spocita checksum

			// vyplneni adres:
			copy(p.src.getByteArray(),vp,12);
			copy(p.dst.getByteArray(),vp,16);

			// protokol vyssi vrstvy:
			byte[] data = translateTransportLayer(p.data);

			v = concat(vp, data);

		}

		return v;
	}

	/**
	 * Translates the packets on transport layer, for now translates only ICMP echo/reply packets.
	 * @param packet
	 * @return
	 */
	private byte[] translateTransportLayer(L4Packet packet) {
		byte [] v = null;

		if(packet.getType()==L4Packet.L4PacketType.ICMP){
			// pretypovani paketu:
			IcmpPacket p = (IcmpPacket) packet;

			// nacteni hlavni hlavicky, tedy toho, co tam bylo vzdycky:
			byte [] header = new byte[4];	// tahleta hlavicka je tam vzdycky
			byte [] echoHeader = null;
			byte [] payload = null;

			// type, code, checksum
			header[0]=(byte)p.type.getIntValue();
			header[1]=(byte)p.code.getIntValue();
			//dalsi 2 bajty jsou checksum

			// vytvareni echo hlavicky:
			if(p.type==IcmpPacket.Type.REPLY||p.type==IcmpPacket.Type.REQUEST){
				echoHeader = new byte [4];
				copy (intToByteArray(p.id),echoHeader,0);
				copy (intToByteArray(p.seq),echoHeader,2);

				// dodavani payloadu:
				if(p.payload!=null){
					payload = p.payload;
				}else{
					payload = new byte [p.getPayloadSize()];
					fillRandom(payload, 0, payload.length);
				}
			}
			v = concat(header, echoHeader);
			v = concat(v,payload);

		}

		return v;
	}

	/**
	 * Method, that calculates or known checksums.
	 * @param packet
	 */
	private void calculateCecksums(JPacket packet) {
		if(packet.hasHeader(JProtocol.IP4_ID)){
			Ip4 ipHeader = packet.getHeader(new Ip4());
			ipHeader.checksum(ipHeader.calculateChecksum());
			if(!ipHeader.isChecksumValid()){
				log(Logger.WARNING,"Bad IP chcesksu while sending packet.",null);
			}
		}
		if (packet.hasHeader(JProtocol.TCP_ID)) {
			Tcp tcpHeader = packet.getHeader(new Tcp());
			tcpHeader.checksum(tcpHeader.calculateChecksum());
		}
		if (packet.hasHeader(JProtocol.ICMP_ID)) {
			Icmp icmpHeader = packet.getHeader(new Icmp());
			int checksum=icmpHeader.calculateChecksum();
			icmpHeader.setByteArray(2,intToByteArray(checksum));
		}
	}


	/**
	 * Vrati ethernetovej kod vyssi vrstvy.
	 * @param type
	 * @return
	 */
	private byte [] translateEthertype(L3Packet.L3PacketType type) {
		byte[] v;
		if (type == ARP) {
			v = new byte[]{0x08, 0x06};
		} else if (type == IPv4) {
			v = new byte[]{0x08, 0x00};
		} else {
			v = new byte[]{0x00, 0x00};	// neco hazet musim
		}

		return v;
	}




// Ostatni verejny metody: ------------------------------------------------------------------------------------------


	@Override
	public String getDescription() {
		return swport.getDescription()+": PacketSender";
	}

	private void log(int logLevel, String msg, Object obj){
		Logger.log(this, logLevel, LoggingCategory.REAL_NETWORK, msg, obj);
	}




// pomocny metody, vetsinou na praci s polem bajtu: --------------------------------------------------------------------

	/**
	 * Copies the whole array src to array dst beginning on destPos. Nakopiruje cely pole src do pole dest od indexu
	 * destPos.
	 *
	 * @param src
	 * @param dest
	 * @param destPos
	 */
	private void copy(byte[] src, byte[] dest, int destPos) {
		try {
			System.arraycopy(src, 0, dest, destPos, src.length);
		} catch (Exception ex) {
			log(Logger.WARNING, "Error while copying arrays.", ex);
		}
	}

	/**
	 * Spoji 2 pole za sebe.
	 * @param a1
	 * @param a2
	 * @return
	 */
	private byte[] concat(byte[] a1, byte[] a2) {
		if (a1 == null) {
			a1 = new byte[0];
		}
		if (a2 == null) {
			a2 = new byte[0];
		}
		byte[] C = new byte[a1.length + a2.length];
		System.arraycopy(a1, 0, C, 0, a1.length);
		System.arraycopy(a2, 0, C, a1.length, a2.length);
		return C;
	}

	/**
	 * Prevede integer do dvoubajtovyho pole.
	 * @param a
	 * @return
	 */
	private byte[] intToByteArray(int a) {
		int length = 2;
		byte[] array = new byte[length];
		for (int i = length - 1; i >= 0; i--) {
			array[i] = (byte) (a & 0xff);
			a = a >>> 8;
		}
		return array;
	}

	/**
	 * Naplni zadany pole nahodnejma datama od indexu begin (vcetne) do indexu end (mimo).
	 * @param array
	 * @param begin
	 * @param end
	 */
	private void fillRandom(byte[] array, int begin, int end) {
		try {
			for (int i = begin; i < end; i++) {
				array[i] = (byte) (Math.random() * 256);
			}
		} catch (Exception ex) {
			log(Logger.WARNING, "Chyba pri nahodnym vyplnovani pole.", ex);
		}
	}

}
