/*
 * Erstellt am 5.4.2012.
 */

package applications;

import commands.ApplicationNotifiable;
import dataStructures.MacAddress;
import dataStructures.PacketItem;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.DhcpPacket;
import dataStructures.packets.IpPacket;
import dataStructures.packets.UdpPacket;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetLayer;
import networkModule.L3.NetworkInterface;
import networkModule.SwitchNetworkModule;
import psimulator2.Psimulator;
import utils.Wakeable;
import static dataStructures.packets.DhcpPacket.DhcpType.*;
import networkModule.L3.IPLayer;
import networkModule.IpNetworkModule;

/**
 *
 * @author Tomas Pitrinec
 */
public class DhcpClient extends Application implements Wakeable {

	private static int ttl = 64;	// ttl, se kterym se budoui odesilat pakety
	private static int request_wait_time = 1; // jak dlouho se po odeslani requestu ceka na ACK
	private static int maxDiscoverCount = 6;

	// promenny pro celej beh aplikace:
	final ApplicationNotifiable command;
	final NetworkInterface iface;
	final int transaction_id;
	final EthernetLayer ethLayer;
	final MacAddress myMac;
	final IPLayer ipLayer;

	// stavovy promenny:
	/**
	 * True na zacatku nebo po tom, co byla aplikace vzbuzena budikem
	 */
	boolean wakedByAlarm = true;
	int discoverCount = 0; // pocet odeslanejch zadosti discover
	/**
	 * 0 - pred zacatkem <br />
	 * 1 - odeslal se discover <br />
	 * 2 - odeslal se request <br />
	 * 3 - vratil se ack - konec
	 */
	int state = 0;
	IpAddress serverIdentifier = null; // ukladam si server, ze kteryho mi poprve prisla OFFER a kterymu jsem poslal request


	public DhcpClient(Device device, ApplicationNotifiable command, NetworkInterface iface) {
		super("dhcp_client", device);
		this.command = command;
		this.iface = iface;
		transaction_id = ((int) Math.random()) * Integer.MAX_VALUE; //nahodne se generuje 32-bitovy transaction ID
		ethLayer = ((SwitchNetworkModule) device.getNetworkModule()).ethernetLayer;
		ipLayer = ((IpNetworkModule) device.getNetworkModule()).ipLayer;
		myMac = iface.getMacAddress();

	}


	@Override
	public void wake() {
		if(isRunning()){
			wakedByAlarm = true;
			worker.wake();
		}
	}

	@Override
	public void doMyWork() {
		// metoda se vykonava ve dvou pripadech - bud bylo vlakno vzbuzeno budikem nebo prisel nejakej pozadavek, ten pozadavek ma prednost
		if(!buffer.isEmpty()) {		// neco je v bufferu
			while(!buffer.isEmpty()){
				handleIncomingPacket(buffer.remove(0));
			}

		} else if (wakedByAlarm){ // probuzeno budikem
			 if(state == 1){ // budik zavolan potom, co nedorazil OFFER
				if (discoverCount <= maxDiscoverCount) {
					sendDiscover();
				} else {
					printEnd();
				}
			} else if (state == 2) { // po odeslani request nedorazil ACK v predpokladanym case.
				sendDiscover();	// nevim, jak se to chova, zatim znovu poslu discover
			}
		} else {
			// neni zadnej pozadavek ani probuzeni budikem - nic se nedela (tohle nastane pri spusteni aplikace
		}

		// nakonec zrusim priznak:
		wakedByAlarm = false;

	}

	private void handleIncomingPacket(PacketItem pItem) {
		// nejdriv se prectu ten paket:
		IpPacket recIp = null;
		DhcpPacket recDhcp;

		// prelozim pakety, a kdyby bylo neco spatne, koncim
		try {	// delam to vsechno najednou
			recIp = pItem.packet;
			recDhcp = (DhcpPacket) ((UdpPacket) recIp.data).getData();
			recDhcp.getSize();	// abych si overil, ze to neni null
		} catch (Exception ex) {
			log(Logger.INFO, "DHCP klientu prisel spatnej paket.", recIp);
			return;
		}

		// samotny zpracovavani, v kazdym stavu jinak:
		if(state==0){
			// nic se nedela
		}else if(state==1) { // odeslal se discover, ceka se na offer
			command.printLine("DHCP"+recDhcp.type+" from "+recDhcp.serverIdentifier);	// vypisu co prislo:
			if (recDhcp.type == OFFER){
				serverIdentifier = recDhcp.serverIdentifier;
				sendPacket(REQUEST, recDhcp.ipToAssign, recDhcp.serverIdentifier);	// posilam request
				state = 2;
				Psimulator.getPsimulator().budik.registerWake(this, request_wait_time*1000);	// nastavuju cekani na ACK
			}
			// jinak se v tomhle stavu nic nedela

		} else if (state == 2){ // odeslan request, cekam na odpoved
			command.printLine("DHCP"+recDhcp.type+" from "+recDhcp.serverIdentifier);	// vypisu co prislo:
			if (recDhcp.type == ACK){
				ipLayer.routingTable.flushRecords(iface);
				ipLayer.changeIpAddressOnInterface(iface, recDhcp.ipToAssign);
				ipLayer.routingTable.addRecord(new IPwithNetmask("0.0.0.0",0), recDhcp.router, iface);
			}
		} else if (state == 3){
			// uz konec - nic se nedela
		}
	}

	/**
	 * Odesle DHCP paket. Udelana tak, aby omhla posilat veskery klientsky pakety.
	 * @param type
	 * @param ipToAssign null if type is discover
	 * @param serverIdentier null if type is discover
	 */
	private void sendPacket(DhcpPacket.DhcpType type, IPwithNetmask ipToAssign, IpAddress serverIdentier){
		// sestavim paket:
		DhcpPacket discover = new DhcpPacket(type, transaction_id, serverIdentier, ipToAssign, null,null, myMac);
		UdpPacket udp = new UdpPacket(DHCP_server.client_port, DHCP_server.server_port, discover);
		IpPacket ip = new IpPacket(new IpAddress("0.0.0.0"), new IpAddress("255.255.255.255"), ttl, udp);
		// poslui paket:
		ethLayer.sendPacket(ip, iface.ethernetInterface, MacAddress.broadcast());

	}

	/**
	 * Posle DHCP discover a naridi budik, kdy se ma zas vzbudit
	 */
	private void sendDiscover(){
		// poslu paket discover:
		sendPacket(DhcpPacket.DhcpType.DISCOVER, null, null);

		// nastavim budik pro vzbuzeni
		int interval = ((int) Math.random()) * 5 + 3; // nahodne se generuje interval, kdy se bude znova posilat discover
				// -> asi je to opravdu nahodne, ale ty cisla jsem si vymyslel
		Psimulator.getPsimulator().budik.registerWake(this, interval*1000);
		// vypisu a nastavim stav:
		command.printLine("DHCPDISCOVER on "+iface.name+" to 255.255.255.255 port "+DHCP_server.server_port+" interval "+interval);
		state = 1;
		discoverCount++;
	}






	@Override
	protected void atStart() {
		printStart();
		ipLayer.routingTable.deleteRecord(new IPwithNetmask("0.0.0.0",0), null, null);
		sendDiscover();
	}

	@Override
	protected void atExit() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void atKill() {
		throw new UnsupportedOperationException("Not supported yet.");
	}





	private void printStart(){
		command.printLine("RTNETLINK answers: No such process");
		command.printLine("Internet Systems Consortium DHCP Client 4.2.2");
		command.printLine("Copyright 2004-2011 Internet Systems Consortium.");
		command.printLine("All rights reserved.");
		command.printLine("For info, please visit https://www.isc.org/software/dhcp/");
		command.printLine("");
		command.printLine("Listening on LPF/"+iface.name+"/"+myMac.toString());
		command.printLine("Sending on   LPF/"+iface.name+"/"+myMac.toString());
		command.printLine("Sending on   Socket/fallback");
		command.printLine("");
	}

	private void printEnd() {
		command.printLine("No DHCPOFFERS received.");
		command.printLine("No working leases in persistent database - sleeping.");
	}


// jen pomocny informativni metody:

	@Override
	public String getDescription() {
		return device.getName() + "_DHCP_client";
	}

	private void log(int logLevel, String msg, Object obj) {
		Logger.log(this, logLevel, LoggingCategory.DHCP, msg, obj);
	}



}
