/*
 * Erstellt am 13.3.2012.
 */
package utils;

/**
 * Rozhrani ktery musej implementovat tridy, ktery chtej pouzivat budik.
 * @author Tomas Pitrinec
 */
public interface Wakeable {

	/**
	 * Slouzi jen ke vzbuzeni, samotna cinnost se musi delat v jinym vlakne.
	 */
	public void wake();

}
