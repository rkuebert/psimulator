package shared.Components;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkCounterModel implements Serializable{

    private int nextId;
    private int nextMacAddress;
    private Map<HwTypeEnum, Integer> nextNumberMap;

    public NetworkCounterModel(int nextId, int nextMacAddress, Map<HwTypeEnum, Integer> nextNumberMap) {
        this.nextId = nextId;
        this.nextMacAddress = nextMacAddress;
        this.nextNumberMap = nextNumberMap;
    }

    public NetworkCounterModel() {
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    
    /**
     * Gets next unique ID
     * @return 
     */
    public int getNextId() {
        return nextId++;
    }
    
    /**
     * Gets current free unique ID
     * @return 
     */
    public int getCurrentId(){
        return nextId;
    }

    /**
     * Increases mac address counter by 1
     */
    public void increaseMacAddressCounter() {
        nextMacAddress++;
    }
    
    /**
     * Gets current mac address (free)
     * @return 
     */
    public int getCurrentMacAddress(){
        return nextMacAddress;
    }
    
    /**
     * Gets number for hwTypeEnum map
     * @param key
     * @return 
     */
    public Integer getFromNumberMap(HwTypeEnum key){
        return nextNumberMap.get(key);
    }
    
    /**
     * Puts number to hwTypeEnumMap
     * @param key
     * @param value 
     */
    public void putToNumberMap(HwTypeEnum key, Integer value){
        nextNumberMap.put(key, value);
    }

    /**
     * Gets next mac address
     * @return 
     */
    public int getNextMacAddress() {
        return nextMacAddress;
    }

    /**
     * Sets next mac address. Used when restoring counter state.
     * @param nextMacAddress 
     */
    public void setNextMacAddress(int nextMacAddress) {
        this.nextMacAddress = nextMacAddress;
    }

    public Map<HwTypeEnum, Integer> getNextNumberMap() {
        return nextNumberMap;
    }

    public void setNextNumberMap(Map<HwTypeEnum, Integer> nextNumberMap) {
        this.nextNumberMap = nextNumberMap;
    }
    
    
    
}
