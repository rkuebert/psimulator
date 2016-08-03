package shared.Components;

import java.io.Serializable;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface AbstractComponentModel extends Identifiable, Serializable{
    
    /**
     * Returns unique ID
     * @return 
     */
    @Override
    public Integer getId();
    
    /**
     * Returs HwType of component
     * @return 
     */
    public HwTypeEnum getHwType();

    /**
     * Sets HwType of component
     * @param hwType 
     */
    public void setHwType(HwTypeEnum hwType);

    /**
     * Gets unique identifier of component
     * @return 
     */
    public void setId(Integer id);
    
}
