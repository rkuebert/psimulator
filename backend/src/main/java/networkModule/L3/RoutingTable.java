/*
 * Geschrieben am Mi 22.2.2012
 */

package networkModule.L3;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Implementation of Routing Table.
 * Datova struktura na ukladani routovacich zaznamu, jinak nic nedela.
 * Z velky casti prekopirovana ze stary routovaci tabulky, public metody prejmenovany do anglictiny.
 * @author neiss
 */
public class RoutingTable implements Loggable{


	private List<Record> records = new LinkedList<>();

	/**
     * Special pro cisco, nevim, co to dela.
     */
    public boolean classless = true;



	/**
	 * Tahleta metoda hleda zaznam v routovaci tabulce, ktery odpovida zadane IP adrese a cely ho vrati. Slouzi
	 * predevsim pro samotne routovani, kdyz potrebuju znat cely zaznam, a ne jen iface (napr. na jakou branu to mam
	 * poslat, jestli se to odesle. Pozor: vrati prvni odpovidajici zaznam! Pozor: routuje jen nad nahozenejma
	 * rozhranima!
	 *
	 * @param cil IP, na kterou je paket posilan
	 * @return null - nenasel se zadnej zaznam, kterej by se pro tuhle adresu hodil
	 */
	public Record findRoute(IpAddress cil) {
		for (Record z : records) {
			if ( z.adresat.isInMyNetwork(cil) && z.iface.isUp) {
				return z; //vraci prvni odpovidajici iface
			}
		}
		return null;
	}

	/**
	 * Prida novej zaznam, priznaku UG, tzn na branu. V okamziku pridani musi bejt brana dosazitelna s priznakem U, tzn.
	 * na iface, ne gw. Lze pridat i zaznam s predvyplnenim rozhranim, i takovej ale musi mit branu uz dosazitelnou
	 * na tomhle iface.
	 *
	 * @param adresat musi bejt vyplnenej
	 * @param brana musi bejt vyplnena
	 * @param rozhr muze bejt null
	 * @return 0: v poradku<br /> 1: existuje stejny zaznam;<br /> 2: iface nenalezeno, pro zadaneho adresata
	 * neexistuje zaznam U<br />
	 */
	public int addRecord(IPwithNetmask adresat, IpAddress brana, NetworkInterface rozhr) {
		boolean rozhraniNalezeno = false;
		for (Record z : records) { //hledani spravnyho iface
			if (z.brana == null) { //tohle by moh bejt zaznam potrebnej zaznam priznaku U
				if (!rozhraniNalezeno && z.adresat.isInMyNetwork(brana)) { //nalezl se adresat brane odpovidajici
					if (rozhr == null) { //rozhrani neni zadano a je potreba ho priradit
						rozhr = z.iface; //takhle to opravdu funguje, 1. polozka se pocita
						rozhraniNalezeno = true;
						break;
					} else { //rozhrani bylo zadano a je potreba zjistit, jestli je brana v dosahu tohodle iface
						if (z.iface == rozhr) { //kdyz se rovnaji, je iface nalezeno
							rozhraniNalezeno = true;
							break;
						}
					}
				}
			}
		}
		if (!rozhraniNalezeno) {
			return 2; // iface nenalezeno, pro zadaneho adresata neexistuje zaznam U
		}
		Record z = new Record(adresat, brana, rozhr);
		return pridaniZaznamu(z);
	}

	/**
     * Prida novej zaznam priznaku U.
     * Spolecna metoda pro linux i cisco.
     * @param adresat ocekava IpAdresu, ktera je cislem site
     * @param rozhr predpoklada se, ze iface na pocitaci existuje
     * @return 0: v poradku<br /> 1: existuje stejny zaznam;<br />
     */
    public int addRecord(IPwithNetmask adresat, NetworkInterface rozhr){
        Record z=new Record(adresat, rozhr);
        return pridaniZaznamu(z);
    }


	/**
     * Prida novy zaznam na vlastni iface.
     * Extra cisco metoda.
     * @param adresat
     * @param rozhr
     * @param primo
     * @return
     * @author Stanislav Řehák
     */
    public int addRecord(IPwithNetmask adresat, NetworkInterface rozhr, boolean primo) {
        Record z=new Record(adresat, rozhr, primo);
        return pridaniZaznamu(z);
	}

	/**
     * Metoda na mazani zaznamu.
     * @param adresat musi byt zadan
     * @param brana muze byt null
     * @param rozhr muze byt null
     * @return true - zaznam smazan<br /> false - zaznam nenalezen - nic nesmazano
     */
    public boolean deleteRecord(IPwithNetmask adresat, IpAddress brana, NetworkInterface rozhr){
        Record z;
        for(int i=0;i<records.size();i++){
            z=records.get(i);
            if(z.adresat.equals(adresat)){ //adresati se rovnaj -> adept na smazani
                if( ( brana == null ) || ( z.brana!=null && z.brana.equals(brana) ) ){//zkracene vyhodnocovani
                            //-> brana nezadana nebo zadana a stejna existuje u zaznamu -> adept na smazani
                    if( rozhr==null || (rozhr!=null && rozhr==z.iface)){  //rozhrani nezadano, nebo zadano a
                        records.remove(i);                                     //odpovida -> smazat
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean deleteRecord(Record z){
        return records.remove(z);
    }

	/**
     * Smaze vsechny zaznamy (U i UG) na zadanem iface. Potreba pro mazani rout, kdyz se zmeni adresa nebo maska
     * na iface.
     * @param rozhr
     * @return pocet smazanych iface (spis pro ladeni, jinej efekt to asi nema)
     */
    public int flushRecords(NetworkInterface rozhr){
        int p = 0; //pocet smazanych zaznamu
        List <Record>smazat = new LinkedList(); //dela se to pres pomocnej seznam, protoze jinak hazela
                                                // java vyjimku ConcurrentModificationException
        for(Record z:records){
            if (z.iface == rozhr){
                smazat.add(z);
                p++;
            }
        }
        for(Record z:smazat){
            records.remove(z);
        }
        return p;
    }

	/**
	 * Smaze uplne vsechny zaznamy v routovaci tabulce.
	 * Odpovida smazVsechnyZaznamy ve stary verzi simulatoru.
	 */
	public void flushAllRecords() {
		records = new LinkedList<>();
    }

    @Deprecated
    public String vypisSeLinuxove(){
        String v="";
        v+="Směrovací tabulka v jádru pro IP\n";
        v+="Adresát         Brána           Maska           Přízn\t Metrik\t Odkaz\t  Užt\t Rozhraní\n";
        for (Record z:records){
            v+=z.vypisSeLinuxove();
        }
        return v;
    }

	/**
	 * Returns number of records in routing table.
	 * @return
	 */
    public int size(){
        return records.size();
    }

    /**
     * Vrati zaznam na urceny posici, iface pro vypisovaci metody.
     * @param posice
     * @return
     */
    public Record getRecord(int posice){
        return records.get(posice);
    }

    /**
	 * Kontroluje, jestli routovaci tabulka neobsahuje uz zaznam s totoznym adresatem (tzn. musi se rovnat adresa i
	 * maska), jestlize ano, vrati ho. Pouziva se v LinuxIpRoute.
	 *
	 * @return null, kdyz se zadnej zaznam nenajde
	 */
	public Record existRecordWithSameAdresat(IPwithNetmask adresat) {
		for (Record z : records) {
			if (z.adresat.equals(adresat)) {   // adresati se rovnaji
				return z;
			}
		}
		return null;
	}

	/**
     * Tahleta metoda slouží k přidávání záznamů z konfiguráku. Neprováděj se žádný kontroly, prostě se to tam
     * přidá. Adresat a iface musej bejt vyplneny, brana jen u priznaku
     * UG, tzn. u routy na branu.
     * @param adresat
     * @param brana
     * @param rozhr
     * @author Stanislav Řehák
     */
    public void addRecordWithoutControl(IPwithNetmask adresat, IpAddress brana, NetworkInterface rozhr) {
		if (!adresat.isNetworkNumber()) {
			throw new RuntimeException("Chyba v konfiguracnim souboru, adresat " + adresat.toString()
					+ " v routovaci tabulce neni cislem site. ");
		}
        Record z = new Record(adresat,brana,rozhr);
        records.add(najdiSpravnouPosici(z), z);
    }

	@Override
	public String getDescription() {
		return "RoutingTable";
	}



// privatni metody ----------------------------------------------------------------------------------------------------

    /**
     * Prida zaznam na spravnou pozici. Jen vytazeny radky z puvodnich metod na pridani zaznamu U nebo UG
     * @param z
     * @return
     * @author Stanislav Řehák
     */
    private int pridaniZaznamu(Record z) {
        if(existujeStejnyZaznam(z))return 1;
        int i=najdiSpravnouPosici(z);
        records.add(i,z);
        return 0;
    }

	    /**
     * Kontroluje, jestli tabulka uz pridavany radek neobsahuje. Zaznam musi obsahovat adresata a iface
     * (to je predem zjisteno), brana se kontroluje, jen kdyz neni null.
     * @param zazn
     * @return
     */
    private boolean existujeStejnyZaznam(Record zazn){
        for(Record z:records){
            if( z.adresat.equals(zazn.adresat) ){   // adresati se rovnaji
                if ( z.brana==null && zazn.brana==null){ //obe brany jsou null
                    if( z.iface==zazn.iface){
                        return true;
                    }
                }
                if(z.brana!=null && zazn.brana!=null){ //obe brany nejsou null a rovnaji se
                    if(z.brana.equals(zazn.brana) && z.iface==zazn.iface){
                        return true;
                    }
                }
            }
        }
        return false;
    }

	/**
	 * Najde spravnou posici pro pridani novyho zaznamu. Skutecny poradi rout je totalne zmatecny (viz. soubor route.txt
	 * v package data), takze to radim jenom podle masky, nakonec ani priznaky nerozhodujou.
	 *
	 * @param z
	 * @return
	 */
	private int najdiSpravnouPosici(Record z) {
		if (z.adresat.getMask().getBits() == 0) {
			return records.size();
		}
		int i = 0;
		//preskakovani delsich masek:
		//pozor, problemy v implementaci kvuli doplnkovymu kodu
		while (i < records.size() //neprekrocit meze
				&& records.get(i).adresat.getMask().getBits() > z.adresat.getMask().getBits() //dokud je vkladana maska mensi
				&& records.get(i).adresat.getMask().getBits() != 0) { //pozor na nulu
			i++;
		}//zastavi se na stejny nebo vetsi masce, nez ma pridavanej zaznam
		//vic se nakonec uz nic neposouva...
		return i;
	}

// --------------------------------------------------------------------------------------------------------------------

	/**
	 * Trida pro jeden radek v routovaci tabulce.
	 * Prakticky cela prekopirovana z minulyho simulatoru.
	 */
	public class Record {

        public final IPwithNetmask adresat;   // ty promenny jsou privatni, nechci, aby se daly zvenci upravovat
		/**
		 * Brana na kterou se bude paket posilat.
		 * Neni-li vyplnena, je to null.
		 */
        public final IpAddress brana;
		/**
		 * Rozhrani na ktere se bude posilat.
		 */
        public final NetworkInterface iface;
        private boolean connected = false; // indikuju, zda tento zaznam je na primo pripojene iface, spise pro cisco

		/**
		 * Potreba pro cisco.
		 * @return
		 */
        public boolean jePrimoPripojene() {
            return connected;
        }

		public Record(IPwithNetmask adresat, NetworkInterface rozhrani) {
			this.adresat = adresat;
			this.iface = rozhrani;
			brana=null;
		}

        /*
         * Konstruktur pro cisco.
         */
        private Record(IPwithNetmask adresat, NetworkInterface rozhrani, boolean pripojene){
            this.adresat=adresat;
            this.iface=rozhrani;
            this.connected = pripojene;
			brana=null;
        }

		private Record(IPwithNetmask adresat, IpAddress brana, NetworkInterface rozhrani){
            this.adresat=adresat;
            this.brana=brana;
            this.iface=rozhrani;
        }

        /*
         * Konstruktur pro cisco.
         */
        private Record(IPwithNetmask adresat, IpAddress brana, NetworkInterface rozhrani, boolean pripojene){
            this.adresat=adresat;
            this.brana=brana;
            this.iface=rozhrani;
            this.connected = pripojene;
        }

        @Deprecated //neni potreba
        private String vypisSeLinuxove() {
            String v="";
            v+=adresat.getIp()+"\t";	//neni sem potreba psat toString(), doplni se to samo
            if(brana==null){
                v+="0.0.0.0\t"+adresat.getMask()+"\tU\t";
            } else{
                v+=brana.toString()+"\t"+adresat.getMask()+"\tUG\t";
            }
            v+="0\t0\t0\t"+iface.name+"\n";
            return v;
        }
    }


}
