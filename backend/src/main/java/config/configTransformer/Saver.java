/*
 * created 5.3.2012
 */
package config.configTransformer;

import device.Device;
import java.util.ArrayList;
import java.util.List;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.CiscoWrapperRT;
import networkModule.L3.CiscoWrapperRT.CiscoRecord;
import networkModule.L3.NetworkInterface;
import networkModule.L3.RoutingTable;
import networkModule.L3.nat.AccessList;
import networkModule.L3.nat.NatTable;
import networkModule.L3.nat.Pool;
import networkModule.L3.nat.PoolAccess;
import networkModule.IpNetworkModule;
import psimulator2.Psimulator;
import shared.Components.EthInterfaceModel;
import shared.Components.HwComponentModel;
import shared.Components.NetworkModel;
import shared.Components.simulatorConfig.*;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Saver {

	Psimulator s = Psimulator.getPsimulator();
	private final NetworkModel networkModel;

	public Saver(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}

	/**
	 * Ulozi nastaveni do netwrkModelu zadanyho v konstruktoru.
	 */
	public void saveToModel() {
		for (Device device : s.devices) {
			HwComponentModel hwComponentModel = networkModel.getHwComponentModelById(device.configID);

			if (hwComponentModel.getDevSettings() == null) {	// nema/li jeste simulatorovy nastaveni, musi se spustit
				hwComponentModel.setDevSettings(new DeviceSettings());
			}

			saveInterfaces(hwComponentModel, device);

			if (device.getNetworkModule().isStandardTcpIpNetMod()) {
				saveRoutingTable((IpNetworkModule) (device.getNetworkModule()), hwComponentModel);

				saveNatTable((IpNetworkModule) (device.getNetworkModule()), hwComponentModel);
			}
		}
	}

	/**
	 * Saves IP address with netmask and isUp of all interfaces of device. Saves also NetworkModuleType.
	 *
	 * @param hwComponentModel
	 * @param device
	 */
	private void saveInterfaces(HwComponentModel hwComponentModel, Device device) {
		if (!(device.getNetworkModule().isStandardTcpIpNetMod())) {
				// -> Zatim se uklada sitovy modul jen tehdy, pokud to je IpNetworkModule, navic se uklada zatim jen IPLayer.
			hwComponentModel.getDevSettings().setNetModType(DeviceSettings.NetworkModuleType.simple_switch_netMod);
			return;
		}

		IpNetworkModule netMod = (IpNetworkModule) device.getNetworkModule();
		hwComponentModel.getDevSettings().setNetModType(DeviceSettings.NetworkModuleType.tcp_ip_netmod);

		for (NetworkInterface iface : netMod.ipLayer.getNetworkIfaces()) {
			EthInterfaceModel ethIfaceModel = hwComponentModel.getEthInterface(iface.configID);
			if (iface.getIpAddress() != null) {
				ethIfaceModel.setIpAddress(iface.getIpAddress().toString());
			}
			ethIfaceModel.setIsUp(iface.isUp);
		}
	}

	private void saveRoutingTable(IpNetworkModule netMod, HwComponentModel model) {
		RoutingTableConfig rtc = new RoutingTableConfig();	// vytvorim novou prazdnou konfiguraci routovaci tabulky
		model.getDevSettings().setRoutingTabConfig(rtc);	// priradim tu novou konfiguraci do nastaveni pocitace
		RoutingTable rt = netMod.ipLayer.routingTable;

		if (netMod.ipLayer instanceof CiscoIPLayer) { // cisco uklada veci z wrapperu, ne obsah RT
			CiscoWrapperRT wrapper = ((CiscoIPLayer) netMod.ipLayer).wrapper;
			for (int i = 0; i < wrapper.getSize(); i++) {
				CiscoRecord record = wrapper.getRecord(i);
				if (record.getGateway() != null) { // adresa brana
					rtc.addRecord(record.getTarget().toString(), null, record.getGateway().toString());
				} else { // adresa iface
					rtc.addRecord(record.getTarget().toString(), record.getInterface().name, null);
				}
			}
		} else {
			for (int i = 0; i < rt.size(); i++) {
				RoutingTable.Record radek = rt.getRecord(i);
				if (radek.brana != null) {
					rtc.addRecord(radek.adresat.toString(), radek.iface.name, radek.brana.toString());
				} else {
					rtc.addRecord(radek.adresat.toString(), radek.iface.name, null);
				}
			}
		}
	}

	private void saveNatTable(IpNetworkModule netMod, HwComponentModel model) {
		NatConfig config = new NatConfig();
		model.getDevSettings().setNatConfig(config);

		NatTable natTable = netMod.ipLayer.getNatTable();

		// inside
		List<String> insides = new ArrayList<>();
		for (NetworkInterface iface : natTable.getInside()) {
			insides.add(iface.name);
		}
		config.setInside(insides);

		// outside
		config.setOutside(natTable.getOutside() != null ? natTable.getOutside().name : null);

		// pool
		List<NatPoolConfig> poolConfig = new ArrayList<>();
		for (Pool pool : natTable.lPool.getSortedPools()) {
			poolConfig.add(new NatPoolConfig(pool.name, pool.getLast() != null ? pool.getFirst().toString() : null, pool.getFirst() != null ? pool.getLast().toString() : null, pool.prefix));
		}
		config.setPools(poolConfig);

		// poolAccess
		List<NatPoolAccessConfig> pac = new ArrayList<>();
		for (PoolAccess pa : natTable.lPoolAccess.getSortedPoolAccess()) {
			pac.add(new NatPoolAccessConfig(pa.access, pa.poolName, pa.overload));
		}
		config.setPoolAccesses(pac);

		// accessList
		List<NatAccessListConfig> alc = new ArrayList<>();
		for (AccessList ac : natTable.lAccess.getList()) {
			alc.add(new NatAccessListConfig(ac.number, ac.ip.getIp().toString(), ac.ip.getMask().getWildcardRepresentation()));
		}
		config.setAccessLists(alc);

		// static rules
		List<StaticRule> rules = new ArrayList<>();
		for (NatTable.StaticRule rule : natTable.getStaticRules()) {
			rules.add(new StaticRule(rule.in.toString(), rule.out.toString()));
		}
		config.setRules(rules);
	}
}
