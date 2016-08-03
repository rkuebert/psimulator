/*
 * created 24.1.2012
 */
package utils;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public interface SmartRunnable {

	public abstract void doMyWork();

	/**
	 * Description se bude pouzivat k pojmenovani vlakna. Bylo by sice logictejsi mit na to metodu getThreadName, ale
	 * takhle se muze vyuzit uz u vetsiny trid existujici metody.
	 *
	 * Metoda se nededi, aby v tom byl vetsi poradek.
	 *
	 * @return
	 */
	public abstract String getDescription();

}
