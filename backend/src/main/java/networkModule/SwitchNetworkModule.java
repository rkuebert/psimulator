/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import dataStructures.DropItem;
import dataStructures.packets.EthernetPacket;
import dataStructures.packets.L2Packet;
import device.Device;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetLayer;

/**
 * Implementation of network module of generic simple switch.
 * Predpoklada protokol ethernet na vsech rozhranich, ostatni pakety zahazuje.
 * @author neiss
 */
public class SwitchNetworkModule extends NetworkModule  implements Loggable{

	public final EthernetLayer ethernetLayer;

	/**
	 * Konstruktor sitovyho modulu predpoklada uz hotovej fysickej modul, protoze zkouma jeho nastaveni.
	 * @param device
	 */
    public SwitchNetworkModule(Device device) {
		super(device);
		ethernetLayer = new EthernetLayer(this);
	}

	/**
	 * Prijimani od fysickyho modulu.
	 * @param packet
	 * @param switchportNumber
	 */
	@Override
	public void receivePacket(L2Packet packet, int switchportNumber) {
		if (packet.getClass() != EthernetPacket.class) {	//kontrola spravnosti paketu
			Logger.log(getDescription(), Logger.WARNING, LoggingCategory.ETHERNET_LAYER,
					"Dropping packet: It is not Ethernet packet, it is " + packet.getClass().getName());
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getDevice().configID));
		} else {
			ethernetLayer.receivePacket((EthernetPacket)packet, switchportNumber);
		}
	}

	@Override
	public String getDescription() {
		return device.getName()+": "+getClass().getName();
	}

	@Override
	public boolean isSwitch() {
		return true;
	}
}
