/*
 * created 19.3.2012
 */
package networkModule.L3.nat;

import dataStructures.ipAddresses.IpAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Pool implements Comparable<Pool> {

	/**
	 * Jmeno poolu
	 */
	public final String name;
	/**
	 * Prirazeny poolName adres.
	 */
	List<IpAddress> pool;
	/**
	 * Ukazuje na dalsi volnou IpAdresu z poolu nebo null, kdyz uz neni volna.
	 */
	IpAddress pointer = null;
	public final int prefix;

	public Pool(String name, int prefix) {
		this.name = name;
		this.prefix = prefix;
		pool = new ArrayList<IpAddress>() {

			@Override
			public boolean add(IpAddress ip) {
				if (pool.isEmpty()) {
					pointer = ip;
				}
				return super.add(ip);
			}
		};
	}

	/**
	 * Vrati getFirst IpAdresu z poolu nebo null, kdyz je poolName prazdny.
	 *
	 * @return
	 */
	public IpAddress getFirst() {
		if (pool.isEmpty()) {
			return null;
		}
		return pool.get(0);
	}

	/**
	 * Vrati dalsi IpAdresu z poolu. Kdyz uz jsem na getLast, tak vracim null (DHU).
	 *
	 * @return
	 */
	private IpAddress getNext() {
		int n = -1;
		for (IpAddress ip : pool) {
			n++;
			if (ip.equals(pointer)) {
				break; // n = index ukazatele
			}
		}
		if (n + 1 == pool.size()) {
			return null;
		}
		return pool.get(n + 1);
	}

	/**
	 * Vrati dalsi IP z poolu nebo null, pokud uz neni dalsi IP.
	 *
	 * @param testovani true, kdyz se zjistuje, zda je jeste IP, nemenim pak pointer na volnou IP
	 * @return
	 */
	public IpAddress getIP(boolean testovani) {
		IpAddress vrat = pointer;
		if (testovani == false) {
			pointer = getNext();
		}
		return vrat;
	}

	/**
	 * Vrati getLast Ip
	 *
	 * @return
	 */
	public IpAddress getLast() {
		if (pool.size() <= 1) {
			return getFirst();
		}
		return pool.get(pool.size() - 1);
	}

	@Override
	public int compareTo(Pool o) {
		return name.compareTo(o.name);
	}
}
