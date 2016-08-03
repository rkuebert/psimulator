/*
 * Erstellt am 20.3.2012.
 */
package physicalModule;

import dataStructures.packets.L2Packet;
import java.util.ArrayList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

/**
 * This is switchport to connect to real interface.
 *
 * Switchport slouzici ke spojeni s realnym pocitacem. Posila a prijima pakety do a z realny site, k tomu ma Catcher a
 * Sender. Nebezi ve vlastnim vlakne, ale catcher a sender bezi.
 *
 * @author neiss
 */
public class RealSwitchport extends Switchport implements Loggable {

	private PacketCatcher catcher;
	private PacketSender sender;
	private Pcap pcap;
	private String ifaceName;


// konstruktory, startovani a zastavovani (nestartuje se pri konstrukci ale az na vyzadani): --------------------------

	public RealSwitchport(AbstractPhysicalModule physicMod, int number, int configID) {
		super(physicMod, number, configID);
		log(Logger.DEBUG,"Byl vytvoren realnej switchport c. "+number+" s configID "+configID, null);
	}

	/**
	 * Nastartuje spojeni s realnou siti
	 * @param ifaceName
	 * @return 0 - vsechno v poradku </br> 1 nepodarilo se otevrit spojeni </br> 2 uz je propojeno
	 */
	public synchronized int start(String ifaceName) {
		if (isConnected()) {
			return 2;
		}

		// ze vseho nejdriv se pokusim otevrit spojeni s realnym pocitacem
		pcap = otevriSpojeni(ifaceName);
		if (pcap == null) {
			log(Logger.WARNING, "Fail. Connection to real computer not succeeded.", null);
			return 1;
		} else {
			this.ifaceName = ifaceName;
			log(Logger.IMPORTANT, "Real interface " + ifaceName + " tied together with switchport n. " + number + ". Connection to real network started.", null);
		}

		// kdyz je spojeni otevreno, spoustim obsluhu:
		catcher = new PacketCatcher(pcap, this);
		sender = new PacketSender(pcap, this);
		return 0;
	}

	public synchronized void stop() {
		if (isConnected()) {
			pcap.close();
			sender.stop();
			pcap = null;
			log(Logger.IMPORTANT, "Real switchport untied.", null);
		} else {
			log(Logger.WARNING, "Attempting to disconnect untied switchport.", null);
		}
	}




// metody pro sitovou komunikaci: ----------------------------------------------------------------------------------

	@Override
	protected void sendPacket(L2Packet packet) {
		if(isConnected()){
			log(Logger.DEBUG, "Jdu poslat paket.", packet);
			sender.sendPacket(packet);
		} else {
			log(Logger.WARNING, "Attempting to send packet on real network while real interface is not connected.", packet);
		}
	}

	@Override
	protected void receivePacket(L2Packet packet) {
		log(Logger.DEBUG, "Chytil jsem paket.", packet);
		physicalModule.receivePacket(packet, this);
	}


// ostatni verejny metody: -------------------------------------------------------------------------------------------

	@Override
	public boolean isConnected() {
		if(pcap==null){
			return false;
		}
		return true;
	}

	@Override
	public String getDescription() {
		return getDeviceName()+": RealSwitchport "+number;
	}

	@Override
	public boolean isReal() {
		return true;
	}

	/**
	 * Metodu pouziva rnetconn.
	 * @return
	 */
	public String getIfaceName() {
		return ifaceName;
	}


// privatni metody: --------------------------------------------------------------------------------------------------

	private Pcap otevriSpojeni(String ifaceName) {

		StringBuilder errbuf = new StringBuilder(); // For any error msgs

		List<PcapIf> alldevs = new ArrayList<>(); // Will be filled with NICs


		// listovani rozhrani, zkopiroval jsem to a az pak jsem zjistil, ze je to zbytecny, uz to tady ale nechavam:
		PcapIf iface = null;

		// nejdriv si najdu vsechny rozhrani:
		int r;
		try {
			r = Pcap.findAllDevs(alldevs, errbuf);
		} catch (UnsatisfiedLinkError ex) {	// This exception is thrown, when jNetPcap is not installed on the computer.
			log(Logger.WARNING, "Some library is missing on yor computer. Did you copy the file libjnetpcap.so (from downloaded zip file) to /usr/lib? "
					+ "Have you installed libpcap library? Error message: ["+ex.getMessage()+"]", null);

			return null;
		}
		if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
			log(Logger.WARNING,"Can't read list of devices. Do you run psimulator as root?", null);
			return null;
		}
		log(Logger.DEBUG,"Network devices found.", null);

		// vyberu spravny rozhrani:
		for (PcapIf device : alldevs) {
			if(device.getName().equals(ifaceName)){
				iface = device;
			}
		}
		if(iface != null){
			log(Logger.DEBUG, "Found and selected iface "+iface.getName(), null);
		} else {
			log(Logger.WARNING, "Iface "+ifaceName+" not found. Connection with real network is not working!", null);
			return null;
		}


		// otevirani rozhrani:
		int snaplen = 64 * 1024;           // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
		int timeout = 10 * 1000;           // 10 seconds in millis
		Pcap vytvorenyPcap =
				Pcap.openLive(iface.getName(), snaplen, flags, timeout, errbuf);

		if (vytvorenyPcap == null) {
			log(Logger.WARNING, "Error while opening device for capture: "+ errbuf.toString(), null);
		}

		return vytvorenyPcap;
	}


// ruzny pomocny metody: -------------------------------------------------------------------------------



	private void log(int logLevel, String msg, Object obj){
		Logger.log(this, logLevel, LoggingCategory.REAL_NETWORK, msg, obj);
	}

	/**
	 * Zkratka pro logovani.
	 * @return
	 */
	public String getDeviceName(){
		return physicalModule.device.getName();
	}





}
