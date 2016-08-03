package logging.networkEvents;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import logging.Logger;
import logging.LoggerListener;
import logging.LoggingCategory;
import shared.NetworkObject;

/**
 * EventsListener is running in own thread, because broadcasting events over network is time consuming operation.
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class EventsListener implements Runnable {

	boolean done = false;
	private final List<ClientSession> clientSessions = Collections.synchronizedList(new LinkedList<ClientSession>());
	/**
	 * NetworkObject queue aka messages, events to be sent
	 */
	private LinkedBlockingQueue<NetworkObject> objectsToBroadCast = new LinkedBlockingQueue<>();
	private PacketTranslator packetTranslator;

	public EventsListener() {
		this.packetTranslator = new PacketTranslator(objectsToBroadCast);
		new Thread(this.packetTranslator).start();   // start packetTranslator
	}

	private void sendNetworkObject(NetworkObject object) {
		try {
			this.objectsToBroadCast.offer(object, 1, TimeUnit.SECONDS);
		} catch (InterruptedException ex) { // this exception should not be thrown because using LinkedBlockedQueue
			Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Cannot add object into broadcasting queue");
		}
	}

	/**
	 * return actual LoggerListener object,
	 *
	 * @return
	 */
	public LoggerListener getPacketTranslator() {
		return this.packetTranslator;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("EventsListener");

		while (!done) {

			NetworkObject ntwObject = null;
			try {
				// get new object from broadcast queue.
				// Timeouting prefered because there is no other simple option how to shutdown on blocking operation
				ntwObject = objectsToBroadCast.poll(2000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
			}  // does not matter

			if (ntwObject == null) // no object reached
			{

				continue;
			}

			Logger.log(Logger.DEBUG, LoggingCategory.EVENTS_SERVER, "Object for broadcasting recieved. Broadcasting ... ");

			synchronized (this.clientSessions) {
				for (Iterator<ClientSession> it = clientSessions.iterator(); it.hasNext();) {
					ClientSession clientSession = it.next();
					if (clientSession.isActive()) {
						clientSession.send(ntwObject);
					} else {
						it.remove();
					}
				}
			}

		}

	}

	public void addClientSession(ClientSession clientSession) {
		synchronized (this.clientSessions) {  // better have exclusive access
			this.clientSessions.add(clientSession);
			clientSession.setListReference(clientSessions);
		}

	}

	public void quit() {
		this.done = true;

	}
}
