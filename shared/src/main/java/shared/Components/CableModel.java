package shared.Components;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class CableModel implements AbstractComponentModel{
    
    /**
     * First component of cable
     */
    private HwComponentModel component1;
    /**
     * Second component of cable
     */
    private HwComponentModel component2;
    /**
     * First interface of cable
     */
    private EthInterfaceModel interface1;
    /**
     * Second interface of cable
     */
    private EthInterfaceModel interface2;
    
    // -------------------------------------------------------
    /**
     * Delay of cable
     */
    private int delay;
    
        /**
     * Type of component
     */
    private HwTypeEnum hwType;
    /**
     * Id of component
     */
    private Integer id;

    public CableModel(Integer id, HwTypeEnum hwType, HwComponentModel component1, HwComponentModel component2, EthInterfaceModel interface1, EthInterfaceModel interface2, int delay) {
        
        // assign values
        this.id=id;
        this.hwType = hwType;
        this.component1 = component1;
        this.component2 = component2;
        this.interface1 = interface1;
        this.interface2 = interface2;
        this.delay = delay;
    }

    /**
     * 
     */
    public CableModel() {
        
    }

    
    @XmlAttribute @XmlID
    public String getIDAsString(){
        return String.valueOf(this.id);
    }
    
    public void setIDAsString(String id){
        this.id = Integer.valueOf(id);
    }

    
    /**
     * Gets first component
     * @return 
     */
    @XmlIDREF
    public HwComponentModel getComponent1() {
        return component1;
    }

    /**
     * Gets second component
     * @return 
     */
    @XmlIDREF
    public HwComponentModel getComponent2() {
        return component2;
    }

    /**
     * Gets first interface
     * @return 
     */
    @XmlIDREF
    public EthInterfaceModel getInterface1() {
        return interface1;
    }

    /**
     * Gets second interface
     * @return 
     */
    @XmlIDREF
    public EthInterfaceModel getInterface2() {
        return interface2;
    }
 
    /**
     * Gets cable delay
     * @return 
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Sets cable delay
     * @param delay 
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    /**
     * Turns cable around. Swaps both ends of cable.
     */
    public void swapComponentsAndEthInterfaces(){
        HwComponentModel tmpComponent = component1;
        component1 = component2;
        component2 = tmpComponent;
        
        EthInterfaceModel tmpImterface = interface1;
        interface1 = interface2;
        interface2 = tmpImterface;
    }

    /**
     * Gets HwType of component
     * @return 
     */
    @Override
    public HwTypeEnum getHwType() {
        return hwType;
    }

    /**
     * Sets HwType of component. Used when restoring from XML.
     * @param hwType 
     */
    @Override
    public void setHwType(HwTypeEnum hwType) {
        this.hwType = hwType;
    }

    /**
     * Gets unique identifier
     * @return 
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Sets ID of component. Used when restoring from XML.
     * @param id 
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Sets first componnent to cable.
     * @param component1 
     */
    public void setComponent1(HwComponentModel component1) {
        this.component1 = component1;
    }

    /**
     * Sets second componnent to cable.
     * @param component1 
     */
    public void setComponent2(HwComponentModel component2) {
        this.component2 = component2;
    }

    /**
     * Sets first interface that is connected
     * @param interface1 
     */
    public void setInterface1(EthInterfaceModel interface1) {
        this.interface1 = interface1;
    }

    /**
     * Sets second interface that is connected
     * @param interface1 
     */
    public void setInterface2(EthInterfaceModel interface2) {
        this.interface2 = interface2;
    }
    
    
    
    
    
    
}
