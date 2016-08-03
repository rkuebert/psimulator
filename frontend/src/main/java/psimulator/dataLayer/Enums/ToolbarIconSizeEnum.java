package psimulator.dataLayer.Enums;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public enum ToolbarIconSizeEnum {
    TINY(16),
    SMALL(22),
    MEDIUM(32),
    LARGE(48);
    
    private final int size;   

    ToolbarIconSizeEnum(int size) {
        this.size = size;
    }
    
    public int size() { 
        return size; 
    }


}
