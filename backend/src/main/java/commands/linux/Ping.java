/*
 * Erstellt am 8.3.2012.
 */

package commands.linux;

import applications.LinuxPingApplication;
import commands.AbstractCommandParser;
import commands.ApplicationNotifiable;
import commands.LongTermCommand;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.IpAddress;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.NetworkInterface;

/**
 * Linuxovy prikaz ping.
 *
 * Znamy odchylky: pri nepovolenym intervalu (0;0,2) by se mela vypsat hlavicka pingu, zatim se nevypisuje.
 * @author Tomas Pitrinec
 */
public class Ping extends LinuxCommand implements LongTermCommand, ApplicationNotifiable {



//parametry prikazu: -------------------------------------------------------------------------------------------

    IpAddress cil; //adresa, na kterou ping posilam
    int count= -1; //pocet paketu k poslani, zadava se prepinacem -c
    int size=56; //velikost paketu k poslani, zadava se -s
    double interval=1; //interval mezi odesilanim paketu v sekundach, zadava se -i, narozdil od vrchnich je dulezitej
    int ttl=64; //zadava se prepinacem -t
    boolean minus_q=false; //tichy vystup, vypisujou se jen statistiky, ale ne jednotlivy pakety
    boolean minus_b=false; //dovoluje pingat na broadcastovou adresu
    boolean minus_h=false;
    //dalsi prepinace, ktery bych mel minimalne akceptovat: -a, -v
	int timeout = 10_000;	// timeout v milisekundach

    //parametry parseru:
    private String slovo; //slovo parseru, se kterym se zrovna pracuje
    /**
     * 0 - v poradku
     * 1 - nezadana adresa
     * 2 - spatna adresa
     * 4 - chyba v ciselny hodnote prepinace
     * 8 - neznamy prepinac
     */
    private int navratovyKod=0;

	private LinuxPingApplication app;



// konstruktor a ostatni nutny verejny metody: ------------------------------------------------------------------------

	public Ping(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		parser.setRunningCommand(this, false);	// registrovani u parseru
		parsujPrikaz();
		if (ladiciVypisovani) {
			printLine(toString());
		}
		boolean applicationStarted = vykonejPrikaz();
		if (!applicationStarted) {
			parser.deleteRunningCommand();	// odregistrovani, kdyz nebyla aplikace spustena
		}
		//Logger.log(this, Logger.DEBUG, LoggingCategory.LINUX_COMMANDS, "Konec metody run. ", null);
	}

	@Override
	public void catchUserInput(String input) {
		// nic nedela
	}


	/**
	 * Tuhle metodu vola aplikace tehdy, kdyz skoncila
	 */
	@Override
	public void applicationFinished() {
		parser.deleteRunningCommand();
	}

	@Override
	public void catchSignal(Signal signal) {
		if(signal==Signal.CTRL_C){

			app.exit();
		}
	}


// privatni metody pro  vykonavani: ------------------------------------------------------------------------------

	/**
	 * Vykonavani, tedy predevsim spousteni pingovaci aplikace.
	 * @return true, kdyz byla spustena aplikace, jinak false
	 */
	private boolean vykonejPrikaz() {
		if(minus_h){
			vypisNapovedu();
		} else if (navratovyKod!=0){
			// nejaka chyba pri parsovani, nic se nedela.
		} else if (!jeRouta()) {
			printLine("connect: Network is unreachable");
		} else {
			app = new LinuxPingApplication(parser.device, this, cil, count, size, timeout, (int)(interval*1000), ttl);
			app.start();
			Logger.log(this, Logger.DEBUG, LoggingCategory.LINUX_COMMANDS, "Spustil jsem pingovou aplikaci a mam zpatky rizeni.", null);
			return true;
		}
		return false;
	}

	/**
	 * Overuje, jestli bude mozne na cilovou adresu pingovat. Tahle metoda je trochu hack, spravne by tu nemela bejt.
	 * Delat to ale v samotny aplikaci je ale pozde, protoze to by se nejdriv musela vypsat ta uvodni hlaska.
	 *
	 * @return
	 */
	private boolean jeRouta() {
		for (NetworkInterface iface : ipLayer.getNetworkIfaces()) {
			if (iface.getIpAddress() != null && iface.getIpAddress().getIp().equals(cil)) {
				return true;
			}
		}
		if (ipLayer.routingTable.findRoute(cil) != null) {
			return true;
		}
		return false;
	}



// privatni metody pro  parsovani: ------------------------------------------------------------------------------

    /**
	 * Parsovani.
     * Cte prikaz, zatim cte jenom IP adresu a nic nekontroluje.
     */
    private void parsujPrikaz(){
        slovo=dalsiSlovo();
        while (!slovo.isEmpty()) {
            if (slovo.length() > 1 && slovo.charAt(0) == '-') { //cteni prepinacu
                zpracujPrepinace();
                if (navratovyKod != 0 || minus_h) {
                    return; // !!!!!!!! NA CHYBU NEBO "-h" SE OKAMZITE KONCI !!!!!!!!!
                }
            } else {
                try { //cteni ip adresy
                    cil = new IpAddress(slovo);
                } catch (BadIpException ex) {
                    navratovyKod |= 2;
                    printLine("ping: unknown host " + slovo);
                }
            }
            slovo=dalsiSlovo();
        }
        //kdyz se vsechno zparsovalo, zkontroluje se, je-li zadana adresa:
        if(cil==null){
            navratovyKod |= 1;
            vypisNapovedu();
        }

    }

    /**
	 * Parsovani.
     * Zpracovava prepinace z jednoho slova. Predpoklada, ze krome minusu bude mit jeste aspon jeden dalsi znak.
     * Ty hlasky, co to vraci, nejsou vzdycky uplne verny
     */
    private void zpracujPrepinace() {
        int uk=1; //ukazatel na znak v tom Stringu, 0 je to minus
        int pom;
        while (uk < slovo.length()) {
            if (slovo.charAt(uk) == 'b') { // -b
                minus_b = true;
            } else if (slovo.charAt(uk) == 'q') { //-q
                minus_q = true;
            } else if (slovo.charAt(uk) == 'h') { //-h
                minus_h = true;
            } else if (slovo.charAt(uk) == 'c') { //-c
                pom = zpracujCiselnejPrepinac(uk);
                if (pom <= 0){ //povoleny interval je 1 .. nekonecno
                    navratovyKod |= 4;
                    printLine("ping: bad number of packets to transmit.");
                } else {
                    count = pom;
                }
                break;
            } else if (slovo.charAt(uk) == 's') { // -s
                pom = zpracujCiselnejPrepinac(uk);
                if (pom < 0 || pom > 65508){//v sesite...
                    navratovyKod |= 4;
                    printLine("ping: bad size of packet.");
                } else {
                    size = pom;
                }
                break;
            } else if (slovo.charAt(uk) == 't') { //-t
                pom = zpracujCiselnejPrepinac(uk);
                if (pom <= 0 || pom > 255){ //povoleny interval je 1 .. nekonecno
                    navratovyKod |= 4;
                    if (pom==-1)printLine("ping: can't set unicast time-to-live: Invalid argument");
                    else printLine("ping: ttl "+pom+" out of range");
                } else {
                    ttl = pom;
                }
                break;
           } else if (slovo.charAt(uk) == 'i') {
				double p = zpracujDoublePrepinac(uk);
				if (p < 0) {
					navratovyKod |= 4;
					printLine("ping: bad timing interval.");

				} else if (p < 0.2) {
					navratovyKod |= 4;
					printLine("ping: cannot flood; minimal interva is 200ms");	// zakazali jsme to, protoze to simulator nevytezovalo
				} else {
					interval = p;
				}
				break;
            }else{
                printLine("ping: invalid option -- '"+slovo.charAt(uk)+"'");
                vypisNapovedu();
                navratovyKod |= 8;
                break;
            }
            uk++;
        }
    }

    /**
	 * Parsovani.
     * Tahleta metoda parsuje ciselne hodnoty prepinace, podle podminek, podle jakych funguje ping
     * (poznamky v mym sesite).
     * @param uk ukazatel na pismeno toho prepinace ve slove
     * @param puvodni hodnota prepinace
     * @return -1 kdyz se zparsovani nepovede
     */
    private int zpracujCiselnejPrepinac(int uk){
        int vratit=0;
        boolean asponJednoCislo=false;
        uk++; //aby ukazoval az za to pismeno
        if(uk>=slovo.length()){ //pismeno toho prepinace bylo poslednim znakem slova, mezi pismenem a
                                    // hodnotou je mezera
            slovo=dalsiSlovo(); //nacitani dalsiho slova
            uk=0;
        }
        while (uk<slovo.length() && Character.isDigit(slovo.charAt(uk))){ //ten cyklus bere jen cislice, to za
            vratit=vratit*10+Character.getNumericValue(slovo.charAt(uk)); //nima ignoruje ( -c12vnf^^$ -> -c12)
            asponJednoCislo=true;
            uk++;
        }
        if(asponJednoCislo){
            return vratit;
        }else{
            return -1;
        }
    }

	/**
	 * Parsovani.
	 * @param uk
	 * @return
	 */
    private double zpracujDoublePrepinac(int uk){
        slovo=dalsiSlovo();
        try{
            return Double.parseDouble(slovo);
        }catch (NumberFormatException ex){
            return -1;
        }
    }


	/**
	 * Vykonavani.
	 */
    private void vypisNapovedu() {
        printLine("Usage: ping [-LRUbdfnqrvVaA] [-c count] [-i interval] [-w deadline]");
        printLine("            [-p pattern] [-s packetsize] [-t ttl] [-I interface or address]");
        printLine("            [-M mtu discovery hint] [-S sndbuf]");
        printLine("            [ -T timestamp option ] [ -Q tos ] [hop1 ...] destination");
    }

	/**
	 * Jen pro ladeni.
	 *
	 * @return
	 */
	@Override
	public String toString() {
		String vratit = "----------------------------------"
				+"\n   Parametry prikazu ping:"
				+ "\r\n\tnavratovyKodParseru: " + navratovyKod;
		vratit += "\r\n\tcount: " + count;
		vratit += "\r\n\tsize: " + size;
		vratit += "\r\n\tttl: " + ttl;
		vratit += "\r\n\tinterval v sekundach: " + interval;
		if (minus_q) {
			vratit += "\r\n\tminus_q: zapnuto";
		}
		if (minus_b) {
			vratit += "\r\n\tminus_b: zapnuto";
		}
		if (cil != null) {
			vratit += "\r\n\tcilova adresa: " + cil.toString();
		} else {
			vratit += "\r\n\tcilova adresa je null";
		}
		vratit+="\n----------------------------------";
		return vratit;
	}




}
