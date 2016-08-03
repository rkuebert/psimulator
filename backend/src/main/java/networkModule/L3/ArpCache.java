/*
 * created 31.1.2012
 */
package networkModule.L3;

import dataStructures.MacAddress;
import dataStructures.ipAddresses.IpAddress;
import device.Device;
import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;

/**
 * Represents ARP cache table.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ArpCache implements Loggable {

	private final Device device;
	/**
	 * Valid record time in ms. <br /> If ARP annoucments on all devices will implement it this number can be much
	 * bigger.
	 */
	private int validRecordTime = 20_000;
	/**
	 * HashMap of records. <br/>
	 * Key - Target <br />
	 * Value - ArpRecord
	 */
	private final Map<Target, ArpRecord> cache = Collections.synchronizedMap(new HashMap<Target, ArpRecord>());

	public ArpCache(Device device) {
		this.device = device;
	}

	public class ArpRecord {

		/**
		 * Timestamp in ms.
		 */
		public final long timeStamp;
		public final MacAddress mac;

		public ArpRecord(MacAddress mac) {
			this.timeStamp = System.currentTimeMillis();
			this.mac = mac;
		}
	}

	public class Target {

		public final IpAddress address;
		public final EthernetInterface iface;

		public Target(IpAddress address, EthernetInterface iface) {
			this.address = address;
			this.iface = iface;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Target other = (Target) obj;
			if (!Objects.equals(this.address, other.address)) {
				return false;
			}
			if (!Objects.equals(this.iface, other.iface)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 79 * hash + Objects.hashCode(this.address);
			hash = 79 * hash + Objects.hashCode(this.iface);
			return hash;
		}
	}

	/**
	 * For platform formatted output only.
	 *
	 * @return
	 */
	public Map<Target, ArpRecord> getCache() {
		return cache;
	}

	/**
	 * Returns MacAddres iff there is a record pair IpAddress with MacAddress AND timeout is not off. <br /> It removes
	 * ArpRecord and returns null, if timeout is off. <br /> Return null if there is no record with given IpAddress.
	 *
	 * @param ip
	 * @return
	 */
	public MacAddress getMacAdress(IpAddress ip, EthernetInterface iface) {
		Target t = new Target(ip, iface);

		ArpRecord record = getRecord(t);
		if (record == null) {
			return null;
		}

		return record.mac; // since MacAddress and IpAddress is never changed I don't have return copy of it
	}

	/**
	 * Updates ARP cache.
	 *
	 * @param ip key for update
	 * @param mac value
	 */
	public void updateArpCache(IpAddress ip, MacAddress mac, EthernetInterface iface) {
		ArpRecord record = new ArpRecord(mac);
		Target target = new Target(ip, iface);
		Logger.log(this, Logger.INFO, LoggingCategory.ARP_CACHE, "Updating ARP cache: IP: " + ip.toString() + " MAC: " + mac + " Interface: " + iface.name, null);
		cache.put(target, record);
	}

	/**
	 * Returns ArpRecord iff there is a record pair IpAddress <> MacAddress AND timeout is not off. It removes ArpRecord
	 * and returns null, if timeout is off. Return null if there is no record with given IpAddress.
	 *
	 * @param ip
	 * @return
	 */
	private ArpRecord getRecord(Target t) {
		ArpRecord record = cache.get(t);

		if (record == null) {
			return null;
		}

		long now = System.currentTimeMillis();
		long time = now - record.timeStamp;
		if (time > validRecordTime) {
			// cisco default is 14400s, here it has to be much smaller,
			// because when someone change his IP address a his neighbour begins to send packets to him, he should ask again
			// with ARP req
			Logger.log(this, Logger.INFO, LoggingCategory.ARP_CACHE, "Deleting old record for IP: " + t.address + " and MAC: " + record.mac + " out of date = " + (time - validRecordTime) + " ms.", null);
			cache.remove(t);
			return null;
		}

		return record;
	}

	/**
	 * Removes timed out records.
	 */
	public void checkArpRecords() {
		ArpRecord record;
		long now = System.currentTimeMillis();
		List<Target> delete = new ArrayList<>();

		synchronized (cache) {
			for (Target t : cache.keySet()) {
				record = cache.get(t);

				if (now - record.timeStamp > validRecordTime) {
					Logger.log(this, Logger.INFO, LoggingCategory.ARP_CACHE, "Deleting old record for IP: " + t.address + " and MAC: " + record.mac + " out of date = " + (now - validRecordTime) + " ms.", null);
					delete.add(t);
				}
			}
			for (Target target : delete) {
				cache.remove(target);
			}
		}
	}

	@Override
	public String toString() {
		String s = "";
		MacAddress mac;
		synchronized (cache) {
			Set<Target> set = cache.keySet();
			for (Target t : set) {
				mac = getMacAdress(t.address, t.iface);
				if (mac != null) {
					s += t + "\t" + mac + "\n";
				}
			}
		}
		return s;
	}

	@Override
	public String getDescription() {
		return device.getName() + " ArpCache";
	}
}
