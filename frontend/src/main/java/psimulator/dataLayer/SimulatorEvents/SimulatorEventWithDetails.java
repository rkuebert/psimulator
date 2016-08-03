package psimulator.dataLayer.SimulatorEvents;

import java.awt.Color;
import psimulator.dataLayer.Singletons.ColorMixerSingleton;
import shared.Components.EthInterfaceModel;
import shared.Components.HwComponentModel;
import shared.SimulatorEvents.SerializedComponents.EventType;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import shared.SimulatorEvents.SerializedComponents.SimulatorEvent;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SimulatorEventWithDetails {

    private transient Color color;
    private transient String from;
    private transient String to;
    private transient HwComponentModel component1;
    private transient HwComponentModel component2;
    private transient EthInterfaceModel eth1;
    private transient EthInterfaceModel eth2;
    private transient Object[] list;
    private transient SimulatorEvent simulatorEvent;

    public SimulatorEventWithDetails(SimulatorEvent simulatorEvent, String from, String to,
            HwComponentModel component1, HwComponentModel component2,
            EthInterfaceModel eth1, EthInterfaceModel eth2) {
        this.from = from;
        this.to = to;
        this.component1 = component1;
        this.component2 = component2;
        this.eth1 = eth1;
        this.eth2 = eth2;
        this.simulatorEvent = simulatorEvent;

        this.color = ColorMixerSingleton.getColorAccodringToPacketType(simulatorEvent.getPacketType());

        Object[] tmp = {simulatorEvent.getTimeStamp(), from, to, simulatorEvent.getPacketType(), color};
        list = tmp;
    }

    /**
     * Returns event type
     * @return 
     */
    public EventType getEventType(){
        return simulatorEvent.getEventType();
    }

    /**
     * i=0..timestamp (long), i=1..from (String), 
     * i=2..to (String), i=3..packet type(PacketType),
     * i=4..color (Color)
     * @param i
     * @return 
     */
    public Object getValueAt(int i) {
        if(i< 0 || i >= list.length){
            return null;
        }
        return list[i];
    }

    /**
     * Gets packet type
     * @return 
     */
    public PacketType getPacketType() {
        return simulatorEvent.getPacketType();
    }

    /**
     * Gets simulator event
     * @return 
     */
    public SimulatorEvent getSimulatorEvent() {
        return simulatorEvent;
    }

    /**
     * Gets cable id
     * @return 
     */
    public int getCableId() {
        return simulatorEvent.getCableId();
    }

    /**
     * Gets detsination device id
     * @return 
     */
    public int getDestId() {
        return simulatorEvent.getDestId();
    }

    /**
     * Gets source device id
     * @return 
     */
    public int getSourcceId() {
        return simulatorEvent.getSourcceId();
    }

    /**
     * Gets timestamp of event
     * @return 
     */
    public long getTimeStamp() {
        return simulatorEvent.getTimeStamp();
    }

    /**
     * Gets detailed description of packet if any.
     * @return 
     */
    public String getDetailsText() {
        if(simulatorEvent.getDetailsText() == null){
            return "";
        }
        return simulatorEvent.getDetailsText();
    }

    public HwComponentModel getComponent1() {
        return component1;
    }

    public HwComponentModel getComponent2() {
        return component2;
    }

    public EthInterfaceModel getEth1() {
        return eth1;
    }

    public EthInterfaceModel getEth2() {
        return eth2;
    }
    
    /**
     * Retruns name of component with interface. If no inetrface, than return only name.
     * If no name and interface, return empty string.
     * @return 
     */
    public String getComponent1NameAndInterface(){
        if(getComponent1()!=null && getEth1() != null){
            return getComponent1().getName()+":"+getEth1().getName();
        }else{
            if(getComponent1()!=null){
                return getComponent1().getName();
            }
        }
        return "";
    }
    
    /**
     * Retruns name of component with interface. If no inetrface, than return only name.
     * If no name and interface, return empty string.
     * @return 
     */
    public String getComponent2NameAndInterface(){
        if(getComponent2()!=null && getEth2() != null){
            return getComponent2().getName()+":"+getEth2().getName();
        }else{
            if(getComponent2()!=null){
                return getComponent2().getName();
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "time=" + simulatorEvent.getTimeStamp() + ", sourceId" + simulatorEvent.getSourcceId()
                + ", destId" + simulatorEvent.getDestId() + ", from=" + from + ", to=" + to
                + ", type=" + simulatorEvent.getPacketType() + ", color=" + color;
    }
}
