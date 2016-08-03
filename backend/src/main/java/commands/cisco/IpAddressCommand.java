/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import commands.completer.Completer;
import dataStructures.ipAddresses.BadNetmaskException;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.Map;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import shell.apps.CommandShell.CommandShell;

/**
 * TODO: IpAddressCommand - vyzkouset a poradne odladit/
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpAddressCommand extends CiscoCommand {

	private final boolean no;
	private final NetworkInterface iface;
	boolean noBezAdresy = false;
	private IPwithNetmask pomocna = null;

	public IpAddressCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
		this.iface = this.parser.configuredInterface;
	}

	@Override
	public void run() {
		 boolean pokracovat = process();
        if (pokracovat) {
            start();
        }
	}

	/**
     * Vim, ze mi prislo '(no) ip address'.
     * @return
     */
    private boolean process() {
        //ip address 192.168.2.129 255.255.255.128

        String ip = nextWord();

        if (ip.isEmpty() && no){
            noBezAdresy = true;
            return true;
        }

        String maska = nextWord();
        if (isEmptyWithIcompleteCommand(ip) || isEmptyWithIcompleteCommand(maska)) {
            return false;
        }

		IpAddress adr;
		try {
			adr = new IpAddress(ip);
		} catch (Exception e) {
			invalidInputDetected();
			return false;
		}

        if (IpAddress.isForbiddenIP(adr)) {
            printLine("Not a valid host address - " + ip);
            return false;
        }

		IPwithNetmask entireIp;
        try {
            entireIp = new IPwithNetmask(ip, maska);
        } catch (BadNetmaskException e) {
            String[] pole = maska.split("\\.");
            String s = "";
            int i;
            for (String bajt : pole) {
                try {
                    i = Integer.parseInt(bajt);
                    s += Integer.toHexString(i);

                } catch (NumberFormatException exs) {
                    invalidInputDetected();
                    return false;
                }
            }
            printLine("Bad mask 0x" + s.toUpperCase() + " for address " + ip);
            return false;
        } catch (Exception e) {
            Logger.log(getDescription(), Logger.WARNING, LoggingCategory.CISCO_COMMAND_PARSER, "Tohle by se asi nemelo stavat?, exception: "+e);
            invalidInputDetected();
            return false;
        }

        if (adr.getBits() == 0) {
            printLine("Not a valid host address - " + ip);
            return false;
        }

        if (entireIp.isNetworkNumber() || entireIp.isBroadcast() || entireIp.getMask().getBits() == 0) {
            // Router(config-if)#ip address 147.32.120.0 255.255.255.0
            // Bad mask /24 for address 147.32.120.0
            printLine("Bad mask /" + entireIp.getMask().getNumberOfBits() + " for address " + entireIp.getIp());
            return false;
        }

//        ladici("konec");
        if (nextWord().length() != 0) {
            invalidInputDetected();
            return false;
        }
		pomocna = entireIp;
        return true;
    }

    private void start() {
		IPLayer ipLayer = getNetMod().ipLayer;

        if (no) {
            if (noBezAdresy) {
				ipLayer.changeIpAddressOnInterface(iface, null);
                return;
            }

            if (iface.getIpAddress().getIp().getBits() != pomocna.getIp().getBits()) {
                printLine("Invalid address");
                return;
            }
            if (iface.getIpAddress().getMask().getBits() != pomocna.getMask().getBits()) {
                printLine("Invalid address mask");
                return;
            }

			ipLayer.changeIpAddressOnInterface(iface, null);
            return;
        }

        ipLayer.changeIpAddressOnInterface(iface, pomocna);
    }

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		Completer tmp = completers.get(CommandShell.CISCO_CONFIG_IF_MODE);
		tmp.addCommand("ip address");
		tmp.addCommand("no ip address");
	}
}
