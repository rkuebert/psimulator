/*
 * created 5.3.2012
 */

package networkModule.L3.nat;

import dataStructures.DropItem;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.IpPacket;
import dataStructures.packets.L4Packet;
import dataStructures.packets.L4Packet.L4PacketType;
import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import utils.Util;

/**
 * TODO: poresit generovani portu kvuli kolizim s poslouchajicima aplikacema.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatTable implements Loggable {

	private final IPLayer ipLayer;

	/**
	 * Dynamic records.
	 */
	List<Record> table = new ArrayList<>();
	/**
	 * Static rules.
	 */
	private List<StaticRule> staticRules = new ArrayList<>();
    /**
     * seznam poolu IP.
     */
    public HolderPoolList lPool;
    /**
     * seznam seznamAccess-listu
     * (= kdyz zdrojova IP patri do nejakeho seznamAccess-listu, tak se bude zrovna natovat)
     */
    public HolderAccessList lAccess;
    /**
     * seznam prirazenych poolu k access-listum
     */
    public HolderPoolAccess lPoolAccess;
    /**
     * Seznam soukromych (inside) rozhrani.
     */
    Map<String, NetworkInterface> inside = new HashMap<>();
    /**
     * Verejne (outside) rozhrani.
     */
    NetworkInterface outside; // = new HashMap<>(); // nevim, jestli potreva jich mit, pak to asi predelam
    private boolean isSetLinuxMasquarade = false;

	/**
	 * Dynamicke zaznamy starsi nez tato hodnota se smazou.
	 */
	private long natRecordLife = 10_000;
	private static int numberOfPorts = 36535;

	/**
	 * V teto prvotni implementaci nebudou vubec reseny kolize s portama aplikaci.
	 * TODO: generovat porty v zavislosti k IP adresam, takto se mohou brzy vycerpat..
	 */
	private Set<Integer> freePorts = new HashSet<>(numberOfPorts);

    public NatTable(IPLayer ipLayer) {
        this.ipLayer = ipLayer;
		lAccess = new HolderAccessList();
        lPoolAccess = new HolderPoolAccess();
		lPool = new HolderPoolList(this);

		for (int i = 1; i <= numberOfPorts; i++) { // naplnim si tabulku volnych portu
			freePorts.add(i);
		}
    }

	//--------------------------------------------- getters and setters ---------------------------------------------

	/**
	 * Returns NetworkAddressTranslation table.
	 * @return
	 */
	public List<Record> getDynamicRules() {
		return table;
	}

	/**
	 * Returns static rules.
	 * @return
	 */
	public List<StaticRule> getStaticRules() {
		return staticRules;
	}

	/**
     * Returns outside interface.
     * @return
     */
    public NetworkInterface getOutside() {
        return outside;
    }

    /**
     * Returns list of inside interfaces.
     * @return
     */
    public Collection<NetworkInterface> getInside() {
        return inside.values();
    }

	/**
     * Returns interface or null.
     * @return
     */
	public NetworkInterface getInside(String name) {
		return inside.get(name);
	}

//	/**
//	 * Returns sorted list of inside interfaces.
//	 * @return
//	 */
//	public List<NetworkInterface> getInsideSorted() {
//		List<NetworkInterface> ifaces = new ArrayList<>(inside.values());
//		Collections.sort(ifaces);
//		return ifaces;
//	}

	@Override
	public String getDescription() {
		return Util.zarovnej(ipLayer.getNetMod().getDevice().getName(), Util.deviceNameAlign)+ " " + "natTable";
	}

	//--------------------------------------------- forward translation ---------------------------------------------

	/**
	 * Executes forward translation of packet.
	 *
	 * @param packet to translate
	 * @param in incomming interface - can be null iff I am sending this packet
	 * @param out outgoing interface - never null
	 * @return
	 */
	public IpPacket translate(IpPacket packet, NetworkInterface in, NetworkInterface out) {

		/*
		 * Nenatuje se kdyz:
		 * 1 - nemam pool
		 *		+ Destination Host Unreachable
		 * 2 - dosly IP adresy z poolu
		 *		+ Destination Host Unreachable
		 * 3 - vstupni neni soukrome nebo vystupni neni verejne
		 * 4 - zdrojova IP neni v seznamu access-listu, tak nechat normalne projit bez natovani
		 * 5 - neni nastaveno outside rozhrani
		 *
		 * Jinak se natuje.
		 */

		if (packet.data == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation,
					"No NAT translation: packet with no L4 data received. Could not gain port number!?", packet);
						// -> Standa to logoval jako warning, ale vzhledem k napojeni na realnou sit se to muze stavat pomerne casto
			return packet;
		}

		boolean vstupniJeInside = false;
		NetworkInterface insideTemp = inside.get(in == null ? null : in.name);
		if (insideTemp != null) {
			vstupniJeInside = true;
		}

		if (outside == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT translation: no outside interface is set.", null);
			return packet; // 5
		}

		if (!out.name.equals(outside.name) || !vstupniJeInside) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT translation: incomming interace is not inside or outgoing interface is not outside.", null);
			return packet; // 3
		}

		IpAddress srcTranslated = findStaticRuleIn(packet.src);
        if (srcTranslated != null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "NAT translation: static rule found.", null);
			return staticTranslation(packet, srcTranslated); // 0
        }

        // neni v access-listech, tak se nanatuje
        AccessList acc = lAccess.getAccessList(packet.src);
        if (acc == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT translation: source address is not in access-lists.", null);
            return packet; // 4
        }

        // je v access-listech, ale neni prirazen pool, vrat DHU
        Pool pool = lPool.getPool(acc);
        if (pool == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, "No NAT translation + sending DHU: source address is in access-lists, but no pool is assigned.", null);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, ipLayer.getNetMod().getDevice().configID));
			// poslat DHU
			ipLayer.getIcmpHandler().sendHostUnreachable(packet.src, null);
            return null; // 1
        }

        IpAddress adr = pool.getIP(true);
        if (adr == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, "No NAT translation + sending DHU: no free IP is available for translation.", null);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, ipLayer.getNetMod().getDevice().configID));
			ipLayer.getIcmpHandler().sendHostUnreachable(packet.src, null);
            return null; // 2
        }

        return dynamicTranslation(packet);
	}

    /**
     * Hleda mezi statickymi pravidly, jestli tam je zaznam pro danou IP.
     * @param zdroj
     * @return zanatovana IP <br />
     *         null pokud nic nenaslo
     */
    private IpAddress findStaticRuleIn(IpAddress zdroj) {
        for (StaticRule rule : staticRules) {
            if (rule.in.equals(zdroj)) {
                return rule.out;
            }
        }
        return null;
    }

	/**
	 * Translates packet with static NetworkAddressTranslation rule.
	 * @param packet to translate
	 * @param srcTranslated new source IP
	 * @return
	 */
	private IpPacket staticTranslation(IpPacket packet, IpAddress srcTranslated) {
		logNatOperation(packet, true, true);
		IpPacket p = new IpPacket(srcTranslated, packet.dst, packet.ttl, packet.data); // port se tu nemeni (je v packet.data)
		logNatOperation(p, true, false);
		return p;
	}

	private void logNatOperation(IpPacket packet, boolean natting, boolean before) {
		String op;
		if (natting) {
			op = "Forward translation ";
		} else {
			op = "Backward translation";
		}

		String when;
		if (before) {
			when = "before";
		} else {
			when = " after";
		}

		Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, String.format(op+": "+when+": "+"src: %s:%d dst: %s:%d",
						packet.src.toString(), packet.data.getPortSrc(), packet.dst.toString(), packet.data.getPortDst()), packet);
	}

	/**
	* Translates packet with dynamic NetworkAddressTranslation. <br />
	* Returns untranslated packet iff there are now free ports number or if packet has no L4 data = without port number.
	* @param packet
	* @return
	*/
	private IpPacket dynamicTranslation(IpPacket packet) {
		deleteOldDynamicRecords();

		InnerRecord tempRecord = generateInnerRecordForSrc(packet);
		if (tempRecord == null) {
			return packet;
		}

		if (packet.data == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT translation: Packet has no L4 data.", packet);
			return packet;
		}

		// projdu aktualni dynamicke zaznamy a jestli uz tam je takovy preklad, tak mu prodlouzim zivot a necham se prelozit
		for (Record record : table) {
			if (record.in.equals(tempRecord)) {
				logNatOperation(packet, true, true);

				L4Packet dataNew = packet.data.getCopyWithDifferentSrcPort(record.out.port); // zmena portu zde

				IpPacket p = new IpPacket(record.out.address, packet.dst, packet.ttl, dataNew);
				logNatOperation(p, true, false);
				record.touch();
				return p;
			}
		}

		// nenasel se stary, tak vygenerujeme novy
		AccessList access = lAccess.getAccessList(packet.src);
		Pool pool = lPool.getPool(access);
        IpAddress srcIpNew = lPool.getIpFromPool(pool);

		Integer srcPortNew;
		try {
			srcPortNew = freePorts.iterator().next();
		} catch (NoSuchElementException e) {
			Logger.log(this, Logger.WARNING, LoggingCategory.NetworkAddressTranslation, "There is no free port available for translation! Returning unchanged packet.", packet);
			return packet;
		}
		freePorts.remove(srcPortNew); // port je obsazen

		InnerRecord newDynamic = new InnerRecord(srcIpNew, srcPortNew, tempRecord.protocol);
		Record r = new Record(tempRecord, newDynamic, packet.dst);

		Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "New dynamic record created: ", r);
		table.add(r);

		return getTranslatedPacket(packet, srcIpNew, srcPortNew);
	}

	/**
	 * Vrati kopii IpPacketu s novou zdrojovou adresou a novym portem
	 *
	 * @param packet old packet
	 * @param srcIpNew source IP of new packet
	 * @param srcPortNew source port of new packet
	 * @return
	 */
	private IpPacket getTranslatedPacket(IpPacket packet, IpAddress srcIpNew, int srcPortNew) {
		L4Packet data = packet.data.getCopyWithDifferentSrcPort(srcPortNew);

		logNatOperation(packet, true, true);

		IpPacket translated = new IpPacket(srcIpNew, packet.dst, packet.ttl, data);

		logNatOperation(translated, true, false);
		return translated;
	}

	private InnerRecord generateInnerRecordForSrc(IpPacket packet) {
		L4PacketType type;
		int port;

		if (packet.data != null) {
			type = packet.data.getType();
			port = packet.data.getPortSrc();
		} else {
			Logger.log(this, Logger.WARNING, LoggingCategory.NetworkAddressTranslation, "generateInnerRecordForSrc: packet with L4 data == null, returning null!", packet);
			return null;
		}

		return new InnerRecord(packet.src, port, type);
	}

	//--------------------------------------------- backward translation ---------------------------------------------

	/**
	 * Executes backward translation of packet.
	 * @param packet to translate
	 * @param in incomming interface
	 * @return
	 */
	public IpPacket backwardTranslate(IpPacket packet, NetworkInterface in) {
		if (in == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT translation: incomming iface is null.", packet);
			return packet;
		}

		if (outside == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT translation: outside is null.", packet);
			return packet;
		}
		if (packet.data == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT translation: Packet has no L4 data.", packet);
			return packet;
		}
		if (outside.name.equals(in.name)) {
			return doBackwardTranslation(packet);
		}
		Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT translation: incomming iface is: "+in.name+", but outside is: "+outside.name, packet);
		return packet;
	}

	private IpPacket doBackwardTranslation(IpPacket packet) {
		deleteOldDynamicRecords();

		// 1) projit staticka pravidla, pokud tam bude sedet packet.dst s record.out.address, tak se vytvori novy a vrati se
		for (StaticRule rule : staticRules) {
			if (rule.out.equals(packet.dst)) {
				logNatOperation(packet, false, true);

				IpPacket translated = new IpPacket(packet.src, rule.in, packet.ttl, packet.data); // port se tu nemeni (je v packet.data)

				logNatOperation(translated, false, false);
				return translated;
			}
		}

		// 2) projit dynamicka pravidla, tam musi sedet IP+port // TODO: NatTable: pridat protokol
		for (Record record : table) {
			if (record.out.address.equals(packet.dst) && record.out.port == packet.data.getPortDst()) {

				logNatOperation(packet, false, true);

				L4Packet dataNew = packet.data.getCopyWithDifferentDstPort(record.in.port); // zmena portu zde
				IpPacket translated = new IpPacket(packet.src, record.in.address, packet.ttl, dataNew);

				logNatOperation(translated, false, false);

				return translated;
			}
		}

		Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "No NAT backward translation: no record available for operation.", packet);
		return packet;
	}

	//--------------------------------------------- cisco stuff ---------------------------------------------

	/**
     * Vrati pozici pro pridani do tabulky.
     * Radi se to dle out adresy vzestupne.
     * @param out
     * @return index noveho zaznamu
     */
    private int getIndexForDynamicTable(IpAddress out) {
        int index = 0;
        for (Record zaznam : table) {
            if (out.getLongRepresentation() < zaznam.out.address.getLongRepresentation()) {
                break;
            }
            index++;
        }
        return index;
    }

	/**
     * Vrati pozici pro pridani do tabulky.
     * Radi se to dle out adresy vzestupne.
     * @param out
     * @return index noveho zaznamu
     */
    private int getIndexForStaticTable(IpAddress out) {
        int index = 0;
        for (StaticRule rule : staticRules) {
            if (out.getLongRepresentation() < rule.out.getLongRepresentation()) {
                break;
            }
            index++;
        }
        return index;
    }

    /**
     * Smaze stare (starsi nez natRecordLife [s]) dynamicke zaznamy v tabulce.
     */
    public void deleteOldDynamicRecords() {
        long now = System.currentTimeMillis();
        List<Record> delete = new ArrayList<>();
        for (Record record : table) {
			if (now - record.getTimestamp() > natRecordLife) {
				freePorts.add(record.out.port);
				delete.add(record);
			}
		}

		table.removeAll(delete);
    }

	//--------------------------------------------- functions for static rules ---------------------------------------------

	/**
     * Prida isStatic pravidlo do tabulky.
     * Razeno vzestupne dle out adresy.
     * @param in zdrojova IP urcena pro preklad
     * @param out nova (prelozena) adresa
     * @return 0 - ok, zaznam uspesne pridan <br />
     *         1 - chyba, in adresa tam uz je (% in already mapped (in -> out)) <br />
     *         2 - chyba, out adresa tam uz je (% similar static entry (in -> out) already exists)
     */
    public int addStaticRuleCisco(IpAddress in, IpAddress out) {

		for (StaticRule rule : staticRules) {
			if (rule.in.equals(in)) {
				return 1;
			}
			if (rule.out.equals(out)) {
				return 2;
			}
        }

        int index = getIndexForStaticTable(out);
        staticRules.add(index, new StaticRule(in, out));
        return 0;
    }

	 /**
     * Smaze vsechny isStatic zaznamy, ktere maji odpovidajici in a out.
     * Dale aktualizuje outside rozhrani co se IP tyce. Nejdrive smaze vsechny krom getFirst,
     * a pak postupne prida ze statickych a pak i z poolu.
     * @return 0 - alespon 1 zaznam se smazal <br />
     *         1 - nic se nesmazalo, pac nebyl nalezen odpovidajici zaznam (% Translation not found)
     */
    public int deleteStaticRule(IpAddress in, IpAddress out) {

        List<StaticRule> smaznout = new ArrayList<>();
        for (StaticRule zaznam : staticRules) {
            if (in.equals(zaznam.in) && out.equals(zaznam.out)) {
                smaznout.add(zaznam);
            }
        }

        if (smaznout.isEmpty()) {
            return 1;
        }

		staticRules.removeAll(smaznout);
        return 0;
    }

    /****************************************** nastavovani rozhrani ***************************************************/
    /**
     * Prida inside rozhrani. <br />
     * Neprida se pokud uz tam je rozhrani se stejnym jmenem. <br />
     * Pro pouziti prikazu 'address nat inside'.
     * @param iface
     */
    public void addInside(NetworkInterface iface) {
		if (!inside.containsKey(iface.name)) { // nepridavam uz pridane
			inside.put(iface.name, iface);
		}
    }

    /**
     * Nastavi outside rozhrani.
     * @param iface
     */
    public void setOutside(NetworkInterface iface) {
        outside = iface;
    }

    /**
     * Smaze toto rozhrani z inside listu.
     * Kdyz to rozhrani neni v inside, tak se nestane nic.
     * @param iface
     */
    public void deleteInside(NetworkInterface iface) {
		inside.remove(iface.name);
    }

    /**
     * Smaze vsechny inside rozhrani.
     */
    public void deleteInsideAll() {
        inside.clear();
    }

    /**
     * Smaze outside rozhrani.
     */
    public void deleteOutside() {
        outside = null;
    }

    //--------------------------------------------- debug stuff ---------------------------------------------

    /**
     * Pomocny servisni vypis.
     * Nejdriv se smazou stare dynamcike zaznamy.
     * @return
     */
    public String getDynamicRulesInUse() {
        deleteOldDynamicRecords();
        String s = "";

        for (Record zaznam : table) {
			s += zaznam.in.getAddressWithPort() + "\t" + zaznam.out.getAddressWithPort() + "\n";
		}
        return s;
    }

    //--------------------------------------------- linux stuff ---------------------------------------------

    /**
     * Nastavi Linux pocitac pro natovani. Kdyz uz je nastavena, nic nedela.
     * Pocitam s tim, ze ani pc ani rozhrani neni null.
     * Jestli jsem to dobre pochopil, tak tohle je ten zpusob natovani, kdy se vsechny pakety jdouci
     * ven po nejakym rozhrani prekladaj na nejakou verejnou adresu, a z toho rozhrani zase zpatky.
     * Prikaz napr: "iptables -t nat -I POSTROUTING -o eth2 -j MASQUERADE" - vsechny pakety jdouci ven
     * po rozhrani eth2 se prekladaj.
     * @param pc
     * @param outside, urci ze je tohle rozhrani outside a ostatni jsou automaticky soukroma.
     */
    public void setLinuxMasquarade(NetworkInterface verejne) {

        if (isSetLinuxMasquarade) {
            return;
        }

        // nastaveni rozhrani
        inside.clear();
        for (NetworkInterface iface : ipLayer.getNetworkIfaces()) {
            if (iface.name.equals(verejne.name)) {
                continue; // preskakuju verejny
            }
            // vsechny ostatni nastrkam do inside
            addInside(iface);
        }
        setOutside(verejne);

        // osefovani access-listu
        int cislo = 1;
        lAccess.deleteAccessLists();
        lAccess.addAccessList(new IPwithNetmask("0.0.0.0", 0), cislo);

        // osefovani IP poolu
        String pool = "ovrld";
        lPool.deletePools();
        lPool.addPool(verejne.getIpAddress().getIp(), verejne.getIpAddress().getIp(), 24, pool);

        lPoolAccess.deletePoolAccesses();
        lPoolAccess.addPoolAccess(cislo, pool, true);

        isSetLinuxMasquarade = true;
    }

    /**
     * Zrusi linux DNAT. Kdyz neni nastavena, nic nedela.
     */
    public void cancelLinuxMasquerade() {

        lAccess.deleteAccessLists();
        lPool.deletePools();
        lPoolAccess.deletePoolAccesses();
        deleteOutside();
        deleteInsideAll();
        isSetLinuxMasquarade = false;
    }

    public boolean isSetLinuxMasquarade() {
        return isSetLinuxMasquarade;
    }

    /**
     * Nastavi promennou na true.
     */
    public void setLinuxMasquaradeFromConfigOnTrue() {
        isSetLinuxMasquarade = true;
    }

    /**
     * Prida staticke pravidlo do NetworkAddressTranslation tabulky. Nic se nekontroluje.
     * @param in zdrojova IP
     * @param out nova zdrojova (prelozena)
     */
    public void addStaticRuleLinux(IpAddress in, IpAddress out) {
		staticRules.add(new StaticRule(in, out));
    }

	//--------------------------------------------- classes ---------------------------------------------

	/**
     * Reprezentuje jeden radek v NetworkAddressTranslation tabulce.
     */
    public class Record {

        /**
         * Zdrojova address.
         */
        public final InnerRecord in;
        /**
         * Zdrojova prelozena address.
         */
        public final InnerRecord out;
        /**
         * Potreba pro vypis v ciscu.
         */
        public final IpAddress target;
        /**
         * Cas vlozeni v ms (pocet ms od January 1, 1970)
         */
        private long timestamp;

        public Record(InnerRecord in, InnerRecord out, IpAddress target) {
            this.in = in;
            this.out = out;
            this.target = target;
            this.timestamp = System.currentTimeMillis();
        }

        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Obnovi zaznam na dalsich natRecordLife sekund.
         */
        public void touch() {
            this.timestamp = System.currentTimeMillis();
        }

		@Override
		public String toString() {
			return in.address.toString()+":"+in.port+" "+in.protocol+" => "+out.address.toString()+":"+out.port+" "+out.protocol;
		}
    }

	public class InnerRecord {
		public final IpAddress address;
		public final int port;
		public final L4PacketType protocol;

		public InnerRecord(IpAddress ip, int port, L4PacketType protocol) {
			this.address = ip;
			this.port = port;
			this.protocol = protocol;
		}

		public String getAddressWithPort() {
			return address + ":" + port;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final InnerRecord other = (InnerRecord) obj;
			if (!Objects.equals(this.address, other.address)) {
				return false;
			}
			if (this.port != other.port) {
				return false;
			}
			if (this.protocol != other.protocol) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 53 * hash + Objects.hashCode(this.address);
			hash = 53 * hash + this.port;
			hash = 53 * hash + (this.protocol != null ? this.protocol.hashCode() : 0);
			return hash;
		}
	}

	public class StaticRule {
		public final IpAddress in;
		public final IpAddress out;

		public StaticRule(IpAddress in, IpAddress out) {
			this.in = in;
			this.out = out;
		}

		@Override
		public String toString() {
			return "in: " + in + " out: " + out;
		}
	}
}
