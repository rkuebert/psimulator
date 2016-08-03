/*
 * Erstellt am 28.10.2011.
 */

package dataStructures.ipAddresses;


/**
 * Represents IPv4 netmask.
 * @author Tomas Pitrinec
 */
public class IpNetmask extends IpAddress{

    public IpNetmask(String dlouhejFormat) {
        super(dlouhejFormat);
        if(! jeMaskou(bits)){
            throw new BadNetmaskException();
        }
    }

    /**
     * Ocekava to masku jako integer, kolik prvnich bitu maj bejt jednicky
     * @param maska
     */
    public IpNetmask(int numberOfBits) {
        if (numberOfBits > 32 || numberOfBits < 0) {
            throw new BadNetmaskException();
        }
        this.bits = 0;
        for (int i = 0; i < numberOfBits; i++) {
            bits = bits | 1 << (31 - i);
        }
    }

	public static IpNetmask maskFromWildcard(String dlouhejFormat) {
		IpAddress adr = new IpAddress(dlouhejFormat);
		IpAddress prevracena = IpAddress.negateAddress(adr);
		return new IpNetmask(prevracena);
	}

    /**
     *
     * @param vzor
     */
    private IpNetmask (IpAddress vzor){
        this.bits=vzor.bits;
        if(! jeMaskou(bits)){
            throw new BadNetmaskException();
        }
    }

    /**
     * Vrati pocet jednickovych bitu masky.
     * @return
     */
    public int getNumberOfBits(){
        if (bits == 0) {
            return 0;
        }
        int pocet = 32;
        int maska = this.bits;
        while ((maska & 1) == 0) {
            maska = maska >> 1;
            pocet--;
        }
        return pocet;
    }

	/**
     * Spocita wildcard z masky a vrati ho jako retezec.
     * @return
     * @author Stanislav Řehák
     */
    public String getWildcardRepresentation() {
        long broadcast = (long)(new IPwithNetmask("255.255.255.255").getBroadcast().getBits());
        long mask = (long) bits;
        long wc = broadcast - mask;
        IpAddress wildcard = IpAddress.createIpFromBits((int)wc);
        return wildcard.toString();
    }

// staticky metody:

    /**
     * Returns netmask, if given string is netmask, othervise returns null.
     * @param ret
     * @return
     */
    public static IpNetmask correctNetmask(String ret){
        IpAddress ip = correctAddress(ret);
        if (ip==null)
            return null;
        else {
            try {
                IpNetmask  mask = new IpNetmask(ip);
                return mask;
            } catch (BadNetmaskException ex) {
                return null;
            }
        }
    }

	public static boolean isCorrectNetmask(String ret) {
		if (correctNetmask(ret) != null) {
			return true;
		}
		return false;
	}

    /**
     * Vraci true, kdyz je zadany integer maskou, tzn., kdyz jsou to nejdriv jednicky a pak nuly.
     * @param maska
     * @return
     */
    private static boolean jeMaskou(int maska) {
        int i = 0;
        while (i < 32 && (maska & 1) == 0) { //tady prochazeji nuly
            i++;
            maska = maska >> 1;
        }
        while (i < 32 && (maska & 1) == 1) { //tady prochazeji jednicky
            i++;
            maska = maska >> 1;
        }
        if (i == 32) {
            return true;
        }
        return false;
    }



}
