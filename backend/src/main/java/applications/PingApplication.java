/*
 * created 8.3.2012
 */

package applications;

import commands.ApplicationNotifiable;
import dataStructures.DropItem;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.IcmpPacket;
import dataStructures.packets.IpPacket;
import device.Device;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logging.Logger;
import logging.LoggingCategory;
import psimulator2.Psimulator;
import utils.Util;
import utils.Wakeable;

/**
 * Represents abstract Ping application. <br />
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomas Pitrinec
 */
public abstract class PingApplication extends TwoThreadApplication implements Wakeable{

	protected IpAddress target;
	protected int count = -1;	// -1 znamena, ze se bude pingovat donekonecna
	protected int payload = 56; // default linux size (without header)
	protected int timeout = 10_000; // zrejme tedy v milisekundach
	protected Stats stats = new Stats();
	protected final ApplicationNotifiable command;

	/**
	 * kdyz nebude zadan, tak se pouzije vychozi systemova hodnota ze sitoveho modulu
	 */
	protected Integer ttl = null;
	/**
	 * Time to wait between sending to pings.
	 */
	protected int waitTime = 1_000;
	/**
	 * Key - seq <br />
	 * Value - timestamp in ms
	 */
	protected Map<Integer, Double> timestamps = new HashMap<>();

//	protected boolean [] sent;
//	protected boolean [] recieved;
	int lastSent = 0; // seq number of last sent packet
	int lastReceived = 0;	// seq number of last received packet

	private transient boolean zavolanoBudikem = false;

	public PingApplication(Device device, ApplicationNotifiable command) {
		super("ping", device);
		this.command = command;
	}

// metody pro predavani informaci z jinejch vlaken -------------------------------------------------------------------

	@Override
	public void wake() {
		if (isRunning()) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Byl jsem probuzen budikem a jdu zavolat svuj worker.", null);
			zavolanoBudikem = true;
			worker.wake();
		} else {
			Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Byl jsem probuzen budikem ale nevolam svuj worker, protoze mam bejt mrtvej.", null);
		}
	}

	//pak je tady jeste receivePacket, ktera je podedena od Application



// metody na vyrizovani sitovejch pozadavku: --------------------------------------------------------------------------

	/**
	 * Dela totez co jinde u ruznejch modulu, tzn kontroluje buffery a vyrizuje je. Poprve je spustena k vyrizeni prvniho pozadavku nejakym jinym vlaknem.
	 */
	@Override
	public void doMyWork() {

		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Spustena metoda doMyWork vlaknem "+Util.threadName(), null);

		IcmpPacket packet;

		while (!buffer.isEmpty()) {
			IpPacket p = buffer.remove(0).packet;

			// zkouseni, jestli je ten paket spravnej:
			if (! (p.data instanceof IcmpPacket)) {
				Logger.log(this, Logger.WARNING, LoggingCategory.PING_APPLICATION, "Dropping packet: PingApplication recieved non ICMP packet", p);
				Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(p, device.configID));
				continue;
			}

			// parovani k odeslanymu paketu, reseni duplikaci:
			packet = (IcmpPacket) p.data;
			Double sendTime = timestamps.get(packet.seq);
			timestamps.remove(packet.seq); // odstranim uz ulozeny
			// TODO: resit nejak lip duplikace paketu, zatim se to loguje:
			if (sendTime == null) {
				Logger.log(this, Logger.WARNING, LoggingCategory.PING_APPLICATION, "Dropping packet: PingApplication doesn't expect such a PING reply "
						+ "(IcmpPacket with this seq="+packet.seq+" was never send OR it was served in a past)", p);
				Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(p, device.configID));
				continue;
			}

			double delay = ((double)System.nanoTime())/1_000_000 - sendTime;

			// vsechno v poradku, paket se zpracuje:
			if (delay <= timeout) { // ok, paket dorazil vcas
				Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Dorazil mi nejaky ping.", packet);

				if (packet.type != IcmpPacket.Type.REPLY) {
					stats.errors++;
				} else {
					stats.odezvy.add(delay);
					stats.prijate++;
				}
				handleIncommingPacket(p, delay);
				lastReceived = packet.seq;

			} else {
				Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Dorazil mi nejaky ping, ale vyprsel timeout.", packet);
			}

			// reseni posledniho paketu:
			if(lastReceived == count){
				Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Prisel mi posledni paket. Koncim.", packet);
				exit();
			}
		}

		if(zavolanoBudikem){
			exit();
		}
		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Opustena metoda doMyWork vlaknem "+Util.threadName(), null);

	}



// metody na delani vlastni prace: ------------------------------------------------------------------------------------

	/**
	 * Tahleta metoda bezi ve vlastnim javovskym vlakne ty aplikace. Posila pingy.
	 */
	@Override
	public void run() {
		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Spustena metoda run vlaknem "+Util.threadName(), null);
		int i = 0;
		while ((i < count || count == -1) && isRunning()) {	// bezi se jen dokud je running
			int seq = i + 1;
			Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, getName() + " posilam ping seq=" + seq, null);
			timestamps.put(seq, (double)System.nanoTime()/1_000_000);
			lastSent = seq;
			transportLayer.icmpHandler.sendRequest(target, ttl, seq, port, payload);
			stats.odeslane++;

			if (seq != count) {	// po poslednim odeslanym paketu uz se neceka
				Util.sleep(waitTime);	// cekani
			} else {	// ale nastavi se budik:
				Psimulator.getPsimulator().budik.registerWake(this, timeout);
			}
			i++;
		}
		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Konci metoda run. Opustena vlaknem "+Util.threadName(), null);
	}

// abstraktni metody, ktery je potreba doimplementovat v konkretnich pingach: ----------------------------------------

	/**
	 * Print stats and exits application.
	 */
	public abstract void printStats();

	/**
	 * Handles incomming packet: REPLY, TIME_EXCEEDED, UNDELIVERED.
	 *
	 * @param delay delay in miliseconds
	 */
	protected abstract void handleIncommingPacket(IpPacket p, double delay);

	/**
	 * Slouzi na hlasku o tom kolik ceho a kam posilam..
	 */
	protected abstract void startMessage();






// metody spousteny pri startovani a ukoncovani aplikace: -------------------------------------------------------------


	@Override
	protected synchronized void atExit() {
		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Zavolana metoda atExit vlaknem "+Thread.currentThread().getName(), null);
		stats.countStats();
		printStats();
		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Ukoncena metoda atExit. ", null);
	}

	@Override
	protected void atKill(){
		command.applicationFinished();
	}


	@Override
	protected void atStart() {
		//kontrola cilovy adresy:
		if (target == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.GENERIC_APPLICATION, "PingApplication has no target! Exiting..", null);
			kill();
		}

		//kontrola, jestli mam port:
		if (getPort() == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.GENERIC_APPLICATION, "PingApplication has no port assigned! Exiting..", null);
			kill();
		}

		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, getName()+" atStart()", null);
		startMessage();

	}


// ostatni public metody, povetsinou gettry a settry: ----------------------------------------------------------------

	public void setCount(int count) {
		this.count = count;
	}

	public void setSize(int size) {
		this.payload = size;
	}

	public void setTarget(IpAddress target) {
		this.target = target;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString(){
		return "ping_app "+PID;
	}




// statistiky: -------------------------------------------------------------------------------------------------------

	/**
	 * Class encapsulating packets statistics.
	 */
	public class Stats {

		/**
		 * pocet odeslanych paketu
		 */
		protected int odeslane = 0;
		/**
		 * pocet prijatych paketu
		 */
		protected int prijate = 0;
		/**
		 * Seznam odezev vsech prijatych icmp_reply.
		 */
		protected List<Double> odezvy = new ArrayList<>();
		/**
		 * Ztrata v procentech.
		 */
		protected int ztrata;
		/**
		 * Uspesnost v procentech.
		 */
		protected int uspech;
		/**
		 * pocet vracenejch paketu o chybach (tzn. typy 3 a 11)
		 */
		protected int errors;
		protected double min;
		protected double max;
		protected double avg;
		protected double celkovyCas; //soucet vsech milisekund

		/**
		 * Propocita min, avg, max, celkovyCas, ztrata.<br />
		 * Pro spravnou funkci staci, aby konkretni pingy delali 3 veci: <br />
		 * 1. pri odeslani icmp_req inkrementovat promennou odeslane <br />
		 * 2. pri prijeti icmp_reply pridat do seznamu odezvy cas paketu. <br />
		 * 3. pred dotazanim na statistiky zavolat tuto metodu countStats() <br />
		 */
		protected void countStats() {
			if (odezvy.size() >= 1) {
				min = odezvy.get(0);
				max = odezvy.get(0);

				double sum = 0;
				for (double d : odezvy) {
					if (d < min) {
						min = d;
					}
					if (d > max) {
						max = d;
					}
					sum += d;
				}

				avg = sum / odezvy.size();
				celkovyCas = sum;
			}
			if (odeslane > 0) {
				ztrata = 100 - (int) ((float) prijate / (float) odeslane * (float) 100);
				uspech = 100 - ztrata;
			}
		}
	}
}
