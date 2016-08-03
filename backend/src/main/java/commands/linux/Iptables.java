/*
 * Erstellt am 16.3.2012.
 */

package commands.linux;

import commands.AbstractCommandParser;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.IpAddress;
import java.util.ArrayList;
import java.util.List;
import networkModule.L3.nat.NatTable;
import networkModule.L3.NetworkInterface;
import utils.Util;

/**
 * Prakticky zkopirovanej z bakalarky, pridal jsem jen moznost -F.
 *
 * @author Tomas Pitrinec
 */
public class Iptables extends LinuxCommand {




    /**
     * Navratovy kod parseru a kontroloru.<br />
     * Funguje klasicky po bitech jako v ifconfigu ap.<br />
     * 0 - vsechno v poradku<br />
     * 1 - nejakej nesmysl v gramatice prikazu, je uveden v promenny nesmysl<br />
     * 2 - tabulka nezadana<br />
     * 4 - prepinac nedokoncen (poslednim slovem je napr -t), uklada se v nedokoncenejPrepinac<br />
     * 8 - spatna tabulka<br />
     * 16 - vicekrat zadany prepinac(e), uklada se v dvojityPrepinace<br />
     * 32 - nespravna adresa -d<br />
     * 64 - spatna adresa --to<br />
     * 128 - nezadan zadny komand -L, -A, -I, -D
     * 256 - zadano vic retezu (vic parametru -A, -I, -D)<br />
     * 512 - spatny cislo pravidla <br />
     * 1024 - neznama akceJump<br />
     * 2048 - vzhledem k akci nebo k pravidlu zakazanej prepinac, uklada se do zakazanyPrepinace <br />
     * 4096 - nepodporovany nebo zakazany prepinac nebo akceJump<br />
     * 8192 - vystupni rozhrani neexistuje<br />
     * 16384 - pro danou moznost chybi nejakej prepinac<br />
     * 32768 - neznamy jmeno retezu<br />
     * 65536 - moc velky cislo k deletovani<br />
     * 131072 - zatim nepodporovanej retez PREROUTING<br />
     */
    int navrKod = 0;
    // ostatni promenny parseru:
    String slovo; //aktualni slovo
    String nesmysl; //k navrKodu 1.
    String tabulka;
    String vstupniRozhr;
    String vystupniRozhr;
    String akceJump; //akce u -j
    String cilAdr;
    String preklAdr;
    List<String> dvojityPrepinace = new ArrayList<>();//prepinace, ktery byly zadany vic nez jednou
    List<String> nepovolenyPrepinace = new ArrayList<>(); //prepinace, ktery jsou v zadany kombinaci
           //nepovoleny, a to budto simulatorem nebo samotnym iptables
    List<String> chybejiciPrepinace = new ArrayList<>();//prepinace, ktery pro moje prepinace chybej
    String nedokoncenejPrepinac;
    boolean minus_h=false;
    boolean zadanoMinus_o = false;
    boolean zadanoMinus_i = false;
    boolean zadanoMinus_j = false;
    boolean zadanoMinus_d = false;
    boolean zadanoToDestination = false;
    boolean zadanRetez = false;
    String cisloPr; //cislo pravidla jako String
    String retez;
    /**
     * 0 - nic<br />
     * 1 - append<br />
     * 2 - insert<br />
     * 3 - delete<br />
     * 4 - list (vypsani)<br />
	 * 5 - flush<br />
     */
    int provest = 0;
    int cisloPravidla = -1; //cislo pravidla pro smazani nebo pridani
    List<String> zakazanyPrepinace = new ArrayList<>();

    //nastaveny promenny:
    boolean minus_n = true;
    IpAddress cilovaAdr;
    IpAddress prekladanaAdr;//ip adresa, na kterou se ma prekladat
    NetworkInterface vystupni;

	private final NatTable natTable;	// zkratka



	public Iptables(AbstractCommandParser parser) {
		super(parser);
		natTable = ipLayer.getNatTable();
	}

	@Override
	public void run() {
		parsujPrikaz();
        zkontrolujPrikaz();
        vypisChybovyHlaseni();
        vykonejPrikaz();
	}





    /**
     * Parsuje prikaz, k cemuz vola i metody dole. Kontroluje gramatiku prikazu.
     *
     */
    private void parsujPrikaz() {
        slovo = dalsiSlovo();
        while (!slovo.equals("")) {
            zpracujBeznyPrepinace();
            if(minus_h)break; // po -h se uz nic dalsiho neparsuje.
            slovo = dalsiSlovo();
        }
    }

    /**
     * Zpracovava bezny prepinace jako -n, -t, -o, -i, -j, -A, -I, -D, -L, -d.
     * Pro ty, co maj pak nejakou hodnotu vetsinou vola specialni funkci.
     * Na zacatku tyhle metody by v promenny slovo melo bejt ulozeny prvni
     * slovo prepinace (napr. -t), na konci posledni slovo prepinace (napr.
     * nat).
     */
    private void zpracujBeznyPrepinace() {
      if (slovo.equals("-h") || slovo.equals("--help")) {
			minus_h = true;
		} else if (slovo.equals("-t")) {
			zpracujMinus_t();
		} else if (slovo.equals("-o")) {
            zpracujMinus_o();
        } else if (slovo.equals("-i")) {
            zpracujMinus_i();
        } else if (slovo.equals("-j")) {
            zpracujMinus_j();
        } else if (slovo.equals("-d")) {
            zpracujMinus_d();
        } else if (akceJump != null && akceJump.equals("DNAT") && (slovo.equals("--to") || slovo.equals("--to-destination"))) {
            // -> dokud neni zadan DNAT, neni --to povoleny
            zpracujToDestination();
        } else if ((slovo.equals("-A")) || (slovo.equals("-I")) || (slovo.equals("-D"))|| (slovo.equals("-F"))) {
            zpracujRetez();
        } else if (slovo.equals("-L")) {
            if (zadanoMinus_j) {
                navrKod |= 256;
            } else {
                provest = 4;
                zadanRetez = true;
            }
        } else if (slovo.equals("-n")) {
            minus_n = true;
            //na tenhleten parametr kaslu, stejne u me nema smyslu
            //spravne by nemelo bejt povoleny -n -n...
        } else if (slovo.equals("")) { //zadny dlasi slovo uz neni
            //nic dalsiho se nedela
        } else {
            navrKod |= 1;
            nesmysl = slovo;
        }
    }

    private void zpracujMinus_t() {
        tabulka = dalsiSlovo();
        if (!tabulka.equals("nat")) {
            if (tabulka.equals("")) {
                navrKod |= 4; //zadano jen -t
                nedokoncenejPrepinac = "-t";
            } else {
                navrKod |= 8;
            }
        }
    }

    private void zpracujMinus_o() {
        if (zadanoMinus_o) {
            navrKod |= 16;
            dvojityPrepinace.add("-o");
        } else {
            zadanoMinus_o = true;
        }
        vystupniRozhr = dalsiSlovo();
        if (vystupniRozhr.equals("")) {
            navrKod |= 4;
            nedokoncenejPrepinac = "-o";
        }
    }

    private void zpracujMinus_i() {
        if (zadanoMinus_i) {
            navrKod |= 16;
            dvojityPrepinace.add("-i");
        } else {
            zadanoMinus_i = true;
        }
        vstupniRozhr = dalsiSlovo();
        if (vstupniRozhr.equals("")) {
            navrKod |= 4;
            nedokoncenejPrepinac = "-i";
        }
    }

    private void zpracujMinus_j() {
        if (zadanoMinus_j) {
            navrKod |= 16;
            dvojityPrepinace.add("-j");
        } else {
            zadanoMinus_j = true;
        }
        akceJump = dalsiSlovo();
        if (akceJump.equals("")) {
            navrKod |= 4;
            nedokoncenejPrepinac = "-j";
        } else {
            if (!(akceJump.equals("MASQUERADE") || akceJump.equals("DNAT"))) {
                navrKod |= 1024;
            }
        }
    }

    private void zpracujMinus_d() {
        if (zadanoMinus_d) {
            navrKod |= 16;
            dvojityPrepinace.add("-d");
        } else {
            zadanoMinus_d = true;
        }
        cilAdr = dalsiSlovo();
        if (cilAdr.equals("")) {
            navrKod |= 4;
            nedokoncenejPrepinac = "-d";
        } else {
            try {
                cilovaAdr = new IpAddress(cilAdr);
            } catch (BadIpException ex) {
                navrKod |= 32;
            }
        }
    }

    private void zpracujToDestination() {
        if (zadanoToDestination) {
            navrKod |= 16;
            dvojityPrepinace.add("--to-destination");
        } else {
            zadanoToDestination = true;
        }
        preklAdr = dalsiSlovo();
        if (preklAdr.equals("")) {
            navrKod |= 4;
            nedokoncenejPrepinac = "--to-destination";
        } else {
            try {
                cilovaAdr = new IpAddress(cilAdr);
            } catch (BadIpException ex) {
                navrKod |= 64;
            }
        }
    }

    /**
     * Zpracovava -A, -I, -D
     */
    private void zpracujRetez() {
        if (zadanRetez) {
            navrKod |= 256;
        } else {
            provest = 1;
			zadanRetez = true;
			if (slovo.equals("-A")) {
				provest = 1;
			} else if (slovo.equals("-I")) {
				provest = 2;
			} else if (slovo.equals("-D")) {
				provest = 3;
			} else if (slovo.equals("-F")) {
				provest = 5;
			}
            retez = dalsiSlovo();
            if (provest == 2) {//insert
				cisloPr = parser.nextWordPeek();
                try {
                    cisloPravidla = Integer.parseInt(cisloPr);
                    dalsiSlovo(); //zvetsovani citace, kdyz se to povedlo
                } catch (NumberFormatException ex) {
                    cisloPravidla = 1;
                }
            }
            if (provest == 3) { //delete
                cisloPr = dalsiSlovo();
                try {
                    cisloPravidla = Integer.parseInt(cisloPr);
                } catch (NumberFormatException ex) {
                    navrKod |= 512;
                }
            }
            if (cisloPravidla != -1 && cisloPravidla < 1) {
                navrKod |= 512;
            }
            if( !retez.equals("PREROUTING") && !retez.equals("POSTROUTING") && !(retez.equals("")&&provest==5) ){
					// -> kdyz retez neni PREROUTING, ani POSTROUTING, ani prazdnej pri flush
                navrKod|=32768;
            }

        }
    }

    /**
     * Kontroluje, jestli byly zadany spravny parametry
     */
    private void zkontrolujPrikaz() {

        if(minus_h)return; //nic se nekontroluje...

        //kontrola spravnosti tabulky - pozor, vyplnuje se jeste navrKod:
        if (tabulka == null) {
            navrKod |= 2; //tabulka nezadana
        }

        if(provest==0){
            navrKod |= 128;
        }

        if (provest == 4 || provest ==3) { //-L - vypisovani, -D - mazani
          // -> poustim to zaroven i pri -D, abych si usetril kopirovani
            if (zadanoMinus_d) {
                zakazanyPrepinace.add("-d");
                navrKod |= 2048;
            }
            if (zadanoMinus_o) {
                zakazanyPrepinace.add("-o");
                navrKod |= 2048;
            }
            if (zadanoMinus_i) {
                zakazanyPrepinace.add("-i");
                navrKod |= 2048;
            }
            if (zadanoMinus_j) {
                zakazanyPrepinace.add("-j");
                navrKod |= 2048;
            }
        }

        if(provest==1 ||provest==2){ //append nebo insert
            if (zadanoMinus_i) { //-i neni u me povoleny ani v jednom
                nepovolenyPrepinace.add("-i");
                navrKod |= 4096;
            }
            if (retez.equals("POSTROUTING")) { //klasickej preklad adres na jednom verejnym rozhrani
                if (zadanoMinus_d) {
                    nepovolenyPrepinace.add("-d");
                    navrKod |= 4096;
                }
                if(zadanoMinus_o){
					vystupni = ipLayer.getNetworkInteface(vystupniRozhr);
                    if(vystupni==null){
                        navrKod |= 8192;
                    }
                }else{
                    chybejiciPrepinace.add("-o");
                    navrKod |= 16384;
                }
                if(zadanoMinus_j){
                    if( ! akceJump.equals("MASQUERADE")){
                        navrKod |= 4096;
                        nepovolenyPrepinace.add("-j "+akceJump); //dam ho tam, i kdyz neni prepinac
                    }
                }else{
                    chybejiciPrepinace.add("-j");
                    navrKod |= 16384;
                }
            }
            if (retez.equals("PREROUTING")){
                navrKod |= 131072; //nepodporovanej retez
                if( ! zadanoMinus_d  ){
                    chybejiciPrepinace.add("-d");
                    navrKod |= 16384;
                }
                if( ! zadanoMinus_j  ){
                    chybejiciPrepinace.add("-j");
                    navrKod |= 16384;
                }else{
                    if( ! akceJump.equals("DNAT")){
                        navrKod |= 4096;
                        nepovolenyPrepinace.add("-j "+akceJump); //dam ho tam, i kdyz neni prepinac
                    }
                }
                if( ! zadanoToDestination  ){
                    chybejiciPrepinace.add("--to-destination");
                    navrKod |= 16384;
                }
                if( zadanoMinus_o  ){
                    zakazanyPrepinace.add("-o"); //ten je tady zakazanej
                    navrKod |= 2048;
                }
            }
        }

        if(provest==3){ //delete
            //prebytecny prepinace se zkoumaly uz s vypisem
            if(cisloPravidla!=1){
                navrKod |= 65536;
            }
        }
    }

    /**
     * Vypisuje chybovy hlaseni.
     * Budou pak chtit seradit podle priority.
     */
    private void vypisChybovyHlaseni() {
        if (ladiciVypisovani) {
            printLine(toString());
            printLine("----------------------------");
        }
        if ((navrKod & 1) != 0) { //nesmysl v gramatice
            printLine("Bad argument `" + nesmysl + "'");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
            return; //asi by tadyu melo bejt...
        }

        if ((navrKod & 2) != 0) { //nezadano jmeno tabulky
            parser.printService(": Normalne by se pouzila tabulka filter, " +
                    "ta ale v tomto simulatoru neni. Podporujeme zatim jen tabulku nat.");
        }
        if ((navrKod & 4) != 0) { //prepinac nedokoncen
            printLine("iptables v1.4.1.1: Unknown arg `" + nedokoncenejPrepinac + "'");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }
        if ((navrKod & 8) != 0) { //zadana spatna tabulka
            printLine("iptables v1.4.1.1: can't initialize iptables table `" + tabulka + "': " +
                    "Table does not exist (do you need to insmod?)");
            printLine("Perhaps iptables or your kernel needs to be upgraded.");
        }

        if ((navrKod & 32768) != 0) { //zadanej spatnej retez
            printLine("iptables: No chain/target/match by that name");
        }

        if ((navrKod & 16) != 0) { //nejakej prepinac zadanej dvakrat
            printLine("iptables v1.4.1.1: multiple " + dvojityPrepinace.get(0) + " flags not allowed");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }

        if ((navrKod & 32) != 0) { //spatna adresa -d
            printLine("iptables v1.4.1.1: host/network `" + cilAdr + "' not found");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }

        if ((navrKod & 64) != 0) { //spatna adresa --to-destination
            printLine("iptables v1.4.1.1: Bad IP address `" + preklAdr + "'");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }

        if ((navrKod & 128) != 0) { //nezadan zadny prikaz
            printLine("iptables v1.4.1.1: no command specified");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }

        if ((navrKod & 256) != 0) { //vic retezu
            parser.printService("Parametry -A, -I, -D nemuzete zadavat vicektrat.");
            // -> normalne to pise: "iptables v1.4.1.1: Can't use -A with -I"  - to se mi nechtelo pamatovat
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }

        if ((navrKod & 512) != 0) { //spatny cislo
            printLine("iptables v1.4.1.1: Invalid rule number `" + cisloPr + "'");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }

        if ((navrKod & 1024) != 0) { //neznama akce
            printLine("iptables v1.4.1.1: Couldn't load target `" + akceJump + "':/lib/xtables/libipt_" +
                    akceJump + ".so: cannot open shared object file: No such file or directory");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }

        if ((navrKod & 2048) != 0) { //zakazanyPrepinace
            printLine("iptables v1.4.1.1: Illegal option `" + vypisSeznam(zakazanyPrepinace)
                    + "' with this command");
            printLine("Try `iptables -h' or 'iptables --help' for more information.");
        }

        if ((navrKod & 4096) != 0) { //pro akci zatim nepodporovany prepinac
            parser.printService(": Takova moznost by mozna normalne byla mozna, simulator ji vsak "
                    +"zatim nepodporuje. Zkuste odstranit prepinac: "+ vypisSeznam(nepovolenyPrepinace));
        }

        if ((navrKod & 8192) != 0) { //zadany vystupni rozhrani neexistuje kontroluje se jen na POSTROUTING)
            parser.printService(": Zadane rozhrani "+vystupniRozhr+" neexistuje.");
        }
        if ((navrKod & 16384) != 0) { //zadany vystupni rozhrani neexistuje kontroluje se jen na POSTROUTING)
            parser.printService(": Pro danou moznost chybeji tyto prepinace: "
                    +vypisSeznam(chybejiciPrepinace));
        }

        if ((navrKod & 131072) != 0) { //nepodporovanej retez PREROUTING
            parser.printService("Retez PREROUTING neni zatim simulatorem podporovan.");
        }


    }


    protected void vykonejPrikaz() {
        if(navrKod!=0){
            return; // provadi se, jen kdyz je vsechno dobre
        }

        if(minus_h){
            vypisHelp();
            return;
        }

        if (provest == 4) {
			vypis();
		} else if (provest == 1 || provest == 2) { //-A nebo -I
			if (!natTable.isSetLinuxMasquarade()) {
				natTable.setLinuxMasquarade(vystupni);
			} else {
				parser.printService("Simulator neumoznuje pridat vic nez jedno pravidlo do tabulky. "
						+ "Nejprve smazte existujici pravidlo.");
			}
		} else if (provest == 3) { //mazani
			if (natTable.isSetLinuxMasquarade()) {
				natTable.cancelLinuxMasquerade();
			} else {
				printLine("iptables: Index of deletion too big");
			}

		} else if(provest==5){
			natTable.cancelLinuxMasquerade();
		}
    }


    private void vypis() {
        printLine("Chain PREROUTING (policy ACCEPT)");
        printLine("target     prot opt source               destination");
        printLine("");
        printLine("Chain POSTROUTING (policy ACCEPT)");
        printLine("target     prot opt source               destination");
        if(natTable.isSetLinuxMasquarade())
            printLine("MASQUERADE  all  --  0.0.0.0/0            0.0.0.0/0");
        printLine("");
        printLine("Chain OUTPUT (policy ACCEPT)");
        printLine("target     prot opt source               destination");
    }

    private String vypisSeznam(List<String> l){
        String vr="";
        for(int i=0;i<l.size();i++){
            if(l.get(i)!=null){
                if(i!=0)vr+=", ";
                vr+=l.get(i);
            }
        }
        return vr;
    }

    @Override
    public String toString() {
        String vratit = "  Parametry prikazu iptables:\n\r\tnavratovyKodParseru: " + Util.rozlozNaMocniny2(navrKod);
        if (tabulka != null) {
            vratit += "\n\r\ttabulka: " + tabulka;
        }
        if (retez != null) {
            vratit += "\n\r\tretez: " + retez;
        }
        vratit += "\n\r\tprovest: " + provest;
        vratit += "\n\r\tcisloPravidla: " + cisloPravidla;
        if (minus_h) {
            vratit += "\n\r\tPOZOR, zadan prepinac -h, tzn., nic dalsiho se neparsuje, nic se nekontro" +
                    "luje, nic se nevypisuje, jen napoveda se vypise.";
        }
        if (zadanoMinus_o) {
            vratit += "\n\r\tvystupniRozhr: " + vystupniRozhr;
        }
        if (zadanoMinus_i) {
            vratit += "\n\r\tvstupniRozhr: " + vstupniRozhr;
        }
        if (zadanoMinus_j) {
            vratit += "\n\r\takceJump: " + akceJump;
        }
        if (zadanoMinus_d) {
            vratit += "\n\r\tcilAdr: " + cilAdr;
            if (cilovaAdr != null) {
                vratit += "\n\r\tcilovaAdr: " + cilovaAdr.toString();
            }
        }



        return vratit;
    }

    private void vypisHelp() {
        printLine("iptables v1.4.1.1    ");
        printLine("");
        printLine("Usage: iptables -[AD] chain rule-specification [options]");
        printLine("       iptables -[RI] chain rulenum rule-specification [options]");
        printLine("       iptables -D chain rulenum [options]                      ");
        printLine("       iptables -[LS] [chain [rulenum]] [options]               ");
        printLine("       iptables -[FZ] [chain] [options]                         ");
        printLine("       iptables -[NX] chain                                     ");
        printLine("       iptables -E old-chain-name new-chain-name                ");
        printLine("       iptables -P chain target [options]                       ");
        printLine("       iptables -h (print this help information)                ");
        printLine("");
        printLine("Commands:");
        printLine("Either long or short options are allowed.");
        printLine("  --append  -A chain            Append to chain");
        printLine("  --delete  -D chain            Delete matching rule from chain");
        printLine("  --delete  -D chain rulenum                                   ");
        printLine("                                Delete rule rulenum (1 = first) from chain");
        printLine("  --insert  -I chain [rulenum]                                            ");
        printLine("                                Insert in chain as rulenum (default 1=first)");
        printLine("  --replace -R chain rulenum");
        printLine("                                Replace rule rulenum (1 = first) in chain");
        printLine("  --list    -L [chain [rulenum]]");
        printLine("                                List the rules in a chain or all chains");
        printLine("  --list-rules -S [chain [rulenum]]");
        printLine("                                Print the rules in a chain or all chains");
        printLine("  --flush   -F [chain]          Delete all rules in  chain or all chains");
        printLine("  --zero    -Z [chain]          Zero counters in chain or all chains");
        printLine("  --new     -N chain            Create a new user-defined chain");
        printLine("  --delete-chain");
        printLine("            -X [chain]          Delete a user-defined chain");
        printLine("  --policy  -P chain target");
        printLine("                                Change policy on chain to target");
        printLine("  --rename-chain");
        printLine("            -E old-chain new-chain");
        printLine("                                Change chain name, (moving any references)");
        printLine("Options:");
        printLine("  --proto       -p [!] proto    protocol: by number or name, eg. `tcp'");
        printLine("  --source      -s [!] address[/mask]");
        printLine("                                source specification");
        printLine("  --destination -d [!] address[/mask]");
        printLine("                                destination specification");
        printLine("  --in-interface -i [!] input name[+]");
        printLine("                                network interface name ([+] for wildcard)");
        printLine("  --jump        -j target");
        printLine("                                target for rule (may load target extension)");
        printLine("  --goto      -g chain");
        printLine("                              jump to chain with no return");
        printLine("  --match       -m match");
        printLine("                                extended match (may load extension)");
        printLine("  --numeric     -n              numeric output of addresses and ports");
        printLine("  --out-interface -o [!] output name[+]");
        printLine("                                network interface name ([+] for wildcard)");
        printLine("  --table       -t table        table to manipulate (default: `filter')");
        printLine("  --verbose     -v              verbose mode");
        printLine("  --line-numbers                print line numbers when listing");
        printLine("  --exact       -x              expand numbers (display exact values)");
        printLine("[!] --fragment  -f              match second or further fragments only");
        printLine("  --modprobe=<command>          try to insert modules using this command");
        printLine("  --set-counters PKTS BYTES     set the counter during insert/append");
        printLine("[!] --version   -V              print package version.");
    }




}
