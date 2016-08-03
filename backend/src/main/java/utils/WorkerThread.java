/*
 * Erstellt am 27.10.2011.
 */
package utils;

import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Thread implements wake and run functions. It sleeps itselfs.
 * @author Tomas Pitrinec
 * @author Stanislav Rehak
 */
public final class WorkerThread implements Runnable, Loggable {

    private Thread myThread;

	/**
	 * Ma-li vlakno spat v metode sleep. Na zacatku spi. Po umreni vlakna nespi, to uz je mrtvy.
	 */
    private volatile boolean isSleeping = true;
	private SmartRunnable smartRunnable;

	/**
	 * Jestli ma vlakno umrit.
	 */
	private boolean dieCalled = false;

    public WorkerThread(SmartRunnable smartRunnable) {
		assert smartRunnable != null;
		this.smartRunnable = smartRunnable;
        myThread = new Thread(this,smartRunnable.getDescription());
        myThread.start();
    }

	/**
	 * Wakes thread so it can work.
	 */
	public synchronized void wake() {

		// bylo-li uz narizeno umrit, nic se nedela
		if (dieCalled) {
			return;
		}

		// kdyz nebylo narizeno umrit, zkusi se to spustit:
		if (isSleeping) {	//kdyz nebezi tak se zapne
			isSleeping = false;
			this.notifyAll();

		} else if (smartRunnable.getClass() == Alarm.class) {	// special function only for alarm !!! - specialni fce JEN PRO BUDIK !!!
			if (myThread.getState() == Thread.State.TIMED_WAITING) {
				myThread.interrupt();
			}
		}

	}

	/**
	 * Wakes thread and dies.
	 */
	public synchronized void die() {

		if (!dieCalled) {	// kdyz uz ho nekdo neusmrtil
			this.dieCalled = true;

			if (isSleeping) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.THREADS, "Vlaknem " + Thread.currentThread().getName() + " na me byla poprve zavolana metoda die a ja jdu notifikovat.", null);
				isSleeping = false;
				this.notifyAll();
			} else {
				Logger.log(this, Logger.DEBUG, LoggingCategory.THREADS, "Vlaknem " + Thread.currentThread().getName()
						+ " na me byla poprve zavolana metoda die, ale vlakno bezi, takze neni potreba notifikovat.", null);
			}
		}
	}

	/**
	 * This function should be never called! It is called automaticaly by thread management.
	 */
	@Override
	public void run() {

		while (!dieCalled) {	// ma-li se umrit kdyz zrovna nebezi doMyWork, umre se okamzite

			try { // tohleto try je tady pro odchytavani veskerejch vyjimek, takhle vlakno neumre, ale i pres vyjimku pokracuje dal
				smartRunnable.doMyWork();
			} catch (Exception e) {
				Logger.log(this, Logger.WARNING, LoggingCategory.THREADS, "Some exception occured: "+e.toString(), e);
			}

			if (!dieCalled) {	// ma-li se umrit po metode doMyWork nespousti se sleep, tzn. jde se na zacatek a skonci se
				sleep();
			}

		}
		Logger.log(this, Logger.DEBUG, LoggingCategory.THREADS, "Moje vlakno se definitivne konci.", null);
	}

	/**
	 * Synchronizovana metoda na spani. Pred 13.3. byla v synchronizovanym bloku v ramci metody run, ted jsem ji pro prehlednost vyclenil sem.
	 */
	private synchronized void sleep() {
		isSleeping = true;
		while (isSleeping) {
			//tenhlecten cyklus je tady proti nejakejm falesnejm buzenim.
			try {
				wait();
			} catch (InterruptedException ex) {
				Logger.log(this, Logger.ERROR, LoggingCategory.THREADS, "InterruptedException in the sleep! Is it normal on not?", ex);
			}
		}
	}

	@Override
	public String getDescription() {
		return "WorkerThread ("+getThreadName()+")";
	}

	public String getThreadName(){
		return myThread.getName();
	}
}
