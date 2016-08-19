package shared.Components;

import java.util.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import shared.Components.simulatorConfig.DeviceSettings;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class HwComponentModel implements PositionInterface, NameInterface, AbstractComponentModel {

    /**
     * LinkedHashMap of EthInterfaces that component owns. Key is the
     * ethInterface ID.
     */
    private Map<Integer, EthInterfaceModel> interfacesMap;
    /**
     * Device name.
     */
    private String name;
    /**
     * X position of component in Default zoom
     */
    private int defaultZoomXPos;
    /**
     * Y position of component in Default zoom
     */
    private int defaultZoomYPos;
    /**
     * Type of component
     */
    private HwTypeEnum hwType;
    /**
     * Id of component
     */
    private Integer id;
    /**
     * Device specific configuration.
     */
    private DeviceSettings devSettings;

    public HwComponentModel(Integer id, HwTypeEnum hwType, String deviceName, List<EthInterfaceModel> ethInterfaces,
            int defaultZoomXPos, int defaultZoomYPos) {

        this.id = id;
        this.hwType = hwType;
        this.name = deviceName;
        this.defaultZoomXPos = defaultZoomXPos;
        this.defaultZoomYPos = defaultZoomYPos;


        this.interfacesMap = new LinkedHashMap<>();

        for (EthInterfaceModel ethInterface : ethInterfaces) {
            interfacesMap.put(ethInterface.getId(), ethInterface);
        }
    }

    public HwComponentModel() {
        this.interfacesMap = new LinkedHashMap<>();
    }

    @XmlAttribute
    @XmlID
    public String getIDAsString() {
        return String.valueOf(this.id);
    }

    public void setIDAsString(String id) {
        this.id = Integer.valueOf(id);
    }

    /**
     * Gets the first available ethInterface. If no interface is available,
     * returns null.
     *
     * @return the first available interface or <code>null</code> if no
     *  interface is available
     */
    public EthInterfaceModel getFirstFreeInterface() {
        for (EthInterfaceModel ei : interfacesMap.values()) {
            if (!ei.hasCable()) {
                return ei;
            }
        }
        return null;
    }

    /**
     * Returns <code>true</code> if this component has an available EthInterface,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if this component has an available EthInterface,
     * <code>false</code> otherwise
     */
    public boolean hasFreeInterace() {
        for (EthInterfaceModel ei : interfacesMap.values()) {
            if (!ei.hasCable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns interface names as a an array of Objects
     * 
     * @return interface names as a an array of Objects
     */
    public Object[] getInterfacesNames() {
        // FIXME Why on earth noth a List<String>?
        Object[] list = new Object[interfacesMap.size()];

        int i = 0;
        for (EthInterfaceModel ei : interfacesMap.values()) {
            list[i] = ei.getName();
            i++;
        }
        return list;
    }

    /**
     * Returns the ethInterface with the specified id
     * 
     * @param interfaceId the interface's id
     * 
     * @return the interface with the given id or <code>null</code> if no
     *  interface with the given id exists
     */
    public EthInterfaceModel getEthInterface(Integer interfaceId) {
        return interfacesMap.get(interfaceId);
    }

    /**
     * Gets ethInterface at specified index
     * @param index
     * @return 
     */
    public EthInterfaceModel getEthInterfaceAtIndex(int index) {
        List<EthInterfaceModel> list = new ArrayList<EthInterfaceModel>(interfacesMap.values());
        return list.get(index);
    }

    /**
     * Gets ethInterfaces count
     * @return 
     */
    public int getEthInterfaceCount() {
        return interfacesMap.size();
    }

    /**
     * Gets X position of component in default zoom
     *
     * @return
     */
    @Override
    public int getDefaultZoomXPos() {
        return defaultZoomXPos;
    }

    /**
     * Gets Y position of component in default zoom
     *
     * @return
     */
    @Override
    public int getDefaultZoomYPos() {
        return defaultZoomYPos;
    }

    /**
     * Sets X position of component in default zoom
     *
     * @param defaultZoomXPos
     */
    @Override
    public void setDefaultZoomXPos(int defaultZoomXPos) {
        this.defaultZoomXPos = defaultZoomXPos;
    }

    /**
     * Sets Y position of component in default zoom
     *
     * @param defaultZoomYPos
     */
    @Override
    public void setDefaultZoomYPos(int defaultZoomYPos) {
        this.defaultZoomYPos = defaultZoomYPos;
    }

    /**
     * Sets name of component
     *
     * @param name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets name of component
     *
     * @return
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns collection of interfaces
     */
    public Collection getEthInterfaces() {
        return interfacesMap.values();
    }

    public Map<Integer, EthInterfaceModel> getInterfacesMap() {
        return interfacesMap;
    }

    public void setInterfacesMap(Map<Integer, EthInterfaceModel> interfacesMap) {
        this.interfacesMap = interfacesMap;
    }
    
    @Override
    public HwTypeEnum getHwType() {
        return hwType;
    }

    @Override
    public void setHwType(HwTypeEnum hwType) {
        this.hwType = hwType;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public DeviceSettings getDevSettings() {
        return devSettings;
    }

    public void setDevSettings(DeviceSettings devSettings) {
        this.devSettings = devSettings;
    }
    
        /**
     * Removes interface with specified ID
     * @param id 
     */
    public void removeInterface(EthInterfaceModel eth){
        interfacesMap.remove(eth.getId());
        
        // unbind eth interface and component
        eth.setHwComponent(null);
    }

    /**
     * Adds interface in parameter to interfaces map
     * @param eth 
     */
    public void addInterface(EthInterfaceModel eth){
        // bind eth interface and component together
        eth.setHwComponent(this);
        
        interfacesMap.put(eth.getId(), eth);
    }
    
    /**
     * Returns minimum interface count for component
     * @return 
     */
    public int getMinInterfaceCount(){
        switch(hwType){
            case CISCO_ROUTER:
            case CISCO_SWITCH:
            case LINUX_ROUTER:
            case LINUX_SWITCH:
                return 2;
            case END_DEVICE_NOTEBOOK:
            case END_DEVICE_PC:
            case END_DEVICE_WORKSTATION:
            case REAL_PC:
                return 1;
            default:
                return 0;
        }
    }
    
    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("*** HW component %s (%s) ***\n", this.getName(), this.getId()));
        sb.append(String.format("\tType: %s\n", this.hwType.toString()));
        sb.append(String.format("\tXPos: %s\n", this.defaultZoomXPos));
        sb.append(String.format("\tYPos: %s\n", this.defaultZoomYPos));
        sb.append("\tNumber of interfaces: ").append(this.interfacesMap.size()).append("\n");
        
        for (Map.Entry<Integer, EthInterfaceModel> entry : interfacesMap.entrySet()) {
            EthInterfaceModel value = entry.getValue();
            sb.append(String.format("\t\t%s\n", value.toString()));
        }
        
        return sb.toString();
    }
}
