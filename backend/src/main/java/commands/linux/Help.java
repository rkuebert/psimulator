/*
 * Erstellt am 16.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 * Vypisuje napovedu simulatoru.
 * TODO: Nutno upravit podle konecny verze.
 * @author Tomas Pitrinec
 */
public class Help extends AbstractCommand {

	public Help(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		if (parser.getWords().get(0).equals("help")) {
			helpCzech();
		}
		if (parser.getWords().get(0).equals("help-en")) {
			helpEnglish();
		}
	}



    protected void helpCzech() {
		printLine("For help in english type: help-en");
		printLine("");
        printLine("Tento prikaz na realnem pocitaci s linuxem neni. Zde je pouze pro informaci, jake prikazy jsou v tomto simulatoru implementovany.");
        printLine("");
        printLine("Simulator ma oproti skutecnemu pocitaci navic tyto prikazy:");
        printLine("uloz / save   ulozeni stavijici virtualni site do souboru");
        printLine("              napr. uloz ./konfiguraky/sit.xml   - ulozi se relativne k ceste, ze ktere je spusten server");
        printLine("help          vypsani teto napovedy");
		printLine("help-en       vypsani teto napovedy v anglictine");
		printLine("rnetconn      prikaz na spravu propojeni simulatoru s realnou siti, obsluhuje vsechna propojeni na vsech virtualnich zarizenich simulatoru");
		printLine("              napovedu k prikazu rnetconn v cestine vypisete prikazem: rnetconn help-cz");
        printLine("");
        printLine("Z linuxovych prikazu jsou podporovany tyto:");
        printLine("ifconfig      parametry adresa, netmask, up, down");
        printLine("route         akce add, del; parametry -net, -host, dev, gw, netmask");
        printLine("iptables      jen pro pridani pravidla k natovani");
        printLine("              napr: iptables -t nat -A POSTROUTING -o eth1 -j MASQUERADE");
        printLine("ping          prepinace -c, -i, -s, -t");
        printLine("traceroute    jen napr. traceroute 1.1.1.1");
        printLine("exit");
        printLine("ip            podprikazy addr a route");
        printLine("echo, cat     jen na zapisovani a cteni souboru /proc/sys/net/ipv4/ip_forward");
        printLine("");
    }

	protected void helpEnglish() {
		printLine("For help in czech type: help");
		printLine("");
        printLine("This command is not present on real computer with Linux. Here is only for information, which Linux commands are supported by this simulator.");
        printLine("");
        printLine("Simulator has these commands, which are not present on real computer:");
        printLine("save          saves virtual network configuration to file");
        printLine("              for example: save ./konfiguraky/sit.xml   - saves in relative path to path, where is siulator running");
        printLine("help          print this help in czech");
		printLine("help-en       print this help in english");
		printLine("rnetconn      command to manage connection of simulator to real network");
		printLine("              for help in english type: rnetconn help");
        printLine("");
        printLine("These linux commands are supported in simulator:");
        printLine("ifconfig      parameters address, netmask, up, down");
        printLine("route         actions add, del; parameters -net, -host, dev, gw, netmask");
        printLine("iptables      only masquerade");
        printLine("              for example: iptables -t nat -A POSTROUTING -o eth1 -j MASQUERADE");
        printLine("ping          options -c, -i, -s, -t");
        printLine("traceroute    for example: traceroute 1.1.1.1");
        printLine("exit");
        printLine("ip            podprikazy addr a route");
        printLine("echo, cat     jen na zapisovani a cteni souboru /proc/sys/net/ipv4/ip_forward");
        printLine("");
    }


}
