/*
 * created 6.3.2012
 */
package commands.cisco;

import commands.AbstractCommandParser;
import commands.completer.Completer;
import commands.completer.Node;
import java.util.Iterator;
import java.util.Map;
import networkModule.L3.ArpCache.ArpRecord;
import networkModule.L3.ArpCache.Target;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.NetworkInterface;
import networkModule.L3.nat.AccessList;
import networkModule.L3.nat.NatTable;
import networkModule.L3.nat.NatTable.Record;
import networkModule.L3.nat.NatTable.StaticRule;
import networkModule.L3.nat.Pool;
import networkModule.L3.nat.PoolAccess;
import shell.apps.CommandShell.CommandShell;
import utils.Util;

/**
 * Trida pro zpracovani a obsluhu prikazu 'show'.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ShowCommand extends CiscoCommand {

	private State showState = null;
	private int ciscoState;
	private final CiscoIPLayer ipLayer;
	/**
	 * Reference on interface in 'show interfaces FastEthernet0/0' command.
	 */
	NetworkInterface iface;

	public ShowCommand(AbstractCommandParser parser) {
		super(parser);
		this.ciscoState = parser.getShell().getMode();
		this.ipLayer = (CiscoIPLayer) getNetMod().ipLayer;
	}

	@Override
	public void run() {
		boolean cont = process();
		if (cont) {
			start();
		}
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		Completer privileged = completers.get(CommandShell.CISCO_PRIVILEGED_MODE);
		Completer user = completers.get(CommandShell.CISCO_USER_MODE);
//		Completer config = completers.get(CommandShell.CISCO_CONFIG_MODE);

		user.addCommand("show ip nat translations");
		user.addCommand("show ip interface brief");
		user.addCommand("show arp");
		privileged.addCommand("show ip nat translations");
		privileged.addCommand("show ip interface brief");
		privileged.addCommand("show arp");

		privileged.addCommand("show running-config");


		Node show = new Node("show");
		Node ifaces = new Node("interfaces");
		show.addChild(ifaces);
		for (NetworkInterface ifa : ipLayer.getSortedNetworkIfaces()) {
			if (ifa.name.matches("[a-zA-Z]+Ethernet[0-9]/[0-9]{1,2}")) {
				int indexOfT = ifa.name.lastIndexOf("t");
				String shortenedIfaceName = ifa.name.substring(0, indexOfT + 1);
				ifaces.addChild(new Node(shortenedIfaceName));
				break;
			}
		}
		privileged.addCommand(show);

		user.addCommand("show ip route");
		privileged.addCommand("show ip route");
	}

	enum State {

		ARP,
		RUN,
		ROUTE,
		NAT,
		QUESTION_MARK,
		INTERFACES,
		IP_INTERFACE,
		IP_INTERFACE_BRIEF,};

	private void start() {
		switch (showState) {
			case ARP:
				arp();
				break;
			case RUN:
				runningConfig();
				break;
			case ROUTE:
				ipRoute();
				break;
			case NAT:
				ipNatTranslations();
				break;
			case INTERFACES:
				interfaces();
				break;
			case IP_INTERFACE:
				ipInterface();
				break;
			case IP_INTERFACE_BRIEF:
				ipInterfaceBrief();
				break;
		}
	}

	private boolean process() {
		// show ip route
		// show running-config      - jen v ROOT rezimu
		// show interfaces          - jen v ROOT rezimu
		// show ip nat translations
		// show ip interface
		// show ip interface brief
		// show arp

		String dalsi = nextWord(); // druhe slovo
		if (dalsi.isEmpty()) {
			printLine("% Type \"show ?\" for a list of subcommands\n");
			return false;
		}

		if (dalsi.equals("?")) {
			String s = "";
			s += psimulator2.Psimulator.getNameOfProgram() + ": in real cisco there is just first word for completion (not the whole command)\n\n";
			s += "  show ip route                   IP routing table\n";
			s += "  show ip nat translations        Translation entries\n";
			if (ciscoState == CommandShell.CISCO_PRIVILEGED_MODE) {
				s += "  show running-config             Current operating configuration\n";
			}
			s += "\n";
			printWithDelay(s, 50);
			return false;
		}

		if (dalsi.startsWith("a") && ciscoState == CommandShell.CISCO_PRIVILEGED_MODE) {
			if (!isCommand("arp", dalsi, 2)) {
				return false;
			}

			showState = State.ARP;
			return true;
		}

		if (dalsi.startsWith("r")) {
			if (ciscoState == CommandShell.CISCO_USER_MODE) {
				invalidInputDetected();
				return false;
			}

			if (!isCommand("running-config", dalsi, 3)) {
				return false;
			}
			showState = State.RUN;
			return true;
		} else {
			if (dalsi.startsWith("in")) {
				if (ciscoState != CommandShell.CISCO_PRIVILEGED_MODE) {
					invalidInputDetected();
					return false;
				}
				if (!isCommand("interfaces", dalsi, 3)) {
					return false;
				}
				return processInterfaces();
			}

			if (!isCommand("ip", dalsi, 2)) {
				return false;
			}

			dalsi = nextWord();
			if (dalsi.startsWith("r")) {
				if (!isCommand("route", dalsi, 2)) {
					return false;
				}
				showState = State.ROUTE;
				return true;
			} else {
				if (dalsi.startsWith("i")) { // interface
					if (!isCommand("interface", dalsi, 3)) {
						return false;
					}

					dalsi = nextWord();
					if (dalsi.isEmpty()) {
						showState = State.IP_INTERFACE;
						return true;
					}

					if (!isCommand("brief", dalsi, 2)) {
						return false;
					}
					showState = State.IP_INTERFACE_BRIEF;
					return true;
				} else {
					if (!isCommand("nat", dalsi, 2)) {
						return false;
					}
					if (ciscoState == CommandShell.CISCO_USER_MODE) {
						invalidInputDetected();
						return false;
					}
					if (!isCommand("translations", nextWord(), 1)) {
						return false;
					}
					showState = State.NAT;
					return true;
				}
			}
		}
	}

	// show interfaces (FastEthernet 0/0)?
	private boolean processInterfaces() {
		showState = State.INTERFACES;

		String rozh = nextWord();
		if (rozh.isEmpty()) {
			return true;
		}
		rozh += nextWord();

		iface = null;
		iface = ipLayer.getNetworkIntefaceIgnoreCase(rozh);
		if (iface == null) {
			if (rozh.matches("[fF].*[0-9]/[0-9]{1,2}")) {
				int end = rozh.indexOf("0");
				String novy = "FastEthernet";
				String cislo = rozh.substring(end, rozh.length());
				rozh = novy + cislo;

				iface = ipLayer.getNetworkIntefaceIgnoreCase(rozh);
				if (iface != null) {
					return true;
				}
			}

			if (ipLayer.existInterfaceNameStartingWith(rozh)) {
				incompleteCommand();
				return false;
			}

			invalidInputDetected();
			return false;
		}
		return true;
	}

	/**
	 * Posle vypis pro prikaz 'show interfaces.
	 */
	private void interfaces() {
		if (iface == null) {
			for (NetworkInterface nIface : ipLayer.getNetworkIfaces()) {
				printWithDelay(getInterfaceReport(nIface), 30);
			}
			printLine("");
			return;
		}
		printWithDelay(getInterfaceReport(iface), 30);
	}

	/**
	 * show arp
	 */
	private void arp() {
		String s = "";
		Target target;
		Map<Target, ArpRecord> arp = ipLayer.getArpCache();
		ipLayer.checkArpRecords();
		s += "Protocol  Address          Age (sec)  Hardware Addr   Type   Interface\n";

		synchronized (arp) {
			Iterator<Target> it = arp.keySet().iterator();

			while (it.hasNext()) {
				target = it.next();

				ArpRecord record = arp.get(target);

				s += Util.zarovnej("Internet", 10);
				s += Util.zarovnej(target.address.toString(), 17);
				s += Util.zarovnej("" + (System.currentTimeMillis() - record.timeStamp) / 1_000, 11);
				s += Util.zarovnej(record.mac.getCiscoRepresentation(), 16);
				s += Util.zarovnej("Dynam", 7);
				s += target.iface.name;

				if (it.hasNext()) {
					s += "\n";
				}
			}
		}

		printWithDelay(s, 50);
	}

	/**
	 * show ip interface
	 */
	private void ipInterface() {
		String s = "";
		NetworkInterface nIface;
		Iterator<NetworkInterface> it = ipLayer.getSortedNetworkIfaces().iterator();

		while (it.hasNext()) {
			nIface = it.next();
			s += nIface.name + " is ";
			if (nIface.isUp) {
				s += "up, line protocol is *not implemented*\n";
			} else {
				s += "administratively down, line protocol is *not implemented*\n";
			}

			if (nIface.getIpAddress() != null) {
				s += "  Internet address is " + nIface.getIpAddress().toString() + "\n";
				s += "  Broadcast address is 255.255.255.255\n";
				s += "  Address determined by setup command\n";
				s += "  MTU is 1500 bytes\n";
				s += "  Helper address is not set\n";
				s += "  Security level is default\n";
				s += "  ICMP unreachables are always sent\n";
				s += "  ICMP mask replies are never sent\n";
				s += "  Router Discovery is disabled\n";
				s += "  IP output packet accounting is disabled\n";
				s += "  TCP/IP header compression is disabled\n";
				s += "  RTP/IP header compression is disabled\n";
				s += "  BGP Policy Mapping is disabled\n";
				s += "  WCCP Redirect outbound is disabled\n";
				s += "  WCCP Redirect inbound is disabled\n";
				s += "  WCCP Redirect exclude is disabled";
			} else {
				s += "  Internet protocol processing disabled";
			}
			if (it.hasNext()) {
				s += "\n";
			}
		}

		printWithDelay(s, 50);
	}

	/**
	 * show ip interface brief
	 */
	private void ipInterfaceBrief() {
		String s = "";
		s += Util.zarovnej("Interface", 28);
		s += Util.zarovnej("Ip-Address", 17);
		s += Util.zarovnej("OK?", 4);
		s += Util.zarovnej("Method", 7);
		s += Util.zarovnej("Status", 23);
		s += "Protocol\n";

		for (NetworkInterface nIface : ipLayer.getSortedNetworkIfaces()) {
			s += Util.zarovnej(nIface.name, 28);
			s += Util.zarovnej((nIface.getIpAddress() != null ? nIface.getIpAddress().getIp().toString() : "unassigned"), 17);
			s += Util.zarovnej("YES", 4);
			s += Util.zarovnej("manual", 7);
			s += Util.zarovnej((nIface.isUp ? "up" : "administratively down"), 23);
			s += "*not implemented*"; // TODO: implement in future - sending keepalive packet if set in configuration
			s += "\n";
		}

		printWithDelay(s, 50);
	}

	/**
	 * Posle vypis pro prikaz 'show ip nat translations.
	 */
	private void ipNatTranslations() {
		String s = "";

		NatTable table = ipLayer.getNatTable();
		table.deleteOldDynamicRecords();

		if (table.getDynamicRules().isEmpty()) {
			s += "\n\n";
			printWithDelay(s, 50);
			return;
		}

		s += Util.zarovnej("Pro Inside global", 24) + Util.zarovnej("Inside local", 20);
		s += Util.zarovnej("Outside local", 20) + Util.zarovnej("Outside global", 20);
		s += "\n";

		for (Record zaznam : table.getDynamicRules()) {
			s += Util.zarovnej("icmp " + zaznam.out.getAddressWithPort(), 24)
					+ Util.zarovnej(zaznam.in.getAddressWithPort(), 20)
					+ Util.zarovnej(zaznam.target.toString(), 20)
					+ Util.zarovnej(zaznam.target.toString(), 20) + "\n";
		}

		for (NatTable.StaticRule rule : table.getStaticRules()) {
			s += Util.zarovnej("--- " + rule.out, 24)
					+ Util.zarovnej(rule.in.toString(), 20)
					+ Util.zarovnej("---", 20)
					+ Util.zarovnej("---", 20) + "\n";
		}

		printWithDelay(s, 50);
	}

	/**
	 * Posle vypis pro prikaz 'show ip route'.
	 */
	private void ipRoute() {
		String s = "";
		s += ipLayer.wrapper.getShIpRoute();
		printWithDelay(s, 50);
	}

	/**
	 * Prikaz 'show running-config' ve stavu # (ROOT). Aneb vypis rozhrani v uplne silenem formatu.
	 */
	private void runningConfig() {
		String s = "";

		s += "Building configuration...\n"
				+ "\n"
				+ "Current configuration : 827 bytes\n"
				+ "!\n"
				+ "version 12.4\n"
				+ "service timestamps debug datetime msec\n"
				+ "service timestamps log datetime msec\n"
				+ "no service password-encryption\n"
				+ "!\n"
				+ "hostname " + getDevice().getName() + "\n"
				+ "!\n";
		if (!debug) {
			s += "boot-start-marker\n"
					+ "boot-end-marker\n"
					+ "!\n"
					+ "!\n"
					+ "no aaa new-model\n"
					+ "!\n"
					+ "resource policy\n"
					+ "!\n"
					+ "mmi polling-interval 60\n"
					+ "no mmi auto-configure\n"
					+ "no mmi pvc\n"
					+ "mmi snmp-timeout 180\n"
					+ "ip subnet-zero\n"
					+ "ip cef\n"
					+ "!\n"
					+ "!\n"
					+ "no ip dhcp use vrf connected\n"
					+ "!\n"
					+ "!\n"
					+ "!\n";
		}

		for (NetworkInterface singleIface : ipLayer.getNetworkIfaces()) {

			s += "interface " + singleIface.name + "\n";
			if (singleIface.getIpAddress() == null) {
				s += " no ip address\n";
			} else {
				s += " ip address " + singleIface.getIpAddress().getIp() + " " + singleIface.getIpAddress().getMask() + "\n";
			}

			if (ipLayer.getNatTable().getOutside() != null && ipLayer.getNatTable().getOutside().name.equals(singleIface.name)) {
				s += " ip nat outside" + "\n";
			}

			if (ipLayer.getNatTable().getInside(singleIface.name) != null) {
				s += " ip nat inside" + "\n";
			}

			if (singleIface.isUp == false) {
				s += " shutdown" + "\n";
			}
			s += " duplex auto\n speed auto\n!\n";
		}

		if (ipLayer.routingTable.classless) {
			s += "ip classless\n";
		}
		s += ipLayer.wrapper.getRunningConfig();

		s += "!\n";
		s += "ip http server\n";

		for (Pool pool : ipLayer.getNatTable().lPool.getSortedPools()) {
			s += "ip nat pool " + pool.name + " " + pool.getFirst() + " " + pool.getLast()
					+ " prefix-length " + pool.prefix + "\n";
		}

		for (PoolAccess pa : ipLayer.getNatTable().lPoolAccess.getSortedPoolAccess()) {
			s += "ip nat inside source list " + pa.access + " pool " + pa.poolName;
			if (pa.overload) {
				s += " overload";
			}
			s += "\n";
		}

		for (StaticRule rule : ipLayer.getNatTable().getStaticRules()) {
			s += "ip nat inside source static " + rule.in + " " + rule.out + "\n";
		}

		s += "!\n";

		for (AccessList access : ipLayer.getNatTable().lAccess.getList()) {
			s += "access-list " + access.number + " permit " + access.ip.getIp() + " " + access.ip.getMask().getWildcardRepresentation() + "\n";
		}

		if (!debug) {
			s += "!\n" + "!\n" + "control-plane\n"
					+ "!\n" + "!\n" + "line con 0\n"
					+ "line aux 0\n" + "line vty 0 4\n" + " login\n" + "!\n" + "end\n\n";
		}
		printWithDelay(s, 10);
	}

	private String getInterfaceReport(NetworkInterface iface) {
		String s = iface.name + " is ";
		boolean up = false;
		if (!iface.isUp) {
			s += "administratively down";
		} else {
			boolean isCableConnected = iface.ethernetInterface.isConnected();
			if (isCableConnected == false) { // bez kabelu && nahozene
				s += "down";
				// This indicates a physical problem,
				// either with the interface or the cable attached to it.
				// Or not attached, as the case may be.
			} else { // s kabelem && nahozene
				s += "up";
				up = true;
			}
		}
		s += ", line protocol is ";
		if (up) {
			s += "up";
		} else {
			s += "down";
			/*
			 * 1/ encapsulation mismatch, such as when one partner in a point-to-point connection is configured for HDLC
			 * and the other for PPP.
			 *
			 * 2/ Whether the DCE is a CSU/DSU or another Cisco router in a home lab, the DCE must supply a clockrate to
			 * the DTE. If that clockrate is not present, the line protocol will come down.
			 */
		}
		s += "\n";
		String mac = iface.getMacAddress().getCiscoRepresentation();
		s += "  Hardware is Gt96k FE, address is " + mac + " (" + mac + ")\n";
		if (iface.getIpAddress() != null) {
			s += "  Internet address is " + iface.getIpAddress().getIp() + "\n";
		}
		s += "  MTU 1500 bytes, BW 100000 Kbit/sec, DLY 100 usec, \n"
				+ "     reliability 255/255, txload 1/255, rxload 1/255\n"
				+ "  Encapsulation ARPA, loopback not set\n"
				+ "  Keepalive set (10 sec)\n"
				+ "  Auto-duplex, Auto Speed, 100BaseTX/FX\n"
				+ "  ARP type: ARPA, ARP Timeout 04:00:00\n"
				+ "  Last input never, output never, output hang never\n"
				+ "  Last clearing of \"show interface\" counters never\n"
				+ "  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\n"
				+ "  Queueing strategy: fifo\n"
				+ "  Output queue: 0/40 (size/max)\n"
				+ "  5 minute input rate 0 bits/sec, 0 packets/sec\n"
				+ "  5 minute output rate 0 bits/sec, 0 packets/sec\n"
				+ "     0 packets input, 0 bytes\n"
				+ "     Received 0 broadcasts, 0 runts, 0 giants, 0 throttles\n"
				+ "     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\n"
				+ "     0 watchdog\n"
				+ "     0 input packets with dribble condition detected\n"
				+ "     115658 packets output, 6971948 bytes, 0 underruns\n"
				+ "     0 output errors, 0 collisions, 1 interface resets\n"
				+ "     0 unknown protocol drops\n"
				+ "     0 babbles, 0 late collision, 0 deferred\n"
				+ "     0 lost carrier, 0 no carrier\n"
				+ "     0 output buffer failures, 0 output buffers swapped out";
		return s;
	}
}
