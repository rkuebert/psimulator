/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

import java.util.Arrays;
import logging.Logger;
import logging.LoggingCategory;
import utils.Util;

/**
 * Implementation of mac address. Vnitrni representace je pomoci integeru, protoze s bytama byl problem se znainkem.
 *
 * @author neiss
 */
public class MacAddress {

	private final byte[] representation;

	/**
	 * Vyjimka pro tvoreni mac adresy.
	 */
	public static class BadMacException extends RuntimeException {

		public BadMacException(String msg) {
			super(msg);
		}
	}

// Konstruktory -----------------------------------------------------------------------------------------------
	/**
	 * Klasickej konstuktor, ocekava mac adresu oddelenou cimkoliv, ale po dvou znakach. Deli se to podle tretiho znaku.
	 *
	 * @param address
	 */
	public MacAddress(String address) {
		this.representation = stringToBytes(address, address.charAt(2));
	}

	/**
	 * Predpoklada mac adresu oddelenou zadanym oddelovacem (napr. pomlckou apod.)
	 *
	 * @param address
	 * @param delimiter
	 */
	public MacAddress(String address, char delimiter) {
		this.representation = stringToBytes(address, delimiter);
	}

	/**
	 * Creates mac address from array of bytes. The array is copied in constructor.
	 *
	 * To pole se v konstruktoru pro jistotu kopiruje, kdyby ho nekde nekdo mezitim menil.
	 *
	 * @param representation
	 */
	public MacAddress(byte[] representation) {
		this.representation = Arrays.copyOf(representation, 6);
	}


// vypisy a porovnavani ---------------------------------------------------------------------------
	/**
	 * Vrati klasickej vypis s oddelenim dvojteckama.
	 *
	 * @return
	 */
	@Override
	public String toString() {
		String vratit = "";
		for (int i = 0; i < 6; i++) {
			vratit += byteToString(representation[i]);
			vratit += ":";
		}
		return vratit.substring(0, vratit.length() - 1);//aby se odmazala ta posledni dvojtecka
	}

	/**
	 * Vrati ve formatu aabb.ccdd.eeff
	 * @return
	 */
	public String getCiscoRepresentation() {
		String vratit = "";
		for (int i = 0; i < 6; i++) {
			vratit += byteToString(representation[i]);
			if (i % 2 == 1) {
				vratit += ".";
			}
		}
		return vratit.substring(0, vratit.length() - 1);//aby se odmazala ta posledni dvojtecka
	}

	/**
	 * Returns copy of this address inner representation.
	 * @return
	 */
	public byte [] getByteArray(){
		return Arrays.copyOf(representation, 6);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MacAddress other = (MacAddress) obj;
		if (!Arrays.equals(this.representation, other.representation)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 23 * hash + Arrays.hashCode(this.representation);
		return hash;
	}

	/**
	 * Porovnavani. Potreba u spanning tree algoritmu.
	 *
	 * @param other
	 * @return
	 */
	public boolean isLessOrEqualThan(MacAddress other) {
		return isByteLessThan(other, 0);
	}

	private boolean isByteLessThan(MacAddress other, int cisloBytu) {
		if (representation[cisloBytu] < other.representation[cisloBytu]) {
			return true;
		} else if (representation[cisloBytu] > other.representation[cisloBytu]) {
			return false;
		} else if (cisloBytu < 5) {
			return isByteLessThan(other, cisloBytu + 1);
		} else {
			return true;
		}
	}

// staticky metody ----------------------------------------------------------------------------------------

	/**
	 * cashovana mac adresa
	 */
	private static MacAddress broadcast;


	/**
	 * Returns true, if given mac address is broadcast
	 *
	 * @param mac
	 * @return
	 */
	public static boolean isBroadcast(MacAddress mac) {
		for (int i = 0; i < 6; i++) {
			if (mac.representation[i] != (byte) 0xff) {
				return false;
			}
		}
		return true;
	}

	public static MacAddress broadcast() {
		if (broadcast == null) {
			byte[] pole = new byte[6];
			for (int i = 0; i < 6; i++) {
				pole[i] = (byte) 255;
			}
			broadcast = new MacAddress(pole);
		}
		return broadcast;
	}

	public static MacAddress getRandomMac() {
		byte[] representation = new byte[6];
		for (int i = 0; i < 6; i++) {
			representation[i] = (byte) (Math.random() * 256);
		}

		MacAddress vratit = new MacAddress(representation);
		if (vratit.equals(broadcast())) {	// kdyby se nahodou vygenerovala broadcastova, tak se musi generovat znova
			return getRandomMac();
		} else {
			return vratit;
		}
	}

// privatni staticky metody -------------------------------------------------------------------------------
	private static byte[] stringToBytes(String adr, char delimiter) {
		byte[] vratit = new byte[6];
		String[] pole = adr.split("\\" + delimiter);
		if (pole.length != 6) {
			throw new BadMacException("Neni to sest bytu, nezparsovala se mac adresa " + adr);
		}
		for (int i = 0; i < 6; i++) {
			vratit[i] = stringToByte(pole[i]);
		}
		return vratit;
	}

	/**
	 * Vytvori z jednoho bytu jako stringu bajt jako integer.
	 *
	 * @param s
	 * @return
	 */
	private static byte stringToByte(String s) {
		if (s.length() != 2) {
			throw new BadMacException("Tenhleten bajt nema spravnou dylku: " + s);
		}
		try {
			return (byte)(Integer.parseInt(s, 16));
		} catch (NumberFormatException ex) {
			throw new BadMacException("Tenhleten bajt nema spravnou dylku: " + s);
		}
	}

	private static String byteToString(byte bajt) {
		String v = Integer.toHexString(Util.byteToInt(bajt));
		if (v.length() < 2) {
			return "0" + v;
		} else {
			return v;
		}
	}
}
