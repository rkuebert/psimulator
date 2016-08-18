package shared.Components;

import java.io.Serializable;

/**
 * An abstract component consisting of an id and the component's
 * hardware type.
 * 
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface AbstractComponentModel extends Identifiable, Serializable {

    /**
     * Returns the components id.
     *
     * @return the components id
     */
    @Override
    public Integer getId();
    
    /**
     * Sets the unique identifier of the component.
     *
     * @param id the unique identifier of the component
     */
    public void setId(Integer id);

    /**
     * Returns the hardware type of the component.
     *
     * @return the hardware type of the component
     */
    public HwTypeEnum getHwType();

    /**
     * Sets the hardware type of the component.
     *
     * @param hwType the hardware type of the component
     */
    public void setHwType(HwTypeEnum hwType);

}
