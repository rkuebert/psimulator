/*
 * created 25.1.2012
 */

package dataStructures.packets;

import dataStructures.packets.L3Packet.L3PacketType;
import dataStructures.MacAddress;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import utils.Util;

/**
 * Represents Ethernet II frames.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomas Pitrinec
 */
public class EthernetPacket extends L2Packet {

	public final MacAddress src;
	public final MacAddress dst;

	private int size;

	/**
	 * Protokol vyssi vrstvy.
	 * Nema pro simulator zadnej velkej vyznam, ale je soucasti toho ethernet II paketu - 3. predaska PSI, slide 32
	 */
	final L3PacketType type;

	public EthernetPacket(MacAddress src, MacAddress dst, L3PacketType ethertype) {
		super();
		this.src = src;
		this.dst = dst;
		this.type = ethertype;
		countSize();
	}

	public EthernetPacket(MacAddress src, MacAddress dst, L3PacketType ethertype, L3Packet data) {
		super(data);
		this.src = src;
		this.dst = dst;
		this.type = ethertype;
		countSize();
	}

	/**
	 * Vrati protokol vyssi vrstvy (polozku type z toho paketu)
	 * @return
	 */
	public L3PacketType getEthertype() {
		return type;
	}

	@Override
	public int getSize() {
		return size;
	}

	private void countSize(){
		int headerLength = 24; //8,6,6,2,?,4 (preambule, mac, mac, typ, data crc) - 3. predaska PSI
		size = headerLength + (data != null ? data.getSize() : 0);
	}

	@Override
	public L2PacketType getType() {
		return L2PacketType.ethernetII;
	}

	@Override
	public String toString(){
		String vratit = "EthPacket: src: "+src+ " dst: "+dst+" "+Util.zarovnej(type.toString(), 4);	// vypis minimalistickej, aby se to veslo na obrazovku
		return vratit;
	}

	@Override
	public String getEventDesc() {
		String s = "=== Ethernet === size: " + size + "\n";
		s += "src: " + src + "  ";
		s += "dst: " + dst + "  ";
		if (data != null) {
			s += "\n" + data.getEventDesc();
		}
		return s;
	}

	@Override
	public PacketType getPacketEventType() {
		if (data != null) {
			return data.getPacketEventType();
		}
		return PacketType.ETHERNET;
	}
}
