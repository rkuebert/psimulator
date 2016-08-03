package shared.SimulatorEvents.SerializedComponents;

import java.io.Serializable;
import shared.NetworkObject;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SimulatorEvent implements Serializable, NetworkObject{
    // those variables has to be loaded from file / recieved via TCP connection, SAVE THEM
    private long timeStamp;
    private int sourcceId;
    private int destId;
    private int cableId;
    private PacketType packetType;
    private String detailsText;
    /**
     * Default is successful until event create changed in PSImulator
     */
    private EventType eventType = EventType.SUCCESSFULLY_TRANSMITTED;
    
    public SimulatorEvent(long timeStamp, int sourcceId, int destId, int cableId,
            PacketType packetType, String detailsText, EventType eventType) {
        this.timeStamp = timeStamp;
        this.packetType = packetType;
        this.sourcceId = sourcceId;
        this.destId = destId;
        this.cableId = cableId;
        this.detailsText = detailsText;
        this.eventType = eventType;
    }

    public SimulatorEvent() {
    }
 
    

    public PacketType getPacketType() {
        return packetType;
    }

    public int getCableId() {
        return cableId;
    }

    public int getDestId() {
        return destId;
    }

    public int getSourcceId() {
        return sourcceId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getDetailsText() {
        return detailsText;
    }

    public void setCableId(int cableId) {
        this.cableId = cableId;
    }

    public void setDestId(int destId) {
        this.destId = destId;
    }

    public void setDetailsText(String detailsText) {
        this.detailsText = detailsText;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }

    public void setSourcceId(int sourcceId) {
        this.sourcceId = sourcceId;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
    
    @Override
    public String toString() {
        return "src:" + this.getSourcceId() + " dst:" + this.getDestId() + " type" + this.getPacketType().toString() + " " + this.getDetailsText();
    }
    
    
    
    
}
