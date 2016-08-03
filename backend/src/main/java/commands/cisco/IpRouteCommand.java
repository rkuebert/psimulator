/*
 * created 13.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import commands.completer.Completer;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.BadNetmaskException;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.Map;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.NetworkInterface;
import shell.apps.CommandShell.CommandShell;
import utils.Util;

/**
 * Class for parsing this commands:
 *
 * ip route 'target' 'mask of target' 'gateway or interface
 * '
 * ip route 0.0.0.0 0.0.0.0 192.168.2.254
 * ip route 192.168.2.0 255.255.255.192 fastEthernet 0/0
 * no ip route ...
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpRouteCommand extends CiscoCommand {

	private final boolean no;

	private IPwithNetmask target;
    private IpAddress gateway;
    private NetworkInterface iface;

	private final CiscoIPLayer ipLayer = (CiscoIPLayer)getNetMod().ipLayer;

	public IpRouteCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
	}

	private boolean process() {

		String adr = "";
		String maska = "";
        try {
            adr = nextWord();
			debug("address: "+adr);

            maska = nextWord();
			debug("mask: "+maska);

            if (adr.isEmpty() || maska.isEmpty()) {
                incompleteCommand();
                return false;
            }
            target = new IPwithNetmask(adr, maska);
        } catch (BadNetmaskException e) {
			debug("bad mask: "+maska);
			invalidInputDetected();
			return false;
		} catch (BadIpException e) {
			debug("bad address: "+adr);
            invalidInputDetected();
            return false;
        } catch (Exception e) {
			debug(Util.stackToString(e));
            invalidInputDetected();
            return false;
		}

        if (!target.isNetworkNumber()) {
            printLine("%Inconsistent address and mask");
            return false;
        }

        if (IpAddress.isForbiddenIP((target.getIp()))) {
            printLine("%Invalid destination prefix");
            return false;
        }

        String next = nextWord();
		debug("next: "+next);

        if (Util.zacinaCislem(next)) { // na branu
            try {
                gateway = new IpAddress(next);
            } catch (BadIpException e) {
                invalidInputDetected();
                return false;
            }

            if (IpAddress.isForbiddenIP(gateway)) {
                printLine("%Invalid next hop address");
                return false;
            }

        } else if (!next.equals("")) { // na rozhrani
            String posledni = nextWord();
            next += posledni; // nemuze byt null

            iface = getNetMod().ipLayer.getNetworkIntefaceIgnoreCase(next);
            if (iface == null) { // rozhrani nenalezeno
                invalidInputDetected();
                return false;
            }

        } else { // empty
            if (no == false) {
                incompleteCommand();
                return false;
            }
        }

        if (!nextWord().equals("")) { // border za spravnym 'ip route <adresat> <maska> <neco> <bordel>'
            invalidInputDetected();
            return false;
        }

        return true;
    }

	@Override
	public void run() {

		boolean cont = process();
        if (cont) {
            start();
        }
	}

	private void start() {

//		if (debug) pc.vypis("pridej="+no);
        if (no == false) {
            if (gateway != null) { // on gateway
                ipLayer.wrapper.addRecord(target, gateway);
            } else { // on interface
                if (iface == null) {
                    return;
                }
                ipLayer.wrapper.addRecord(target, iface);
            }
        } else { // delete
            int n = ipLayer.wrapper.deleteRecord(target, gateway, iface);
            if (n == 1) {
                printLine("%No matching route to delete");
            }
        }
	}

	@Override
	protected void fillCompleters(Map<Integer, Completer> completers) {
		Completer tmp = completers.get(CommandShell.CISCO_CONFIG_MODE);
		tmp.addCommand("ip route");
		tmp.addCommand("no ip route");
	}
}
