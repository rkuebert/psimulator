/*
 * created 2.5.2012
 */
package physicalModule;

import dataStructures.packets.IpPacket;
import dataStructures.packets.L2Packet;
import logging.Loggable;
import networkModule.IpNetworkModule;
import networkModule.L4.IcmpHandler;

/**
 * Parent of all simulator switchport (except RealSwitchport!).
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class AbstractSimulatorSwitchport extends Switchport implements Loggable {

	protected IcmpHandler icmpHandler = null;
	protected boolean hasIpNetworkModule;
	private boolean firstTime = true;
	protected final int deviceID;

	public AbstractSimulatorSwitchport(AbstractPhysicalModule physicMod, int number, int configID) {
		super(physicMod, number, configID);
		this.deviceID = physicMod.device.configID;
	}

	protected abstract void sendPacketFurther(L2Packet packet);

	protected abstract void receivePacketFurther(L2Packet packet);

	@Override
	protected final void sendPacket(L2Packet packet) {
		if (firstTime) {
			getIcmpHandler();
		}

		sendPacketFurther(packet);
	}

	@Override
	protected final void receivePacket(L2Packet packet) {
		if (firstTime) {
			getIcmpHandler();
		}

		receivePacketFurther(packet);
	}

	private void getIcmpHandler() {
		firstTime = false;
		hasIpNetworkModule = physicalModule.device.getNetworkModule().isStandardTcpIpNetMod();
		if (hasIpNetworkModule) {
			icmpHandler = ((IpNetworkModule) (physicalModule.device.getNetworkModule())).transportLayer.icmpHandler;
		}
	}

	/**
	 * In configuration it isn't saved, that switchport is real, it's discovered while cables are plugged in. So I need
	 * this function to convert this SimulatorSwitchport to RealSwitchport.
	 *
	 * V konfiguraci neni ulozeno, je-li switchport realny, to se zjisti az podle toho, jestli kabel od neho natazenej
	 * vede k realnymu pocitaci. Proto potrebuju tuto metodu, abych moh puvodne vytvoreny simulator switchport
	 * konvertovat na realnej.
	 */
	public void replaceWithRealSwitchport() {
		physicalModule.addSwitchport(number, true, configID);
	}

	protected void handleSourceQuench(L2Packet packet) {
		if (packet.data != null && packet.data instanceof IpPacket) {
			IpPacket p = (IpPacket) packet.data;
			icmpHandler.sendSourceQuench(p.src, p);
		} else {
//			Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: queue is full - packet is not IP so no source-quench is sent.", packet.toStringWithData());
//			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicalModule.device.configID));
		}
	}

	@Override
	public boolean isReal() {
		return false;
	}
}
