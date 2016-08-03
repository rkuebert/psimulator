/*
 * Erstellt am 26.10.2011.
 */

package networkModule;

import dataStructures.packets.L2Packet;
import device.Device;
import physicalModule.AbstractPhysicalModule;
import physicalModule.PhysicMod;


//TODO: napsat javadoc anglicky.
/**
 * Nejabstraknejsi ze sitovejch modulu. Interface representující síťový modul počítače bez rozhraní pro aplikační
 * vrstvu, prakticky tedy použitelnej jen pro switch. Síťový modul zajišťuje síťovou komunikaci na 2.,3. a 4. vrstvě
 * ISO/OSI modelu.
 *
 * @author neiss
 */


public abstract class NetworkModule {

    protected Device device;

	public NetworkModule(Device device) {
		assert device != null;
		this.device = device;
	}

    public Device getDevice() {
        return device;
    }

	public AbstractPhysicalModule getPhysicMod() {
		return device.physicalModule;
	}

	/**
	 * Implementovat synchronizovane!
	 * @param packet
	 * @param swport
	 */
    public abstract void receivePacket(L2Packet packet, int  switchportNumber);

	/**
	 * Returns true, if device is switch and have only link layer.
	 * @return
	 */
	public abstract boolean isSwitch();

	/**
	 * Jestli je to klasickej TCP/IP Network Modul.
	 *
	 * @return true, kdyz je potomkem nebo instanci tridy TcpIpNetmod
	 */
	public final boolean isStandardTcpIpNetMod(){
		return IpNetworkModule.class.isAssignableFrom(this.getClass());	// funguje to, mam to otestovany
	}

}
