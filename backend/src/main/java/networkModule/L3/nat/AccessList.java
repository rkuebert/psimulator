/*
 * created 19.3.2012
 */
package networkModule.L3.nat;

import dataStructures.ipAddresses.IPwithNetmask;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
/**
 * Trida reprezentujici jeden seznam-list.
 */
public class AccessList {

	public final IPwithNetmask ip;
	/**
	 * V podstate unikatni jmeno AccessListu.
	 */
	public final int number;

	public AccessList(IPwithNetmask ip, int cislo) {
		this.ip = ip;
		this.number = cislo;
	}
}