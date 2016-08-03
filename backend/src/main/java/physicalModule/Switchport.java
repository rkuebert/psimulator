/*
 * Erstellt am 26.10.2011.
 */
package physicalModule;

import dataStructures.packets.L2Packet;

/**
 * Represents abstract physical switchport.
 * Sends and receives packet through cable.
 *
 * It is not running in its own thread, thread of PhysicMod handles it.
 *
 * @author neiss, haldyr
 */
public abstract class Switchport {

	protected AbstractPhysicalModule physicalModule;

	/**
	 * Unique number in PhysicMod.
	 */
	public final int number;

	/**
	 * ID takove, jake je v konfiguracnim souboru.
	 */
	public final int configID;

	/**
	 * ID pocitace.
	 */
	protected final int deviceID;


	public Switchport(AbstractPhysicalModule physicMod, int number, int configID) {
		this.physicalModule = physicMod;
		this.number = number;
		this.configID = configID;
		this.deviceID = physicMod.device.configID;
	}


	/**
	 * Try to send packet through this interface.
	 * It just adds packet to buffer (if capacity allows) and notifies connected cable that it has work to do.
	 */
	protected abstract void sendPacket(L2Packet packet);

	/**
	 * Receives packet from cable and pass it to physical module.
	 */
	protected abstract void receivePacket(L2Packet packet);

	/**
	 * Returns true, if on the other end of cable is connected other network device.
	 * @return
	 */
	public abstract boolean isConnected();

	public abstract boolean isReal();

	@Override
	public String toString() {
		return "Switchport number=" + number + ", configID=" + configID;
	}
}
