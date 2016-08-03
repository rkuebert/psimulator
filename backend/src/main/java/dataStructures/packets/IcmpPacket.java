/*
 * created 29.2.2012
 */
package dataStructures.packets;

import shared.SimulatorEvents.SerializedComponents.PacketType;
import utils.Util;
import static dataStructures.packets.IcmpPacket.Type.*;
import static dataStructures.packets.IcmpPacket.Code.*;
import java.util.Arrays;

/**
 * Represents ICMP packet.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomas Pitrinec
 */
public class IcmpPacket extends L4Packet {

	private static int HEADER_LENGTH = 8;	// staticka promenna pro dylku hlavicky
	private static int DEFAULT_PAYLOAD_SIZE = 56;

	/**
	 * REPLY, REQUEST, UNDELIVERED, TIME_EXCEEDED.
	 */
	public final Type type;
	/**
	 * DHU, DNU, PU, ..
	 */
	public final Code code;
	/**
	 * Identifier is used like a port in TCP or UDP to identify a session.
	 */
	public final int id;
	/**
	 * Sequence number is incremented on each echo request sent. The echoer returns these same values (id+seq) in the
	 * echo reply.
	 */
	public final int seq;

	/**
	 * Payload, vyplneno pouze pokud paket pochazi z realne site. Je potreba ho predavat do odpovedi!
	 */
	public final byte [] payload;

	/**
	 * Dylka payloadu, vyplnena vzdy, kdyz je paket z realny site, tak je to jeho skutecna dylka.
	 */
	public final int payloadSize;


// konstruktory: -----------------------------------------------------------------------------------------------------

	/**
	 * Creates IcmpPacket with given type and code. <br /> id and seq is set to 0
	 * PayloadSize se doplni automaticky, payload se nastavi na null.
	 *
	 * @param type
	 * @param code
	 */
	public IcmpPacket(Type type, Code code) {
		this.type = type;
		this.code = code;
		this.id = 0;
		this.seq = 0;
		payload = null;
		payloadSize=DEFAULT_PAYLOAD_SIZE;
	}

	/**
	 * Creates IcmpPacket with given type, code, id, seq.
	 * PayloadSize se doplni automaticky, payload se nastavi na null.
	 *
	 * @param type
	 * @param code
	 * @param id
	 * @param seq
	 */
	public IcmpPacket(Type type, Code code, int id, int seq) {
		this.type = type;
		this.code = code;
		this.id = id;
		this.seq = seq;
		payload = null;
		payloadSize=DEFAULT_PAYLOAD_SIZE;
	}

	/**
	 * Uplnej konstruktor.
	 *
	 * @param type
	 * @param code
	 * @param id
	 * @param seq
	 * @param payloadSize
	 * @param payload
	 */
	public IcmpPacket(Type type, Code code, int id, int seq, int payloadSize, byte[] payload) {
		this.type = type;
		this.code = code;
		this.id = id;
		this.seq = seq;
		this.payloadSize = payloadSize;
		this.payload = payload;
	}

	/**
	 * Uplny konstruktor z typu a kodu jako integeru.
	 * Creates IcmpPacket with given type, code, id, seq. Payload cannot be null!
	 *
	 * @param type
	 * @param code
	 * @param id
	 * @param seq
	 * @param payload
	 */
	public IcmpPacket(int type, int code, int id, int seq, byte[] payload) throws Exception {
		this.type = intToTypeEnum(type);
		this.code = intToCodeEnum(code);
		this.id = id;
		this.seq = seq;
		payloadSize = payload.length;
		this.payload = payload;
	}


// metody: -----------------------------------------------------------------------------------------------------

	@Override
	public L4PacketType getType() {
		return L4PacketType.ICMP;
	}

	@Override
	public int getSize(){
		return payloadSize+HEADER_LENGTH;
	}

	@Override
	public String toString(){
		return "IcmpPacket: "+Util.zarovnej(type.toString(), 7)+" "+code+" id: " + id + " seq="+seq;
	}

	public int getPayloadSize() {
		if (payload != null) {
			return payload.length;
		} else {
			return payloadSize; // vraci velikost dat bez hlavicky
		}
	}

	@Override
	public int getPortSrc() {
		return id;
	}

	@Override
	public int getPortDst() {
		return id;
	}

	@Override
	public L4Packet getCopyWithDifferentSrcPort(int port) {
		return new IcmpPacket(type, code, port, seq, payloadSize, payload);
	}

	@Override
	public L4Packet getCopyWithDifferentDstPort(int port) {
		return new IcmpPacket(type, code, port, seq, payloadSize, payload);
	}

	@Override
	public String getEventDesc() {
		String s = "=== ICMP === \n";
		s += "type: " + type + "   ";
		s += "code: " + code + "\n";
		s += "id: " + id + "   ";
		s += "seq: " + seq + "   ";
		s += "payloadSize: " + payloadSize;
		return s;
	}

	@Override
	public PacketType getPacketEventType() {
		return PacketType.ICMP;
	}




// Enums for type and code (witch converting functions): ------------------------------------------------------------

	/**
	 * Types of ICMP packet.
	 */
	public enum Type {

		/**
		 * Ozvěna.
		 */
		REPLY(0),
		/**
		 * Žádost o ozvěnu.
		 */
		REQUEST(8),
		/**
		 * Signalizace nedoručení IP paketu.
		 */
		UNDELIVERED(3),
		/**
		 * Čas (ttl) vypršel.
		 */
		TIME_EXCEEDED(11),
		/**
		 * This message may be generated if a router or host does not have sufficient buffer space to process the
		 * request, or may occur if the router or host buffer is approaching its limit.
		 */
		SOURCE_QUENCH(4);

		private int value; // value as int

		// Constructor
		Type(int value) {
			this.value = value;
		}

		public int getIntValue(){
			return value;
		}

	}

	public static Type intToTypeEnum(int t) throws Exception{
		switch(t){
			case 0: return REPLY;
			case 8: return REQUEST;
			case 3: return UNDELIVERED;
			case 11: return TIME_EXCEEDED;
			case 4: return SOURCE_QUENCH;
		}
		throw new Exception("ICMP type "+t+" cannot be recognized");
	}

	/**
	 * Podtypy icmp paketu, pro kazdej typ jinej vyznam, u nas to ma vyznam jen pro typ UNDELIVERED.
	 */
	public enum Code {

		/**
		 * U typu 3 (undelivered) to je destination network unreachable, jinak nic (default)
		 */
		ZERO(0),
		//     * 1 - nedosažitelný uzel (host unreachable)
		HOST_UNREACHABLE(1),
		//     * 2 - nedosažitelný protokol (protocol unreachable)
		PROTOCOL_UNREACHABLE(2),
		//     * 3 – nedosažitelný port (port unreachable)
		PORT_UNREACHABLE(3),
		//     * 4 - nedosažitelná síť (network unreachable)

		//     * 5 – nutná fragmentace, ale není povolena
		FRAGMENTAION_REQUIRED(4),
		//     * 6 – neznámá cílová síť (destination network unknown)
		DESTINATION_NETWORK_UNKNOWN(6);

		private int value; // value as int
		Code(int value){
			this.value = value;
		}
		public int getIntValue(){
			return value;
		}
	}
	public static Code intToCodeEnum(int t) throws Exception{
		switch(t){
			case 0: return ZERO;
			case 1: return HOST_UNREACHABLE;
			case 2: return PROTOCOL_UNREACHABLE;
			case 3: return PORT_UNREACHABLE;
			case 4: return FRAGMENTAION_REQUIRED;
			case 6: return DESTINATION_NETWORK_UNKNOWN;
		}
		throw new Exception("ICMP code "+t+" cannot be recognized");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IcmpPacket other = (IcmpPacket) obj;
		if (this.type != other.type) {
			return false;
		}
		if (this.code != other.code) {
			return false;
		}
		if (this.id != other.id) {
			return false;
		}
		if (this.seq != other.seq) {
			return false;
		}
		if (!Arrays.equals(this.payload, other.payload)) {
			return false;
		}
		if (this.payloadSize != other.payloadSize) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 67 * hash + (this.type != null ? this.type.hashCode() : 0);
		hash = 67 * hash + (this.code != null ? this.code.hashCode() : 0);
		hash = 67 * hash + this.id;
		hash = 67 * hash + this.seq;
		hash = 67 * hash + Arrays.hashCode(this.payload);
		hash = 67 * hash + this.payloadSize;
		return hash;
	}
}
