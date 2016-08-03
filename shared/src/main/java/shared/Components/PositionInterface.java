package shared.Components;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface PositionInterface {
    /**
     * Gets X coordinate in default zoom
     * @return 
     */
    public int getDefaultZoomXPos();
    /**
     * Gets Y coordinate in default zoom
     * @return 
     */
    public int getDefaultZoomYPos();
    
    /**
     * Sets X coordinate in default zoom
     * @param defaultZoomXPos 
     */
    public void setDefaultZoomXPos(int defaultZoomXPos);
    /**
     * Sets Y coordinate in default zoom
     * @param defaultZoomYPos 
     */
    public void setDefaultZoomYPos(int defaultZoomYPos);
}
