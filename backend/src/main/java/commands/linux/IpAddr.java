/*
 * Erstellt am 15.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.BadNetmaskException;
import dataStructures.ipAddresses.IPwithNetmask;
import java.util.List;
import networkModule.L3.NetworkInterface;
import utils.Util;

/**
 *
 * @author Tomas Pitrinec
 */
public class IpAddr extends LinuxCommand {

	private Ip puv;

    //promenny parseru:
    private String slovo;
    boolean zadanaAdresa;

    /**
     * 1 - vypsat <br />
     * 2 - pridat adresu<br />
     * 3 - odebrat adresu<br />
     * 4 - flush <br />
     * 5 - vypsani helpu <br />
     */
    private int akce=0;

    /**
     * 0 - v poradku <br />
     * 0. (tzn. 2^0) - nejakej nesmysl (treba i vicekrat zadana adresa) <br />
     * 1. - spatna adresa <br />
     * 2. - zadano sice dev, ale nic po nem... <br />
     * 3. - zadano spatne rozhrani <br />
     * 4. - u akce add nebo del nezadana adresa <br />
     * 5. - nezadano rozhrani, kdyz by melo <br />
     * 6. - neznama akce (neni to show flush add ani del)<br />
     * 7. - adresa se nemuze pridat, protoze uz na rozhrani jedna je a vic jich nepodporuju <br />
     * 8. - adresa se nemuze pridat, uz tam jedna stejna je <br />
     * 9. - mazana adresa neni na rozhrani <br />
     * 10. - u mazani neni zadana maska <br />
     * 11. - nevykonava se metoda vykonejPrikaz (ukladam to sem jen kvuli prehlednosti) <br />
     */
    int navrKod=0; //je dobry pouzit funkci Util.md

    //jeste nenastaveny parametry:
    String rozhrRet;
    String adresa;

    //spravne nastaveny parametry:
    IPwithNetmask adr;
    NetworkInterface rozhr;
    boolean nastavenaMaska=false; //jestli byla nastavena maska za lomitkem. Sice je defaultni, ale
            //nekdy je potreba vedet, jestli byla nebo nebyla nastavena

    //spatny parametry:
    String necoNavic;



	public IpAddr(AbstractCommandParser parser, Ip puv) {
		super(parser);
		this.puv = puv;
	}



	@Override
	public void run() {
        parsujPrikaz();
        zkontrolujPrikaz();
        vykonejPrikaz();
        vypisChybovyHlaseni();
	}



    protected void vykonejPrikaz() {

        if(navrKod!=0 && navrKod!=Util.md(10)){
            //musi bejt vsechno v poradku, krome nezadany masky u mazani, to se prikaz presto vykonava
            navrKod |= Util.md(11);
            return; //KDYZ NENI VSECHNO V PORADKU, UTECE SE!
        }

        if(akce==2){
            if(rozhr.getIpAddress()==null){
				ipLayer.changeIpAddressOnInterface(rozhr, adr);
            }else{
                if(rozhr.getIpAddress().equals(adr)){ //je tam nastavena stejna adresa
                    navrKod |= Util.md(8);
                }else{ //je tam nastavena adresa a ja jich nepodporuju vic na jednom rozhrani
                    navrKod |= Util.md(7);
                }
            }
        }
        if (akce == 3) {
            if (rozhr.getIpAddress() == null) { //kdyz na rozhrani zadna adresa neni, tak rovnou utecu
                navrKod |= Util.md(9);
            } else {
                if (nastavenaMaska) { //kdyz byla nastavena maska za lomitkem, tak tato maska musi pri
                    //mazani souhlasit
                    if (rozhr.getIpAddress().equals(adr)) { // souhlasi i maska
						ipLayer.changeIpAddressOnInterface(rozhr, null);
                    } else {
                        navrKod |= Util.md(9);
                    }
                } else {//kdyz maska nebyla zadana, smaze se prvni, ktera odpovida, ale vypise se
                    //chybovy hlaseni (resi metoda na jejich vypis
                    if (rozhr.getIpAddress().getIp().equals(adr)){//souhlasi jen adresa
						ipLayer.changeIpAddressOnInterface(rozhr, null);
                    } else {
                        navrKod |= Util.md(9);
                    }
                }
            }
        }
        if(akce==4){
			ipLayer.changeIpAddressOnInterface(rozhr, null);//u me se muze smazat jen jedna adresa
        }
        if(akce==1){
            vypisInfo();
        }
        if(akce==5){
            vypisHelp();
        }
    }

	private void vypisInfo() {
		int poradi = 1;
		for (NetworkInterface r : ipLayer.getSortedNetworkIfaces()) {
			if (rozhr == null || rozhr == r) {	// bylo-li zadano rozhrani, musi souhlasit
				vypisRozhrani(r, poradi);
				poradi++;
			}
		}
	}


    private void vypisRozhrani(NetworkInterface r, int poradi){
        String prvniRadek=poradi+": "+r.name+": <";
		if(!r.ethernetInterface.isConnected()){	// kdyz neni rozhrani k nicemu pripojeny:
            prvniRadek+="NO-CARRIER,";
        }
        prvniRadek+="BROADCAST,MULTICAST,UP,10000> mtu 1500 qdisc pfifo_fast qlen 1000";
        printLine(prvniRadek);
        if(puv.family==0 || puv.family==puv.fam_ethernet){
            printLine("    link/ether "+r.getMacAddress().toString()+" brd ff:ff:ff:ff:ff:ff");
        }
        if( (puv.family==0 || puv.family==puv.fam_ipv4) &&r.getIpAddress()!=null ){
            printLine("    inet "+r.getIpAddress().toString()+" brd "+
                r.getIpAddress().getBroadcast().toString()+" scope global "+r.name);
        }

    }

    /**
     * Kontroluje parametry, jako adresu ap.
     * Neco (napr. existence mazany adresy) se ale zjisti az pri vykonavani.
     * U parametru, ktery kontroluje, nejdriv zjisti, jesli jsou zadany, takze nespadne, kdyz jsou null.
     * Kontroluje to podle akce, kterou ma provist.
     */
    private void zkontrolujPrikaz(){
        if(akce==2||akce==3){
            //kontrola adresy:
            if (adresa == null) {
                navrKod |= Util.md(4);
            } else {
                if (adresa.contains("/")) {
                    nastavenaMaska = true;
                }
                try {
                    adr = new IPwithNetmask(adresa, 32, false);
                } catch (BadIpException | BadNetmaskException ex) {
                    navrKod |= Util.md(1); // opravdu je uplne jedno, co je spatne...
                }
            }
            //kontrola rozhrani:
            if (rozhrRet == null) {
                navrKod |= Util.md(5);
            } else {
                zkontrolujRozhrani();
            }
        }
        if(akce==3){
            if(!nastavenaMaska){
                navrKod|=Util.md(10);
            }
        }
        if(akce==4){//flush
            //jen kontrola rozhrani:
            if (rozhrRet == null) {
                navrKod |= Util.md(5);
            } else {
                zkontrolujRozhrani();
            }
        }
        if(akce==1){//vypis
            if (rozhrRet != null) {
                zkontrolujRozhrani();
            }
        }
    }
    /**
     * Kontroluje rozhrani, rozhrRet nesmi bejt null.
     */
    private void zkontrolujRozhrani() {
        rozhr = ipLayer.getNetworkInteface(rozhrRet);
        if (rozhr == null) { //rozhrani nenalezeno napr. ip a a 1.1.1.1 dev dsa
            navrKod |= Util.md(3);
        }
    }

    private void vypisChybovyHlaseni() {
        if(ladiciVypisovani)printLine(toString());

        /*
         * Poradi vypisovani:
         * 1. neznámej příkaz (nk 6) - vypsat, utýct
         * 2. špatná adresa (nk 1) -||-
         * 3. nějakej nesmysl navíc (nk 0) -||-
         * 4. zadáno dev a nic po něm (nk 2)
         * 5. chybějící rozhraní (nk 5)
         * 6. nezadaná maska u del (nk 10) - vypíše se hlášení, ale vypisujou se i další hlášení, neutíká se
         * 7. neexistující rozhraní (nk 3)
         * 8. nezadaná adresa (nk 4)
         * 9. ostatni (nk 8, 7, 9) - nemuzou nastat soucasne
         *
         * Utika se pres returny
         */

        if (navrKod == 0) {
            return;
        }
        if ((navrKod & Util.md(6)) != 0) { //neznama akce
            printLine("Command \"" + necoNavic + "\" is unknown, try \"ip addr help\".");
            return;
        }
        if ((navrKod & Util.md(1)) != 0) { //spatna adresa
            printLine("Error: an inet prefix is expected rather than  \"" + adresa + "\"");
            return;
        }
        if ((navrKod & Util.md(0)) != 0) { //nejakej nesmysl
            printLine("Error: either \"local\" is duplicate, or \"" + necoNavic + "\" is a garbage.");
            return;
        }
        if ((navrKod & Util.md(2)) != 0) { // napsano dev a nic po nem
            printLine("Command line is not complete. Try option \"help\"");
            return;
        }
        if ((navrKod & Util.md(5)) != 0) { //nezadano rozhrani, kdyz by melo
            printLine("Not enough information: \"dev\" argument is required.");
            return;
        }
        if ((navrKod & Util.md(10)) != 0) { //u mazani nezadana maska
            printLine("Warning: Executing wildcard deletion to stay compatible with old scripts.");
            printLine("         Explicitly specify the prefix length (1.1.1.1/32) to avoid this warning.");
            printLine("         This special behaviour is likely to disappear in further releases,");
            printLine("         fix your scripts!");
        }
        if ((navrKod & Util.md(3)) != 0) { // neexistujici rozhrani
            if (akce == 2 || akce == 3) {
                printLine("Cannot find device \"" + rozhrRet + "\"");
            } else {
                printLine("Device \"" + rozhrRet + "\" does not exist.");
            }
            return;
        }
        if ((navrKod & Util.md(4)) != 0) { // u akce add nebo del nezadana adresa
            if (akce == 2) {
                printLine("RTNETLINK answers: Invalid argument");
            } else {
                printLine("RTNETLINK answers: Cannot assign requested address");
            }
        }
        if ((navrKod & Util.md(8)) != 0) { // na rozhrani je stejna adresa
            printLine("RTNETLINK answers: File exists");
            return;
        }
        if ((navrKod & Util.md(7)) != 0) { // na rozhrani existuje jina adresa
            parser.printService("Simulator nepodporuje vice adres na jednom rozhrani. Na rozhrani " +
                    rozhr.name + " je jiz adresa " + rozhr.getIpAddress().toString() +
                    ". Odstrante ji prikazem flush nebo del. ");
            return;
        }
        if ((navrKod & Util.md(9)) != 0) { // mazana adresa neexistuje
            printLine("RTNETLINK answers: Cannot assign requested address");
            return;
        }
    }

    /**
     * Parsovani prikazu.
     * Pri volani podrazeny metody ta metoda dostava prvni ji uzitecnu slovo.
     */
    private void parsujPrikaz() {
        slovo=dalsiSlovo();
        if(slovo.equals("")){   // nic nezadano - vsecho vypsat
            akce=1;
        } else if ("add".startsWith(slovo)){
            slovo = dalsiSlovo();
            parsujAdd();
        } else if ("del".startsWith(slovo)){
            slovo = dalsiSlovo();
            parsujDel();
        } else if ("show".startsWith(slovo)){
            slovo = dalsiSlovo();
            parsujShow();
        } else if ("flush".startsWith(slovo)){
            slovo = dalsiSlovo();
            parsujFlush();
        } else if ("help".startsWith(slovo)){
            akce=5;
            //dal se nepokracuje
        } else{
            necoNavic=slovo;
            navrKod |= Util.md(6);
        }
    }

    private void parsujAdd() {
        akce = 2;

        if (slovo.equals("")) {
            //nic se nedeje, prijde se na to az pri kontrole...
        } else if (slovo.equals("dev")) {
            slovo=dalsiSlovo();
            parsujDev();
        } else { //vsechno ostatni se povazuje za adresu...
            parsujAdresu();
        }
    }

    private void parsujDel(){
        akce = 3;

        if (slovo.equals("")) {
            //nic se nedeje, prijde se na to az pri kontrole...
        } else if (slovo.equals("dev")) { //ip a a dev wlan0 - to se vypise jiny chybovy hlaseni
            slovo=dalsiSlovo();
            parsujDev();
        } else {
            parsujAdresu();
        }
    }

    private void parsujShow(){
        akce=1;
        if(slovo.equals("")){

        }else if(slovo.equals("dev")){
            slovo=dalsiSlovo();
            parsujDev();
        }else{//vsechno ostatni se povazuje za nazev rozhrani
            parsujDev();
        }
    }

    private void parsujFlush(){
        akce=4;
        if(slovo.equals("")){
            //zatim v poradku, bude to kontrolovat az kontrola
        }else if(slovo.equals("dev")){
            slovo=dalsiSlovo();
            parsujDev();
        }else{//vsechno ostatni se povazuje za nazev rozhrani
            parsujDev();
        }
    }

    /**
     * Parsuje adresu.
     * Predpoklada, ze ji nemuze prijit prazdnej String.
     */
    private void parsujAdresu() {
        if(zadanaAdresa){
            navrKod |=Util.md(0);
            necoNavic=slovo;
            return;
        }
        zadanaAdresa=true;
        adresa=slovo;
        slovo=dalsiSlovo();
        if(slovo.equals("dev")){
            slovo=dalsiSlovo();
            parsujDev();
        }else if(slovo.equals("")){
            //konec prikazu, nic se nedela...
        }else{
            //nejakej nesmysl, povazuju ho ale za ip adresu, to si to vyresi...
            parsujAdresu();
        }
    }

    private void parsujDev() {
        if (slovo.equals("")) { //ip a a 1.1.1.1 dev
            navrKod|=Util.md(2);
        } else {
            rozhrRet=slovo;
            //dalsi pokracovani:
            slovo=dalsiSlovo();
            if(slovo.equals("")){
                //v poradku - nic se nedeje
            }else {
                if(akce==2||akce==3){ // akce je ad nebo del
                    if(slovo.equals("dev")){
                        slovo=dalsiSlovo();
                        parsujDev();
                    }else{//vsechno ostatni se povazuje za adresu
                        parsujAdresu();
                    }
                }else{ // akce je show nebo flush - nic dalsiho nesmi bejt
                    necoNavic=slovo;
                    navrKod|=Util.md(0);
                }
            }
        }
    }


    @Override
    public String toString(){
        String vratit = "--------------------------\r\n   Parametry prikazu ip address" +
                ":\r\n\tnavratovyKodParseru: "
                + Util.rozlozNaLogaritmy2(navrKod);
        vratit += "\r\n\takce: "+akce;
        if(adresa!=null)vratit += "\r\n\tzapsana adresa: "+adresa;
        if(rozhrRet!=null)vratit +=  "\r\n\tzapsane rozhrani: "+rozhrRet;
        if(necoNavic!=null)vratit +=  "\r\n\tnecoNavic: "+necoNavic;

        if(adr!=null)vratit += "\r\n\tnastavena adresa: "+adr.toString();
        if(rozhr!=null)vratit +=  "\r\n\tnastavene rozhr: "+rozhr.name;
        vratit += "\r\n--------------------------";
        return vratit;
    }

    private void vypisHelp() {
        printLine("Usage: ip addr {add|del} IFADDR dev STRING");
        printLine("       ip addr {show|flush} [ dev STRING ] [ scope SCOPE-ID ]");
        printLine("                            [ to PREFIX ] [ FLAG-LIST ] [ label PATTERN ]");
        printLine("IFADDR := PREFIX | ADDR peer PREFIX");
        printLine("          [ broadcast ADDR ] [ anycast ADDR ]");
        printLine("          [ label STRING ] [ scope SCOPE-ID ]");
        printLine("SCOPE-ID := [ host | link | global | NUMBER ]");
        printLine("FLAG-LIST := [ FLAG-LIST ] FLAG");
        printLine("FLAG  := [ permanent | dynamic | secondary | primary |");
        printLine("           tentative | deprecated ]");
    }



}
