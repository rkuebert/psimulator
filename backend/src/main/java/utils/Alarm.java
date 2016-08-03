/*
 * Erstellt am 13.3.2012.
 */
package utils;

import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Budik. Vzbudi zaregitrovanej Wakeable.
 *
 * @author Tomas Pitrinec
 */
public class Alarm implements SmartRunnable, Loggable{

	/**
	 * Prioritni fronta klientu a jejich casu. Neexistuje synchronizovana prioritni fronta, proto se do ni bude davat
	 * specialnima matodama.
	 */
	private final SynchronizedQueue clients;
	protected final WorkerThread worker;

	public Alarm() {
		clients =  new SynchronizedQueue();
		this.worker = new WorkerThread(this);
	}

	@Override
	public void doMyWork() {

		while (!clients.isEmpty()) {

			long absNextTime = clients.peek().absTime;
			long relativeTimeToSleep = absNextTime - System.currentTimeMillis();


			if (relativeTimeToSleep <= 0) {	// kdyby budik zaspal, rovnou vzbudit
				try {	// jednou mi to tu hodilo NullPointer, nevim proc, radsi to kontroluju (ale je to asi zbytecny, jen pro jistotu)
					wakeObject(clients.poll());
				} catch (NullPointerException ex) {
					Logger.log(this,Logger.WARNING,LoggingCategory.ALARM,ex.getMessage(),ex);
				}

			} else { //nezaspal, jde spat:

				// spani:
				try {
					Logger.log(this, Logger.DEBUG, LoggingCategory.ALARM, "Jdu spat.", null);
					Thread.sleep(relativeTimeToSleep);
					// po vyspani probudim objekt:
					wakeObject(clients.poll());

				} catch (InterruptedException ex) {	// pri spani me nekdo prerusil
					// kdyz me neco vzbudi, musim znovu prepocitat dylku spanku - jdu na zacatek
				}
			}
		}
	}

	private void wakeObject(WakeableItem item) {
		if (item == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.ALARM, "Ke vzbuzeni mam objekt null, prusvih.", null);
		} else {
			Logger.log(this, Logger.DEBUG, LoggingCategory.ALARM, "Jdu vzbudit objekt", item.obj);
			item.obj.wake();
		}
	}

	/**
	 * Zaregistruje vzbuzeni.
	 *
	 * @param client klient, kterej se ma vzbudit
	 * @param relTime za jak dlouho se ma vzbudit (v milisekundach)
	 */
	public void registerWake(Wakeable client, long relTime) {
		long absTimeToWake = System.currentTimeMillis() + relTime;
		clients.add(new WakeableItem(absTimeToWake, client));
		Logger.log(this, Logger.DEBUG, LoggingCategory.ALARM, "Zaregistroval jsem objekt, mam ho vzbudit za "+relTime+" ms.", client);

		worker.wake();
	}

	@Override
	public String getDescription() {
		return "System alarm";
	}


	private class SynchronizedQueue {

		private final Queue<WakeableItem> fronta;

		public SynchronizedQueue() {
			fronta = new PriorityQueue<>();
		}

		private synchronized void add(WakeableItem item) {
				fronta.add(item);
		}

		private synchronized WakeableItem peek() {
			return fronta.peek();
		}

		private synchronized boolean isEmpty() {
			return fronta.isEmpty();
		}

		private synchronized WakeableItem poll(){
			return fronta.poll();
		}
	}

	/**
	 * Polozka fronty ke vzbuzeni.
	 */
	private class WakeableItem implements Comparable<WakeableItem> {

		public final long absTime;
		public final Wakeable obj;

		public WakeableItem(long absTime, Wakeable obj) {
			this.absTime = absTime;
			this.obj = obj;
		}

		@Override
		public int compareTo(WakeableItem o) {
			return ((Long) absTime).compareTo(o.absTime);
		}
	}






}
