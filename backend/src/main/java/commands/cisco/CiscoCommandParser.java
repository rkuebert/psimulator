/*
 * created 22.2.2012
 */
package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import commands.LongTermCommand.Signal;
import commands.completer.Completer;
import commands.completer.Node;
import commands.linux.Ifconfig;
import commands.linux.Route;
import device.Device;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.IpNetworkModule;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.NetworkInterface;
import networkModule.NetworkModule;
import shell.apps.CommandShell.CommandShell;
import static shell.apps.CommandShell.CommandShell.*;
import utils.Util;


/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoCommandParser extends AbstractCommandParser implements Loggable {

	/**
     * Specialni formatovac casu pro vypis servisnich informaci z cisca.
     */
    private DateFormat formator = new SimpleDateFormat("MMM  d HH:mm:ss.SSS");

	/**
     * Pomocna promenna pro zachazeni s '% Ambiguous command: '
     */
    private boolean isAmbiguousCommand = false;
	/**
     * Rozhrani, ktere se bude upravovat ve stavu IFACE
     */
    protected NetworkInterface configuredInterface = null;

	private AbstractCommand command = null;

	private final boolean debug = Logger.isDebugOn(LoggingCategory.CISCO_COMMAND_PARSER);

	private final CiscoIPLayer ipLayer;

	private List<CiscoCommand> availableCommands = new ArrayList<>();

	public CiscoCommandParser(Device device, CommandShell shell) {
		super(device, shell);
		shell.getPrompt().setPrefix(device.getName());
		shell.getPrompt().setSuffix(">");
		shell.getPrompt().setCurrentPath("/");
		shell.getPrompt().showPath(false);

		if (debug) {
			changeMode(CISCO_PRIVILEGED_MODE);
		}

		NetworkModule nm = device.getNetworkModule();
		if (nm.isStandardTcpIpNetMod()) {
			this.ipLayer = (CiscoIPLayer) ((IpNetworkModule) nm).ipLayer;
		} else {
			this.ipLayer = null; // never happen, because L2 only devices have no telnet access
		}

		if (device.commandCompleters == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.CISCO_COMMAND_PARSER, "Vytvarim Completery", null);
			device.commandCompleters = new HashMap<>();
			addCompleters();
			addCompletionData();

			Logger.log(this, Logger.DEBUG, LoggingCategory.CISCO_COMMAND_PARSER, "completery: \n"+device.commandCompleters.toString(), null);
		}

		printService("Type command 'help' for list of supported commands.");
	}

	@Override
	protected final void addCompleters() {
		device.commandCompleters.put(CommandShell.CISCO_USER_MODE, new CiscoCompleter());
		device.commandCompleters.put(CommandShell.CISCO_PRIVILEGED_MODE, new CiscoCompleter());
		device.commandCompleters.put(CommandShell.CISCO_CONFIG_MODE, new CiscoCompleter());
		device.commandCompleters.put(CommandShell.CISCO_CONFIG_IF_MODE, new CiscoCompleter());
		device.commandCompleters.put(CommandShell.CISCO_CONFIG_DHCP, new CiscoCompleter());
		Logger.log(this, Logger.DEBUG, LoggingCategory.CISCO_COMMAND_PARSER, "Pocet completeru: "+device.commandCompleters.size(), null);
	}

	@Override
	protected final void addCompletionData() {
		registerCommands();
		registerParserCommands();
	}

	/**
	 * All available cisco commands put here!
	 */
	private void registerCommands() {
		availableCommands.add(new AccessListCommand(this, false));
		availableCommands.add(new ConfigureCommand(this));
		availableCommands.add(new DhcpMode(this, ""));
		availableCommands.add(new IpDhcpExcludedCommand(this, false));
		availableCommands.add(new HelpCommand(this));
		availableCommands.add(new IpAddressCommand(this, false));
		availableCommands.add(new IpCommand(this, false));
		availableCommands.add(new IpNatCommand(this, false));
		availableCommands.add(new IpNatInterfaceCommand(this, false));
		availableCommands.add(new IpRouteCommand(this, false));
		availableCommands.add(new PingCommand(this));
		availableCommands.add(new QuestionCommand(this));
		availableCommands.add(new ShowCommand(this));
		availableCommands.add(new TracerouteCommand(this));

		for (CiscoCommand cmd : availableCommands) {
			cmd.fillCompleters(device.commandCompleters);
		}

		availableCommands.clear(); // no longer neeeded
	}

	private void registerParserCommands() {
		// user mode
		Completer user = device.commandCompleters.get(CommandShell.CISCO_USER_MODE);
		user.addCommand("enable");
		user.addCommand("logout");

		// privileged mode
		Completer privileged = device.commandCompleters.get(CommandShell.CISCO_PRIVILEGED_MODE);
		privileged.addCommand("enable");
		privileged.addCommand("disable");
		privileged.addCommand("logout");

		// config mode
		Completer config = device.commandCompleters.get(CommandShell.CISCO_CONFIG_MODE);
		config.addCommand("end");

		Node iface = new Node("interface");
		for (NetworkInterface rozh : ipLayer.getNetworkIfaces()) {
			if (rozh.name.matches("[a-zA-Z]+Ethernet[0-9]/[0-9]{1,2}")) {
				int indexOfT = rozh.name.lastIndexOf("t");
				String shortenedIfaceName = rozh.name.substring(0, indexOfT+1);
				iface.addChild(new Node(shortenedIfaceName));
				break;
			}
		}
		config.addCommand(iface);

		// config-if mode
		Completer configif = device.commandCompleters.get(CommandShell.CISCO_CONFIG_IF_MODE);
		configif.addCommand("end");
		configif.addCommand("shutdown");
		configif.addCommand("no shutdown");
		configif.addCommand(iface);

		// all modes
		device.addCommandToAllCompleters("exit");
		device.addCommandToAllCompleters("save");
		device.addCommandToAllCompleters("help");
	}

	@Override
	public void processLineForParsers() {

		isAmbiguousCommand = false;

		String first = nextWord();

		if (first.equals("help")) {
            command = new HelpCommand(this);
			command.run();
            return;
        }

		if (first.equals("?")) {
			command = new QuestionCommand(this);
			command.run();
			return;
		}

		try {

//			tryComplete(line);

			if (debug) {
				if (first.equals("ifconfig")) {
					command = new Ifconfig(this);
					command.run();
					return;
				}
				if (first.equals("route")) {
					command = new Route(this);
					command.run();
					return;
				}
				if (isCommand("show", first)) {
					command = new ShowCommand(this);
					command.run();
					return;
				}
			}

			switch (mode) {
				case CISCO_USER_MODE:
					if (isCommand("enable", first)) {
						changeMode(CISCO_PRIVILEGED_MODE);
						return;
					}
					if (isCommand("ping", first)) {
						command = new PingCommand(this);
						command.run();
						return;
					}
					if (isCommand("traceroute", first)) {
						command = new TracerouteCommand(this);
						command.run();
						return;
					}
					if (isCommand("show", first)) {
						command = new ShowCommand(this);
						command.run();
						return;
					}
					if (isCommand("exit", first) || isCommand("logout", first)) {
						shell.closeSession();
						return;
					}
					break;

				case CISCO_PRIVILEGED_MODE:
					if (isCommand("enable", first)) { // funguje u cisco taky, ale nic nedela
						return;
					}
					if (isCommand("disable", first)) {
						changeMode(CISCO_USER_MODE);
						return;
					}
					if (isCommand("ping", first)) {
						command = new PingCommand(this);
						command.run();
						return;
					}
					if (isCommand("traceroute", first)) {
						command = new TracerouteCommand(this);
						command.run();
						return;
					}
					if (isCommand("configure", first)) {
						command = new ConfigureCommand(this);
						command.run();
						return;
					}
					if (isCommand("show", first)) {
						command = new ShowCommand(this);
						command.run();
						return;
					}
					if (isCommand("exit", first) || isCommand("logout", first)) {
						shell.closeSession();
						return;
					}

					if (debug) {
						if (isCommand("ip", first)) {
							command = new IpCommand(this, false);
							command.run();
							return;
						}
						if (isCommand("access-list", first)) {
							command = new AccessListCommand(this, false);
							command.run();
							return;
						}
						if (isCommand("no", first)) {
							no();
							return;
						}
					}
					break;

				case CISCO_CONFIG_MODE:
					if (isCommand("exit", first) || first.equals("end")) {
						changeMode(CISCO_PRIVILEGED_MODE);
						return;
					}
					if (isCommand("ip", first)) {
						command = new IpCommand(this, false);
						command.run();
						return;
					}
					if (isCommand("interface", first)) {
						iface();
						return;
					}
					if (isCommand("access-list", first)) {
						command = new AccessListCommand(this, false);
						command.run();
						return;
					}
					if (isCommand("no", first)) {
						no();
						return;
					}
					// TODO: here add command for switching to DHCP mode.


					break;

				case CISCO_CONFIG_IF_MODE:
					if (isCommand("exit", first)) {
						changeMode(CISCO_CONFIG_MODE);
						return;
					}
					if (first.equals("end")) {
						changeMode(CISCO_PRIVILEGED_MODE);
						return;
					}
					if (isCommand("ip", first)) {
						command = new IpCommand(this, false);
						command.run();
						return;
					}
					if (isCommand("no", first)) {
						no();
						return;
					}
					if (isCommand("shutdown", first)) {
						shutdown();
						return;
					}
					if (isCommand("interface", first)) {
						iface();
						return;
					}
					break;

				case CISCO_CONFIG_DHCP:
					if (isCommand("exit", first)) {
						changeMode(CISCO_CONFIG_MODE);
						return;
					}
					command = new DhcpMode(this, first);
					command.run();
					return;
			}

			if (isAmbiguousCommand) {
				isAmbiguousCommand = false;
				ambiguousCommand();
				return;
			}

			switch (mode) {
				case CISCO_CONFIG_MODE:
				case CISCO_CONFIG_IF_MODE:
					invalidInputDetected();
					break;

				default:
					shell.printLine("% Unknown command or computer name, or unable to find computer address");
			}
		} catch (Exception e) {
			Logger.log(this, Logger.WARNING, LoggingCategory.CISCO_COMMAND_PARSER, e.toString() + "\n", e);
		}
	}

	@Override
	public void catchSignal(Signal sig) {

		if (runningCommand != null) {
//			shell.print("^Z");
//			shell.printLine("");
			runningCommand.catchSignal(sig);
			return;
		}



		switch (sig) {
//			case CTRL_SHIFT_6:
				// tady predat bezicimu prikazu a ten to preda bezici aplikaci
//				break;

			case CTRL_Z:
				shell.print("^Z");
				Util.sleep(100);
				shell.printLine("");
				switch (mode) {
					case CISCO_CONFIG_IF_MODE:
					case CISCO_CONFIG_MODE:
						changeMode(CISCO_PRIVILEGED_MODE);
						break;
				}
				shell.printPrompt();
				break;


			case CTRL_C:
				shell.printLine("");
				switch (mode) {
					case CISCO_CONFIG_IF_MODE:
					case CISCO_CONFIG_MODE:
						changeMode(CISCO_PRIVILEGED_MODE);
						break;
				}
				break;
			case CTRL_D:
				if (debug) {
					shell.closeSession();
				}
				break;
			default:
			// no reaction
		}
	}

	/**
	 * Vrati
	 */
	protected String getFormattedTime() {
		Date d = new Date();
		return formator.format(d);
	}

	/**
     * Tato metoda simuluje zkracovani commandu tak, jak cini cisco.
     * @param command command, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd command, ktery zadal uzivatel
     * @return Vrati true, pokud retezec cmd je jedinym moznym commandem, na ktery ho lze doplnit.
     */
    protected boolean isCommand(String command, String cmd) {

        int i = 10;

        // Zde jsou zadefinovany vsechny commandy. Jsou rozdeleny do poli podle poctu znaku,
        // ktere je potreba k jejich bezpecne identifikaci. Cisla byla ziskana z praveho cisca IOS version 12.4
        String[] jedna = {"terminal", "inside", "outside", "source", "static", "pool", "netmask", "permit"};
        // + ip, exit
        String[] dva = {"show", "interface", "no", "shutdown", "enable", "classless",
            "access-list", "ping", "logout", "nat", "traceroute"};
        // + ip, exit
        String[] tri = {"running-config", "name-server", "nat", "address"};
        // + exit
        String[] ctyri = {"configure", "disable"};
        //String[] pet = {"route"};

        List<String[]> seznam = new ArrayList<>();
        seznam.add(jedna);
        seznam.add(dva);
        seznam.add(tri);
        seznam.add(ctyri);
        //seznam.add(pet);

        int n = 0;
        for (String[] pole : seznam) { // nastaveni spravne delky dle zarazeni do seznamu
            n++;
            for (String s : pole) {
                if (s.equals(command)) {
                    i = n;
                }
            }
        }

        if (command.equals("exit")) { // specialni chovani commandu exit v ruznych stavech
            switch (mode) {
                case CISCO_USER_MODE:
                case CISCO_PRIVILEGED_MODE:
                    i = 2;
                    break;
                case CISCO_CONFIG_MODE:
                    i = 3;
                    break;
                case CISCO_CONFIG_IF_MODE:
				case CISCO_CONFIG_DHCP:
                    i = 1;
            }
        }
        if (command.equals("ip")) { // specialni chovani commandu ip v ruznych stavech
            switch (mode) {
                case CISCO_CONFIG_MODE:
                case CISCO_PRIVILEGED_MODE:
                    i = 2;
                    break;
                case CISCO_CONFIG_IF_MODE:
                    i = 1;
            }
        }
        if (command.equals("route")) { // specialni chovani commandu route v ruznych stavech
            switch (mode) {
                case CISCO_PRIVILEGED_MODE:
                    i = 2;
                    break;
                case CISCO_CONFIG_MODE:
                    i = 5;
            }
        }

        if (cmd.length() >= i && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny command
            return true;
        }

        if (command.startsWith(cmd)) {
            isAmbiguousCommand = true;
        }

        return false;
    }

	/**
     * Vypise chybovou hlasku pri zadani neplatneho vstupu.
     */
    protected void invalidInputDetected() {
        shell.printLine("\n% Invalid input detected.\n");
    }

	/**
     * Vypise chybovou hlasku pri zadani nekompletniho commandu.
     */
    protected void incompleteCommand() {
        shell.printLine("% Incomplete command.");
    }

    /**
     * Vypise hlasku do konzole "% Ambiguous command: ".
     */
    protected void ambiguousCommand() {
        shell.printLine("% Ambiguous command:  \"" + line + "\"");
    }

	/**
	 * Target mode we want change to.
	 * @param mode
	 */
	protected final void changeMode(int mode) {
		switch (mode) {
			case CISCO_USER_MODE:
				shell.setMode(mode);
				shell.getPrompt().setSuffix(">");
				break;

			case CISCO_PRIVILEGED_MODE:
				shell.setMode(mode);
				shell.getPrompt().setSuffix("#");

				if (this.mode == CISCO_CONFIG_MODE || this.mode == CISCO_CONFIG_IF_MODE) { // jdu z configu
					shell.printWithDelay(getFormattedTime() + ": %SYS-5-CONFIG_I: Configured from console by console", 100);
				}
				break;

			case CISCO_CONFIG_MODE:
				shell.setMode(mode);
				shell.getPrompt().setSuffix("(config)#");

				if (this.mode == CISCO_PRIVILEGED_MODE) { // jdu z privilegovaneho
					shell.printLine("Enter configuration commands, one per line.  End with 'exit'."); // zmena oproti ciscu: End with CNTL/Z.
				} else if (this.mode == CISCO_CONFIG_IF_MODE) {
					configuredInterface = null;
				}
				break;

			case CISCO_CONFIG_IF_MODE:
				shell.setMode(mode);
				shell.getPrompt().setSuffix("(config-if)#");
				break;

			default:
				throw new RuntimeException("Change to unsupported mode: "+mode);

		}
	}

	/**
     * Prepne cisco do stavu config-if (IFACE). <br />
     * Kdyz ma prikaz interface 2 argumenty, tak se sloucej do jednoho (pripad: interface fastEthernet 0/0).<br />
     * 0 nebo vice nez argumenty znamena chybovou hlasku.<br />
     * Do globalni promenne 'aktualni' uklada referenci na rozhrani, ktere chce uzivatel konfigurovat.<br />
     * prikaz 'interface fastEthernet0/1'
     */
    private void iface() {

        String rozhrani;
        switch (words.size()) {
            case 1:
                incompleteCommand();
                return;
            case 2:
                rozhrani = words.get(1);
                break;
            case 3:
                rozhrani = words.get(1) + words.get(2);
                break;
            default:
                invalidInputDetected();
                return;
        }

		this.configuredInterface = ipLayer.getNetworkIntefaceIgnoreCase(rozhrani);

        if (configuredInterface == null) {
			if (rozhrani.matches("[fF].*[0-9]/[0-9]{1,2}")) {
				int end = rozhrani.indexOf("0");
				String novy = "FastEthernet";
				String cislo = rozhrani.substring(end, rozhrani.length());
				rozhrani = novy+cislo;

				this.configuredInterface = ipLayer.getNetworkIntefaceIgnoreCase(rozhrani);
				if (this.configuredInterface != null) {
					changeMode(CommandShell.CISCO_CONFIG_IF_MODE);
					return;
				}
			}

			if (ipLayer.existInterfaceNameStartingWith(rozhrani)) {
				incompleteCommand();
				return;
			}

            invalidInputDetected();
            return;
        }

		changeMode(CommandShell.CISCO_CONFIG_IF_MODE);
    }

	/**
     * Jednoduchy parser pro prikazy: <br />
     * no ip nat inside<br />
     * no ip route <br />
     * no access-list <br />
     *
     * no ip address <br />
     * no ip nat inside/outside <br />
     * no shutdown
     */
    private void no() {

        String dalsi = nextWord(); // sezere se ip, shutdown, access-list
        if (dalsi.isEmpty()) {
            incompleteCommand();
            return;
        }
        if (mode == CISCO_CONFIG_MODE || (debug && mode == CISCO_PRIVILEGED_MODE)) {
            if (isCommand("access-list", dalsi)) {
                command = new AccessListCommand(this, true);
				command.run();
                return;
            }
            if (isCommand("ip", dalsi)) {
                command = new IpCommand(this, true);
				command.run();
                return;
            }
        }
        if (mode == CISCO_CONFIG_IF_MODE) {
            if (isCommand("shutdown", dalsi)) {
                noshutdown();
                return;
            }
            if (isCommand("ip", dalsi)) {
                command = new IpCommand(this, true);
				command.run();
                return;
            }
        }

        if (isAmbiguousCommand) {
			ambiguousCommand();
        } else {
            invalidInputDetected();
        }
    }

	/**
     * Tento prikaz zapne rozhrani, ktere je definovano v aktualnim nastovacim rezimu (napr.: interface fastEthernet0/0)
     * a aktualizuje routovaci tabulku.
     *
     * prislo 'no shutdown'
     */
    private void noshutdown() {
        if (configuredInterface.isUp == false) { // kdyz nahazuju rozhrani
            Util.sleep(500);
            Date d = new Date();
            shell.printLine(getFormattedTime() + ": %LINK-3-UPDOWN: Interface " + configuredInterface.name + ", changed state to up");
            Util.sleep(100);
            shell.printLine(getFormattedTime() + ": %LINEPROTO-5-UPDOWN: Line protocol on Interface " + configuredInterface.name + ", changed state to up");
            configuredInterface.isUp = true;
            ipLayer.wrapper.update();
        }
    }

	/**
     * Shodi rozhrani a zaktualizuje routovaci tabulku.
     */
    private void shutdown() {
        if (configuredInterface.isUp) {
            Util.sleep(250);
            Date d = new Date();
            shell.printLine(getFormattedTime() + ": %LINK-5-UPDOWN: Interface " + configuredInterface.name + ", changed state to down");
            Util.sleep(100);
            shell.printLine(getFormattedTime() + ": %LINEPROTO-5-UPDOWN: Line protocol on Interface " + configuredInterface.name + ", changed state to down");
            configuredInterface.isUp = false;
            ipLayer.wrapper.update();
        }
    }

	@Override
	public String getDescription() {
		return device.getName()+": CiscoCommandParser";
	}

	private void tryComplete(String line) {
		if (debug) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.CISCO_COMMAND_PARSER, line + " doplneno na '" + device.commandCompleters.get(mode).complete(line, shell) + "'", null);
		}
	}
}
