/*
 * created 12.5.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import commands.completer.Completer;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import shell.apps.CommandShell.CommandShell;

/**
 * Controls commands in DHCP mode.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class DhcpMode extends CiscoCommand {

	private final String first;
	private boolean no = false;

	public DhcpMode(AbstractCommandParser parser, String first) {
		super(parser);
		this.first = first;
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		Completer c = completers.get(CommandShell.CISCO_CONFIG_DHCP);
		c.addCommand("dns-server");
		c.addCommand("network");
		c.addCommand("default-router");
	}

	@Override
	public void run() {
		/*
			router(dhcp-config)#dns-server 147.32.67.6 147.32.1.9 147.32.1.20
			router(dhcp-config)#network 192.168.0.0 255.255.255.0
			router(dhcp-config)#default-router 192.168.0.1
		 */

		if ((isCommandWithoutOutput("no", first, 2)) && !no) {
			no = true;
			run();
			return;
		}

		if (isCommandWithoutOutput("dns-server", first, 2)) {
			List<IpAddress> listOfDnsServers = new ArrayList<>();

			while (!nextWordPeek().equals("")) {
				String next = nextWord();
				IpAddress dnsServer;
				try {
					dnsServer = new IpAddress(next);
				} catch (Exception e) {
					invalidInputDetected();
					return;
				}
				listOfDnsServers.add(dnsServer);
			}

			debug("dns-server: "+listOfDnsServers);
			// TODO: here set list of DNS server to DHCP application


		} else if (isCommandWithoutOutput("network", first, 4)) {
			IpAddress addr = null;
			IpNetmask mask = null;
			try {
				addr = new IpAddress(nextWord());
				mask = new IpNetmask(nextWord());
			} catch (Exception e) {
				invalidInputDetected();
				return;
			}
			IPwithNetmask network = new IPwithNetmask(addr, mask);

			debug("network: "+network);
			// TODO: here set range of assigned IP addresses to DHCP application

		} else if (isCommandWithoutOutput("default-router", first, 2)) {
			IpAddress gw = null;
			try {
				gw = new IpAddress(nextWord());
			} catch (Exception e) {
				invalidInputDetected();
				return;
			}

			debug("gateway: "+gw);
			// TODO: here set default gw to DHCP application


		} else {
			unsupportedCommand();
		}
	}

}
