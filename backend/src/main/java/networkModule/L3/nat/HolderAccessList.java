/*
 * http://www.samuraj-cz.com/clanek/cisco-ios-8-access-control-list/
 */

package networkModule.L3.nat;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Datova struktura pro seznam Access-listu.
 * Kazdy access-list obsahuje jmeno (number 1-2699) a IPwithNetmask,
 * ktera definuje rozsah pristupnych IP adres.
 *
 * OK
 *
 * @author Stanislav Řehák
 */
public class HolderAccessList {

    private final List<AccessList> list = new ArrayList<>(); // nechat jako List, mapa to byti nemuze!

	public List<AccessList> getList() {
		return list;
	}

    /**
     * Prida do seznamu Access-listu na spravnou pozici dalsi pravidlo. <br />
     * Je to razeny dle number access-listu.
     * Pocitam s tim, ze ani jedno neni null.
     * @param adresa
     * @param number
     */
    public void addAccessList(IPwithNetmask adresa, int cislo) {
        for (AccessList zaznam : list) {
            if (zaznam.ip.equals(adresa)) { // kdyz uz tam je, tak nic nedelat
                return;
            }
        }
        int index = 0;
        for (AccessList access : list) {
            if (cislo < access.number) {
                break;
            }
            index++;
        }
        list.add(index, new AccessList(adresa, cislo));
    }

    /**
     * Smaze vsechny seznam-listy s danym cislem.
     * @param number
     */
    public void deleteAccessList(int cislo) {
        List<AccessList> smazat = new ArrayList<>();
        for (AccessList zaznam : list) {
            if (zaznam.number == cislo) {
                smazat.add(zaznam);
            }
        }

		list.removeAll(smazat);
    }

    /**
     * Smaze vsechny access-listy
     */
    public void deleteAccessLists() {
        list.clear();
    }

    /**
     * Vrati prvni access-list do ktereho spada ip.
     * Kdyz zadny takovy nenajde, tak vrati null.
     * @param ip
     * @return
     */
    public AccessList getAccessList(IpAddress ip) {
        for (AccessList access : list) {
            if (access.ip.isInMyNetwork(ip)) { // TODO: zkontrolovat spravnou funkcnost!
                return access;
            }
        }
        return null;
    }
}
