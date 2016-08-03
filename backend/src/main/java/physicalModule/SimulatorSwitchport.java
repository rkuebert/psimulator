/*
 * created 24.1.2012
 */
package physicalModule;

import dataStructures.DropItem;
import dataStructures.packets.IpPacket;
import dataStructures.packets.L2Packet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.IpNetworkModule;
import networkModule.L4.IcmpHandler;

/**
 * Represent's switchport on layer 2.
 * Nebezi ve vlasti vlakne, ma jeden bufer odchozich paketu, ktery plni fysicka vrstva a vybira kabel.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class SimulatorSwitchport extends AbstractSimulatorSwitchport implements Loggable {

	protected Cable cable;

	/**
	 * Storage for packets to be sent.
	 */
	private final List<L2Packet> buffer = Collections.synchronizedList(new LinkedList<L2Packet>());

	/**
	 * Current size of buffer in bytes.
	 */
	private int size = 0;
	/**
	 * Capacity of buffer in bytes.
	 */
	private int capacity = 150_000; // zatim: 100 x max velikost ethernetovyho pakatu
	/**
	 * Count of dropped packets.
	 *
	 * @param packet
	 * @return
	 */
	private int dropped = 0;


// konstruktory a buildeni pri startu: --------------------------------------------------------------------------------

	public SimulatorSwitchport(AbstractPhysicalModule physicMod, int number, int configID) {
		super(physicMod, number, configID);
	}

// metody pro sitovou komunikaci:

	@Override
	protected void sendPacketFurther(L2Packet packet) {
		int packetSize = packet.getSize();

		Logger.log(this, Logger.DEBUG, LoggingCategory.PHYSICAL, "velikost paketu: ", packetSize);

		if (size + packetSize > capacity) { // run out of capacity
			Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: Queue is full.", packet.toStringWithData());
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicalModule.device.configID));
			dropped++;

			if (hasIpNetworkModule) {
				handleSourceQuench(packet);
			}

		} else if (cable == null) { // no cable attached
			Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: No cable is attached.", packet.toStringWithData());
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicalModule.device.configID));
			dropped++;
		} else {
			size += packetSize;
			buffer.add(packet);
			cable.worker.wake();
		}
	}

	/**
	 * Receives packet from cable and pass it to physical module.
	 */
	@Override
	protected void receivePacketFurther(L2Packet packet) {
		physicalModule.receivePacket(packet, this);
	}

	/**
	 * Removes packet form buffer and returns it, decrements size of buffer. Synchronised via buffer. Throws exception
	 * when this method is called and no packet is in buffer.
	 *
	 * @return
	 */
	public L2Packet popPacket() {
		L2Packet packet;
		packet = buffer.remove(0);
		size -= packet.getSize();
		return packet;
	}

	/**
	 * Returns true if buffer is empty.
	 */
	public boolean isEmptyBuffer() {
		return buffer.isEmpty();
	}

	/**
	 * Returns true, if on the other end of cable is connected other network device.
	 *
	 * @return
	 */
	@Override
	public boolean isConnected() {
		if (cable == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.PHYSICAL, "Cable is null!", null);
			return false;
		}
		if (cable.getTheOtherSwitchport(this) != null) {
			return true;
		}
		Logger.log(this, Logger.DEBUG, LoggingCategory.PHYSICAL, "The other end of the cable is null!", null);
		return false;
	}

	@Override
	public String getDescription() {
		return "SimulatorSwitchport number: "+number + ", configID: "+configID;
	}
}
