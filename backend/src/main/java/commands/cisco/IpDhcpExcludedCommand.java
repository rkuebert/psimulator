/*
 * created 12.5.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import commands.completer.Completer;
import dataStructures.ipAddresses.IpAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpDhcpExcludedCommand extends CiscoCommand {

	final private boolean no;

	public IpDhcpExcludedCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		completers.get(CommandShell.CISCO_CONFIG_MODE).addCommand("ip dhcp excluded-address");
	}

	@Override
	public void run() {
		if (isCommand("excluded-address", nextWord(), 1)) {
			List<IpAddress> listOfExcluded = new ArrayList<>();

			while (!nextWordPeek().equals("")) {
				IpAddress excludeIP;
				try {
					excludeIP = new IpAddress(nextWord());
				} catch (Exception e) {
					invalidInputDetected();
					return;
				}
				listOfExcluded.add(excludeIP);
			}

			// TODO: tady pridat aplikaci neco
			debug("excluded-address: "+listOfExcluded);
		}
	}





}
