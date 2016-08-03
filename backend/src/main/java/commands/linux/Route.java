/*
 * Erstellt am 7.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import networkModule.L3.RoutingTable;
import psimulator2.Psimulator;
import utils.Util;

/**
 * Linuxovy prikaz route.
 *
 * @author Tomas Pitrinec
 */
public class Route extends LinuxCommand {


    // pomocny promenny pro parser prikazu:
    private String slovo; //drzi si slovo ke zpracovani
    private boolean poDevNepokracovat=false; //kdyz je iface zadano jen eth0, a ne dev eth0, tak uz nemuze
                                              //prijit zadnej dalsi prikaz jako gw nebo netmask

    //nastaveni prikazu:
    boolean minus_n=false;
    boolean minus_v=false;
    boolean minus_e=false;
    boolean minus_h=false;
    private String adr; //adresat
    private String maska;
    private int pocetBituMasky;
    private NetworkInterface rozhr=null; // iface
    private boolean nastavovanaBrana=false; // jestli uz byla zadana brana
    private boolean nastavovanoRozhrani=false; // jestli uz bylo zadano iface
    private boolean nastavovanaMaska=false;
    private boolean minusHost = false; //zadano -host
    private boolean minusNet = false; //zadano -net
    private IPwithNetmask ipAdresa;
    private IpAddress brana;
    boolean defaultni=false; //jestli neni defaultni routa (0.0.0.0/0)

    /**
     * Je to pole bitu (bity pocitany odzadu od nuly, jako mocnina):<br />
     * nevyplneno (0) - zadna akce, jenom vypsat <br />
     * 0. bit (1) - add <br />
     * 1. bit (2) - del <br />
     * 2. bit (4) - flush <br />
     */
    private int akce = 0;

    /**
     * Je to pole bitu (bity pocitany odzadu od nuly, jako mocnina):<br />
     * 0. bit (1) - nespravnej prepinac <br />
     * 1. bit (2) - malo parametru u akce <br />
     * 2. bit (4) - spatny adresat, spatna maska za lomitkem <br />
     * 3. bit (8) - spatna brana <br />
     * 4. bit (16) - brana zadavana vice nez jednou <br />
     * 5. bit (32) - nezname iface <br />
     * 6. bit (64) - iface zadano bez dev a pak jeste neco pokracovalo, nic se nesmi nastavit <br />
     * 7. bit (128) - nejakej nesmysl navic <br />
     * 8. bit (256) - pri parametru -host byla zadana maska -> nic neprovadet <br />
     * 9. bit (512) - maska zadavana vice nez jednou (jakymkoliv zpusobem) -> nic nenastavovat <br />
     * 10. bit (1024) - maska je nespravna <br />
     * 11. bit (2048) - IP adresata neni cislem site
     */
    int navratovyKod=0;

    /**
     * 1 - stejnej zaznam existuje, resp. neexistuje (u del)
     * 2 - brana neni dosazitelna U priznakem
     * 4 - zaznam ke smazani neexistuje
     */
    private int navratovyKodProvedeni=0;


	public Route(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		parsujPrikaz();
        vykonejPrikaz();
	}


	/**
	 * Samotny provedeni uz hotovyho prikazu.
	 */
   protected void vykonejPrikaz() {
		if (ladiciVypisovani) {
			printLine(this.toString());
		}
		if (minus_h) {
			vypisDelsiNapovedu();
			return; //POZOR, tady se utika
		}
		if (navratovyKod == 0) { //bez chyby
			if (akce == 0) { //nic nedelat, jenom vypsat
				vypisTabulku();
			}
			if (akce == 1) { //add
				if (brana == null) { //brana nezadana
					navratovyKodProvedeni = ipLayer.routingTable.addRecord(ipAdresa, rozhr);
				} else {
					navratovyKodProvedeni = ipLayer.routingTable.addRecord(ipAdresa, brana, rozhr);
				}
				if (navratovyKodProvedeni == 1) {
					printLine("SIOCADDRT: File exists");
				} else if (navratovyKodProvedeni == 2) {
					printLine("SIOCADDRT: No such process");
//					Logger.log(this, Logger.DEBUG, LoggingCategory.LINUX_COMMANDS, "Nemuzu pridat zaznam do tabulky "
//							+navratovyKodProvedeni, null);
				}
			} else if (akce == 2) {
				if (!ipLayer.routingTable.deleteRecord(ipAdresa, brana, rozhr)) {
					printLine("SIOCDELRT: No such process");
					navratovyKodProvedeni = 4;
				}
			} else if (akce == 4) {
				ipLayer.routingTable.flushAllRecords();
			}
		}
	}
//*******************************************************************************************************
//metody na parsovani prikazu:

    /**
     * Precte prikaz a nastavi mu parametry. Rovnou kontroluje, spravnost parametru.
     * Odchylka: Nezparsuje route -ven
     */
    private void parsujPrikaz() {
		// prepinace:
		slovo = dalsiSlovo();
		while (slovo.length() > 1 && slovo.charAt(0) == '-') {
			if (slovo.equals("-n") || slovo.equals("--numeric")) {
				minus_n = true;
			} else if (slovo.equals("-v") || slovo.equals("--verbose")) {
				minus_v = true;
			} else if (slovo.equals("-e") || slovo.equals("--extend")) {
				minus_e = true;
			} else if (slovo.equals("-en") || slovo.equals("-ne")) {	// bylo to v navodu na eduxu, tak jsem to sem pridal
				minus_e = true;
				minus_n = true;
			} else if (slovo.equals("-h") || slovo.equals("--help")) {
				minus_h = true;
			} else {
				printLine("route: invalid option -- " + slovo); //neznamej prepinac
				vypisDelsiNapovedu();
				navratovyKod = navratovyKod | 1;
				return;
			}
			slovo = dalsiSlovo();
		}
        // dalsi parsovani:
        if(minus_h){
            //to se pak uz nic neparsuje.
        }else{
            if (slovo.equals("add")) {
                slovo = dalsiSlovo();
                nastavAdd();
            }else if (slovo.equals("del")) {
                slovo = dalsiSlovo();
                nastavDel();
            }else
            if (slovo.equals("flush")) {
                slovo = dalsiSlovo();
                nastavFlush();
            }else if ( ! slovo.equals("")) { //nejakej nesmysl
                vypisDelsiNapovedu();
                navratovyKod |=128;
            }
        }
    }

	/**
	 * Parsovani.
	 */
    private void nastavAdd() { //i ukazuje na posici prvniho prvku za add
        akce |= 1;
        nastavAddNeboDel();
    }

	/**
	 * Parsovani.
	 */
    private void nastavDel() {
        akce |= 2;
        nastavAddNeboDel();
    }

    /**
	 * Parsovani.
     * Protoze add a del maji stejnou syntaxi, spolecnej kod z jejich metod jsem hodil do tyhle metody.
     */
    private void nastavAddNeboDel(){
        if(slovo.equals("")){ //konec
            navratovyKod=navratovyKod|2;
            vypisKratkouNapovedu();
        }else if(slovo.equals("-net")){
            slovo=dalsiSlovo();
            nastavMinus_net();
        }else if(slovo.equals("-host")){
            slovo=dalsiSlovo();
            nastavMinus_host();
        }else { //cokoliv jinyho se povazuje za adresu adresata - hosta
            nastavMinus_host();
        }
    }

    private void nastavFlush() {
        printService("Flush normalne neni podporovano, ale v simulatoru se zaznam smazal.");
        printLine("Spravny prikaz je: \"ip route flush all\"");
        akce=4;
    }

	/**
	 * Parsovani.
	 */
    private void nastavMinus_net() {
        minusNet = true;
		boolean bezChyby = true;
		//cteni stringu:
		if (slovo.equals("default") || slovo.equals("0.0.0.0") || slovo.equals("0.0.0.0/0")) { //default
			nastavDefault();
		} else if (slovo.contains("/")) { // slovo obsahuje lomitko -> mohla by to bejt adresa s maskou
			bezChyby = parsujIpSMaskou(slovo);
		} else { // slovo neobsahuje lomitko -> mohla by to bejt samotna IP adresa
			if (IpAddress.isCorrectAddress(slovo)) { //samotna IP je spravna
				adr = slovo;
			} else { //samotna IP neni spravna
				printLine(adr + ": unknown host");
				navratovyKod |= 4; //spatny adresat
				bezChyby = false;
			}
		}
        //adresa je prectena, kdyz je vsechno v poradku, pokracuje se dal:
        if(bezChyby) {
            slovo=dalsiSlovo();
            if(slovo.equals("gw")){
                slovo=dalsiSlovo();
                nastavGw();
            }else if(slovo.equals("dev")){
                slovo=dalsiSlovo();
                nastavDev();
            }else if(slovo.equals("netmask")){
                slovo=dalsiSlovo();
                nastavNetmask();
            }else if(slovo.equals("") && akce ==2){ //prazdnej retezec u akce del
                //konec prikazu
            }else{ //cokoliv ostatniho, i nic, se povazuje za iface
                poDevNepokracovat=true;
                nastavDev();
            }
            //tedka je jeste nutno zjistit, jestli byla nastavena maska
            if(nastavovanaMaska && navratovyKod==0 && ! defaultni){
                nastavAdresu();
            }else{
                if(! defaultni){ //kdyz bylo zadano defaultni, nic se nedeje
                    navratovyKod |=4;
                    printLine("SIOCADDRT: Invalid argument");
                }
            }
        }


    }

	/**
	 * Parsovani.
	 */
    private void nastavMinus_host() { //predpokladam, ze ve slove je ulozena uz ta adresa
        minusHost=true;
        boolean chyba=false;
        if(slovo.equals("default")){
            nastavDefault();
        }else if( ! IpAddress.isCorrectAddress(slovo)){ //adresa je spatna
            if(slovo.contains("/")){ //kdyz je zadana IP adresa s maskou (zatim na to kaslu a kontroluju jen
                                     //lomitko vypise se jina hlaska, nez normalne.
                printLine("route: netmask doesn't make sense with host route");
                vypisDelsiNapovedu();
                navratovyKod |= 256;
            }else{
                printLine(slovo+": unknown host");
                navratovyKod |= 4;
            }
            chyba=true;
        }else{ //adresa je dobra
            adr=slovo;
            ipAdresa=new IPwithNetmask(adr,32); // a adresa se rovnou vytvori
        }
        if(!chyba){ //kdyz nenastala chyba, tak se rozhoduje, co bude dal
            slovo=dalsiSlovo();
            if(slovo.equals("gw")){
                slovo=dalsiSlovo();
                nastavGw();
            }else if(slovo.equals("dev")){
                slovo=dalsiSlovo();
                nastavDev();
            }else if(slovo.equals("netmask")){ //on to pozna a hodi chybu
                nastavNetmask();
            }else if(slovo.equals("") && akce ==2){ //prazdnej retezec u akce del
                //konec prikazu
            }else{ //cokoliv ostatniho, i nic, se povazuje za iface
                poDevNepokracovat=true;
                nastavDev();
            }
        }
    }

	/**
	 * Parsovani.
	 */
    private void nastavGw() {//ceka, ze ve slove je uz ta IP adresa
        if(nastavovanaBrana){ //kontroluje se, jestli se to necykli s nastavDev()
            vypisKratkouNapovedu();
            navratovyKod |= 16;
            return;
        }
        nastavovanaBrana=true;
        boolean chyba=false;
        try {
			brana = new IpAddress(slovo);
		} catch (RuntimeException e) {
			chyba = true;
		}
        if ( chyba ){
            printLine(slovo+": unknown host");
            navratovyKod |= 8;
        }else{ //spravna brana
            slovo=dalsiSlovo();
            if (slovo.equals("dev")) {
				slovo = dalsiSlovo();
				nastavDev();
			} else if (slovo.equals("netmask")) {
				slovo = dalsiSlovo();
				nastavNetmask();
			} else if (slovo.equals("")) { //konec prikazu
			} else { //vsechno ostatni se povazuje za nazev iface
				poDevNepokracovat = true; //zadano bez dev -> uz se nemuze pokracovat
				nastavDev();
			}
        }
    }

	/**
	 * Parsovani.
	 */
    private void nastavDev() {
		if (nastavovanoRozhrani) { //kontroluje se, jestli se to necykli s nastavGw()
			vypisKratkouNapovedu();
			navratovyKod |= 16;
		}
		nastavovanoRozhrani = true;
		rozhr = ipLayer.getNetworkInteface(slovo);
		if (rozhr == null) { // iface nebylo nalezeno
			if (ladiciVypisovani) {
				rozhr = new NetworkInterface(-1, slovo, null);
			}
			printLine("SIOCADDRT: No such device");
			navratovyKod |= 32;
		} else { //rozhrani je spravne a je jiz ulozeno v promenne rozhr
			slovo = dalsiSlovo();
			if (slovo.equals("gw")) {
				if (poDevNepokracovat) { //rozhrani bylo zadano bez dev -> to uz se pak nesmi pokracovat
					vypisKratkouNapovedu();
					navratovyKod |= 64;
				} else {
					slovo = dalsiSlovo();
					nastavGw();
				}
			} else if (slovo.equals("netmask")) {
				if (poDevNepokracovat) { //rozhrani bylo zadano bez dev -> to uz se pak nesmi pokracovat
					vypisKratkouNapovedu();
					navratovyKod |= 64;
				} else {
					slovo = dalsiSlovo();
					nastavNetmask();
				}
			} else if (slovo.equals("")) { //konec prikazu
				//v poradku, konci se
			} else { //nejakej dalsi nesmysl
				vypisKratkouNapovedu();
				navratovyKod |= 128; //nic se neprovede
			}
		}
	}

	/**
	 * Parsovani.
	 */
    private void nastavNetmask(){
        if(minusHost){ // kontrola, jestli to vubec muzu nastavovat
            navratovyKod |= 256;
            printLine("route: netmask doesn't make sense with host route");
            vypisDelsiNapovedu();
            return; //nic se nema nastavovat
        }
        if(nastavovanaMaska){ // kontrola, jestli uz maska nebyla nastavena
            navratovyKod |= 512; //maska nastavovana dvakrat
            vypisKratkouNapovedu();
            return; //nic se nema nastavovat
        }
        nastavovanaMaska=true;
		if (!IpNetmask.isCorrectNetmask(slovo)) {
			printLine("route: bogus netmask " + slovo + "");
			navratovyKod |= 1024;
		} else { //spravna maska
			maska = slovo;
			slovo = dalsiSlovo();
			if (slovo.equals("dev")) {
				slovo = dalsiSlovo();
				nastavDev();
			} else if (slovo.equals("gw")) {
				slovo = dalsiSlovo();
				nastavGw();
			} else if (slovo.equals("")) {
				//konec prikazu
			} else { //vsechno ostatni se povazuje za nazev iface
				poDevNepokracovat = true; //zadano bez dev -> uz se nemuze pokracovat
				nastavDev();
			}
        }
    }

	/**
	 * Parsovani.
	 */
    private void nastavDefault() { //slouzi k nastavovani deafult
        adr="default";
        pocetBituMasky=0;
		ipAdresa = new IPwithNetmask("0.0.0.0", 0);	// a adresa se rovnou vytvori
        defaultni=true;
        //nastavovanaMaska=true;    //zakomentovano 18.5.2011, potom, co jsem zjistil, ze na mym pocitaci projde i
            // "route add default netmask 255.0.0.0 gw 192.168.0.20", pricemz to nastavi
            // "0.0.0.0         192.168.0.20    255.0.0.0       UG    0      0        0 eth0"
    }


//*********************************************************************************************************
//dalsi funkce:

	/**
	 * Kdyz obsahuje lomitko, pred lomitkem precte IP adresu a za lomitkem pocet bitu masky, zada to do tridnich
	 * promennejch adr a pocetBituMasky a vyplni nastavovanaMaska na true. Kdyz je neco spatne, vrati false a nastavi
	 * navratovy kod na |= 4. Kdyz vsechno probehne v poradku, tak se nakonec pokusi vytvori IP adresu adresata.
	 *
	 * @param adrm
	 * @return
	 */
	private boolean parsujIpSMaskou(String adrm) {
		nastavovanaMaska = true;
		int lomitko = adrm.indexOf('/');
		if (lomitko == -1) {// string musi obsahovat lomitko
			throw new RuntimeException("Tohle by nikdy nemelo nastat. Kontaktujte prosim tvurce softwaru.");
		} else {
			if (lomitko < adrm.length() - 1) { // lomitko nesmi byt poslednim znakem retezce
				String adresa = adrm.substring(0, lomitko);
				String mask = adrm.substring(lomitko + 1, adrm.length());
				if (IpAddress.isCorrectAddress(adresa)) { //adresa je spravna
					boolean chyba = false;
					try {        // pokus o parsovani masky
						pocetBituMasky = Integer.parseInt(mask); //parsuje se jako integer
					} catch (NumberFormatException ex) { //integer se nepovedlo zparsovat
						chyba = true; //kdyz to neni integer
						vypisKratkouNapovedu(); //napr: route add -net 128.0.0.0/3d dev eth0
					}
					if (!chyba) { //vsechno v poradku, muze se to nastavit
						adr = adresa;
						pocetBituMasky = pocetBituMasky % 32; //opravdu to tak funguje, dokonce i se zapornejma
						//cislama
						if (pocetBituMasky == 0) { //tohle jediny neni povoleny, opravdu
							printLine("SIOCADDRT: Invalid argument");//napr: route add -net 128.0.0.0/64 dev eth0
						} else { //konecne vsechno spravne
							return true; // NAVRAT Z METODY, KDYZ JE VSECHNO SPRAVNE
						}
					}
				} else { //adresa neni spravna
					printLine(adresa + ": Unknown host");
				}
			} else { //kdyz je lomitko poslednim znakem retezce
				printLine("SIOCADDRT: Invalid argument"); //napr: route add -net 1.0.0.0/ dev eth0
			}
		}
		// kdyz se vyskytne nejaka chyba:
		navratovyKod |= 4; //spatny adresat
		return false;
	}

    /**
     * Tahlecta metoda vezme String adr a String maska nebo int pocetBituMasky a udela z nich instanci
     * tridy IpAdresa.
     */
    private void nastavAdresu(){
        if (!nastavovanaMaska){
            throw new RuntimeException("K tomuhle by nikdy nemelo dojit. Tahleta metoda se vola, az kdyz" +
                    "je adresa i maska nastavena. Kontaktujte prosim tvurce softwaru.");
        }
        if(navratovyKod == 0){ //doted musi bejt vsechno v poradku
            if(maska != null){//maska byla zadana parametrem netmask
                ipAdresa = new IPwithNetmask(adr, maska);
            }else{//maska byla zadana za lomitkem
                ipAdresa = new IPwithNetmask( adr, pocetBituMasky);
            }
            if( ! ipAdresa.isNetworkNumber() ) { //adresa neni cislem site, to je chyba
                navratovyKod |= 2048; //adresat neni cislem site
                printLine("route: netmask doesn't match route address");
                //printLine("route: síťová maska nevyhovuje adrese cesty");
                vypisDelsiNapovedu();
            }
        }
    }

    private void vypisTabulku() {

        String v; //string na vraceni
        //printLine("Směrovací tabulka v jádru pro IP");
        printLine("Kernel IP routing table");
        //printLine("Adresát         Brána           Maska           Přízn Metrik Odkaz  Užt Rozhraní");
        printLine("Destination     Gateway         Genmask         Flags Metric Ref    Use Iface");
        int pocet = ipLayer.routingTable.size();
        for (int i = 0; i < pocet; i++) {
            v="";
            RoutingTable.Record z = ipLayer.routingTable.getRecord(i);
            if (z.iface.isUp) {
                v += Util.zarovnej(z.adresat.getIp().toString(),16);
                if (z.brana == null) {
                    if (z.adresat.getMask().toString().equals("255.255.255.255")) {
                        v += Util.zarovnej("0.0.0.0", 16) + Util.zarovnej(z.adresat.getMask().toString(), 16) + "UH    ";
                    } else {
                        v += Util.zarovnej("0.0.0.0", 16) + Util.zarovnej(z.adresat.getMask().toString(), 16) + "U     ";
                    }
                } else {
                    if (z.adresat.getMask().toString().equals("255.255.255.255")) {
                        v += Util.zarovnej(z.brana.toString(), 16) + Util.zarovnej(z.adresat.getMask().toString(), 16)
                                + "UGH   ";
                    } else {
                        v += Util.zarovnej(z.brana.toString(), 16) + Util.zarovnej(z.adresat.getMask().toString(), 16)
                                + "UG    ";
                    }

                }
                v += "0      0        0 " + z.iface.name;
                printLine(v);
            }
        }
    }

    /**
     * Jen pro ladeni.
     * @return
     */
    @Override
    public String toString(){
        String vratit = "----------------------------------"
				+ "\n   Parametry prikazu route:\r\n\tnavratovyKodParseru: "
				+ Util.rozlozNaMocniny2(navratovyKod)
				+ "\n\t" + parser.getWordsAsString();
        vratit += "\r\n\takce (1=add, 2=del): " + akce;
        vratit+="\r\n\tprepinace: ";
        if(minus_n)vratit+=" -n";if(minus_e)vratit+=" -e";if(minus_v)vratit+=" -v";
        if (adr != null) {
            vratit += "\r\n\tip: " + adr;
            if (ipAdresa != null) {
                vratit += "\r\n\tvypsana ipAdresa, ktera se nastavi: " + ipAdresa.toString();
            }else{
                vratit += "\r\n\tvypsana ipAdresa, ktera se nastavi: NEPODARILO SE NASTAVIT";
            }
        }
        if (nastavovanaMaska) {
            vratit += "\r\n\tpocetBituMasky: " + pocetBituMasky;
            vratit += "\r\n\tmaska: " + maska;
        }
        if(nastavovanaBrana){
            if(brana != null){
                vratit+="\r\n\tbrana: "+brana.toString();
            }else{
                vratit+="\r\n\tbrana: null";
            }
        }
        if (nastavovanoRozhrani) {
            if ( (navratovyKod & 32) ==32 ){ //rozhrani neexistuje
                vratit += "\r\n\trozhrani neexistuje: " + rozhr.name;
            }else{
                vratit += "\r\n\trozhrani: " + rozhr.name;
            }
        }
		vratit+="\n----------------------------------";

        return vratit;
    }

    /**
     * Tyto metody byly udělány nahrazením v Kate. Znak pro začátek řádku je ^ a pro konec řádku $.
     */
    private void vypisKratkouNapovedu() {
        printLine("Usage: inet_route [-vF] del {-host|-net} Target[/prefix] [gw Gw] [metric M] [[dev] If]");
        printLine("       inet_route [-vF] add {-host|-net} Target[/prefix] [gw Gw] [metric M]");
        printLine("                              [netmask N] [mss Mss] [window W] [irtt I]");
        printLine("                              [mod] [dyn] [reinstate] [[dev] If]");
        printLine("       inet_route [-vF] add {-host|-net} Target[/prefix] [metric M] reject");
        printLine("       inet_route [-FC] flush      NOT supported");
    }

    private void vypisDelsiNapovedu() {
        //napoveda z pocitacu ve skole:
        printLine("Usage: route [-nNvee] [-FC] [<AF>]           List kernel routing tables");
        printLine("       route [-v] [-FC] {add|del|flush} ...  Modify routing table for AF.");
        printLine("");
        printLine("       route {-h|--help} [<AF>]              Detailed usage syntax for specified AF.");
        printLine("       route {-V|--version}                  Display version/author and exit.");
        printLine("");
        printLine("        -v, --verbose            be verbose");
        printLine("        -n, --numeric            don't resolve names");
        printLine("        -e, --extend             display other/more information");
        printLine("        -F, --fib                display Forwarding Information Base (default)");
        printLine("        -C, --cache              display routing cache instead of FIB");
        printLine("");
        printLine("  <AF>=Use '-A <af>' or '--<af>'; default: inet");
        printLine("  List of possible address families (which support routing):");
        printLine("    inet (DARPA Internet) inet6 (IPv6) ax25 (AMPR AX.25)");
        printLine("    netrom (AMPR NET/ROM) ipx (Novell IPX) ddp (Appletalk DDP)");
        printLine("    x25 (CCITT X.25)");

    }



}
