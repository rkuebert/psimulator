/*
 * Erstellt am 26.10.2011.
 */

package networkModule;

import device.Device;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.IPLayer;
import networkModule.L3.LinuxIPLayer;
import networkModule.L4.TransportLayer;

/**
 * Síťový modul pro počítač, tedy včetně rozhraní pro aplikace.
 * Dedi od SwitchNetworkModule, tedy se v tyhle tride resi spis komunikace nahoru.
 * @author neiss
 */
public class IpNetworkModule extends SwitchNetworkModule {


	public final IPLayer ipLayer;
	public final TransportLayer transportLayer;

	/**
	 * Konstruktor sitovyho modulu.
	 * Predpoklada uz hotovej pocitac a fysickej modul, protoze zkouma jeho nastaveni.
	 * @param device
	 */
	public IpNetworkModule(Device device) {
		super(device);
		switch (device.type) {
			case cisco_router:
				this.ipLayer = new CiscoIPLayer(this);
				break;
			case linux_computer:
				this.ipLayer = new LinuxIPLayer(this);
				break;
			default:
				Logger.log(this, Logger.ERROR, LoggingCategory.GENERIC, "Vytvari se TcpIpNetMod pro neco jinyho nez cisco_router a linux_computer!", device.type);
				this.ipLayer = null;
		}
		this.transportLayer = new TransportLayer(this);
	}

    //tady budou muset bejt metody pro posilani dat a pro registraci aplikaci, tedy komunikaci s aplikacema



	@Override
	public final boolean isSwitch() {	// final, nejde uz dal prepisovat
		return false;
	}
}
