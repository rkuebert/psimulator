/*
 * Erstellt am 15.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.BadNetmaskException;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.ArrayList;
import java.util.List;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.NetworkInterface;
import networkModule.L3.RoutingTable;
import psimulator2.Psimulator;
import utils.Util;

/**
 *
 * @author Tomas Pitrinec
 */
public class IpRoute extends LinuxCommand {


    //promenny parseru:
    private String slovo;
    boolean zadanaAdresa;

    /**
     * 1 - vypsat <br />
     * 2 - pridat adresu<br />
     * 3 - odebrat adresu<br />
     * 4 - flush <br />
     * 5 - vypsani helpu <br />
     * 6 - get <br />
     */
    private int akce=0;

    /**
     * 0 - v poradku <br />
     * 0. (tzn. 2^0) - nejakej nesmysl (treba i vicekrat zadana adresat). Vsechno se povazuje
     * za adresu, tenhle kod zadava metoda parsujAdresu, kdyz uz jednu zadanou ma a dalsi nemuze.
     * (zadava parser) <br />
     * 1. - spatny adresat (zadava parser) <br />
     * 2. - napsano dev nebo via, ale nic po nem... (zadava parser) <br />
     * 3. - zadano neexistujici iface (kontrola) <br />
     * 4. -  <br />
     * 5. - chybejici parametry u add nebo del (kontrola) <br />
     * 6. - neznama akce (neni to show flush add ani del) (zadava parser) <br />
     * 7. - spatna adresa parametru via (zadava parser) <br />
     * 8. - adresat se nemuze pridat, uz v routovaci tabulce jeden stejnej je (provedeni) <br />
     * 9. - mazany zaznam (u akce del) na iface neni (provedeni) <br />
     * 10. - nevykonava se metoda zkontroluj <br />
     * 11. - nevykonava se metoda vykonejPrikaz (ukladam to sem jen kvuli prehlednosti) <br />
     * 12. - u prikazu get volan zakazany parametr via (zadava parser) <br />
     * 13. - chybejici parametry u flush (kontrola) <br />
     * 14. - chybejici adresa u get (kontrola) <br />
     * 15. - zaznam typu UG (na branu) se nemuze pridat, protoze brana neni dosazitelna U priznakem
     * (zadava provedeni) <br />
     * 16. - nothing to flush (provedeni) <br />
     * 17. - get nenaslo adresu (provedeni) <br />
     * 18. - u get zadanej parametr dev - normalne spravnej, ale ja ho pro zmatenost nepodporuju (kontrola)
     */
    int navrKod=0; //je dobry pouzit funkci Util.md

    //jeste nenastaveny parametry:
    String rozhrRet;


    //spravne nastaveny parametry:
    IPwithNetmask adresat=null; //nastavuje parser
    NetworkInterface rozhr; //nastavuje kontrola
    IpAddress brana; //nastavuje parser
    boolean all=false; //bylo-li zadano slovicko all (vyznam u show a flush)

    //spatny parametry:
    String necoSpatne; //do tohodle stringu se ukladaj spatne nastaveny parametry: adresa, via, nesmysly...

	// zkratky:
	RoutingTable rTable;





	public IpRoute(AbstractCommandParser parser) {
		super(parser);
		rTable = ipLayer.routingTable;
	}



	@Override
	public void run() {
		parsujPrikaz();
        zkontrolujPrikaz();
        if(ladiciVypisovani){
            printLine(toString());
        }
        vykonejPrikaz();
        vypisChybovyHlaseni();
	}






    private void vypisChybovyHlaseni(){

        /*
         * Tahle metoda vypisuje jen chyby parseru a kontroly, provedeni si je vypisuje samo.
         * V tomhle prikaze se po kazdy chybe utece a dalsi se uz nevypisuje
         *
         * Poradi vypisovani:
         * Chyby najity v parserem (nemuze jich nastat vic soucasne):
         * 1. neznámej příkaz (nk 6) - vypsat, utýct
         * 2. špatná adresa adresata (nk 1), spatna adresa via (nk 7) - nemuzou nastat soucasne, protoze
         *      parser se pri prvni takovyhle chybe ukonci.
         * 3. nějakej nesmysl navíc (nk 0) -||-
         * 4. zadáno dev a nic po něm (nk 2)
         * Chyby najity kontrolou:
         * 5. neexistující rozhraní (nk 3)
         * 5. chybejici parametry u add nebo del (nk 5)
         * 6.
         * 7.
         * 8. nezadaná adresa, kdyz by mela (nk 4)
         * 9. ostatni (nk 8, 7, 9) - nemuzou nastat soucasne
         *
         * Utika se pres returny.
         */

        if (navrKod == 0) {
            return;
        }
      //parser:
        if ( (navrKod & Util.md(6)) != 0 ) { //neznama akce
            printLine("Command \"" + necoSpatne + "\" is unknown, try \"ip addr help\".");
            return;
        }
        if ( ( (navrKod & Util.md(1)) != 0 ) || ( (navrKod & Util.md(12)) != 0) ) { //spatna adresa
            printLine("Error: an inet prefix is expected rather than  \"" + necoSpatne + "\"");
            return;
        }
        if ((navrKod & Util.md(0)) != 0) { //nejakej nesmysl
            printLine("Error: either \"local\" is duplicate, or \"" + necoSpatne + "\" is a garbage.");
            return;
        }
        if ((navrKod & Util.md(2)) != 0) { // napsano dev a nic po nem
            printLine("Command line is not complete. Try option \"help\"");
            return;
        }
      //kontrola:
        if ((navrKod & Util.md(5)) != 0) { // chybejici parametry u add nebo del
            printLine("RTNETLINK answers: No such device");
            return;
        }
        if ((navrKod & Util.md(3)) != 0) { // spatne iface
            printLine("Cannot find device \""+rozhrRet+"\"");
            return;
        }
        if ((navrKod & Util.md(13)) != 0) { // chybi parametry u flush
            printLine("\"ip route flush\" requires arguments.");
            return;
        }
        if ((navrKod & Util.md(18)) != 0) { // u get zadanej nepodporovanej parametr dev
            parser.printService("Parametr dev u akce get je normalne mozny, neni ale simulatorem " +
                    "podporovany, protoze v pripade zadani nespravneho rozhrani vraci tezko zjistitelne " +
                    "nesmysly. Zadejte tedy prosim prikaz get bez tohoto parametru.");
            //tady nedavam return
        }
        if ((navrKod & Util.md(14)) != 0) { // chybi adresa u get
            printLine("need at least destination address");
            return;
        }


    }

    /**
     * Kontroluje prikaz jenom v pripade, ze v parseru bylo vsechno v poradku. Jinak hned utece a da o tom
     * zpravu do navrKodu
     */
    private void zkontrolujPrikaz() {
        if(navrKod != 0){
            navrKod |= Util.md(10);
            return;
        }

        //kontrola iface, jestlize bylo zadano:
        if (rozhrRet != null) {
			rozhr = ipLayer.getNetworkInteface(rozhrRet);
            if (rozhr == null) { //rozhrani nenalezeno napr. ip a a 1.1.1.1 dev dsa
                navrKod |= Util.md(3);
            }
        }

        //spolecna kontrola pro add a del, kontroluje se hlavne, byla-li zadana cilova adresa:
        if(akce==2 || akce==3){
            //kontrola adresy:
            if(adresat==null){
				adresat = new IPwithNetmask("0.0.0.0", 0); //jestlize nebyl adresat zadan,
                    // hodi se sem implicitni - tzn default (0.0.0.0/0)
            }
        }

        //specialni kontrola pro akci add, kontroluju, bylo-li zadano dev nebo via:
        if(akce==2){
            if(brana==null && rozhrRet==null){ //kontroluju to radsi pres ten string, aby tam nebyly
                        //zbytecne 2 navrKody na neexistujici iface
                navrKod |= Util.md(5);
            }
        }

        //akce show nepotrebuje zadnou zvlastni kontrolu

        //kontrola pro akci flush, musi u ni bejt zadano aspon jedno: adresat, brana, iface, all
        if(akce==4){
            if(adresat==null && rozhr==null && brana==null && !all){
                navrKod |= Util.md(13);
            }
        }

        //kontrola pro akci get, musi u ni bejt zadana adresa:
        if(akce==6){
            if(adresat==null){
                navrKod |= Util.md(14);
            }
            if(rozhr!=null){
                navrKod |= Util.md(18);
            }
        }

    }

    /**
     * Vykonava prikaz jenom v pripade, ze predtim bylo vsechno v poradku. Jinak hned utece a da o tom
     * zpravu do navrKodu.
     * Sama si vypisuje chybovy hlaseni.
     */
    protected void vykonejPrikaz() {
        if(navrKod != 0){
            navrKod |= Util.md(11);
            return;
        }

        if(akce==2){//add
            if(rTable.existRecordWithSameAdresat(adresat)!=null){
                navrKod |= Util.md(8);
                printLine("RTNETLINK answers: File exists");
            } else { //v poradku, zaznam se muze pridat
                if (brana == null) { //brana nezadana
					rTable.addRecord(adresat, rozhr);
                    // -> tohle uz musi projit, protoze se predtim zkontrolovalo, ze neexistuje
                    //    zaznam se stejnym adresatem
                } else {
                    int nk=0;
					nk = rTable.addRecord(adresat, brana, rozhr);
                    if(nk==2){//zaznam nejde pridat, protoze na brana neni dosazitelna
                        navrKod |= Util.md(15);
                        printLine("RTNETLINK answers: Network is unreachable");
                    }
                }
            }
        }
        if (akce == 3) {//del
			if(!rTable.deleteRecord(adresat, brana, rozhr)){
                navrKod |= Util.md(9);
                printLine("RTNETLINK answers: No such process");
            }
        }
        if(akce==1){ //show - vypsani
            String v;
            for(int i=0; i<rTable.size();i++){ //vypisuje abulku po radcich
                v="";
                RoutingTable.Record z = rTable.getRecord(i);
                if (z.iface.isUp) {
                    //radek se zobrazi jen za nejakejch podminek:
                    if ((adresat == null || adresat.equals(z.adresat)) //adresat nezadan, nebo souhlasi
                            && (rozhr == null || rozhr == z.iface) //rozhrani nezadano, nebo souhlasi
                            && (brana == null || brana.equals(z.brana))) //brana nezadana, nebo dobra
                    {
                        v += z.adresat.toString();
                        if (brana == null && z.brana != null) {
                            // -> brana se vypise, jen kdyz nebyla zadana jako filtr a je zadana
                            v += " via " + z.brana.toString();
                        }
                        if (rozhr == null) { //rozhrani se vypise, jen kdyz nebylo zadano jako filtr
                            v += " dev " + z.iface.name;
                        }
                    }
                    if (!v.equals("")) {
                        printLine(v);
                    }
                }
            }
        }
        if (akce == 4) { //flush
            List<RoutingTable.Record> keSmazani = new ArrayList<>();
            for (int i = 0; i < rTable.size(); i++) { //vypisuje abulku po radcich
                RoutingTable.Record zazn = rTable.getRecord(i);
                //radek se smaze jen za nejakejch podminek:
                if ((adresat == null || adresat.equals(zazn.adresat)) //adresat nezadan, nebo souhlasi
                        && (rozhr == null || rozhr == zazn.iface) //rozhrani nezadano, nebo souhlasi
                        && (brana == null || brana.equals(zazn.brana))) //brana nezadana, nebo dobra
                {
                    keSmazani.add(zazn); //zatim se to jen oznacuje, aby to spravne fungovalo, kdyz
                        //se jede forcyklem...
                }
            }
            if (keSmazani.isEmpty()) {
                navrKod |= Util.md(16);
                printLine("Nothing to flush.");
            } else {
                for (RoutingTable.Record z : keSmazani) {
                    rTable.deleteRecord(z);
                }
            }
        }
        if (akce==5){
            vypisHelp();
        }
        if(akce==6){ //get
            RoutingTable.Record zazn=rTable.findRoute(adresat.getIp());
            if(zazn==null){
                printLine("RTNETLINK answers: Network is unreachable");
                navrKod |=Util.md(17);
            }else{ //zaznam se podarilo najit, vypise se
                String prvni=adresat.getIp().toString();
                if(zazn.brana!=null){
                    prvni+= " via "+zazn.brana.toString();
                }
                prvni+=" dev "+zazn.iface.name+
                        "  src "+zazn.iface.getIpAddress().getIp().toString();
                printLine(prvni);
                printLine("    cache  mtu 1500 advmss 1460 hoplimit 64");
            }
        }
    }

    /**
     * Parsovani prikazu. <br />
     * Pri volani podrazeny metody ta metoda dostava prvni ji uzitecnu slovo. <br />
     * Vsechny akce (add, del, show, flush, get) se parsujou vicemene stejne, jen u get je zakazanej
     * parametr via. <br />
     * IP se parsujou primo v parseru, existence iface se tady ale nekontroluje. <br />
     * Jakmile parser narazi na jednu chybu, dal uz nepokracuje. <br />
     */
    private void parsujPrikaz() {
        slovo=dalsiSlovo();
        if(slovo.equals("")){   // nic nezadano - vsecho vypsat
            akce=1;
        } else if ("add".startsWith(slovo)){
            akce=2;
            slovo = dalsiSlovo();
            parsujParametry();
        } else if ("del".startsWith(slovo)){
            akce=3;
            slovo = dalsiSlovo();
            parsujParametry();
        } else if ("show".startsWith(slovo)){
            akce=1;
            slovo = dalsiSlovo();
            if(slovo.equals("all")){
                all=true;
                slovo = dalsiSlovo();
            }
            parsujParametry();
        } else if ("flush".startsWith(slovo)){
            akce=4;
            slovo = dalsiSlovo();
            parsujParametry();
        } else if ("get".startsWith(slovo)){
            akce=6;
            slovo = dalsiSlovo();
            parsujParametry();
        } else if ("help".startsWith(slovo)){
            akce=5;
            //dal se nepokracuje
        } else{
            necoSpatne=slovo;
            navrKod |= Util.md(6);
        }
    }

    /**
     * Parsuje vsechny parametry, tzn slova dev, via a adresu, na ne pak zavola specialni funkce
     */
    private void parsujParametry(){
        if (slovo.equals("")) {
            //konec prikazu, nic se nedeje...
        } else if (slovo.equals("dev")) {
            slovo=dalsiSlovo();
            parsujDev();
        } else if (slovo.equals("via")) {
            slovo=dalsiSlovo();
            parsujVia();
        } else if ( slovo.equals("all") && (akce==1||akce==4 ||akce==6) ) {
            all=true;
            slovo=dalsiSlovo();
            parsujParametry();
        } else { //vsechno ostatni se povazuje za adresu...
            parsujAdresu();
        }
    }

	/**
	 * Parsuje adresu. Predpoklada, ze ji nemuze prijit prazdnej String.
	 */
	private void parsujAdresu() {
		IPwithNetmask vytvarena;
		if (zadanaAdresa && (akce == 2 || akce == 3)) {
			navrKod |= Util.md(0);
			necoSpatne = slovo;
			return;
		}
		zadanaAdresa = true;
		if (slovo.equals("default")) {//kdyz je to de
			vytvarena = new IPwithNetmask("0.0.0.0", 0);
		} else {
			if (slovo.startsWith("default")) {
			}

			try {
				vytvarena = new IPwithNetmask(slovo, 32, false);
			} catch (BadNetmaskException | BadIpException ex) {
				navrKod |= Util.md(1);
				necoSpatne = slovo;
				return;
			}
		}
		adresat = vytvarena;
		slovo = dalsiSlovo();
		parsujParametry();
	}

	private void parsujVia() {
		if (akce == 6) { //u akce get neni tenhle parametr povolenej
			navrKod |= Util.md(12); //jen pro poradek, je to ale jinak brany jako adresa
			necoSpatne = "via";
			return; //parsovani se na spatnou adresu konci
		}
		if (slovo.equals("")) { //ip a a 1.1.1.1 via
			navrKod |= Util.md(2);
		} else {
			//nastovovani brany:
			try {
				brana = new IpAddress(slovo);
			} catch (BadIpException ex) {
				navrKod |= Util.md(7);
				necoSpatne = slovo;
				return; //POZOR!!!!!!!!! Tady se utika a konci parsovani, kdyz je spatna adresa
			}
			//dalsi pokracovani:
			slovo = dalsiSlovo();
			parsujParametry();
		}
	}

    private void parsujDev() {
        if (slovo.equals("")) { //ip a a 1.1.1.1 dev
            navrKod|=Util.md(2);
        } else {
            rozhrRet=slovo;
            //dalsi pokracovani:
            slovo=dalsiSlovo();
            parsujParametry();
        }
    }

    @Override
    public String toString(){
        String vratit = "--------------------------\r\n   Parametry prikazu ip route" +
                ":\r\n\tnavratovy kod po parsovani a kontrole: "
                + Util.rozlozNaLogaritmy2(navrKod);
        vratit += "\r\n\takce: "+akce;
        if(rozhrRet!=null)vratit +=  "\r\n\tzapsane rozhrani: "+rozhrRet;
        if(necoSpatne!=null)vratit +=  "\r\n\tnecoNavic: "+necoSpatne;

        if(adresat!=null)vratit += "\r\n\tnastaveny adresat: "+adresat.toString();
        if(rozhr!=null)vratit +=  "\r\n\tnastavene rozhr: "+rozhr.name;
        if(brana!=null)vratit +=  "\r\n\tnastavena brana: "+brana.toString();
        vratit += "\r\n--------------------------";
        return vratit;
    }

    private void vypisHelp() {
        printLine("Usage: ip route { list | flush } SELECTOR");
        printLine("       ip route get ADDRESS [ from ADDRESS iif STRING ]");
        printLine("                            [ oif STRING ]  [ tos TOS ]");
        printLine("       ip route { add | del | change | append | replace | monitor } ROUTE");
        printLine("SELECTOR := [ root PREFIX ] [ match PREFIX ] [ exact PREFIX ]");
        printLine("            [ table TABLE_ID ] [ proto RTPROTO ]");
        printLine("            [ type TYPE ] [ scope SCOPE ]");
        printLine("ROUTE := NODE_SPEC [ INFO_SPEC ]");
        printLine("NODE_SPEC := [ TYPE ] PREFIX [ tos TOS ]");
        printLine("             [ table TABLE_ID ] [ proto RTPROTO ]");
        printLine("             [ scope SCOPE ] [ metric METRIC ]");
        printLine("INFO_SPEC := NH OPTIONS FLAGS [ nexthop NH ]...");
        printLine("NH := [ via ADDRESS ] [ dev STRING ] [ weight NUMBER ] NHFLAGS");
        printLine("OPTIONS := FLAGS [ mtu NUMBER ] [ advmss NUMBER ]");
        printLine("           [ rtt TIME ] [ rttvar TIME ] [ window NUMBER]");
        printLine("           [ cwnd NUMBER ] [ hoplimit NUMBER ] [ initcwnd NUMBER ]");
        printLine("           [ ssthresh NUMBER ] [ realms REALM ] [ src ADDRESS ]");
        printLine("           [ rto_min TIME ]");
        printLine("TYPE := [ unicast | local | broadcast | multicast | throw |");
        printLine("          unreachable | prohibit | blackhole | nat ]");
        printLine("TABLE_ID := [ local | main | default | all | NUMBER ]");
        printLine("SCOPE := [ host | link | global | NUMBER ]");
        printLine("FLAGS := [ equalize ]");
        printLine("MP_ALGO := { rr | drr | random | wrandom }");
        printLine("NHFLAGS := [ onlink | pervasive ]");
        printLine("RTPROTO := [ kernel | boot | static | NUMBER ]");
        printLine("TIME := NUMBER[s|ms|us|ns|j]");
    }


}

