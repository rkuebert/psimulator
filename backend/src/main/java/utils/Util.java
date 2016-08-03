/*
 * Erstellt am 6.3.2012.
 */
package utils;

import dataStructures.packets.IcmpPacket;
import dataStructures.packets.IpPacket;
import dataStructures.packets.L4Packet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Tomas Pitrinec
 */
public class Util {

	public static final int deviceNameAlign = 8;

	public static boolean availablePort(int port) {
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/*
					 * should not be thrown
					 */
				}
			}
		}

		return false;
	}

	/**
	 * Klasicky printStackTrace hazi do stringu. Prejato z http://www.rgagnon.com/javadetails/java-0029.html.
	 *
	 * @param e
	 * @return
	 */
	public static String stackToString(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return "stack trace ---\r\n" + sw.toString()
					+ "---------------\r\n";
		} catch (Exception e2) {
			return "Error in stackToString";
		}
	}

	/**
	 * Rozlozi cislo na mocniny dvojky. Pouzivam pri vypisovani navratovyho kodu.
	 *
	 * @param c
	 * @return
	 */
	public static String rozlozNaMocniny2(int c) {
		String vratit = "";
		for (int i = 0; i < 31; i++) {
			if ((c & (1 << i)) != 0) {
				if (vratit.equals("")) {
					vratit += (1 << i);
				} else {
					vratit += " + " + (1 << i);
				}
			}
		}
		if (vratit.equals("")) {
			vratit = "0";
		}
		return vratit;
	}

	public static String rozlozNaLogaritmy2(int c) {
		String vratit = "";
		for (int i = 0; i < 31; i++) {
			if ((c & (1 << i)) != 0) {
				if (vratit.equals("")) {
					vratit += (log2(1 << i));
				} else {
					vratit += ", " + (log2(1 << i));
				}
			}
		}
		if (vratit.equals("")) {
			vratit = "Zadny chybovy kod nebyl zadan.";
		}
		return vratit;
	}

	private static int log2(int num) {
		return (int) (Math.log(num) / Math.log(2));
	}

	public static int md(int c) {
		return (1 << c);
	}

	/**
	 * Zaokrouhluje na tri desetinna mista.
	 *
	 * @param d
	 * @return
	 */
	public static double zaokrouhli(double d) {
		return ((double) Math.round(d * 1000)) / 1000;
	}

	public static boolean jeInteger(String ret) {
		try {
			int a = Integer.parseInt(ret);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Uspi aktualni vlakno na pocet ms.
	 *
	 * @param ms
	 */
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			// ok
		}
	}

	/**
	 * Uspi aktualni vlakno na pocet ns.
	 *
	 * @param ms
	 */
	public static void sleepNano(int ns) {
		try {
			Thread.sleep(0, ns);
		} catch (InterruptedException ex) {
			// ok
		}
	}

	/**
	 * Dorovna zadanej String mezerama na zadanou dylku. Kdyz je String delsi nez zadana dylka, tak nic neudela a String
	 * vrati nezmenenej. Protoze String se nikdy nemeni, ale vzdy se vytvori novej, se zadavany, Stringem se nic
	 * nestane.
	 *
	 * @param ret
	 * @param dylka
	 * @return
	 */
	public static String zarovnej(String ret, int dylka) {
		int dorovnat = dylka - ret.length();
		for (int i = 0; i < dorovnat; i++) {
			ret = ret + " ";
		}
		return ret;
	}

	/**
	 * Zjistuje, zda dany retezec zacina cislem. Nesmi byt static, jinak to hazi java.lang.IncompatibleClassChangeError:
	 * Expecting non-static method
	 *
	 * @param s
	 * @return
	 */
	public static boolean zacinaCislem(String s) {
		if (s.length() == 0) {
			return false;
		}

		if (Character.isDigit(s.charAt(0))) {
			return true;
		} else {
			return false;
		}
	}

	public static String threadName() {
		return Thread.currentThread().getName();
	}

	/**
     * Zarovnava zleva mezerami do maximalni dylky.
     * Kdyz je ret delsi nez dylka, tak vrati nezmenenej retezec.
     * @param ret
     * @param dylka
     * @return
     */
    public static String zarovnejZLeva(String ret, int dylka) {
        //if (ret.length() >= dylka) return ret;
        int dorovnat = dylka - ret.length();
        String s = "";
        for(int i=0;i<dorovnat;i++){
            s += " ";
        }
        return s+ret;
    }

	/**
	 * Prevadi byte na inst, jako kdyby byly bez znaminka.
	 * @param b
	 * @return
	 */
	public static int byteToInt(byte b) {
		int a = b & 0xff;	// bitova operace, jinak se totiz pretypovava byte jako se znaminkem, takhel se ty pocatecni jednicky vyandujou
		return a;
	}

	/**
	 * Tato metoda rozseka vstupni string na jednotlivy words (jako jejich oddelovac se bere mezera) a ulozi je do
	 * seznamu words, ktery dedi od Abstraktni.
	 * @autor Stanislav Řehák
	 */
	public static List<String> splitLine(String line) {
		line = line.trim(); // rusim bile znaky na zacatku a na konci
		String[] bileZnaky = {" ", "\t"};
		for (int i = 0; i < bileZnaky.length; i++) { // odstraneni bylych znaku
			while (line.contains(bileZnaky[i] + bileZnaky[i])) {
				line = line.replace(bileZnaky[i] + bileZnaky[i], bileZnaky[i]);
			}
		}
		String[] pole = line.split(" ");
		return Arrays.asList(pole);
	}

	/**
	 * Returns true if packet is ICMP REQUEST.
	 *
	 * @param packet
	 * @return
	 */
	public static boolean isPacketIcmpRequest(IpPacket packet) {
		if (packet.data != null && packet.data.getType() == L4Packet.L4PacketType.ICMP) {
			IcmpPacket p = (IcmpPacket) packet.data;
			if (p.type == IcmpPacket.Type.REQUEST) {
				return true;
			}

		}
		return false;
	}
}
