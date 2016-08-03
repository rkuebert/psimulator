

package shared.telnetConfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import shared.NetworkObject;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class TelnetConfig implements NetworkObject, Serializable{
    
    /**
     * key is a componentID
     */
    Map<Integer,ConfigRecord> configRecords;

    public TelnetConfig() {
        this.configRecords = new HashMap<>();
    }

    public Map<Integer, ConfigRecord> getConfigRecords() {
        return configRecords;
    }

    public void setConfigRecords(Map<Integer, ConfigRecord> configRecords) {
        this.configRecords = configRecords;
    }

    /**
     * 
     * @param key compnent ID / DeviceID
     * @param value {@link ConfigRecord}
     * @return 
     */
    public ConfigRecord put(Integer key, ConfigRecord value) {
        return configRecords.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        
        for (Map.Entry<Integer, ConfigRecord> entry : configRecords.entrySet()) {
            Integer integer = entry.getKey();
            ConfigRecord configRecord = entry.getValue();
            
            sb.append("ComponentID: ").append(configRecord.getComponentId()).append(" port:").append(configRecord.getPort()).append("\n");
        }
        
        
        return sb.toString();
    }
    
    
    

}
