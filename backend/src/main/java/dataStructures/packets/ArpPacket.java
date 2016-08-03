/*
 * created 1.2.2012
 */

package dataStructures.packets;

import dataStructures.MacAddress;
import dataStructures.ipAddresses.IpAddress;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * Represents ARP packet.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ArpPacket extends L3Packet {

	public enum ArpOperation { // only theese 2 options exists
		ARP_REQUEST,
		ARP_REPLY;
	}

	/**
	 * Inquirer IP address.
	 */
	public final IpAddress senderIpAddress;
	/**
	 * Inquirer MAC address.
	 */
	public final MacAddress senderMacAddress;
	/**
	 * Target IP address.
	 */
	public final IpAddress targetIpAddress;
	/**
	 * Target MAC address.
	 * ARP request has always 00:00:00:00:00:00
	 */
	public final MacAddress targetMacAddress;
	/**
	 * ARP request || ARP reply
	 */
	public final ArpOperation operation;

	/**
	 * Constructor for creating ARP reply.
	 *
	 * @param senderIpAddress inquirer IP address
	 * @param senderMacAddress inquirer MAC zdroje
	 * @param targetIpAddress my IP address
	 * @param targetMacAddress my MAC address (searched address)
	 */
	public ArpPacket(IpAddress senderIpAddress, MacAddress senderMacAddress, IpAddress targetIpAddress, MacAddress targetMacAddress) {
		super(null);
		this.senderIpAddress = senderIpAddress;
		this.senderMacAddress = senderMacAddress;
		this.targetIpAddress = targetIpAddress;
		this.targetMacAddress = targetMacAddress;
		this.operation = ArpOperation.ARP_REPLY;
		countSize();
	}

	/**
	 * Constructor for creating ARP request.
	 * Sends iff target MAC address is unknown.
	 *
	 * @param senderIpAddress sending interface's IP address
	 * @param senderMacAddress sending interface's MAC address
	 * @param targetIpAddress IP adresa pro kterou hledam MAC adresu
	 */
	public ArpPacket(IpAddress senderIpAddress, MacAddress senderMacAddress, IpAddress targetIpAddress) {
		super(null);
		this.senderIpAddress = senderIpAddress;
		this.senderMacAddress = senderMacAddress;
		this.targetIpAddress = targetIpAddress;
		this.targetMacAddress = new MacAddress("00:00:00:00:00:00");
		this.operation = ArpOperation.ARP_REQUEST;
		countSize();
	}

	/**
	 * Constructor for creating ARP request.
	 * Sends iff IP address on network interface was changed in order to notify other devices.
	 *
	 * @param senderIpAddress IP address of changed interface
	 * @param senderMacAddress MAC address of changed interface
	 */
	public ArpPacket(IpAddress senderIpAddress, MacAddress senderMacAddress) {
		super(null);
		this.senderIpAddress = senderIpAddress;
		this.senderMacAddress = senderMacAddress;
		this.targetIpAddress = new IpAddress("0.0.0.0"); // asi k nicemu
		this.targetMacAddress = new MacAddress("00:00:00:00:00:00");
		this.operation = ArpOperation.ARP_REQUEST;
		countSize();
	}

	@Override
	public L3PacketType getType() {
		return L3PacketType.ARP;
	}

	@Override
	public String toString(){
		return "ArpPacket: "+ operation +" sender: "+senderIpAddress + " " + senderMacAddress + " target: "+ targetIpAddress + " " + targetMacAddress;
	}

	@Override
	protected final void countSize() {
		this.size = 28;
	}

	@Override
	public String getEventDesc() {
		String s = "=== ARP "+operation+" ===\n";
		s += "sender: " + senderMacAddress + "  ";
		s += senderIpAddress + "\n";
		s += "target: " + targetMacAddress + "  ";
		s += targetIpAddress;
		return s;
	}

	@Override
	public PacketType getPacketEventType() {
		return PacketType.ARP;
	}
}
