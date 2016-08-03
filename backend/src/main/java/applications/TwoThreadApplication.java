/*
 * Erstellt am 13.3.2012.
 */
package applications;

import device.Device;

/**
 * Toto je trida pro aplikace, ktery nejen ze poslouchaj pozadavky ze site, ale taky sami neco delaji. K tomu, aby mohla
 * delat obe veci najednou ma 2 vlakna, jedno klasicky pres SmartRunable, ktery se probouzi a zase uspava podle
 * pozadavku ze site a druhy uplne normalni javovsky vlakno, ktery neco udela a pak se ukonci. Takhle funguje napr. ping
 * nebo traceroute.
 *
 * @author Tomas Pitrinec
 */
public abstract class TwoThreadApplication extends Application implements Runnable {

	private Thread myThread;
	/**
	 * Jestli se ma bezet nebo uz skoncit. Potomci si ho sami musi volat!
	 */

	public TwoThreadApplication(String name, Device device) {
		super(name, device);
		myThread = new Thread(this, device.getName()+": TwoThreadedApp_vlakno_na_popredi");
	}

	/**
	 * Prepisuju metodu zdedenou po aplikaci, aby se v ni spustilo to moje vlakno.
	 */
	@Override
	public synchronized void start() {
		super.start();
		myThread.start();
	}

	public String getMyThreadName(){
		return myThread.getName();
	}
}
