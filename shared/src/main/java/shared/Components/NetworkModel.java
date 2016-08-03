package shared.Components;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
@XmlRootElement
//@XmlType(propOrder={"componentsMap", "cablesMap", "networkCounterModel", "lastEditTimestamp" })
public class NetworkModel implements Identifiable, Serializable{
    /**
     * Map with components. Identified by component ID.
     */
    private Map<Integer, HwComponentModel> componentsMap;
    /**
     * Map with cables. Identified by cable ID.
     */
    private Map<Integer, CableModel> cablesMap;
    /**
     * Counter with numer lines.
     */
    private NetworkCounterModel networkCounterModel;
    /**
     * Last edit timestamp in milliseconds.
     */
    private long lastEditTimestamp;
    /**
     * Id of network
     */
    private Integer id;
 
    public NetworkModel(LinkedHashMap<Integer, HwComponentModel> componentsMap, LinkedHashMap<Integer, CableModel> cablesMap, 
            long lastEditTimestamp, Integer id, NetworkCounterModel networkCounterModel) {
        this.componentsMap = componentsMap;
        this.cablesMap = cablesMap;
        this.networkCounterModel = networkCounterModel; 
        this.lastEditTimestamp = lastEditTimestamp;
        this.id = id;
    }

    /**
     * no-arg constructor needed for jaxb
     */
    public NetworkModel() {
    }

    
    /**
     * Gets timestamp of last edit
     * @return 
     */
    public long getLastEditTimestamp() {
        return lastEditTimestamp;
    }

    /**
     * Sets timestamp of last edit
     * @param lastEditTimestamp 
     */
    public void setLastEditTimestamp(long lastEditTimestamp) {
        this.lastEditTimestamp = lastEditTimestamp;
    }

    @XmlElement(name = "counter")
    public NetworkCounterModel getNetworkCounterModel() {
        return networkCounterModel;
    }
    
    /**
     * Gets unique ID
     * @return 
     */
    @Override
    public Integer getId() {
        return id;
    }
    
    /**
     * Gets count of hw components
     * @return 
     */
    public int getHwComponentsCount() {
        return componentsMap.size();
    }
    
    /**
     * Gets all components as collection
     * @return 
     */
    public Collection<HwComponentModel> getHwComponents() {
        return componentsMap.values();
    }
    
    /**
     * Gets all cables as collection
     * @return 
     */
    public Collection<CableModel> getCables() {
        return cablesMap.values();
    }

    /**
     * Adds component to components
     * @param component 
     */
    public void addHwComponent(HwComponentModel component) {
        componentsMap.put(component.getId(), component);
    }
    
    /**
     * Adds all components from list to components
     * @param componentList 
     */
    public void addHwComponents(List<HwComponentModel> componentList) {
        for (HwComponentModel component : componentList) {
            addHwComponent(component);
        }
    }
    
    /**
     * Removes component from components
     * @param component 
     */
    public void removeHwComponent(HwComponentModel component) {
        componentsMap.remove(component.getId());
    }
    
    /**
     * Removes all components from components
     * @param componentList 
     */
    public void removeHwComponents(List<HwComponentModel> componentList) {
        for(HwComponentModel component : componentList){
            removeHwComponent(component);
        }
    }

    /**
     * Add cable to cables map
     * @param cableModel 
     */
    public void addCable(CableModel cableModel){
        cablesMap.put(cableModel.getId(), cableModel);
    }
    
    /**
     * Removes calbe from cables map
     * @param cableModel 
     */
    public void removeCable(CableModel cableModel){
        cablesMap.remove(cableModel.getId());
    }
    
    /**
     * Gets cables count
     * @return 
     */
    public int getCablesCount() {
        return cablesMap.size();
    }
    
    /**
     * Gets component by ID
     * @param id
     * @return 
     */
    public HwComponentModel getHwComponentModelById(int id){
        return componentsMap.get(id);
    }
    
    /**
     * Gets cable by ID
     * @param id
     * @return 
     */
    public CableModel getCableModelById(int id){
        return cablesMap.get(id);
    }

    /**
     * Gets all cables in map
     * @return 
     */
    public Map<Integer, CableModel> getCablesMap() {
        return cablesMap;
    }

    /**
     * Sets calbes map
     * @param cablesMap 
     */
    public void setCablesMap(Map<Integer, CableModel> cablesMap) {
        this.cablesMap = cablesMap;
    }

    /**
     * Gets components map
     * @return 
     */
    public Map<Integer, HwComponentModel> getComponentsMap() {
        return componentsMap;
    }

    /**
     * Sets components map
     * @param componentsMap 
     */
    public void setComponentsMap(Map<Integer, HwComponentModel> componentsMap) {
        this.componentsMap = componentsMap;
    }

    /**
     * Sets unique ID
     * @param id 
     */
    public void setId(Integer id) {
        this.id = id;
    }

    public void setNetworkCounterModel(NetworkCounterModel networkCounterModel) {
        this.networkCounterModel = networkCounterModel;
    }
    
    
}
