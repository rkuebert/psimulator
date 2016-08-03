/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.CableItem;
import dataStructures.packets.L2Packet;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Represents cable.
 * This cable process packets on both sides of the cable.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Cable extends AbstractCable implements SmartRunnable, Loggable {

	protected final WorkerThread worker;

	/**
	 * Delay in milliseconds
	 */
	private long delay;

	/**
	 * Creates cable with given delay time.
	 * @param id
	 * @param delay
	 */
	public Cable(int configID, long delay) {
		super(configID);
		this.delay = delay;
		this.worker = new WorkerThread(this);
	}

	/*
	 * Sets first connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public void setFirstSwitchport(SimulatorSwitchport swport) {
		swport.cable = this;
		this.firstCon = swport;
	}

	/*
	 * Sets second connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public void setSecondSwitchport(SimulatorSwitchport swport) {
		swport.cable = this;
		this.secondCon = swport;
	}

	@Override
	public void doMyWork() {
		L2Packet packet;
		boolean firstIsEmpty = true;
		boolean secondIsEmpty = true;

		do {
			SimulatorSwitchport first = (SimulatorSwitchport) firstCon; // mohlo by to byt vne while-cyklu, ale co kdyz nekdo zapoji kabel (konektor) do rozhrani a my budem chtit, aby se to rozjelo?
			SimulatorSwitchport second = (SimulatorSwitchport) secondCon;

			if ((first != null) && !first.isEmptyBuffer()) {
				packet = first.popPacket();
				if (second != null) {
					makeDelay();
					Logger.log(this, Logger.INFO, LoggingCategory.CABEL_SENDING, "Sending packet through cabel..", new CableItem(packet, getFirstIdDevice(), getSecondIdDevice(), configID));
					second.receivePacket(packet);
				}
				firstIsEmpty = first.isEmptyBuffer();
			}

			if ((second != null) && !second.isEmptyBuffer()) {
				packet = second.popPacket();
				if (first != null) {
					makeDelay();
					Logger.log(this, Logger.INFO, LoggingCategory.CABEL_SENDING, "Sending packet through cabel..", new CableItem(packet, getSecondIdDevice(), getFirstIdDevice(), configID));
					first.receivePacket(packet);
				}
				secondIsEmpty = second.isEmptyBuffer();
			}

		} while (!firstIsEmpty || !secondIsEmpty);
	}

	private void makeDelay() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException ex) {
			// ok
		}
	}

	@Override
	public String getDescription() {
		return "Cable: 1_ID=" + getFirstIdDevice() + " " + "2_ID=" + getSecondIdDevice();
	}
}
