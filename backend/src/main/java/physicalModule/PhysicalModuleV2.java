/*
 * created 2.5.2012
 */
package physicalModule;

import dataStructures.packets.L2Packet;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;

/**
 * New physical module: every switchports runs in its own thread.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PhysicalModuleV2 extends AbstractPhysicalModule {

	public PhysicalModuleV2(Device device) {
		super(device);
	}

	@Override
	public void addSwitchport(int number, boolean realSwitchport, int configID) {
		Switchport swport;
		if (!realSwitchport) {
			swport = new SimulatorSwitchportV2(this, number, configID);
		} else {
			swport = new RealSwitchport(this, number, configID);
		}
		switchports.put(swport.number, swport);
	}

	@Override
	public void receivePacket(L2Packet packet, Switchport switchport) {
		getNetMod().receivePacket(packet, switchport.number);
	}

	@Override
	public void sendPacket(L2Packet packet, int switchportNumber) {
		Switchport swport = switchports.get(switchportNumber);
		if (swport == null) {
			Logger.log(this, Logger.ERROR, LoggingCategory.PHYSICAL, "Trying to send packet to non-existent switchport number: "+switchportNumber, packet);
		}
		swport.sendPacket(packet);
	}

	@Override
	public String getDescription() {
		return device.getName() + ": "+getClass().getSimpleName();
	}
}
