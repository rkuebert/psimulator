package psimulator.dataLayer.Network;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import shared.Components.*;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkFacade {
    
    private NetworkModel networkModel; 
    private NetworkComponentsFactory networkComponentsFactory;
    
    public NetworkFacade(){
        networkComponentsFactory = new NetworkComponentsFactory();
    }

    /**
     * Gets current network model.
     * @return 
     */
    public NetworkModel getNetworkModel() {
        return networkModel;
    }

    /**
     * Sets network model from parameter as current model.
     * @param network 
     */
    public void setNetworkModel(NetworkModel network) {
        this.networkModel = network;
    }
    
    /**
     * Creates new network model.
     * @return 
     */
    public NetworkModel createNetworkModel(){
        return networkComponentsFactory.createEmptyNetworkModel();
    }
    
    /**
     * Creates hwComponent in network.
     * @param hwType
     * @param interfacesCount
     * @param defaultZoomXPos
     * @param defaultZoomYPos
     * @return 
     */
    public HwComponentModel createHwComponentModel(HwTypeEnum hwType, int interfacesCount, int defaultZoomXPos, int defaultZoomYPos){
        HwComponentModel hwComponentModel = networkComponentsFactory.createHwComponent(hwType, interfacesCount, defaultZoomXPos, defaultZoomYPos); 
        return hwComponentModel;
    }
    
    /**
     * Creates cable in network.
     * @param hwType
     * @param component1
     * @param component2
     * @param interface1
     * @param interface2
     * @return 
     */
    public CableModel createCableModel(HwTypeEnum hwType, HwComponentModel component1, HwComponentModel component2, EthInterfaceModel interface1, EthInterfaceModel interface2){
        CableModel cableModel = networkComponentsFactory.createCable(hwType, component1, component2, interface1, interface2);
        return cableModel;
    }
    
    /**
     * Creates eth interface for component in parameter. The number will be according to sequence.
     * @param component
     * @param sequence
     * @return 
     */
    public EthInterfaceModel createEthInterface(HwComponentModel component, int sequence){
        return networkComponentsFactory.createNextEthInterface(component, sequence);
    }
    
    // -----------------------------------------------
    
    /**
     * Gets networkCounter of network.
     * @return 
     */
    public NetworkCounterModel getNetworkCounterModel(){
        return networkModel.getNetworkCounterModel();
    }
    
    /**
     * Get cables count.
     * @return 
     */
    public int getCablesCount(){
        return networkModel.getCablesCount();
    }
    
    /**
     * Gets component count.
     * @return 
     */
    public int getHwComponentsCount(){
        return networkModel.getHwComponentsCount();
    }
    
    /**
     * Returns all cables from network.
     * @return 
     */
    public Collection<CableModel> getCables(){
        return networkModel.getCables();
    } 
    
    /**
     * Adds cable in parameter to network.
     * @param cable 
     */
    public void addCable(CableModel cable) {
        cable.getInterface1().setCable(cable);
        cable.getInterface2().setCable(cable);
        
        // add cable to hash map
        networkModel.addCable(cable);

        // set timestamp of edit
        editHappend();
    }
    
    /**
     * Adds all cables in list to network.
     * @param cableList 
     */
    public void addCables(List<CableModel> cableList) {
        for (CableModel c : cableList) {
            addCable(c);
        }
    }
    
    /**
     * Removes cable in parameter from network.
     * @param cable
     */
    public void removeCable(CableModel cable) {
        cable.getInterface1().removeCable();
        cable.getInterface2().removeCable();

        // remove cable from hash map
        networkModel.removeCable(cable);
        
        // set timestamp of edit
        editHappend();
    }
    
    /**
     * Removes all cables in list from network.
     * @param cableList 
     */
    public void removeCables(List<CableModel> cableList) {
        for (Iterator<CableModel> it = cableList.iterator(); it.hasNext();) {
            removeCable(it.next());
        }
    }
    
    /**
     * Gets all components in network as collection.
     * @return 
     */
    public Collection<HwComponentModel> getHwComponents() {
        return networkModel.getHwComponents();
    }
    
    /**
     * Adds component in parameter to network.
     * @param component 
     */
    public void addHwComponent(HwComponentModel component) {
        networkModel.addHwComponent(component);
        
        // set timestamp of edit
        editHappend();
    }
    
    /**
     * Adds components in parameter list to network.
     * @param componentList 
     */
    public void addHwComponents(List<HwComponentModel> componentList) {
        for (HwComponentModel component : componentList) {
            addHwComponent(component);
        }
    }
    
    /**
     * Removes component in parameter from network.
     * @param component 
     */
    public void removeHwComponent(HwComponentModel component) {
        networkModel.removeHwComponent(component);

        // set timestamp of edit
        editHappend();
    }
    
    /**
     * Removes all components in list from network.
     * @param componentList 
     */
    public void removeHwComponents(List<HwComponentModel> componentList) {
       for(HwComponentModel component : componentList){
            networkModel.removeHwComponent(component);
        }

        // set timestamp of edit
        editHappend();
    }
    
    /**
     * Returns hwComponent identified by id.
     * @param id
     * @return 
     */
    public HwComponentModel getHwComponentModelById(int id){
        return networkModel.getHwComponentModelById(id);
    }
    
    /**
     * Returns cable identified by id.
     * @param id
     * @return 
     */
    public CableModel getCableModelById(int id){
        return networkModel.getCableModelById(id);
    }
    
    /**
     * Gets last edit timestamp. It is time in millis of last edit of network.
     * @return 
     */
    public long getLastEditTimestamp(){
        return networkModel.getLastEditTimestamp();
    }


    /**
     * Call whenever the network changes
     */
    public void editHappend(){
        //
        networkModel.setLastEditTimestamp(System.currentTimeMillis());        
    }
}
