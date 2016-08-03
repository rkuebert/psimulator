/*
 * Erstellt am 1.3.2012.
 */
package config.configTransformer;


import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;
import device.Device;
import filesystem.ArchiveFileSystem;
import filesystem.FileSystem;
import java.io.File;
import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.IpNetworkModule;
import networkModule.L2.EthernetInterface;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.CiscoWrapperRT;
import networkModule.L3.NetworkInterface;
import networkModule.L3.nat.NatTable;
import networkModule.NetworkModule;
import networkModule.SwitchNetworkModule;
import physicalModule.*;
import psimulator2.Psimulator;
import shared.Components.*;
import shared.Components.simulatorConfig.DeviceSettings.NetworkModuleType;
import shared.Components.simulatorConfig.*;

/**
 *
 * @author Tomas Pitrinec
 * @author Stanislav Rehak
 */
public class Loader implements Loggable {

	Psimulator s = Psimulator.getPsimulator();
	/**
	 * odkladaci mapa mezi ID a cislama switchportu
	 * Klicem je id z konfiguraku, hodnotou je prirazeny cislo switchportu
	 * Pozor, pouziva se pro vsechny pocitace (tedy predpoklada se
	 */
	private Map<Integer, Integer> switchporty = new HashMap<>();	// odkladaci mapa mezi ID a cislama switchportu
	private final NetworkModel networkModel;

	/**
	 * Mnozina idecek, slouzi ke kontrolovani, je-li kazdy ID unikatni (kdyby se nejak rozbil konfigurak, treba pres svnko.
	 */
	private Set<Integer> idecka = new HashSet<>();
	/**
	 * Mnozina jmen, slouzi ke kontrolovani, zda jeden Device ma unikatni jmena sitovych rozhrani.
	 */
	private Set<String> names = new HashSet<>();
	/**
	 * Cisco potrebuje mit natahane kabely na to, aby mohl nacist nastaveni routovaci tabulky.
	 */
	private Map<CiscoIPLayer, RoutingTableConfig> ciscoSettings = new HashMap<>();
	private String configFilename;
	private String projectName;

	public Loader(NetworkModel networkModel, String configFileName) {
		this.networkModel = networkModel;
		this.configFilename=configFileName;
		projectName = configFilename.substring(0, configFilename.length());
	}

	/**
	 * Metoda slouzi k nahravani konfigurace z Martinova modelu.
	 *
	 */
	public void loadFromModel() {

		try {	// chytaji se tady vsechny vyjimky vyhozeny z konfigurace

			for (HwComponentModel device : networkModel.getHwComponents()) {
				registerID(device.getId());
				if (device.getHwType() == shared.Components.HwTypeEnum.REAL_PC) {
					continue;
				}

				s.devices.add(createDevice(device));
			}

			if (s.devices.isEmpty()) {
				Logger.log(this, Logger.ERROR, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "No device was found in configuration file! Exiting. ", null);
			}

			if (s.devices.get(0).physicalModule instanceof PhysicMod) {
				connectCables();
			} else {
				connectCablesV2();
			}

			updateRoutingTableForCisco();

		} catch (Exception ex) {
			Logger.log(this, Logger.INFO, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "XML configuration file is corrupted: ", ex);
			Logger.log(this, Logger.ERROR, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "XML configuration file is corrupted. Exiting. " + ex.toString(), null);
		}

		Logger.log(this, Logger.IMPORTANT, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Configuration succesfully loaded from: "+Psimulator.getPsimulator().lastConfigFile, null);
	}

	/**
	 * Metoda na vytvoreni jednoho pocitace (device).
	 *
	 * @param model
	 * @return
	 */
	private Device createDevice(HwComponentModel model) {

		// vytvoreni samotnyho pocitace:
		Device pc = new Device(model.getId(), model.getName(), convertType(model.getHwType()));
//		System.out.printf("device: id: %s name: %s, type: %s \n", model.getId(), model.getDeviceName(), model.getHwType());

		// setup filesystem (je to uz tady, aby s nim mohl zachazet sitovy modul (napr. pro ip_forward)):
		pc.setFilesystem(setupFilesystem(model));

		// vytvoreni fysickyho modulu:
		AbstractPhysicalModule pm = pc.physicalModule;
		//buildeni switchportu:
		int cislovaniSwitchportu = 0;
		names = new HashSet<>();
		for (EthInterfaceModel ifaceModel : (Collection<EthInterfaceModel>) model.getEthInterfaces()) { // prochazim interfacy a pridavam je jako switchporty
			registerID(ifaceModel.getId());
			registerName(ifaceModel.getName(), model);
			pm.addSwitchport(cislovaniSwitchportu, false, ifaceModel.getId());	// realnej switchport se resi az s kabelama
			switchporty.put(ifaceModel.getId(), cislovaniSwitchportu);
			cislovaniSwitchportu++;
		}

		// nastaveni sitovyho modulu
		NetworkModule nm = createNetworkModule(model, pc);
		pc.setNetworkModule(nm);

		return pc;
	}

	private FileSystem setupFilesystem(HwComponentModel model){
		String pathSeparator = System.getProperty("file.separator");

		File filesystemDir = new File(projectName+"-DATA");

		if(!filesystemDir.isDirectory() && 	!filesystemDir.mkdirs())  // if does not exist and was not sucessfully created
			Logger.log(Logger.ERROR, LoggingCategory.FILE_SYSTEM, "Cannot find nor create filesystem directory. Fatal error");

		String pathFileSystem = filesystemDir.getAbsolutePath() + pathSeparator + model.getIDAsString() + model.getName().replaceAll("\\W", "") + "." + ArchiveFileSystem.getFileSystemExtension();
		return new ArchiveFileSystem(pathFileSystem);
	}

	/**
	 * Metoda na prevedeni Martinova typu pocitace na nas typ.
	 *
	 * @param t
	 * @return
	 */
	private Device.DeviceType convertType(HwTypeEnum t) {
		Device.DeviceType type;
		if ((t == HwTypeEnum.LINUX_ROUTER) || (t == HwTypeEnum.END_DEVICE_NOTEBOOK) || (t == HwTypeEnum.END_DEVICE_PC) || t == HwTypeEnum.END_DEVICE_WORKSTATION) {
			type = Device.DeviceType.linux_computer;
		} else if (t == HwTypeEnum.CISCO_ROUTER) {
			type = Device.DeviceType.cisco_router;
		} else if (t == HwTypeEnum.CISCO_SWITCH || t == HwTypeEnum.LINUX_SWITCH) {
			type = Device.DeviceType.simple_switch;
		} else {
			throw new LoaderException("Unknown or forbidden type of network device: " + t);
		}
		return type;
	}

	/**
	 * Vytvareni sitovyho modulu. Predpoklada jiz kompletni fysickej modul.
	 *
	 * @param model konfigurace pocitace
	 * @param pc odkaz na uz hotovej pocitac
	 * @return
	 */
	private NetworkModule createNetworkModule(HwComponentModel model, Device pc) {

		DeviceSettings.NetworkModuleType netModType;

		if (model.getDevSettings() != null) {
			netModType = model.getDevSettings().getNetModType();	// zjisteni typu modulu z konfiguraku
		} else { // neni ulozeno v konfiguraci, o jaky typ modulu se jedna
			switch (pc.type) {
				case cisco_router:
				case linux_computer:
					netModType = NetworkModuleType.tcp_ip_netmod;
					break;
				case simple_switch:
					netModType = NetworkModuleType.simple_switch_netMod;
					break;
				default:
					throw new AssertionError();
			}
		}

		if (netModType == DeviceSettings.NetworkModuleType.tcp_ip_netmod) {	// modul je pro router
			return createIpNetworkModule(model, pc);
		} else if (netModType == DeviceSettings.NetworkModuleType.simple_switch_netMod) {
			return createSwitchNetworkModule(model, pc);
		} else {
			throw new LoaderException("Unknown or forbidden type of network module.");
		}

	}

	/**
	 * Metoda vytvori sitovej model routeru. Ke kazdymu switchportu priradi jeden interface, pojmenuje je, pripadne
	 * nastavi adresy, vytvori a nastavi routovaci tabulku a nat.
	 *
	 * @param model
	 * @param pc
	 * @return
	 */
	private IpNetworkModule createIpNetworkModule(HwComponentModel model, Device pc) {
		IpNetworkModule nm = new IpNetworkModule(pc);	// vytvoreni sitovyho modulu, pri nem se

		//nahrani interfacu:
		for (EthInterfaceModel ifaceModel : model.getInterfacesAsList()) {	// pro kazdy rozhrani

			EthernetInterface ethInterface = nm.ethernetLayer.addInterface(ifaceModel.getName(), new MacAddress(ifaceModel.getMacAddress()));
			// -> pridani novyho rozhrani ethernetovy vrstve, interface si jeste podrzim, abych mu moh pridavat switchporty
			int cisloSwitchportu = switchporty.get(ifaceModel.getId());	// zjistim si z odkladaci mapy, ktery cislo switchportu mam priradit
			ethInterface.addSwitchportSettings(nm.ethernetLayer.getSwitchport(cisloSwitchportu));	// samotny prirazeni switchportu

			IPwithNetmask ip = null;
			if (ifaceModel.getIpAddress() != null && !ifaceModel.getIpAddress().equals("")) {
				ip = new IPwithNetmask(ifaceModel.getIpAddress(), 24, true);
			}

			NetworkInterface netInterface = new NetworkInterface(ifaceModel.getId(), ifaceModel.getName(), ip, ethInterface, ifaceModel.isIsUp());
			nm.ipLayer.addNetworkInterface(netInterface);
		}

		//nahrani osatnich nastaveni sitovyho modulu:

		// nastaveni routovaci tabulky:
		if (model.getDevSettings() != null) {	// network modul uz byl nekdy ulozenej, nahrava se z neho

			if (nm.ipLayer instanceof CiscoIPLayer) { // cisco specific
				CiscoIPLayer layer = ((CiscoIPLayer) (nm.ipLayer));
				ciscoSettings.put(layer, model.getDevSettings().getRoutingTabConfig());
			} else {
				for (Record record : model.getDevSettings().getRoutingTabConfig().getRecords()) { //pro vsechny zaznamy
					IPwithNetmask adresat = new IPwithNetmask(record.getDestination(), 32, false);
					IpAddress brana = null;
					if (record.getGateway() != null) {
						brana = new IpAddress(record.getGateway());
					}
					NetworkInterface iface = nm.ipLayer.getNetworkInteface(record.getInterfaceName());
					nm.ipLayer.routingTable.addRecordWithoutControl(adresat, brana, iface);
				}
			}
		} else {	// network modul jeste nebyl ulozenej, je cerstve vytvorenej Martinouvym simulatorem, je potreba tabulku donastavit dle rozhrani
			if (nm.ipLayer instanceof CiscoIPLayer) { // cisco specific
				CiscoIPLayer layer = ((CiscoIPLayer) (nm.ipLayer));
				ciscoSettings.put(layer, null);
			} else {
				nm.ipLayer.updateNewRoutingTable();
			}
		}

		// nastaveni NATu:
		if (model.getDevSettings() != null && model.getDevSettings().getNatConfig() != null) {
			NatConfig config = model.getDevSettings().getNatConfig();
			NatTable natTable = nm.ipLayer.getNatTable();

			// inside
			if (config.getInside() != null) {
				for (NetworkInterface iface : nm.ipLayer.getSortedNetworkIfaces()) {
					if (config.getInside().contains(iface.name)) {
						natTable.addInside(iface);
					}
				}
			}

			// outside
			if (config.getOutside() != null) {
				NetworkInterface iface = nm.ipLayer.getNetworkInteface(config.getOutside());
				if (iface != null) {
					natTable.setOutside(iface);
				}
			}

			// pool
			if (config.getPools() != null) {
				for (NatPoolConfig pool : config.getPools()) {
					IpAddress start = new IpAddress(pool.getStart());
					IpAddress end = new IpAddress(pool.getEnd());
					natTable.lPool.addPool(start, end, pool.getPrefix(), pool.getName());
				}
			}

			// poolAccess
			if (config.getPoolAccesses() != null) {
				for (NatPoolAccessConfig poolAcc : config.getPoolAccesses()) {
					natTable.lPoolAccess.addPoolAccess(poolAcc.getNumber(), poolAcc.getPoolName(), poolAcc.isOverload());
				}
			}

			// accessList
			if (config.getAccessLists() != null) {
				for (NatAccessListConfig acc : config.getAccessLists()) {
					IpNetmask mask = IpNetmask.maskFromWildcard(acc.getWildcard());
					IpAddress adr = new IpAddress(acc.getAddress());
					IPwithNetmask all = new IPwithNetmask(adr, mask);
					natTable.lAccess.addAccessList(all, acc.getNumber());
				}
			}

			// static rules
			if (config.getRules() != null) {
				for (StaticRule rule : config.getRules()) {
					natTable.addStaticRuleLinux(new IpAddress(rule.getIn()), new IpAddress(rule.getOut()));
				}
			}
		}

		//TODO pripadne nejaky dalsi nastaveni 4. vrstvy?


		return nm;
	}

	/**
	 * Vytvori sitovej modul switche, uplne ignoruje jeho nastaveni z konfigurace (kdyby tam nejaky bylo).
	 *
	 * @param model
	 * @param pc
	 * @return
	 */
	private NetworkModule createSwitchNetworkModule(HwComponentModel model, Device pc) {
		SwitchNetworkModule nm = new SwitchNetworkModule(pc);
		EthernetInterface ethIface = nm.ethernetLayer.addInterface("switch_default", MacAddress.getRandomMac());
			// -> switchi se priradi jedno rozhrani a da se mu nahodna mac
		nm.ethernetLayer.addAllSwitchportsToGivenInterface(ethIface);
		ethIface.switchingEnabled = true;
		return nm;
	}

	/**
	 * Projde vsechny kabely a spoji nase sitovy prvky. Specialne taky resi realny switchporty.
	 *
	 * @param network
	 */
	private void connectCables() {
		for (CableModel cableModel : networkModel.getCables()) {	// pro vsechny kabely
			registerID(cableModel.getId());	// registruje se id
			Cable cable = new Cable(cableModel.getId(), cableModel.getDelay());	// vytvari se novej kabel

			HwComponentModel pcModel1 = cableModel.getComponent1();
			HwComponentModel pcModel2 = cableModel.getComponent2();

			if (pcModel1.getHwType() != HwTypeEnum.REAL_PC && pcModel2.getHwType() != HwTypeEnum.REAL_PC) { // oba dva pocitace simulovany

				Logger.log(this, Logger.DEBUG, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Ani jedna komponenta neni realna.", cable);

				// zapojeni 1. switchportu:
				SimulatorSwitchport swportFirst = (SimulatorSwitchport) findSwitchportFor(cableModel.getComponent1(), cableModel.getInterface1());
				cable.setFirstSwitchport(swportFirst);
				cable.setFirstDeviceId(cableModel.getComponent1().getId());

				// zapojeni 2. switchportu:
				SimulatorSwitchport swportSecond = (SimulatorSwitchport) findSwitchportFor(pcModel2, cableModel.getInterface2());
				cable.setSecondSwitchport(swportSecond);
				cable.setSecondDeviceId(cableModel.getComponent2().getId());

			} else if(pcModel1.getHwType() == HwTypeEnum.REAL_PC && cableModel.getComponent2().getHwType() == HwTypeEnum.REAL_PC){ // oba 2 pocitace realny
				// nepripustny stav, hodi se vyjimka
				throw new LoaderException("There are two real devices connected to each other which is forbidden: "+pcModel1.getName()+" a "+pcModel2.getName());

			} else if (pcModel1.getHwType()==HwTypeEnum.REAL_PC) {	// pocitac 1 je realnej
				// pocitaci 2 se nastavi realnej switchport:
				Logger.log(this, Logger.DEBUG, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Jdu vytvorit realnej switchport.", null);
				SimulatorSwitchport swportSecond = (SimulatorSwitchport) findSwitchportFor(pcModel2, cableModel.getInterface2());
				swportSecond.replaceWithRealSwitchport();

			} else { // posledni moznost - pocitac 2 je realnej
				Logger.log(this, Logger.DEBUG, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Jdu vytvorit realnej switchport.", null);
				SimulatorSwitchport swportFirst = (SimulatorSwitchport) findSwitchportFor(pcModel1, cableModel.getInterface1());
				swportFirst.replaceWithRealSwitchport();
			}
		}
	}

	/**
	 * Projde vsechny kabely a spoji nase sitovy prvky. Specialne taky resi realny switchporty.
	 *
	 * @param network
	 */
	private void connectCablesV2() {
		for (CableModel cableModel : networkModel.getCables()) {	// pro vsechny kabely
			registerID(cableModel.getId());	// registruje se id
			CableV2 cable = new CableV2(cableModel.getId());	// vytvari se novej kabel

			HwComponentModel pcModel1 = cableModel.getComponent1();
			HwComponentModel pcModel2 = cableModel.getComponent2();

			if (pcModel1.getHwType() != HwTypeEnum.REAL_PC && pcModel2.getHwType() != HwTypeEnum.REAL_PC) { // oba dva pocitace simulovany

				Logger.log(this, Logger.DEBUG, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Ani jedna komponenta neni realna.", cable);

				// zapojeni 1. switchportu:
				SimulatorSwitchportV2 swportFirst = (SimulatorSwitchportV2) findSwitchportFor(cableModel.getComponent1(), cableModel.getInterface1());
				cable.setFirstSwitchport(swportFirst);
				cable.setFirstDeviceId(cableModel.getComponent1().getId());

				// zapojeni 2. switchportu:
				SimulatorSwitchportV2 swportSecond = (SimulatorSwitchportV2) findSwitchportFor(pcModel2, cableModel.getInterface2());
				cable.setSecondSwitchport(swportSecond);
				cable.setSecondDeviceId(cableModel.getComponent2().getId());

				if (!swportFirst.isConnected()) {
					throw new LoaderException("1. swport is not connected to the 2. swport! "+swportFirst.getDescription());
				}

			} else if(pcModel1.getHwType() == HwTypeEnum.REAL_PC && cableModel.getComponent2().getHwType() == HwTypeEnum.REAL_PC){ // oba 2 pocitace realny
				// nepripustny stav, hodi se vyjimka
				throw new LoaderException("There are two real devices connected to each other which is forbidden: "+pcModel1.getName()+" a "+pcModel2.getName());

			} else if (pcModel1.getHwType()==HwTypeEnum.REAL_PC) {	// pocitac 1 je realnej
				// pocitaci 2 se nastavi realnej switchport:
				Logger.log(this, Logger.DEBUG, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Jdu vytvorit realnej switchport.", null);
				SimulatorSwitchportV2 swportSecond = (SimulatorSwitchportV2) findSwitchportFor(pcModel2, cableModel.getInterface2());
				swportSecond.replaceWithRealSwitchport();

			} else { // posledni moznost - pocitac 2 je realnej
				Logger.log(this, Logger.DEBUG, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Jdu vytvorit realnej switchport.", null);
				SimulatorSwitchportV2 swportFirst = (SimulatorSwitchportV2) findSwitchportFor(pcModel1, cableModel.getInterface1());
				swportFirst.replaceWithRealSwitchport();
			}
		}
	}

	/**
	 * Najde switchport, ktery odpovida zadanemu zarizeni a rozhrani.
	 *
	 * @param component1
	 * @param interface1
	 * @return
	 */
	private Switchport findSwitchportFor(HwComponentModel component1, EthInterfaceModel interface1) {
		for (Device device : s.devices) {
			if (device.configID == component1.getId()) {
				for (Switchport swp : device.physicalModule.getSwitchports().values()) {
					if ((swp instanceof SimulatorSwitchport || swp instanceof SimulatorSwitchportV2) && swp.configID == interface1.getId()) {
						return swp;
					}
				}
			}
		}

		throw new LoaderException(String.format("Could not find Device with id=%d a for Switchport with id=%d", component1.getId(), interface1.getId()));
	}

	@Override
	public String getDescription() {
		return getClass().getName();
	}

	private void registerID(int id){
		if(idecka.contains(id)){
			Logger.log(this, Logger.ERROR, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "There are two objects in configuration file with the same ID: id = "+id, null);
		}
		idecka.add(id);
	}

	private void registerName(String name, HwComponentModel model){
		if(names.contains(name)){
			Logger.log(this, Logger.ERROR, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "There are two interfaces with the same name: "+name+ " on the same Device: ("+model.getName()+")", null);
		}
		names.add(name);
	}

	/**
	 * Updates cisco routing table.
	 */
	private void updateRoutingTableForCisco() {
		for(CiscoIPLayer layer : ciscoSettings.keySet()) {
			CiscoWrapperRT wrapper = layer.wrapper;

			wrapper.update(); // nasype IP z rozhrani

			if (ciscoSettings.get(layer) != null) {
				for (Record record : ciscoSettings.get(layer).getRecords()) { //pro vsechny zaznamy
					IPwithNetmask adresat = new IPwithNetmask(record.getDestination(), 32, false);
					if (record.getGateway() != null) {
						IpAddress brana = new IpAddress(record.getGateway());
						wrapper.addRecord(adresat, brana);
					} else {
						NetworkInterface iface = layer.getNetworkInteface(record.getInterfaceName());
						wrapper.addRecord(adresat, iface);
					}
				}
			}
        }
	}
}
