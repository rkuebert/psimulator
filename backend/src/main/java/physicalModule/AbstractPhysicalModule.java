/*
 * created 2.5.2012
 */
package physicalModule;

import dataStructures.packets.L2Packet;
import device.Device;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.NetworkModule;

/**
 * Parent of all physical modules. It works as facade.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class AbstractPhysicalModule implements Loggable {

	/**
	 * List of interfaces.
	 */
	protected Map<Integer, Switchport> switchports = new HashMap<>();
	/**
	 * Odkaz na PC.
	 */
	public final Device device;

	// Konstruktory a vytvareni modulu: ----------------------------------------------------------------------------------------------
	public AbstractPhysicalModule(Device device) {
		this.device = device;
	}

	/**
	 * Pridani switchportu
	 *
	 * @param number cislo switchportu
	 * @param realSwitchport je-li switchport realnym rozhranim (tzn. vede k realnymu pocitaci)
	 * @param configID id z konfigurace - tedy ID u EthInterfaceModel
	 */
	public abstract void addSwitchport(int number, boolean realSwitchport, int configID);

	// Verejny metody na posilani paketu - komunikace s ostatnima vrstvama ------------------------------------------------

	/**
	 * Adds incoming packet from cabel to the buffer. Sychronized via buffer. Wakes worker.
	 *
	 * @param packet to receive
	 * @param iface which receives packet
	 */
	public abstract void receivePacket(L2Packet packet, Switchport iface);

	/**
	 * Adds incoming packet from network module to the buffer and then try to send it via cabel. Sychronized via buffer.
	 * Wakes worker.
	 *
	 * @param packet to send via physical module
	 * @param iface through it will be send
	 */
	public abstract void sendPacket(L2Packet packet, int switchportNumber);

	// ruzny dulezity gettry rozhrani k ostatnim vrstvam (fasada): -----------------------------------------

	/**
	 * Returns numbers of switchports.
	 * Uses Network Module to explore network hardware afer start.
	 * @return
	 */
	public List<Integer> getNumbersOfPorts(){
		List<Integer> vratit = new LinkedList<>();
		for(Switchport swport: switchports.values()){
			vratit.add(swport.number);
		}
		return vratit;
	}

	/**
	 * Pro prikaz rnetconn, kterej se switchportama manipuluje. Nepouzivat v sitovym modulu.
	 * @return
	 */
	public Map<Integer, Switchport> getSwitchports() {
		return switchports;
	}

	public boolean isSwitchportConnected (int switchportNumber){
		Switchport swport = switchports.get(switchportNumber);
		if(swport==null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.PHYSICAL, "isSwitchportConnected false; protoze", swport);
			return false;
		}
		else return swport.isConnected();
	}



// Pomocny metody a zkratky: -------------------------------------------------------------------------------------------

	protected NetworkModule getNetMod(){
		return device.getNetworkModule();
	}


	public class BufferItem {

		L2Packet packet;
		Switchport switchport;

		public BufferItem(L2Packet packet, Switchport switchport) {
			this.packet = packet;
			this.switchport = switchport;
		}
	}
}
