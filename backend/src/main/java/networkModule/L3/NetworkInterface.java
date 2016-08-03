/*
 * Erstellt am 22.2.2012.
 */
package networkModule.L3;

import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import java.util.Objects;
import networkModule.L2.EthernetInterface;

/**
 * Representation of network interface in 3th layer in Network Module.
 * Representuje prakticky jen nastaveni interfacu, samotna trida nic nedela.
 * @author neiss
 */
public class NetworkInterface implements Comparable<NetworkInterface> {

	public final int configID;

	public final String name;
	/**
	 * Interface is Up.
	 * default behavior on linux: true
	 * default behavior on cisco: false
	 */
	public boolean isUp = true;
	/**
	 * Je naschval protected! Nekdy v budoucnu mozna budeme chtit pridat nejake akce, kdyz se zmeni IP..
	 * IP na rozhrani se meni pred IPLayer a metodu changeIpAddressOnInterface().
	 */
	protected IPwithNetmask ipAddress;
	public final EthernetInterface ethernetInterface;

	/**
	 * Only to create temporary interface in commands and tests.
	 * @param configID
	 * @param name
	 * @param iface
	 */
	public NetworkInterface(Integer configID, String name, EthernetInterface iface) {
		this.configID = configID;
		this.name = name;
		this.ethernetInterface = iface;
	}

	public NetworkInterface(Integer configID, String name, IPwithNetmask ipAddress, EthernetInterface ethernetInterface, boolean isUp) {
		this.configID = configID;
		this.name = name;
		this.ipAddress = ipAddress;
		this.ethernetInterface = ethernetInterface;
		this.isUp = isUp;
	}

	/**
	 * Getter for IP address with mask.
	 * Setter is not available. Set IP address on IPLayer with method setIpAddressOnInterface()
	 * @return
	 */
	public IPwithNetmask getIpAddress() {
		return ipAddress;
	}

	/**
	 * Jen zkratka.
	 * @return
	 */
	public MacAddress getMacAddress() {
		return ethernetInterface.getMac();
	}

	/**
	 * Aby se to dalo radit podle jmena interface.
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(NetworkInterface o) {
		return name.compareTo(o.name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NetworkInterface other = (NetworkInterface) obj;
		if (this.configID != other.configID) {
			return false;
		}
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 61 * hash + this.configID;
		hash = 61 * hash + Objects.hashCode(this.name);
		return hash;
	}
}
