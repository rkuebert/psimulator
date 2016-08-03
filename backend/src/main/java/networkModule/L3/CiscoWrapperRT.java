/*
 * created 13.3.2012
 */

package networkModule.L3;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import device.Device;
import java.util.ArrayList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.RoutingTable.Record;
import utils.Util;

/**
 * Trida reprezentujici wrapper nad routovaci tabulkou pro system cisco.
 * Tez bude sefovat zmenu v RT dle vlastnich iface.
 * Cisco samo o sobe ma tez 2 tabulky: <br />
 *      1. zadane uzivatelem (tato trida) <br />
 *      2. vypocitane routy z tabulky c. 1 (trida RoutovaciTabulka)
 * @author Stanislav Řehák <rehaksta@fit.cvut.cz>
 */
public class CiscoWrapperRT implements Loggable {

	/**
     * Jednotlive radky wrapperu.
     */
    private List<CiscoRecord> records;
	private final IPLayer ipLayer;
	private final Device device;
    /**
     * Odkaz na routovaci tabulku, ktera je wrapperem ovladana.
     */
    private RoutingTable routingTable;
    /**
     * ochrana proti smyckam v routovaci tabulce.
     * Kdyz to projede 50 rout, tak se hledani zastavi s tim, ze smula..
     */
    private int counter = 0;
    private boolean debug = false;

    public CiscoWrapperRT(Device device, IPLayer ipLayer) {
        records = new ArrayList<>();
		this.ipLayer = ipLayer;
        this.routingTable = ipLayer.routingTable;
		this.device = device;
    }

    /**
     * Vnitrni trida pro reprezentaci CiscoZaznamu ve wrapperu.
     * Adresat neni null, ale bud iface nebo brana je vzdy null.
     */
    public class CiscoRecord {

        private IPwithNetmask target; // with mask
        private IpAddress gateway;
        private NetworkInterface iface;
        private boolean connected = false;

        private CiscoRecord(IPwithNetmask target, IpAddress gateway) {
            this.target = target;
            this.gateway = gateway;
        }

        private CiscoRecord(IPwithNetmask target, NetworkInterface iface) {
            this.target = target;
            this.iface = iface;
        }

        /**
         * Pouze pro ucely vypisu RT!!! Jinak nepouzivat!
         * @param target
         * @param gateway
         * @param iface
         */
        private CiscoRecord(IPwithNetmask target, IpAddress gateway, NetworkInterface iface) {
            this.target = target;
            this.gateway = gateway;
            this.iface = iface;
        }

        public IPwithNetmask getTarget() {
            return target;
        }

        public IpAddress getGateway() {
            return gateway;
        }

        public NetworkInterface getInterface() {
            return iface;
        }

        private void setConnected() {
            this.connected = true;
        }

        public boolean isConnected() {
            return connected;
        }

        @Override
        public String toString() {
            String s = target.getIp() + " " + target.getMask() + " ";
            if (gateway == null) {
                s += iface.name;
            } else {
                s += gateway;
            }
            return s;
        }

        /**
         * CiscoZaznamy se rovnaji pokud adresat ma stejnou adresu i masku &&
         * ( se takto rovnaji i brany ) || ( iface se jmenuji stejne nehlede na velikost pismen )
         * @param obj
         * @return
         */
        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != CiscoRecord.class) {
                return false;
            }

            if (target.equals(((CiscoRecord) obj).target)) {
                if (gateway != null && ((CiscoRecord) obj).gateway != null) {
                    if (gateway.equals(((CiscoRecord) obj).gateway)) {
                        return true;
                    }
                } else if (iface != null && ((CiscoRecord) obj).iface != null) {
                    if (iface.name.equalsIgnoreCase(((CiscoRecord) obj).iface.name)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.target != null ? this.target.hashCode() : 0);
            hash = 37 * hash + (this.gateway != null ? this.gateway.hashCode() : 0);
            hash = 37 * hash + (this.iface != null ? this.iface.hashCode() : 0);
            return hash;
        }
    }

    /**
     * Tato metoda bude aktualizovat RoutovaciTabulku dle tohoto wrapperu.
     */
    public void update() {
		Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "update RT, pocet static zaznamu: "+records.size(), null);

        // smazu RT
        routingTable.flushAllRecords();
		Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "update, mazu RT.", null);

        // nastavuju counter
        this.counter = 0;

        // pridam routy na nahozena iface
        for (NetworkInterface iface : ipLayer.getNetworkIfaces()) {
            if (iface.isUp && iface.getIpAddress() != null && iface.ethernetInterface.isConnected()) {
				Logger.log(this, Logger.INFO, LoggingCategory.WRAPPER_CISCO, "Adding route from online interface to routing table: "+iface.getIpAddress(), null);
                routingTable.addRecord(iface.getIpAddress().getNetworkNumber(), iface, true);
            }
        }

        // propocitam a pridam routy s prirazenyma rozhranima
        for (CiscoRecord record : records) {
            if (record.iface != null) { // kdyz to je na iface
                if (record.iface.isUp) {
					Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "Pridavam zaznam na rozhrani.", record);
                    routingTable.addRecord(record.target, record.iface);
                }
            } else { // kdyz to je na branu
                NetworkInterface odeslat = findInterfaceForGateway(record.gateway);
                if (odeslat != null) {
                    if (odeslat.isUp) {
						Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "nasel jsem pro "+record.target.toString() + " rozhrani "+odeslat.name, null);
                        routingTable.addRecordWithoutControl(record.target, record.gateway, odeslat);
                    } else {
						Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "nasel jsem pro "+record.target.toString() + " rozhrani "+odeslat.name+", ale je zhozene!!!", null);
					}
                } else {
					Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "Nenasel jsem pro tento zaznam zadne rozhrani, po kterem by to mohlo odejit..", record);
//                    System.out.println("nenasel jsem pro "+ zaznam);
                }
            }
        }
    }

    /**
     * Vrati iface, na ktere se ma odesilat, kdyz je zaznam na branu.
     * Tato metoda pocita s tim, ze v RT uz jsou zaznamy pro nahozena iface.
     * @param gateway
     * @return kdyz nelze nalezt zadne iface, tak vrati null
     */
    NetworkInterface findInterfaceForGateway(IpAddress gateway) {

        counter++;
        if (counter >= 101) {
            return null; // ochrana proti smyckam
        }
        for (int i = records.size() - 1; i >= 0; i--) { // prochazim opacne (tedy vybiram s nejvyssim poctem jednicek)

            // kdyz to je na rozsah vlastniho iface
			Record record = routingTable.findRoute(gateway);
            if (record != null && record.iface != null) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "najdiRozhraniProBranu: nalezeno rozhrani.. ok", gateway);
                return record.iface;
            } else {
				Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "najdiRozhraniProBranu: NEnalezeno rozhrani pro "+gateway, null);
			}

            // kdyz to je na branu jako v retezu
            CiscoRecord rec = records.get(i);
            if (rec.target.isInMyNetwork(gateway)) {
                if (rec.iface != null) { // 172.18.1.0 255.255.255.0 FastEthernet0/0
                    return rec.iface;
                }
                return findInterfaceForGateway(rec.gateway);
            }
        }
        return null;
    }

    /**
     * Pridava do wrapperu novou routu na branu.
     * @param address
     * @param gateway
     */
    public void addRecord(IPwithNetmask address, IpAddress gateway) {
        CiscoRecord z = new CiscoRecord(address, gateway);
        addRecord(z);
    }

    /**
     * Pridava do wrapperu novou routu na iface.
     * @param address
     * @param iface
     */
    public void addRecord(IPwithNetmask address, NetworkInterface iface) {
        CiscoRecord z = new CiscoRecord(address, iface);
        addRecord(z);
    }

    /**
     * Prida do wrapperu novou routu na iface. Pote updatuje RT je-li potreba.
     * V teto metode se kontroluje, zda adresat je cislem site.
     * @param zaznam, ktery chci vlozit
     */
    private void addRecord(CiscoRecord record) {

        if (!record.getTarget().isNetworkNumber()) { // vyjimka pro nacitani z konfiguraku, jinak to je osetreno v parserech
//            throw new RuntimeException("Adresa " + zaznam.getTarget().getIp() + " neni cislem site!");
			Logger.log(this, Logger.WARNING, LoggingCategory.WRAPPER_CISCO, "Address " + record.getTarget().getIp() + " is not network number! Skipping..", record);
			return;
        }

        for (CiscoRecord z : records) { // zaznamy ulozene v tabulce se uz znovu nepridavaji
            if (record.equals(z)) {
                return;
            }
        }

        records.add(getPositionIndex(record, true), record);
        update();
    }

    /**
     * Malinko prasacka metoda pro pridani zaznamu do RT pouze pro vypis!
     * @param record
     */
    private void addRecordForOutputOnly(Record record) {
        CiscoRecord ciscoRecord = new CiscoRecord(record.adresat, record.brana, record.iface);
        if (record.jePrimoPripojene()) {
            ciscoRecord.setConnected();
        }
        records.add(getPositionIndex(ciscoRecord, false), ciscoRecord);
    }

    /**
     * Smaze zaznam z wrapperu + aktualizuje RT. Rozhrani maze podle jmena!
     * Muze byt zadana bud adresa nebo adresa+brana nebo adresa+iface.
     *
     * no ip route IP MASKA DALSI? <br />
     * IP a MASKA je povinne, DALSI := { ROZHRANI | BRANA } <br />
     *
     * @param address
     * @param gateway
     * @param iface
     * @return 0 = ok, 1 = nic se nesmazalo
     */
    public int deleteRecord(IPwithNetmask address, IpAddress gateway, NetworkInterface iface) {
        int i = -1;

        if (address == null) {
            return 1;
        }
        if (gateway != null && iface != null) {
            return 1;
        }

        // maze se zde pres specialni seznam, inac to hazi concurrent neco vyjimku..
        List<CiscoRecord> delete = new ArrayList();

        for (CiscoRecord z : records) {
            i++;

            if (!z.target.equals(address)) {
                continue;
            }

            if (gateway == null && iface == null) {
                delete.add(records.get(i));
            } else if (gateway != null && iface == null && z.gateway != null) {
                if (z.gateway.equals(gateway)) {
                    delete.add(records.get(i));
                }
            } else if (gateway == null && iface != null) {
                if (z.iface.name.equals(iface.name)) {
                    delete.add(records.get(i));
                }
            }
        }

        if (delete.isEmpty()) {
            return 1;
        }

		records.removeAll(delete);

        update();

        return 0;
    }

    /**
     * Smaze vsechny zaznamy ve wrapperu + zaktualizuje RT
     * Prikaz 'clear ip route *'
     */
    public void deleteAllRecords() {
        records.clear();
        update();
    }

    /**
     * Vrati pozici, na kterou se bude pridavat zaznam do wrapperu.
     * Je to razeny dle integeru cile.
     * @param pridavany, zaznam, ktery chceme pridat
     * @param nejminBituVMasce rika, jestli chceme radit nejdrive zaznamy maskou o mensim poctu 1,
     * pouziva se pri normalnim vkladani do wrapperu, false pro vypis RT
     * @return
     */
    private int getPositionIndex(CiscoRecord record, boolean nejminBituVMasce) {
        int i = 0;
        for (CiscoRecord rec : records) {
            if (isLessIp(record.target, rec.target, nejminBituVMasce)) {
                break;
            }
            i++;
        }
        return i;
    }

    /**
     * Vrati true, pokud je prvni adresa mensi nez druha, pokud se rovnaji, tak rozhoduje maska.
     * @param first
     * @param second
     * @return
     */
    private boolean isLessIp(IPwithNetmask first, IPwithNetmask second, boolean nejminBituVMasce) {

        // kdyz maj stejny IP a ruzny masky
        if (first.getIp().toString().equals(second.getIp().toString())) {
            if (nejminBituVMasce) { // pro pridani do wrapperu
                if (first.getMask().getNumberOfBits() < second.getMask().getNumberOfBits()) {
                    return true;
                }
            } else { // pro vypis RT
                if (first.getMask().getNumberOfBits() > second.getMask().getNumberOfBits()) {
                    return true;
                }
            }
        }
        if (first.getIp().getLongRepresentation() < second.getIp().getLongRepresentation()) {
            return true;
        }
        return false;
    }

    /**
     * Vrati CiscoRecord na indexu.
     * @param index
     * @return
     */
    public CiscoRecord getRecord(int index) {
        return records.get(index);
    }

    /**
     * Vrati pocet zaznamu ve wrapperu.
     * @return
     */
    public int getSize() {
        return records.size();
    }

    /**
     * Pro vypis pres 'sh run'
     * @return
     */
    public String getRunningConfig() {
        String s = "";
        for (CiscoRecord z : records) {
            s += "ip route " + z + "\n";
        }
        return s;
    }

    /**
     * Vrati vypis routovaci tabulky.
     * Kasle se na tridni vypisy pro adresaty ze A rozsahu, protoze se v laborce takovy rozsah nepouziva.
     * @return
     */
    public String getShIpRoute() {
        String s = "";

        if (debug) {
            s += "Codes: C - connected, S - static\n\n";
        } else {
            s += "Codes: C - connected, S - static, R - RIP, M - mobile, B - BGP\n"
                    + "       D - EIGRP, EX - EIGRP external, O - OSPF, IA - OSPF inter area\n"
                    + "       N1 - OSPF NSSA external type 1, N2 - OSPF NSSA external type 2\n"
                    + "       E1 - OSPF external type 1, E2 - OSPF external type 2\n"
                    + "       i - IS-IS, su - IS-IS summary, L1 - IS-IS level-1, L2 - IS-IS level-2\n"
                    + "       ia - IS-IS inter area, * - candidate default, U - per-user static route\n"
                    + "       o - ODR, P - periodic downloaded static route\n\n";
        }

//        CiscoWrapperRT wrapper = ((CiscoPocitac) pc).getWrapper();
        boolean defaultGW = false;
        String brana = null;
		IPwithNetmask zeros = new IPwithNetmask("0.0.0.0", 0);
        for (int i = 0; i < getSize(); i++) {
            if (getRecord(i).target.equals(zeros)) {
                if (getRecord(i).gateway != null) {
                    brana = getRecord(i).gateway.toString();
                }
                defaultGW = true;
            }
        }

        s += "Gateway of last resort is ";
        if (defaultGW) {
            if (brana != null) {
                s += brana;
            } else {
                s += "0.0.0.0";
            }
            s += " to network 0.0.0.0\n\n";
        } else {
            s += "not set\n\n";
        }

        // vytvarim novy wrapperu kvuli zabudovanemu razeni
        CiscoWrapperRT wrapper_pro_razeni = new CiscoWrapperRT(device, ipLayer);
        for (int i = 0; i < routingTable.size(); i++) {
            wrapper_pro_razeni.addRecordForOutputOnly(routingTable.getRecord(i));
        }

        for (CiscoRecord czaznam : wrapper_pro_razeni.records) {
            s += getRecordForShow(czaznam);
        }

        return s;
    }

    /**
     * Vrati vypis cisco zaznamu ve spravnem formatu pro RT
     * @param record
     * @return
     */
    private String getRecordForShow(CiscoRecord record) {
        String s = "";

        if (record.isConnected()) { //C       21.21.21.0 is directly connected, FastEthernet0/0
            s += "C       " + record.getTarget().getNetworkNumber() + " is directly connected, " + record.getInterface().name + "\n";
        } else { //S       18.18.18.0 [1/0] via 51.51.51.9
            if (record.getTarget().equals(new IPwithNetmask("0.0.0.0", 0))) {
                s += "S*      ";
            } else {
                s += "S       ";
            }
            s += record.getTarget().getIp() + "/" + record.getTarget().getMask().getNumberOfBits();
            if (record.getGateway() != null) {
                s += " [1/0] via " + record.getGateway();
            } else {
                s += " is directly connected, " + record.getInterface().name;
            }
            s += "\n";
        }

        return s;
    }

	@Override
	public String getDescription() {
		return Util.zarovnej(device.getName(), Util.deviceNameAlign) + "wrapper";
	}
}
