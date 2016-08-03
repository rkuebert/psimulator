package psimulator.dataLayer.Singletons;

import java.util.ArrayList;
import java.util.List;
import shared.Components.HwTypeEnum;
import shared.Components.NetworkCounterModel;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class GeneratorSingleton {

    private NetworkCounterModel networkCounterModel;

    private GeneratorSingleton() {
    }

    public static GeneratorSingleton getInstance() {
        return IdGeneratorSingletonHolder.INSTANCE;
    }

    private static class IdGeneratorSingletonHolder {

        private static final GeneratorSingleton INSTANCE = new GeneratorSingleton();
    }

    /**
     *
     */
    public void initialize(NetworkCounterModel networkCounterModel) {
        this.networkCounterModel = networkCounterModel;
    }

    /**
     * Returns next free ID for unique identification in network model.
     *
     * @return Free id.
     */
    public int getNextId() {
        return networkCounterModel.getNextId();
    }


    /**
     * Creates list with names of interfaces for one single device. Names are
     * created according to hwType.
     *
     * @param hwTpe HwType for wich the names are generated.
     * @param count Count of generated names.
     * @return List with generated names.
     */
    public List<String> getInterfaceNames(HwTypeEnum hwType, int count) {
        List<String> names = new ArrayList<>();

        int counter = 0;

        for (int i = 0; i < count; i++) {
            String name = getInterfaceName(hwType, counter);
            names.add(name);
            counter++;
        }

        return names;
    }
    
    /**
     * Creates single interface name according to hwType and sequence number.
     * @param hwType HwType for wich the name is generated.
     * @param sequence Sequence number of interface
     * @return Generated interface name
     */
    public String getInterfaceName(HwTypeEnum hwType, int sequence){
        String prefix = "";
        String suffix = "";

        switch (hwType) {
            case LINUX_ROUTER:
            case LINUX_SWITCH:
            case END_DEVICE_NOTEBOOK:
            case END_DEVICE_PC:
            case END_DEVICE_WORKSTATION:
            case REAL_PC:
                prefix = "eth";
                break;
            case CISCO_ROUTER:
            case CISCO_SWITCH:
                prefix = "FastEthernet0/";
                break;
            default:
                // this should never happen
                System.err.println("error in GeneratorSingleton2");
        }
        
        return (prefix + sequence + suffix);
    }

    /**
     * Generates name for hwType. The name has a number from a number line for
     * each hwType extra. For example for PC will be generated PC0, for Switch
     * swich
     *
     * @param hwTp
     * @return generated name.
     */
    public String getNextDeviceName(HwTypeEnum hwType) {
        String name;

        String prefix = "";
        int number = 0;

        switch (hwType) {
            case LINUX_ROUTER:
                prefix = "router";
                // get number
                number = networkCounterModel.getFromNumberMap(HwTypeEnum.LINUX_ROUTER).intValue();
                // increase counter
                networkCounterModel.putToNumberMap(HwTypeEnum.LINUX_ROUTER, new Integer(number + 1));
                break;
            case CISCO_ROUTER:
                prefix = "Router";
                // get number
                number = networkCounterModel.getFromNumberMap(HwTypeEnum.LINUX_ROUTER).intValue();
                // increase counter
                networkCounterModel.putToNumberMap(HwTypeEnum.LINUX_ROUTER, new Integer(number + 1));
                break;
            case LINUX_SWITCH:
                prefix = "switch";
                // get number
                number = networkCounterModel.getFromNumberMap(HwTypeEnum.LINUX_SWITCH).intValue();
                // increase counter
                networkCounterModel.putToNumberMap(HwTypeEnum.LINUX_SWITCH, new Integer(number + 1));
                break;
            case CISCO_SWITCH:
                prefix = "Switch";
                // get number
                number = networkCounterModel.getFromNumberMap(HwTypeEnum.LINUX_SWITCH).intValue();
                // increase counter
                networkCounterModel.putToNumberMap(HwTypeEnum.LINUX_SWITCH, new Integer(number + 1));
                break;
            case END_DEVICE_NOTEBOOK:
                prefix = "notebook";
                // get number
                number = networkCounterModel.getFromNumberMap(hwType).intValue();
                // increase counter
                networkCounterModel.putToNumberMap(hwType, new Integer(number + 1));
                break;
            case END_DEVICE_PC:
                prefix = "pc";
                // get number
                number = networkCounterModel.getFromNumberMap(hwType).intValue();
                // increase counter
                networkCounterModel.putToNumberMap(hwType, new Integer(number + 1));
                break;
            case END_DEVICE_WORKSTATION:
                prefix = "workstation";
                // get number
                number = networkCounterModel.getFromNumberMap(hwType).intValue();
                // increase counter
                networkCounterModel.putToNumberMap(hwType, new Integer(number + 1));
                break;
            case REAL_PC:
                prefix = "realpc";
                // get number
                number = networkCounterModel.getFromNumberMap(hwType).intValue();
                // increase counter
                networkCounterModel.putToNumberMap(hwType, new Integer(number + 1));
                break;
            default:
                // this should never happen
                System.err.println("error in GeneratorSingleton1");
        }

        name = prefix + number;

        return name;
    }

    /**
     * Creates mac address in format AA-11-E0-XX-XX-XX, where Xes are generated
     * in line from 0 in each project.
     *
     * @return Generated mac address
     */
//    public String getNextMacAddress() {
//        String macAddress;
//
//        String macAddressManufacturerPrefix = "AA-11-E0-";
//        String macAddressDeviceSuffix = "";
//
//        String tmp = Integer.toHexString(networkCounterModel.getCurrentMacAddress()).toUpperCase();
//
//        // fill the rest of address
//        for (int i = 0; i < 6; i++) {
//            // insert zeros until tmp hit
//            if (6 - i <= tmp.length()) {
//                macAddressDeviceSuffix += tmp.charAt(tmp.length() - (6 - i));
//            } else {
//                macAddressDeviceSuffix += "0";
//            }
//
//            // if we have to make dash
//            if (i % 2 == 1 && i < 6 - 1) {
//                macAddressDeviceSuffix += "-";
//            }
//        }
//
//        // increase counter
//        //nextMacAddress++;
//        networkCounterModel.increaseMacAddressCounter();
//
//        // glue two parts together
//        macAddress = macAddressManufacturerPrefix + macAddressDeviceSuffix;
//
//        return macAddress;
//    }

    /**
     * Creates new random MAC address. Address will not be broadcast MAC address.
     * @return 
     */
    public static String getNextMacAddress() {
        byte[] representation = new byte[6];
        for (int i = 0; i < 6; i++) {
            representation[i] = (byte) (Math.random() * 256);
        }

        boolean broadcast = true;
        for (int i = 0; i < representation.length; i++) { // check if broadcast address
            if ((int) representation[i] != -1) { // == 255
                broadcast = false && broadcast;
            }
        }

        if (broadcast) {
            return getNextMacAddress();
        }

        String ret = "";
        for (int i = 0; i < 6; i++) {
            String v = Integer.toHexString(representation[i] & 0xff);
            if (v.length() < 2) {
                v = "0" + v;
            }
            ret += v;
            ret += "-";
        }
        
        ret = ret.toUpperCase();
        
        return ret.substring(0, ret.length() - 1);// remove last dash 
    }
}
