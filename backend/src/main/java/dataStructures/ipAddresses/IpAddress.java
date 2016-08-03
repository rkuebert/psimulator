/*
 * Erstellt am 27.10.2011.
 */

package dataStructures.ipAddresses;

import utils.Util;


/**
 * Representation of IPv4 IP adress.
 * @author Tomas Pitrinec
 */
public class IpAddress {

    /**
     * Inner representation of ip adress.
     */
    protected int bits;


// konstruktory: -----------------------------------------------------------------------------------------------

    /**
     * Creates an IP adress from String in format 1.2.3.4
     * @param ret
     */
    public IpAddress(String ret) {
        bits=stringToBits(ret);
    }

	/**
	 * Creates IP address from byte array.
	 * @param array
	 * @throws BadIpException iff the array hasn't length=4
	 */
	public IpAddress(byte[]array){
		bits = byteArrayToInt(array);
	}

    /**
     * Only for construktor classes in this package.
     */
    protected IpAddress(){}


// verejny metody: ----------------------------------------------------------------------------------------------
	/**
	 * Returns inner representation.
	 * @return
	 */
    public int getBits(){
        return bits;
    }

    /**
     * Vrati IP adresu jako string ve formatu 1.2.3.4
     * @return
     */
    @Override
    public String toString(){
        return bitsToString(bits);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IpAddress other = (IpAddress) obj;
        if (this.bits != other.bits) {
            return false;
        }
        return true;
    }

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 59 * hash + this.bits;
		return hash;
	}

	/**
     * Vrati long hodnotu z adresy. Vhodne pro porovnavani adres.
     * @param ip
     * @return
     * @author Stanislav Řehák
     */
    public long getLongRepresentation() {
        long l = 0;
        String[] pole = toString().split("\\.");
        l += Long.valueOf(pole[0]) * 256 * 256 * 256;
        l += Long.valueOf(pole[1]) * 256 * 256;
        l += Long.valueOf(pole[2]) * 256;
        l += Long.valueOf(pole[3]);
        return l;
    }

	/**
	 * Returns IP address as array of byte.
	 *
	 * @return
	 */
	public byte[] getByteArray() {
		int[] poleInt = bitsToArray(bits);
		byte[] vratit = new byte[4];
		for (int i = 0; i < 4; i++) {
			vratit[i] = (byte) poleInt[i];
		}
		return vratit;
	}

	/**
	 * Returns true iff IP addrees is from range 127.0.0.0 - 127.255.255.255.
	 * @return
	 */
	public boolean isLocalSubnet127() {
		int[] pole = bitsToArray(bits);
		if (pole[0] == 127) {
			return true;
		}
		return false;
	}


// staticky metody pro ruzny prevadeni a tak: ----------------------------------------------------------------------------------------

    /**
     * Vrati adresu utvorenou ze stringu, kdyz je zadanej string ip adresou, jinak vrati null.
     * @param ret
     * @return
     */
    public static IpAddress correctAddress(String ret){
        try{
            IpAddress ip = new IpAddress(ret);
            return ip;
        } catch (BadIpException bIP){
            return null;
        }
    }

	public static boolean isCorrectAddress(String ret) {
		if (correctAddress(ret) != null) {
			return true;
		}
		return false;
	}

    /**
     * Vrati adresu o jedna vetsi.
     * Udelany metosou pokus - omyl, ale testy prosly.
     * @param p
     * @return adresu o jednicku vetsi, maska bude 255.0.0.0
     * @author Stanislav Řehák
     */
    public static IpAddress nextAddress(IpAddress p){
        int nova=(int) ( (long)(p.bits) + 1L );
        return createIpFromBits(nova);
    }

    /**
     * Negates given ip.
     * Neguje ip adresu, tzn. tam, kde driv byly jednicky dava nuly a naopak.
     * Nahrada za Standovu starou vratMaskuZWildCard, narozdil od ni ale uz nezkouma, jestli je vysledek maskou.
     * @param ip
     * @return
     */
    public static IpAddress negateAddress(IpAddress ip){
        return createIpFromBits(~ip.bits);
    }

	/**
	 * Vrati true, kdyz je adresa v RFC zakazana, tedy kdyz je prvni bajt vetsi nez 223.
	 * Odpovida jeZakazanaIpAdresa ze staryho psimulatoru.
	 * @param ip
	 * @return
	 */
	public static boolean isForbiddenIP(IpAddress ip) {
		/*
        A 	0 	0–127    	255.0.0.0 	7 	24 	126 	16 777 214
        B 	10 	128-191 	255.255.0.0 	14 	16 	16384 	65534
        C 	110 	192-223 	255.255.255.0 	21 	8 	2 097 152 	254
        D 	1110 	224-239 	multicast
        E 	1111 	240-255 	vyhrazeno jako rezerva
         */
		int[] pole = bitsToArray(ip.getBits());
		if (pole[0] >= 224) {
			return true;
		} else {
			return false;
		}
	}

    /**
     * Vytvori adresu ze integeru vnitrni reprezentace (z bitu)
     * @param r - ta vnitrni reprezentace
     * @return
     */
    public static IpAddress createIpFromBits(int r){
        IpAddress vratit = new IpAddress();
        vratit.bits = r;
        return vratit;
    }


    /**
     * Ze stringu ve tvaru 1.2.3.4 vrati int vnitrni representace.
     * Pritom samozrejme zkontroluje spravnost a kdyztak vyhodi vyjimku.
     * @param ret
     */
    protected static int stringToBits(String adr) throws BadIpException{
        if (!adr.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")) {
            throw new BadIpException("Bad IP: \""+adr+"\"");
        }
        //uz vim, ze se to sklada z cisel, pokracuju tedy:
        String[] pole = adr.split("\\."); //pole Stringu s jednotlivejma cislama
        int bajt; //aktualni bajt
        int vysledek=0;
        for (int i = 0; i < 4; i++) {
            bajt = Integer.valueOf(pole[i]);
            if (bajt < 0 || bajt > 255) { //podle me ta kontrola musi bejt takhle
                throw new BadIpException();
            }
            vysledek = vysledek | bajt << (8 * (3 - i));
        }
        return vysledek;
    }

    /**
     * Bity prevadi na pole integeru.
     * @param cislo
     * @return
     */
    protected static int[] bitsToArray(int bits) { //prevadi adresu do citelny podoby
        int[] pole = new int[4];
        int tmp;
        for (int i = 0; i < 4; i++) {
            tmp = bits & (255 << (3 - i) * 8);
            pole[i] = tmp >>> ((3 - i) * 8);
        }
        return pole;
    }

    /**
     * Pole integeru prevadi na string.
     * @param array
     * @return
     */
    protected static String arrayToString(int[] array) {
        String ret = array[0] + "." + array[1] + "." + array[2] + "." + array[3];
        return ret;
    }

    /**
     * Ze zadanejch bitu vytvori string.
     * @param bits
     * @return
     */
    protected static String bitsToString(int bits) {
        return arrayToString(bitsToArray(bits));
    }

	protected static int byteArrayToInt(byte[] array) {
		int vratit = 0;
		if (array.length != 4) {
			throw new BadIpException("Given array has length=" + array.length);
		}
		for (int i = 0; i < 4; i++) {
			vratit = vratit * 256 + Util.byteToInt(array[i]);
		}
		return vratit;
	}








}
