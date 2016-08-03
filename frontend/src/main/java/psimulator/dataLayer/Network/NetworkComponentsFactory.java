package psimulator.dataLayer.Network;

import java.util.*;
import psimulator.dataLayer.Singletons.GeneratorSingleton;
import shared.Components.*;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkComponentsFactory {

    public NetworkComponentsFactory() {
    }

    /**
     * Creates empty network model with emtpty maps.
     * @return 
     */
    public NetworkModel createEmptyNetworkModel() {
        LinkedHashMap<Integer, HwComponentModel> componentsMap = new LinkedHashMap<Integer, HwComponentModel>();
        LinkedHashMap<Integer, CableModel> cablesMap = new LinkedHashMap<Integer, CableModel>();
        long lastEditTimestamp = 0L;
        Integer id = new Integer(-1);
        NetworkCounterModel networkCounterModel = createEmptyNetworkCounter();

        NetworkModel networkModel = new NetworkModel(componentsMap, cablesMap, lastEditTimestamp, id, networkCounterModel);
        
        return networkModel;
    }
    
    /**
     * Creates new initialized network counter.
     * @return 
     */
    public NetworkCounterModel createEmptyNetworkCounter(){
        
        int nextId = 0;
        int nextMacAddress = 0;
        Map<HwTypeEnum, Integer> nextNumberMap = new EnumMap<HwTypeEnum, Integer>(HwTypeEnum.class);
        
        for (HwTypeEnum hwTypeEnum : HwTypeEnum.values()) {
            nextNumberMap.put(hwTypeEnum, new Integer(0));
        }
        
        NetworkCounterModel networkCounterModel = new NetworkCounterModel(nextId, nextMacAddress, nextNumberMap);
        
        return networkCounterModel;
    }

    /**
     * Creates cable betweem two components.
     * @param hwType
     * @param component1
     * @param component2
     * @param interface1
     * @param interface2
     * @return 
     */
    public CableModel createCable(HwTypeEnum hwType, HwComponentModel component1, HwComponentModel component2, EthInterfaceModel interface1, EthInterfaceModel interface2) {
        Integer id = GeneratorSingleton.getInstance().getNextId();

        int delay;

        // set delay according to type
        switch (hwType) {
            case CABLE_ETHERNET:
                delay = 10;
                break;
            case CABLE_OPTIC:
            default:
                delay = 2;
                break;
        }

        CableModel cable = new CableModel(id, hwType, component1, component2, interface1, interface2, delay);

        return cable;
    }

    /**
     * Creates component with desired parameters.
     * @param hwType
     * @param interfacesCount
     * @param defaultZoomXPos
     * @param defaultZoomYPos
     * @return 
     */
    public HwComponentModel createHwComponent(HwTypeEnum hwType, int interfacesCount, int defaultZoomXPos, int defaultZoomYPos) {
        // generate ID for HwComponent
        Integer id = GeneratorSingleton.getInstance().getNextId();

        // generate device name for HwComponent
        String deviceName = GeneratorSingleton.getInstance().getNextDeviceName(hwType);

        // generate names for interface
        List<String> ethInterfaceNames = GeneratorSingleton.getInstance().getInterfaceNames(hwType, interfacesCount);

        // create interfaces
        List<EthInterfaceModel> ethInterfaces = new ArrayList<>();

        for (int i = 0; i < interfacesCount; i++) {
            ethInterfaces.add(createEthInterface(ethInterfaceNames.get(i), hwType));
        }

        // create hw component
        HwComponentModel hwComponent = new HwComponentModel(id, hwType, deviceName, ethInterfaces, defaultZoomXPos, defaultZoomYPos);

        // set HwComponent to ethInterfaces
        for (EthInterfaceModel ethInterface : ethInterfaces) {
            ethInterface.setHwComponent(hwComponent);
        }

        return hwComponent;
    }

    /**
     * Creates new EthInterfaceModel for device of hwType type and name interfaceName.
     * @param interfaceName
     * @param hwType
     * @return 
     */
    private EthInterfaceModel createEthInterface(String interfaceName, HwTypeEnum hwType) {
        String macAddress;
        String ipAddress;
        boolean isUp = false;

        // do not generate MAC for switches and real pc
        switch (hwType) {
            case LINUX_SWITCH:
            case CISCO_SWITCH:
            case REAL_PC:
                macAddress = "";
                ipAddress = "";
                break;
            default:
                macAddress = GeneratorSingleton.getInstance().getNextMacAddress();
                ipAddress = "";
                break;
        }
        
        switch (hwType) {
            case LINUX_ROUTER:
                isUp = true;
                break;
            case CISCO_ROUTER:
                isUp = false;
                break;
            case END_DEVICE_WORKSTATION:
            case END_DEVICE_NOTEBOOK:
            case END_DEVICE_PC:
                isUp = true;
                break;
            default: 
                isUp = false;
                break;
        }

        Integer interfaceId = GeneratorSingleton.getInstance().getNextId();
        HwComponentModel hwComponentModel = null;
        CableModel cable = null;

        EthInterfaceModel ethInterfaceModel = new EthInterfaceModel(interfaceId, hwType, hwComponentModel, cable, ipAddress, macAddress, interfaceName, isUp);

        return ethInterfaceModel;
    }
    
    /**
     * Creates next eth interface for existing component.
     * @param component
     * @return 
     */
    public EthInterfaceModel createNextEthInterface(HwComponentModel component, int sequence){
        HwTypeEnum hwType = component.getHwType();
        // generate interface name
        String interfaceName = GeneratorSingleton.getInstance().getInterfaceName(hwType, sequence);
        
        // create interface
        EthInterfaceModel ethInterface = createEthInterface(interfaceName, hwType);
        
        // Component is NOT SET to interface, will be set when it is really added to component
        
        return ethInterface;
    }
}
