package logging.networkEvents;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import logging.Logger;
import logging.LoggingCategory;

/**
 * server socket thread --- connection listener
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class EventServer implements Runnable {

	private int port;
	private EventsListener listener;
	private boolean quit = false;
	private ServerSocket serverSocket;

	public EventServer(int port) {
		this.port = port;
		this.listener = new EventsListener();

		// now is the right time to start EventListener thread
		Thread thread = new Thread(this.listener);
		thread.start();
	}

	public EventsListener getListener() {
		return listener;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("EventServer");
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ex) {
			Logger.log(Logger.ERROR, LoggingCategory.EVENTS_SERVER, "IOException occured when creating server socket");
			return;
		}

		Logger.log(Logger.DEBUG, LoggingCategory.EVENTS_SERVER, "EventServer server socket successfully created.");



		while (!quit) {
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				Logger.log(Logger.DEBUG, LoggingCategory.EVENTS_SERVER, "Client with hostname " + clientSocket.getInetAddress().getHostName() + " connected");
			} catch (IOException ex) {
				if (!quit) {
					Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "IOException occured when creating client socket. No other client socket will be created");
				}
				return;
			}

			ClientSession clientSession = new ClientSession(clientSocket);
			clientSession.initCommunication();

			this.listener.addClientSession(clientSession);

		}

	}

	public void stop() {
		this.quit = true;
		try {
			this.serverSocket.close();
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "IOException occured when closing server socket");
		}

	}
}
