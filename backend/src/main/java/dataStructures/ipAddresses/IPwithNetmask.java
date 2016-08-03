/*
 * Erstellt am 28.10.2011.
 */
package dataStructures.ipAddresses;

/**
 * Represents IP with netmask.
 * Pouziva se u nastaveni rozhrani, v routovaci tabulce apod.
 * @author Tomas Pitrinec
 */
public class IPwithNetmask {

// vnitrni representace ip a maskou
    private IpAddress ip;
    private IpNetmask mask;

// konstruktory: ----------------------------------------------------------------------------------------------------

	/**
	 * Creates a new instance from string of IP address, the mask is computed automatically.
	 * @param adr
	 */
    public IPwithNetmask(String adr) {
        ip = new IpAddress(adr);
        dopocitejMasku();
    }

	/**
	 * Creates new instance from two strings.
	 * @param adr
	 * @param maska
	 */
    public IPwithNetmask(String adr, String maska) {
        ip = new IpAddress(adr);
        mask = new IpNetmask(maska);
    }

	/**
	 * Creates new instance from IP as string and mask as number of bits (tedy pocet jednickovejch bitu).
	 * @param adr
	 * @param maska number of bits
	 */
    public IPwithNetmask(String adr, int maska) {
        ip = new IpAddress(adr);
        mask = new IpNetmask(maska);
    }

	/**
	 * Creates new instance from existing IP and mask as number of bits.
	 * @param adr
	 * @param maska number of bits
	 */
    public IPwithNetmask(IpAddress adr, int maska) {
        ip = adr;
        mask = new IpNetmask(maska);
    }

	/**
	 * Creates new instance from existing IP and netmask.
	 * @param adr
	 * @param mask
	 */
    public IPwithNetmask(IpAddress adr, IpNetmask mask) {
		ip = adr;
		this.mask = mask;
	}

    /**
     * Vytvori adresu s maskou ze zadaneho Stringu, kde muze nebo nemusi byt
     * zadana adresa za lomitkem. <br /> Je-li moduloMaska nastaveno na true,
     * maska za lomitkem se vymoduluje 32. POZOR: tzn., ze i maska /32 se
     * vymoduluje na /0! (takhle funguje LinuxIfconfig i LinuxRoute)<br /> Je-li
     * modulo maska nastaveno na false, musi byt maska spravna, tzn. v intervalu
     * <0,32>. <br /> Na chybny vstupy to hazi SpatnaMaskaException nebo
     * SpatnaAdresaException, pricemz, kdyz je spatny oboje, ma
     * SpatnaAdresaException prednost. <br />
     * Nebyla-li maska zadana ve stringu, nastavi se defMaska.
     *
     * @param adrm
     * @param defMaska - pocet bitu, na -1 se maska dopocitava
     * @param moduloMaska
     */
    public IPwithNetmask(String adrm, int defMaska, boolean moduloMaska) {
        //nejdriv se pro jistotu zkontrolujou zadany hodnoty:
        if (moduloMaska && defMaska < -1 && defMaska > 32) {
            throw new RuntimeException("V programu nastala chyba, kontaktujte prosim tvurce softwaru.");
        }

        int lomitko = adrm.indexOf('/');
        if (lomitko == -1) { // retezec neobsahuje lomitko
            ip = new IpAddress(adrm); //nastavuje se adresa
            if (defMaska < 0) {
                dopocitejMasku();
            } else {
                mask = new IpNetmask(defMaska);
            }
        } else {  // je to s lomitkem, musi se to s nim zparsovat
            String adr = adrm.substring(0, lomitko);
            ip = new IpAddress(adr); //nastavuje se uz tady, aby prvni vyjimka se hazela na adresu
            String maska = adrm.substring(lomitko + 1, adrm.length());
            int m;
            //kontrola masky, jestli je to integer:
            try {
                m = Integer.parseInt(maska);
            } catch (NumberFormatException ex) {
                throw new BadNetmaskException();
            }
            if (moduloMaska) { //pripadne prepocitani masky:
                m = m % 32;
            }
            mask = new IpNetmask(m);  // nastaveni masky
        }
    }

// metody pro porovnavani (mezi sebou), vypisy a getry: --------------------------------------------------------------------------------------------------

    public IpAddress getIp() {
        return ip;
    }

    public IpNetmask getMask() {
        return mask;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IPwithNetmask other = (IPwithNetmask) obj;
        if (this.ip != null && other.ip != null && ! this.ip.equals(other.ip) ) {
            return false;
        }
        if (this.mask != null && other.mask != null && ! this.mask.equals(other.mask) ) {
            return false;
        }
        return true;
    }

	/**
	 * Returns IP address with slash and netmask as number of bits.
	 * Vypise jako IP lomitko pocet bitu.
	 * @return
	 */
    @Override
    public String toString() {
        return ip.toString() + "/" + mask.getNumberOfBits();
    }





// verejny metody poskytujici dalsi informace o siti a podobne: ----------------------------------------------------------------------

    public IpAddress getBroadcast() {
        return IpAddress.createIpFromBits( cisloSite() | ~mask.getBits() );
    }

    public IPwithNetmask getNetworkNumber(){
        IpAddress adr = IpAddress.createIpFromBits(cisloSite());
        return new IPwithNetmask(adr,mask);
    }

    /**
     * Vraci true, kdyz je adresa s maskou cislem site.
     * @return
     */
    public boolean isNetworkNumber(){
        if(this.equals(getNetworkNumber())) return true;
        else return false;
    }

	/**
	 * Vraci true, kdyz je adresa s maskou broadcastem site.
	 * @return
	 */
	public boolean isBroadcast() {
		if (this.getIp().equals(getBroadcast())) return true;
		else return false;
	}

    /**
     * Vraci true, jestlize adresa v parametru (comparedIP je v moji siti. Porovnava se to podle toho, jestli maj stejny cisla site.
     * Returns true, if comparedIP is in my network.
     * @param comparedIP
     * @return
     */
    public boolean isInMyNetwork (IpAddress comparedIP){
        int druhyCisloSite = comparedIP.getBits() & mask.bits; //jakoby cislo site ty porovnavany
        if (druhyCisloSite == cisloSite()) return true; //tady se ty dve cisla site porovnavaj
        else return false;
    }

// privatni metody: ----------------------------------------------------------------------------------------------------------------------

	/**
	 * Setts the automatically computed mask. The mask is computed from IP address according to rules in comentar.
	 *
	 * Nastavi dopocitanou masku, podle tridy IP. Vyuziva se v konstruktoru, kdyz neni maska zadana.
	 */
	private void dopocitejMasku() {

//            A 	0 	0–127    	255.0.0.0 	7 	24 	126 	16 777 214
//            B 	10 	128-191 	255.255.0.0 	14 	16 	16384 	65534
//            C 	110 	192-223 	255.255.255.0 	21 	8 	2 097 152 	254
//            D 	1110 	224-239 	multicast
//            E 	1111 	240-255 	vyhrazeno jako rezerva

		int bajt = IpAddress.bitsToArray(ip.getBits())[0]; //tady je ulozenej prvni bajt adresy
		if (bajt < 128) {
			mask = new IpNetmask(8);
		} else if (bajt >= 128 && bajt < 192) {
			mask = new IpNetmask(16);
		} else if (bajt >= 192) {
			mask = new IpNetmask(24);
		}
	}

    /**
     * Da vnitrni representaci cisla site.
     * @return
     */
    private int cisloSite() { //vraci 32bit integer
        return mask.getBits() & ip.getBits();
    }
}
