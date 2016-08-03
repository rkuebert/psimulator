/*
 * Erstellt am 6.3.2012.
 */

package commands.linux;

import commands.AbstractCommandParser;
import dataStructures.ipAddresses.*;
import java.util.ArrayList;
import java.util.List;
import networkModule.L3.NetworkInterface;
import utils.Util;

/**
 *
 * @author Tomas Pitrinec
 */
public class Ifconfig extends LinuxCommand {


    String jmenoRozhrani; //jmeno rozhrani, jak bylo zadano
    /**
     * Do tyhle promenny se uklada jenom IP adresa bez masky za lomitkem.
     */
    List <IpAddress> seznamIP=new ArrayList<IpAddress>();
    String spatnaAdresa=null; //prvni ze spatnejch zadanejch adres - ta se totiz vypisuje
    /**
     * Maska jako String (napr. 255.255.255.0);
     */
    String maska;
    String broadcast;
    int pocetBituMasky= -1;
    List <String> add=new ArrayList<String>(); //IP adresy, ktera se ma pridat
    List <String> del=new ArrayList<String>();  //ipadresy, ktery se maj odebrat
    List <String> neplatnyAdd=new ArrayList<String>();	//spatny IP adresy, ktera se mely pridat
    List <String> neplatnyDel=new ArrayList<String>();	//spatny IP adresy, ktery se mely odebrat
    NetworkInterface rozhrani; //rozhrani, se kterym se bude operovat
    int pouzitIp = -1; //cislo seznamIP, ktera IP se ma pouzit
    int upDown = 0; // 1 pro up, 2 pro down
    boolean minus_a = false;
    boolean minus_v = false;
    boolean minus_s = false;
    boolean minus_h = false;

    /**
     * Do tyhle promenny bude metoda parsujPrikaz zapisovat, jakou chybu nasla:<br />
     * 0: vsechno v poradku<br />
     * 1: spatny prepinac (neznama volba)<br />
     * 2: nejaka chyba v gramatice prikazu (napr: ifconfig wlan0 1.2.3.5 netmask)
     *    potreba provest, co je dobre, a vypsat napovedu --help<br />
     * 4: rozhrani neexistuje<br />
     * 8: zadano vice ipadres, bere se posledni spravna<br />
     * 16: spatna  IP adresa<br />
     * 32: pocet bitu masky vetsi nez 32<br />
     * 64: neplatna IP adresa parametru add<br />
     * 128: neplatna IP adresa parametru del<br />
     * 256: zakazana IP adresa<br />
     */
    int navrKod = 0;

	/**
	 * Konstruktor.
	 * @param parser
	 */
	public Ifconfig(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		parsujPrikaz();
        zkontrolujPrikaz();
        vypisChybovyHlaseni();
        vykonejPrikaz();
	}


    protected void parsujPrikaz() {
        String slovo;
        // prepinace:
        slovo=dalsiSlovo();
        while (slovo.startsWith("-")) { //kdyz je prvnim znakem slova minus
            if (slovo.equals("-a")) {
                minus_a = true;
            } else if (slovo.equals("-v")) {
                minus_v = true;
            } else if (slovo.equals("-s")) {
                minus_s = true;
            } else if (slovo.equals("-h")||slovo.equals("--help")) {
                minus_h = true;
            } else {
                errNeznamyPrepinac(slovo);
                return; //tady ifconfig uz zbytek neprovadi, i kdyby byl dobrej
            }
            slovo=dalsiSlovo();
        }
        //jmenoRozhrani je to prvni za prepinacema:
        if(! slovo.isEmpty()){
            jmenoRozhrani = slovo;
        }
        slovo=dalsiSlovo();
        //parametry:
        //Zjistil jsem, ze neznamej parametr se povazuje za adresu nebo za adresu s maskou.
        boolean pokracovat=true;
        while (!slovo.isEmpty() && pokracovat) { //dokud v tom slove neco je
            if (slovo.equals("netmask")) {//m
                maska = dalsiSlovo();
                if(maska.isEmpty()){
                    maska=null;
                    navrKod |= 2;
                }
            } else if (slovo.equals("broadcast")) {//adresa pro broadcast, ta si vubec dela uplne, co se ji zachce
                broadcast = dalsiSlovo();
                if(broadcast.isEmpty()){
                    broadcast=null;
                    navrKod |= 2;
                }
            } else if (slovo.equals("add")) {
                add.add(dalsiSlovo());
                if(add.isEmpty()){
                    add=null;
                    navrKod |= 2;
                }
            } else if (slovo.equals("del")) {
                del.add(dalsiSlovo());
                if(del.isEmpty()){
                    del=null;
                    navrKod |= 2;
                }
            } else if (slovo.equals("up")) {
                upDown = 1;
            } else if (slovo.equals("down")) {
                upDown = 2;
            } else { //kdyz to neni nic jinyho, tak to ifconfig povazuje za seznamIP adresu
                //je-li adresa spatna, musi parsovani skoncit.
                if(slovo.contains("/")){
                }
                try {
                    IPwithNetmask vytvarena = new IPwithNetmask(slovo, -1, true); // maska se kdyztak dopocita podle tridy
                                // a normalne se moduluje
                    if (slovo.contains("/")) { // kdyz masku obsahuje, musi se to ulozit
                        pocetBituMasky = vytvarena.getMask().getNumberOfBits();
                    }
                    seznamIP.add(vytvarena.getIp());
                } catch (BadIpException ex) {
                    spatnaAdresa = slovo;
                    pokracovat = false;
                    navrKod |= 16;
                }
            }
            slovo = dalsiSlovo();
        }
    }

    /**
     * Tahlecta metoda kontroluje jen hodnoty parametru, na nektery chyby, napr. gramaticky (nespravny
     * prepinace, vice parametru netmask ap.), predpokladam, ze uz se prislo. Posila klientovi hlaseni
     * o chybach.
     */
    private void zkontrolujPrikaz(){
        if (jmenoRozhrani==null) return; //uzivatel zadal jen ifconfig, mozna nejaky prepinace, ale nic vic
        //-------------------
        //kontrola existence rozhrani
        rozhrani=ipLayer.getNetworkInteface(jmenoRozhrani);
        if (rozhrani==null){
            //tady se nic nevypisuje, protoze ostatni se v ifconfigu asi vyhodnocuje driv (kdyz je spatne
            //rozhrani i ipadresa, tak se jako spatna ukaze IP adresa
            navrKod |= 4;
        }
        //------------------------
        //kontrola IP
        if(seznamIP.size()>1){ //jestli neni moc IP adres
            navrKod |= 8;
        }
        for (int i=0;i<seznamIP.size();i++){ //kontrola spravnosti IP
            //kontroluje se jen zakazanost, spravnost se kontroluje v parseru
            if(IpAddress.isForbiddenIP(seznamIP.get(i))){
                            // -> adresa je spravna, ale zakazana
                navrKod |= 256; //zakazana IP
            } else { //spravna adresa
                pouzitIp=i; //pouzije se posledni prijatelna adresa
            }

        }
        //--------------------
        //kontrola masky
        //string masky se nekontroluje, protoze pro to IpAdresa nema metodu, kontroluje se az pri nastavovani
        //maska za lomitkem se kontroluje v parseru
        //---------------------
        //kontrola IP adres add (pridavani nove IP)
		for (int i = 0; i < add.size(); i++) {
			IpAddress ip = IpAddress.correctAddress(add.get(i));
			if (ip == null || IpAddress.isForbiddenIP(ip)) {
				navrKod |= 64;
				neplatnyAdd.add(add.get(i));
			}
		}
        for(int i=0;i<neplatnyAdd.size();i++){ //musi se to mazat v jinym cyklu, aby to nevylezlo ven
            add.remove(neplatnyAdd.get(i));
        }
        //---------------------
        //kontrola IP adres del (odebirani existujici IP)
        for(int i=0;i<del.size();i++){
            IpAddress ip = IpAddress.correctAddress(del.get(i));
			if (ip == null || IpAddress.isForbiddenIP(ip)) {
                navrKod |= 128;

                neplatnyDel.add(del.get(i));
            }
        }
        for(int i=0;i<neplatnyDel.size();i++){ //musi se to mazat v jinym cyklu, aby to nevylezlo ven
            del.remove(neplatnyDel.get(i));
        }
        //---------------------
    }

    /**
     * Metoda na vypisovani chybovejch hlášení. Projde návratovej kód a pošle
     * hlášení podle jejich priority.
     * O prioritách více v sešitě (14.4.) a v souboru IfconfigChyby.txt.
     * Odpovídá  metodě vykonejPrikaz() ve starý versi Ifconfigu.
     */
    private void vypisChybovyHlaseni(){
        if(ladiciVypisovani){
			printLine("----------------------------------");
            printLine(toString());
            printLine("----------------------------------");
        }

        // Serazeny je to podle priority - co se vypise driv:
        if (navrKod == 0) { // v poradku
            //nic se nevypisuje
        }
        if ((navrKod & 1) != 0) {
            //Spatnej prepinac, to se nic neprovadi
            //Jediny hlaseni, ktery se vypisuje uz driv v parseru, ma stejne nejvyssi prioritu a
            //nic dalsiho uz se neprovadi, tak by to byla akorat zbytecna prace
            return; //nic dalsiho se neprovadi
        }
        if ((navrKod & 16) != 0) { //aspon jedna z adres je neplatna
            printLine(spatnaAdresa+": unknown host");
            printLine("ifconfig: `--help' gives usage information.");
            return;
        }
        if ((navrKod & 4) != 0) { //rozhrani neexistuje
            if(pouzitIp!=-1) //adresa byla zadana
                printLine("SIOCSIFADDR: No such device");
            printLine(jmenoRozhrani + ": error fetching interface information: Device not found");
            // vypis o masce ma mensi prioritu, je az pod chybou v gramatice (navratovyKod & 2)
        }
        if ((navrKod & 256) != 0) {//zakazana ip adresa
            if((navrKod & 4) == 0) //vypisuje se, jen kdyz rozhrani je v poradku
                printLine("SIOCSIFADDR: Invalid argument");
        }
        if ((navrKod & 64) != 0) { //neplatna adresa add
            for(int i=0;i<neplatnyAdd.size();i++){ //vsechny se poporade vypisou
                printLine(neplatnyAdd.get(i)+": unknown host");
            }
        }
        if ((navrKod & 128) != 0) { //vsechny se poporade vypisou
            for(int i=0;i<neplatnyDel.size();i++){
                printLine(neplatnyDel.get(i)+": unknown host");
            }
        }
        if ((navrKod & 4) != 0) { //rozhrani neexistuje
            //pokracovani zezhora - vypis o masce ma totiz nizsi prioritu
            if(pocetBituMasky != -1 ||maska!=null) //maska byla zadana
                printLine("SIOCSIFNETMASK: No such device");
        }
        if ((navrKod & 2) != 0) { //nejaka chyba v gramatice
            vypisHelp();
            if (ladiciVypisovani) {
                printLine("blok pro navratovy kod 2, navratovy kod:" + navrKod);
            }
        }



        if ((navrKod & 8) != 0) { //zadano vice ip adres
            //nic se nevypisuje
        }
        if ((navrKod & 32) != 0) {//pocetBituMasky byl vetsi nez 32,
            // metoda zkontrolujPrikaz to uz opravila
            // nic se nevypisuje
        }

        if(broadcast!=null || add.size()>0 ||del.size()>0){
			parser.printService("Parametry broadcast, add a del prikazu ifconfig zatim nejsou podporovane.");
        }


    }

    /**
	 * Vykonava samotnej prikaz. U navratovyhoKodu 1 (spatnejPrepinac) a 4 (rozhrani neexistuje) nic neprovadi. Odpovidá
	 * metodě proved() ze starý verse ifconfigu.
	 */
	protected void vykonejPrikaz() {
		if (minus_h) {
			vypisHelp();
			return;
		}
		if ((navrKod & 4) != 0 || ((navrKod) & 1) != 0) { //kdyz navratovy kod obsahuje 4 nebo 1
			// U navratovyhoKodu 1 (spatnejPrepinac) a 4 (rozhrani neexistuje) nic neprovadi.
		} else {
			if (rozhrani == null) { //vypsat vsechno
				if (navrKod == 0) { //vypisuje se, jen kdyz je to ale vsechno v poradku
					for (NetworkInterface rozhr : ipLayer.getSortedNetworkIfaces()) {
						vypisRozhrani(rozhr);
					}
				}
			} else { //rozhrani bylo zadano
				if (seznamIP.isEmpty() && add.isEmpty() && del.isEmpty()
						&& maska == null && broadcast == null && upDown == 0) { //jenom vypis rozhrani
					if (navrKod == 0) { //vypisuje se, jen kdyz je to ale vsechno v poradku
						vypisRozhrani(rozhrani);
					}
				} else { //nastavovani
					nastavAdresuAMasku(rozhrani);
					//nastavovani broadcastu zatim nepodporuju
					//nastavovani parametru add zatim nepodporuju
					//nastavovani parametru del zatim nepodporuju
					if (upDown == 1) {
						nahodRozhrani(rozhrani);	// specialni metoda kvuli vypisu
					}
					if (upDown == 2) {
						rozhrani.isUp = false;
					}
				}
			}
		}
	}

    /**
     * Pokusi se nejprve nastavit adresu (pokud je zadana), pak masku ze Stringu (je-li zadana)
     * a nakonec i masku z pocetBituMasky (je-li zadana), protoze ta ma vetsi prioritu. Pokud se
     * provedla nejaka zmena vyridi nakonec routovaci tabulku. Sama nic nenastavuje, ale pouziva
     * k tomu privatni metody. Je-li zadana maska obema zpusoby, zmeni se dvakrat (tim padem i
     * routovaci tabulka, i kdyz je vysledek stejnej jako predchozi hodnoty, napr:
     * ifconfig eth0 1.1.1.1/24 netmask 255.255.0.0 se zmeni nejprv na tu ze stringu, pak na
     * tu za lomitkem)
     * @param r
     */
    private void nastavAdresuAMasku(NetworkInterface r) { //nastavuje ip
        boolean zmena=false; // jestli se vykonala nejaka zmena, nebo jestli zadany hodnoty byly stejny
                                // jako puvodni -> kvuli zmenam routovaci tabulky

        //nastavovani adresy:
        if (pouzitIp != -1){ //adresa byla zadana, musi se nastavit
            nahodRozhrani(r);
            String nastavit = seznamIP.get(pouzitIp).toString();
            if (r.getIpAddress()!=null && nastavit.equals(r.getIpAddress().toString())){
                //ip existuje a je stejna, nic se nemeni
            } else { //IP adresa neni stejna, bude se menit
				ipLayer.changeIpAddressOnInterface(r, vytvorAdresu(nastavit));
                zmena=true;
            }
        }

        //nastavovani masky ze Stringu m
        if (maska != null) { //zadana maska jako 255.255.255.0
            nahodRozhrani(r);
            if(r.getIpAddress()!=null && r.getIpAddress().getMask().toString().equals(maska)){
                //ip adresa existuje a ma stejnou masku, nic se nemeni
            }else{//zadana hodnota je jina nez puvodni, musi se menit
                priradMasku(r, maska);
                zmena=true;
            }
        }

        //nastavovani masky za lomitkem
        if (pocetBituMasky != -1) { //zadana adresa s maskou za lomitkem
            if (r.getIpAddress() != null && ( r.getIpAddress().getMask().getNumberOfBits() == pocetBituMasky ) ){
                //ip adresa existuje a ma stejnou masku, nic se nemeni
            }else{//zadana hodnota je jina nez puvodni, musi se menit
                priradMasku(r, pocetBituMasky);
                zmena=true;
            }
        }

        //kdyz se provedla nejaka zmena, musi se to projevit v routovaci tabulce:
        if(zmena)vyridRoutovani(r);

    }

    /**
     * Vytvori novou adresu, nenastavuje masku, ale hlida, jestli IpAdresu lze pouzit,
     * nebo jestli na ni neni nejaka specialni akce.
     * @param ip
     * @return null pro 0.0.0.0
     */
    private IPwithNetmask vytvorAdresu(String adr){
        if(adr.equals("0.0.0.0")){ //mazani adresy z rozhrani
            return null;
        }else{
            return new IPwithNetmask(adr);
        }
    }

    /**
     * Zadane IP adrese nastavi masku podle zadaneho poctuBitu (masky). pocetBitu musi bejt spravny cislo.
     * Kdyz je adresa null, posle chybovy hlaseni a skonci.
     * @param ip
     * @param pocetBitu
     */
	private void priradMasku(NetworkInterface iface, int pocetBitu) {
		if (iface.getIpAddress() == null) {
			printLine("SIOCSIFNETMASK: Cannot assign requested address");
		} else {
			ipLayer.changeIpAddressOnInterface(iface, new IPwithNetmask(iface.getIpAddress().getIp(), pocetBitu));
		}
	}

    /**
     * Pokusi se nastavit masku podle parametru m, ktery musi bejt spravnym stringem.
     * Je-li zadana IP null, vypise chybovy hlaseni a ukonci se.
     * @param ip adresa, ktera se ma zmenit
     * @param m string masky; nesmi bejt null
     */
    private void priradMasku(NetworkInterface iface, String m){//pokusi se nastavit masku
        if(iface.getIpAddress() == null){ //neni nastavena IP adresa, vypise se chybovy hlaseni a skonci se
            printLine("SIOCSIFNETMASK: Cannot assign requested address");
        } else {
			try{//je potreba zkontrolovat spravnost masky!!! //proto vyjimka
				IpNetmask mask = new IpNetmask(m);
				ipLayer.changeIpAddressOnInterface(iface, new IPwithNetmask(iface.getIpAddress().getIp(), mask));
			}catch(BadNetmaskException ex){
				printLine("SIOCSIFNETMASK: Invalid argument");
			}

        }
    }

    private void vyridRoutovani(NetworkInterface r){
		ipLayer.routingTable.flushRecords(r); //mazani rout
        if(r.getIpAddress()!=null){
			ipLayer.routingTable.addRecord(r.getIpAddress().getNetworkNumber(), r);
        }
    }

	/**
	 * Nahodi rozhrani. Potreba kvuli vypisum.
	 * @param r
	 */
    private void nahodRozhrani(NetworkInterface r) {
		if (!r.isUp) {
			r.isUp = true;
			printLine(r.name + ": link up, 100Mbps, full-duplex, lpa 0x41E1");
		}
	}

    private void vypisRozhrani(NetworkInterface r){
		if (rozhrani == null && (!r.isUp && !minus_a)) {
			return; // v hromadnym vypise se nevypisujou schozeny rozhrani
		}

		int a = (int) (Math.random() * 100); //nahodne cislo 0 - 99
		int b = (int) (Math.random() * 100); //nahodne cislo 0 - 99

		printLine(r.name + "\tLink encap:Ethernet  HWadr " + r.getMacAddress());
		if (r.getIpAddress() != null) {
			printLine("\tinet adr:" + r.getIpAddress().getIp().toString() + "  Bcast:"
					+ r.getIpAddress().getBroadcast().toString()
					+ "  Mask:" + r.getIpAddress().getMask().toString());
		}
		printLine("\tUP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1"); //asi ne cesky
		if (r.ethernetInterface.isConnected()) {
			printLine("\tRX packets:" + (a * b) + " errors:" + (b / 50) + "+ dropped:"
					+ (a / 20) + " overruns:" + (a / 50) + " frame:0");
			printLine("\tTX packets:" + (b * 100 + a) + " errors:0 dropped:0 overruns:0 carrier:0");
		} else {
			printLine("\tRX packets:0 errors:0 dropped:0 overruns:0 frame:0");
			printLine("\tTX packets:0 errors:0 dropped:0 overruns:0 carrier:0");
		}
		printLine("\tcollisions:0 txqueuelen:1000");
		if (r.ethernetInterface.isConnected()) {
			printLine("\tRX bytes:" + (a * 1000 + b * 10 + (a / 10)) + " ("
					+ ((a * 1000 + b * 10 + (a / 10)) / 1000) + " KiB)  TX bytes:1394 (1.3 KiB)");
		} else {
			printLine("\tRX bytes:0 (0 KiB)  TX bytes:1394 (1.3 KiB)");
		}
		printLine("\tInterrupt:12 Base address:0xdc00");
		printLine("");
	}

	@Deprecated //zjistil jsem, ze tahle metoda vlastne neni vubec potreba
	private void unknownHost(String vypsat) {
		printLine(vypsat + ": Unknown host");
		printLine("ifconfig: `--help' vypíše návod k použití.)");
	}

    @Override
    public String toString() {
        String vratit = "  Parametry prikazu ifconfig:"
				+ "\n\t"+parser.getWordsAsString()
				+ "\n\tnavratovyKodParseru: "+ Util.rozlozNaMocniny2(navrKod)
				+ "\n\tminus_a: "+minus_a;
        if (jmenoRozhrani != null) {
            vratit += "\n\trozhrani: " + jmenoRozhrani;
        }
        if (seznamIP != null) {
            vratit += "\n\tip: " + seznamIP;
        }
        vratit+="\n\r\tpouzitIp: "+pouzitIp;
        if (pocetBituMasky != -1) {
            vratit += "\n\tpocetBituMasky: " + pocetBituMasky;
        }
        if (maska != null) {
            vratit += "\n\tmaska: " + maska;
        }
        if (add != null) {
            vratit += "\n\tadd: " + add;
        }
        if (del != null) {
            vratit += "\n\tdel: " + del;
        }

        return vratit;
    }

    private void errNeznamyPrepinac(String ret) {
        printLine("ifconfig: neznámá volba `" + ret + "'.");
        printLine("ifconfig: `--help' vypíše návod k použití.");
        navrKod = 1;
    }

    private void vypisHelp() { // funkce na ladiciVypisovani napovedy --help

        printLine("Usage:");
        printLine("  ifconfig [-a] [-v] [-s] <interface> [[<AF>] <address>]");
        printLine("  [add <address>[/<prefixlen>]]");
        printLine("  [del <address>[/<prefixlen>]]");
        printLine("  [[-]broadcast [<address>]]  [[-]pointopoint [<address>]]");
        printLine("  [netmask <address>]  [dstaddr <address>]  [tunnel <address>]");
        printLine("  [outfill <NN>] [keepalive <NN>]");
        printLine("  [hw <HW> <address>]  [metric <NN>]  [mtu <NN>]");
        printLine("  [[-]trailers]  [[-]arp]  [[-]allmulti]");
        printLine("  [multicast]  [[-]promisc]");
        printLine("  [mem_start <NN>]  [io_addr <NN>]  [irq <NN>]  [media <type>]");
        printLine("  [txqueuelen <NN>]");
        printLine("  [[-]dynamic]");
        printLine("  [up|down] ...");
        printLine("");
        printLine("  <HW>=Hardware Type.");
        printLine("  List of possible hardware types:");
        printLine("    loop (Local Loopback) slip (Serial Line IP) cslip (VJ Serial Line IP)");
        printLine("    slip6 (6-bit Serial Line IP) cslip6 (VJ 6-bit Serial Line IP) adaptive (Adaptive Serial Line IP)");
        printLine("    strip (Metricom Starmode IP) ash (Ash) ether (Ethernet)");
        printLine("    tr (16/4 Mbps Token Ring) tr (16/4 Mbps Token Ring (New)) ax25 (AMPR AX.25)");
        printLine("    netrom (AMPR NET/ROM) rose (AMPR ROSE) tunnel (IPIP Tunnel)");
        printLine("    ppp (Point-to-Point Protocol) hdlc ((Cisco)-HDLC) lapb (LAPB)");
        printLine("    arcnet (ARCnet) dlci (Frame Relay DLCI) frad (Frame Relay Access Device)");
        printLine("    sit (IPv6-in-IPv4) fddi (Fiber Distributed Data Interface) hippi (HIPPI)");
        printLine("    irda (IrLAP) ec (Econet) x25 (generic X.25)");
        printLine("    eui64 (Generic EUI-64)");
        printLine("  <AF>=Address family. Default: inet");
        printLine("  List of possible address families:");
        printLine("    unix (UNIX Domain) inet (DARPA Internet) inet6 (IPv6)");
        printLine("    ax25 (AMPR AX.25) netrom (AMPR NET/ROM) rose (AMPR ROSE)");
        printLine("    ipx (Novell IPX) ddp (Appletalk DDP) ec (Econet)");
        printLine("    ash (Ash) x25 (CCITT X.25)");
    }






	private void debug(String s){
		if(ladiciVypisovani){
			printLine(s);
		}
	}

}
