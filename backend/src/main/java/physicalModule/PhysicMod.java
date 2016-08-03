/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.packets.L2Packet;
import device.Device;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Seznam sitovych rozhrani reprezentujici fyzicke rozhrani
 *
 * Old physical module: module + every cabel have its own threads
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomas Pitrinec
 */
public class PhysicMod extends AbstractPhysicalModule implements SmartRunnable, Loggable {

	/**
	 * Queue for incomming packets from cabels.
	 */
	private final List<BufferItem> receiveBuffer = Collections.synchronizedList(new LinkedList<BufferItem>());
	/**
	 * Queue for incomming packets from network module.
	 */
	private final List<BufferItem> sendBuffer = Collections.synchronizedList(new LinkedList<BufferItem>());
	/**
	 * Working thread.
	 */
	private WorkerThread worker;

// Konstruktory a vytvareni modulu: -----------------------------------------------------------------------------------

	public PhysicMod(Device device) {
		super(device);
		this.worker = new WorkerThread(this);
	}

	@Override
	public void addSwitchport(int number, boolean realSwitchport, int configID) {
		Switchport swport;
		if (!realSwitchport) {
			swport = new SimulatorSwitchport(this, number, configID);
		} else {
			swport = new RealSwitchport(this, number, configID);
		}
		switchports.put(swport.number, swport);
	}

// Verejny metody na posilani paketu - komunikace s ostatnima vrstvama ------------------------------------------------
	/**
	 * Adds incoming packet from cabel to the buffer. Sychronized via buffer. Wakes worker.
	 *
	 * @param packet to receive
	 * @param iface which receives packet
	 */
	@Override
	public void receivePacket(L2Packet packet, Switchport iface) {
		receiveBuffer.add(new BufferItem(packet, iface));
		worker.wake();
	}

	/**
	 * Adds incoming packet from network module to the buffer and then try to send it via cabel. Sychronized via buffer.
	 * Wakes worker.
	 *
	 * @param packet to send via physical module
	 * @param iface through it will be send
	 */
	@Override
	public void sendPacket(L2Packet packet, int switchportNumber) {
		Switchport swport = switchports.get(switchportNumber);
		if (swport == null) {
			Logger.log(this, Logger.ERROR, LoggingCategory.PHYSICAL, "Trying to send packet to non-existent switchport number: "+switchportNumber, packet);
		}
		sendBuffer.add(new BufferItem(packet, swport));
		worker.wake();
	}

// Samotna prace a ruzny dulezity gettry rozhrani k ostatnim vrstvam (fasada): ----------------------------------------

	@Override
	public void doMyWork() {

		while (!receiveBuffer.isEmpty() || !sendBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {
				BufferItem m = receiveBuffer.remove(0);
				getNetMod().receivePacket(m.packet, m.switchport.number);
			}

			if (!sendBuffer.isEmpty()) {
				BufferItem m = sendBuffer.remove(0);
				m.switchport.sendPacket(m.packet);
			}
		}
	}

// Pomocny metody a zkratky: ------------------------------------------------------------------------------------------

	@Override
	public String getDescription() {
		return device.getName() + ": PhysicMod";
	}
}
