/*
 * created 2.5.2012
 */
package physicalModule;

import dataStructures.DropItem;
import dataStructures.packets.L2Packet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import utils.SmartRunnable;
import utils.Util;
import utils.WorkerThread;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class SimulatorSwitchportV2 extends AbstractSimulatorSwitchport implements Loggable, SmartRunnable {

	protected CableV2 cable;

	/**
	 * Processing time in [ns] for sending 1 packet.
	 */
	int processingDelay = 100;

	protected final WorkerThread worker;
	private final Loggable object;
	/**
	 * Current size of buffer in bytes.
	 */
	private AtomicInteger sizeReceive = new AtomicInteger(0);
	/**
	 * Current size of buffer in bytes.
	 */
	private AtomicInteger sizeSend = new AtomicInteger(0);
	/**
	 * Capacity of buffer in bytes.
	 */
	private final int capacity = 150_000; // zatim: 100 x max velikost ethernetovyho pakatu


	private Stats sendStats = new Stats();
	private Stats receiveStats = new Stats();

	/**
	 * Storage for packets to be received.
	 */
	private final List<L2Packet> receiveBuffer = Collections.synchronizedList(new LinkedList<L2Packet>() {

		@Override
		public boolean add(L2Packet packet) {
			int packetSize = packet.getSize();

			if (sizeReceive.get() + packetSize > capacity) { // run out of capacity
				Logger.log(object, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: Queue is full.", packet.toStringWithData());
				Logger.log(object, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicalModule.device.configID));
				receiveStats.droppedPackets.getAndIncrement();

				if (hasIpNetworkModule) {
					handleSourceQuench(packet);
				}
			} else {
				sizeReceive.addAndGet(packetSize);
				receiveStats.processedBytes.addAndGet(packetSize);
				receiveStats.processedPackets.getAndIncrement();
			}

			return super.add(packet);
		}

		@Override
		public L2Packet remove(int index) {
			L2Packet packet = super.remove(index);
			sizeReceive.set(sizeReceive.get() - packet.getSize());
			return packet;
		}
	});
	/**
	 * Storage for packets to be sent.
	 */
	private final List<L2Packet> sendBuffer = Collections.synchronizedList(new LinkedList<L2Packet>() {

		@Override
		public boolean add(L2Packet packet) {
			int packetSize = packet.getSize();

			if (sizeSend.get() + packetSize > capacity) { // run out of capacity
				Logger.log(object, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: Queue is full.", packet.toStringWithData());
				Logger.log(object, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicalModule.device.configID));
				sendStats.droppedPackets.getAndIncrement();

				if (hasIpNetworkModule) {
					handleSourceQuench(packet);
				}
			} else {
				sizeSend.addAndGet(packetSize);
				sendStats.processedBytes.addAndGet(packetSize);
				sendStats.processedPackets.getAndIncrement();
			}

			return super.add(packet);
		}

		@Override
		public L2Packet remove(int index) {
			L2Packet packet = super.remove(index);
			sizeSend.set(sizeSend.get() - packet.getSize());
			return packet;
		}
	});

	// konstruktory a buildeni pri startu: --------------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param physicMod
	 * @param number
	 * @param configID ID of EthernetInterfaceModel from XML.
	 */
	public SimulatorSwitchportV2(AbstractPhysicalModule physicMod, int number, int configID) {
		super(physicMod, number, configID);
		this.worker = new WorkerThread(this);

		object = this; // for add methods in buffers
	}

	// konstruktory a buildeni pri startu: --------------------------------------------------------------------------------

	@Override
	protected void sendPacketFurther(L2Packet packet) {
		sendBuffer.add(packet);
		worker.wake();
	}

	@Override
	protected void receivePacketFurther(L2Packet packet) {
		receiveBuffer.add(packet);
		worker.wake();
	}

	@Override
	public void doMyWork() {
		while (!sendBuffer.isEmpty() || !receiveBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {
				L2Packet packet = receiveBuffer.remove(0);
				physicalModule.receivePacket(packet, this);
			}

			if (!sendBuffer.isEmpty()) {
				L2Packet packet = sendBuffer.remove(0);

				if (cable == null) {
					Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: No cable is attached.", packet.toStringWithData());
					Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicalModule.device.configID));
					sendStats.droppedPackets.getAndIncrement();
				} else {
					AbstractSimulatorSwitchport dest = cable.getTheOtherSwitchport(this);
					if (dest == null) {
						Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: No switchport in cable on the other side.", packet.toStringWithData());
						Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicalModule.device.configID));
						sendStats.droppedPackets.getAndIncrement();
					} else {
						makeDelay(packet);
						cable.transmit(packet, this, dest);
					}
				}
			}
		}
	}

	private void makeDelay(L2Packet packet) {
		Util.sleepNano(packet.getSize() * cable.sendingSpeed + processingDelay);
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
		Logger.log(this, Logger.DEBUG, LoggingCategory.PHYSICAL, "Na druhy strane kabelu je null!", null);
		return false;
	}

	@Override
	public String getDescription() {
		return "SimulatorSwitchportV2 number: " + number + ", configID: " + configID;
	}
}
