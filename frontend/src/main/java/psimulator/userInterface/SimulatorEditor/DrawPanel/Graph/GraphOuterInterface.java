package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Observer;
import psimulator.dataLayer.Network.NetworkFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Actions.RemovedComponentsWrapper;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.BundleOfCablesGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.CableGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.Markable;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm.GeneticGraph;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface GraphOuterInterface {

    /**
     * 
     * Returns width of Graph in actual zoom
     * @return
     */
    public int getWidth();
    /**
     * Returns height of Graph in actual zoom
     * @return 
     */
    public int getHeight();
    
    /**
     * Gets preffered size of graph
     * @return 
     */
    public Dimension getPreferredSize();
    
    /**
     * Gets preffered size of graph in default zoom
     * @return 
     */
    public Dimension getPreferredSizeDefaultZoom();
    
    /**
     * Changes position of HwComponentGraphic component by Dimension offsetInDefaultZoom
     * according to boolean positive.
     * @param component - Component that is being moved
     * @param offsetInDefaultZoom - offset in default Zoom
     * @param positive - orientation of move
     */
    public void doChangePositionOfAbstractHwComponent(HwComponentGraphic component, Dimension offsetInDefaultZoom, boolean positive);
    /**
     * Changes position of all HwComponentGraphic component in list by Dimension offsetInDefaultZoom
     * according to boolean positive.
     * @param components - List of components to move 
     * @param offsetInDefaultZoom - offset in default Zoom
     * @param positive - orientation of move
     */
    public void doChangePositionOfAbstractHwComponents(List<HwComponentGraphic> components, Dimension offsetInDefaultZoom, boolean positive);
    /**
     * Aligns all components to grid
     * @return HashMap - map of component+dimension pairs
     */
    public HashMap<HwComponentGraphic, Dimension> doAlignComponentsToGrid();
    /**
     * Aligns all components to grid
     * @return HashMap - map of component+dimension pairs
     */
    public HashMap<HwComponentGraphic, Dimension> doAlignMarkedComponentsToGrid();
    /**
     * Removes all marked AbstractHwComponents and Cables and returns them
     * wrapped.
     * @return  RemovedComponentsWrapper - list of AbstractHwComponents and Cables
     */
    public RemovedComponentsWrapper doRemoveMarkedComponents();
    
    /**
     * Changes position of all components according to genetic graph
     * @param geneticGraph
     * @return HashMap - map of component+dimension pairs
     */
    public HashMap<HwComponentGraphic, Dimension> doChangePositions(GeneticGraph geneticGraph);
    
    
    /**
     * If component is cable, then mark or unmark it and add/remove it to marked components.
     * If component is HwComponentGraphic, than mark/unmark it and its all cables and add/remove
     * it to marked components.
     * @param marked True if mark, false if unmark.
     * @param component Component that needs to be marked.
     */
    public void doMarkComponentWithCables(Markable component, boolean marked);
    
    
    /**
     * Marks cable as marked
     * @param cable 
     */
    public void doMarkCable(CableGraphic cable);
    
    /**
     * Marks all components
     */
    public void doMarkAllComponents();
    
    /**
     * unmarks all marked components
     */
    public void doUnmarkAllComponents();
    /**
     * Returns count of marked AbstractHWComponentsCount in graph
     * @return count of marked AbstractHWComponentsCount in graph
     */
    public int getMarkedAbstractHWComponentsCount();
    
    /**
     * 
     * @return marked cables count
     */
    public int getMarkedCablesCount();
    
    
    /**
     * Retruns new ArrayList with marked cables
     * @return 
     */
    public List<CableGraphic> getMarkedCablesCopy();
    /**
     * Retruns new ArrayList with marked components
     * @return 
     */
    public List<HwComponentGraphic> getMarkedHwComponentsCopy();
     /**
     * Gets upper left bound point from all components
     * @param components to look in
     * @return UpperLeft bound point
     */
    public Point getUpperLeftBound(List<HwComponentGraphic> components);
    /**
     * Gets all HwComponentGraphic in list. It is NOT a copy
     * @return List of HwComponentGraphic
     */
    public Collection<HwComponentGraphic> getHwComponents();
    
    /**
     * Gets all CableGraphic in list. It is NOT a copy
     * @return 
     */
    public Collection<CableGraphic> getCables();
    /**
     * Gets all BundleOfCablesGraphic in list. It is NOT a copy
     * @return 
     */
    public List<BundleOfCablesGraphic> getBundlesOfCables();
    /**
     * Gets count of cables in graph
     * @return 
     */
    public int getCablesCount();
    /**
     * Gets count of AbstractHwComponents in graph
     * @return 
     */
    public int getAbstractHwComponentsCount();
    
    /**
     * Removes all cables from cableList in Graph
     * @param cableList 
     */
    public void removeCables(List<CableGraphic> cableList);
    /**
     * removes cable from graph
     * @param cable 
     */
    public void removeCable(CableGraphic cable);
    /**
     * removes HwComponentGraphic from graph
     * @param component 
     */
    public void removeHwComponent(HwComponentGraphic component);
    /**
     * removes all AbstractHwComponents in list from graph
     * @param componentList 
     */
    public void removeHwComponents(List<HwComponentGraphic> componentList);
    
    /**
     * Adds all cables in list to graph
     * @param cableList 
     */
    public void addCables(List<CableGraphic> cableList);
    /**
     * adds cable to graph
     * @param cable 
     */
    public void addCable(CableGraphic cable);
    /**
     * Adds HwComponentGraphic to graph
     * @param component 
     */
    public void addHwComponent(HwComponentGraphic component);
    /**
     * Adds all AbstractHwComponents in list to graph
     * @param componentList 
     */
    public void addHwComponents(List<HwComponentGraphic> componentList);

    /**
     * Get timestamp of last edit
     * @return 
     */
    public long getLastEditTimestamp();
    
    /**
     * Call when changed: IP, MAC, NAME, DELAY etc. 
     */
    public void editHappend();
    
    /**
     * Adds observer to be notified with GRAPH_COMPONENT_CHANGED
     * @param obsrvr 
     */
    public void addObserver(Observer obsrvr);
    
    /**
     * Removes observer from observer list
     * @param obsrvr 
     */
    public void deleteObserver(Observer obsrvr);
}
