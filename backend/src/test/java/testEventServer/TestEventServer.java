/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testEventServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.junit.Test;
import shared.NetworkObject;

/**
 * simple test of EventServer... just connect, read && print
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class TestEventServer {

	public TestEventServer() {
	}

	@Test
	public void test() throws UnknownHostException, IOException, ClassNotFoundException {

		Socket clientSocket = new Socket("127.0.0.1", 12000);
		ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
		clientSocket.setSoTimeout(1000);
		

		int repeatReadTimes = 5;  // aka 5 secons i no object readed
		int times = 0;

		while (true) {
			NetworkObject networkObject = null;
			
			try {
				
				networkObject = (NetworkObject) inputStream.readObject();
			} catch (SocketTimeoutException timeout) { // its ok.. no need to be handled, just check if object is not null
			}

			if (networkObject == null) // if read timeouted
			{
				if (times > repeatReadTimes) // final timeout
				{
					return;
				} else {	// repeat read
					times++;
					continue; // continue while
				}

			} else { // process object

				System.out.println(networkObject);

			}

		}


	}
}
