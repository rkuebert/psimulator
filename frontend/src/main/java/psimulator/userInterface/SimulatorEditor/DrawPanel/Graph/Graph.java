package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph;

import java.awt.*;
import java.util.List;
import java.util.*;
import javax.swing.JComponent;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.Network.NetworkFacade;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Actions.RemovedComponentsWrapper;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.*;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm.GeneticGraph;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Support.CustomObservable;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class Graph extends JComponent implements GraphOuterInterface, GraphBuilderInterface {

    private LinkedHashMap<Integer, HwComponentGraphic> componentsMap = new LinkedHashMap<>();
    private LinkedHashMap<Integer, CableGraphic> cablesMap = new LinkedHashMap<>();
    //
    private List<BundleOfCablesGraphic> bundlesOfCables = new ArrayList<>();
    private List<CableGraphic> markedCables = new ArrayList<>();
    private List<HwComponentGraphic> markedAbstractHwComponents = new ArrayList<>();
    private Grid grid;
    private int widthDefault;
    private int heightDefault;
    //
    //private long lastEditTimestamp;
    //
    private CustomObservable customObservable = new CustomObservable();
    private NetworkFacade networkFacade;

    public Graph() {
    }

    /**
     * Use to initialize references in all graph components after load.
     *
     * @param drawPanel
     * @param dataLayer
     */
    public void initialize(DrawPanelInnerInterface drawPanel, DataLayerFacade dataLayer) {
        // 
        this.networkFacade = dataLayer.getNetworkFacade();
        
        // init references
        setInitReferencesToComponents(dataLayer);

        // update size of graph with all components
        updateSizeWithAllComponents();

        // init grid
        grid = new Grid((GraphOuterInterface) this);

        // set timestamp of edit
        editHappend();
    }

    /**
     * Use in initialize to put references of parameters to all components in
     * graph.
     *
     * @param dataLayer
     * @param imageFactory
     */
    private void setInitReferencesToComponents(DataLayerFacade dataLayer) {
        // get Collection of values contained in LinkedHashMap
        Collection<HwComponentGraphic> colection = componentsMap.values();
        // obtain an Iterator for Collection
        Iterator<HwComponentGraphic> it = colection.iterator();

        // get all marked components
        while (it.hasNext()) {
            HwComponentGraphic component = it.next();
            // set references
            component.setInitReferences(dataLayer);
            // initialize
            component.initialize();
        }

        // set references to all BundleOfCablesGraphic
        for (BundleOfCablesGraphic boc : bundlesOfCables) {
            // boc performs the same operation on its cables
            boc.setInitReferences(dataLayer);
        }

        // initialize cables
        for (CableGraphic cable : getCables()) {
            // set references
            cable.setInitReferences(dataLayer);
            // initialize
            cable.initialize();
        }
    }

    /**
     * Use in initialize to make the graph size according to all components
     */
    private void updateSizeWithAllComponents() {
        // get Collection of values contained in LinkedHashMap
        Collection<HwComponentGraphic> colection = componentsMap.values();
        // obtain an Iterator for Collection
        Iterator<HwComponentGraphic> it = colection.iterator();

        // update graph with all components
        while (it.hasNext()) {
            HwComponentGraphic component = it.next();
            updateSizeAddComponent(component.getLowerRightCornerLocation());
        }
    }

    public void doUpdateImages() {
        // get Collection of values contained in LinkedHashMap
        Collection<HwComponentGraphic> colection = componentsMap.values();
        // obtain an Iterator for Collection
        Iterator<HwComponentGraphic> it = colection.iterator();

        // get all marked components
        while (it.hasNext()) {
            it.next().doUpdateImages();
        }

        for (BundleOfCablesGraphic boc : bundlesOfCables) {
            boc.doUpdateImages();
        }

        // update size by recalculate
        updateSizeByRecalculate();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;


        // GRID PAINT
        /*
         * g2.setColor(Color.gray); grid.paintComponent(g2);
         * g2.setColor(Color.black);
         *
         * g2.setColor(Color.gray); g2.drawLine(getWidth(), 0, getWidth(),
         * getHeight()); g2.drawLine(0, getHeight(), getWidth(), getHeight());
         * g2.setColor(Color.black);
         */

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        // DRAW cables
        for (AbstractComponentGraphic c : getBundlesOfCables()) {
            if (!c.isMarked()) {
                c.paint(g2);
            }
        }

        /*
         * for (AbstractComponentGraphic c : markedCables) { c.paint(g2);
        }
         */

        // DRAW HWcomponents
        for (AbstractComponentGraphic c : getHwComponents()) {
            if (!c.isMarked()) {
                c.paint(g2);
            }
        }

        for (AbstractComponentGraphic c : markedAbstractHwComponents) {
            c.paint(g2);
        }
    }
    
    
     /**
     * Call when something in graph changed (move, add, remove)
     */
    @Override
    public void editHappend() {
        // propagate edit happend to network
        networkFacade.editHappend();

        // inform about graph change
        customObservable.notifyAllObservers(ObserverUpdateEventType.GRAPH_COMPONENT_CHANGED);
    }

    @Override
    public long getLastEditTimestamp() {
        
        return networkFacade.getLastEditTimestamp();
        
        //return lastEditTimestamp;
    }

    
    // ----- OBSERVER ----------------------------------------------------------
    @Override
    public synchronized void addObserver(Observer obsrvr) {
        customObservable.addObserver(obsrvr);
    }

    @Override
    public synchronized void deleteObserver(Observer obsrvr) {
        customObservable.deleteObserver(obsrvr);
    }
    // END ----- OBSERVER ----------------------------------------------------------

    // ----- BUNDLE OF CABLES CABLE MANIPULATION -------------------------------
    /**
     * Returns bundle of cables between component1 and component2. If such a
     * bundle does not exist, it creates it and adds it to graph and both
     * components.
     *
     * @param component1
     * @param component2
     * @return
     */
    private BundleOfCablesGraphic getBundleOfCables(HwComponentGraphic component1, HwComponentGraphic component2) {
        BundleOfCablesGraphic bundle = null;

        // find bundle to place the cable in
        for (BundleOfCablesGraphic boc : bundlesOfCables) {
            if ((boc.getComponent1() == component1 && boc.getComponent2() == component2)
                    || (boc.getComponent1() == component2 && boc.getComponent2() == component1)) {
                bundle = boc;
                break;
            }
        }

        // if there is not a bundle between component1 and component2, we make the bundle
        if (bundle == null) {
            bundle = new BundleOfCablesGraphic(component1, component2);
            bundlesOfCables.add(bundle);
            component1.addBundleOfCables(bundle);
            component2.addBundleOfCables(bundle);
        }
        return bundle;
    }

    /**
     * removes BundleOfCablesGraphic from both components and graph
     *
     * @param bundleOfCables
     */
    private void removeBundleOfCables(BundleOfCablesGraphic bundleOfCables) {
        bundleOfCables.getComponent1().removeBundleOfCables(bundleOfCables);
        bundleOfCables.getComponent2().removeBundleOfCables(bundleOfCables);

        bundlesOfCables.remove(bundleOfCables);
    }
    // END----- BUNDLE OF CABLES CABLE MANIPULATION -------------------------------

    // ----- CABLE MANIPULATION -----------------------------------------
    /**
     * Use only on graph build!
     *
     * @param cable
     */
    @Override
    public void addCableOnGraphBuild(CableGraphic cable) {
        addCable(cable, false);

    }

    @Override
    public void addCable(CableGraphic cable) {
        addCable(cable, true);

    }

    private void addCable(CableGraphic cable, boolean propagateToNetwork) {
        // get bundle of cables between c1 and c2
        BundleOfCablesGraphic boc = getBundleOfCables(cable.getComponent1(), cable.getComponent2());

        // set component1 and component2 in calbe and bundle of cables the same
        if (cable.getComponent1() != boc.getComponent1()) {
            cable.swapComponentsAndEthInterfaces();
        }

        boc.addCable(cable);

        if (propagateToNetwork) {
            // add cable to network
            networkFacade.addCable(cable.getCableModel());
        }

        // add cable to hash map
        cablesMap.put(cable.getId().intValue(), cable);

        if(propagateToNetwork){
            // set timestamp of edit
            editHappend();
        }
    }

    @Override
    public void addCables(List<CableGraphic> cableList) {
        for (CableGraphic c : cableList) {
            addCable(c, true);
        }
    }

    /**
     * removes cable from graph
     *
     * @param cable
     */
    @Override
    public void removeCable(CableGraphic cable) {
        // get bundle of cables between c1 and c2
        BundleOfCablesGraphic boc = getBundleOfCables(cable.getComponent1(), cable.getComponent2());

        boc.removeCable(cable);

        // if no cable in bundle of cables
        if (boc.getCablesCount() == 0) {
            // remove bundle of cables
            removeBundleOfCables(boc);
        }

        // remove cable from hash map
        cablesMap.remove(cable.getId().intValue());
        
        // remove cable from network
        networkFacade.removeCable(cable.getCableModel());

        // set timestamp of edit
        editHappend();
    }

    @Override
    public void removeCables(List<CableGraphic> cableList) {
        for (Iterator<CableGraphic> it = cableList.iterator(); it.hasNext();) {
            removeCable(it.next());
        }
    }
    // END----- CABLE MANIPULATION -----------------------------------------

    // ----- HW COMPONENT MANIPULATION-------------------------------
    @Override
    public void addHwComponent(HwComponentGraphic component) {
        //components.add(component);
        componentsMap.put(component.getId().intValue(), component);
        updateSizeAddComponent(component.getLowerRightCornerLocation());

        // add to network
        networkFacade.addHwComponent(component.getHwComponentModel());

        // set timestamp of edit
        editHappend();
    }

    /**
     * Use FROM BUILDER ONLY when components not initialized (do not have
     * references on zoom manager and etc.)
     */
    @Override
    public void addHwComponentWithoutGraphSizeChange(HwComponentGraphic component) {
        componentsMap.put(component.getId().intValue(), component);

        // do not add to network
    }

    @Override
    public void addHwComponents(List<HwComponentGraphic> componentList) {
        for (HwComponentGraphic component : componentList) {
            addHwComponent(component);
        }
    }

    @Override
    public void removeHwComponent(HwComponentGraphic component) {       
        //components.remove(component);
        Collection<HwComponentGraphic> colection = componentsMap.values();
        colection.remove(component);

        // remove from network
        networkFacade.removeHwComponent(component.getHwComponentModel());

        //updateSizeRemoveComponents(component.getLowerRightCornerLocation());
        updateSizeByRecalculate();

        // set timestamp of edit
        editHappend();
    }

    @Override
    public void removeHwComponents(List<HwComponentGraphic> componentList) {
        //components.removeAll(componentList);
        Collection<HwComponentGraphic> colection = componentsMap.values();
        colection.removeAll(componentList);

        // remove from network
        Iterator<HwComponentGraphic> it = componentList.iterator();
        while (it.hasNext()) {
            networkFacade.removeHwComponent(it.next().getHwComponentModel());
        }


        //updateSizeRemoveComponents(getLowerRightBound(components));
        updateSizeByRecalculate();

        // set timestamp of edit
        editHappend();
    }
    // END ----- HW COMPONENT MANIPULATION-------------------------------

    // ----- GRAPH SIZES -----------------------------------------------
    @Override
    public Point getUpperLeftBound(List<HwComponentGraphic> components) {
        Point p = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

        for (HwComponentGraphic c : components) {
            if (c.getX() < p.x) {
                p.x = c.getX();
            }
            if (c.getY() < p.y) {
                p.y = c.getY();
            }
        }
        return p;
    }

    /**
     * Gets lower right bound point from all components
     *
     * @param components to look in
     * @return LowerRight bound point
     */
    private Point getLowerRightBound(Collection<HwComponentGraphic> components) {
        Point p = new Point(0, 0);

        for (HwComponentGraphic c : components) {
            Point tmp = c.getLowerRightCornerLocation();

            if (tmp.x > p.x) {
                p.x = tmp.x;
            }
            if (tmp.y > p.y) {
                p.y = tmp.y;
            }

        }
        return p;
    }

    /**
     * Gets lower right bound point from all graph components
     *
     * @return LowerRight bound point
     */
    private Point getGraphLowerRightBound() {
        return getLowerRightBound(componentsMap.values());
    }

    /**
     * Returns preffered size in actual zoom
     *
     * @return
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }

    @Override
    public Dimension getPreferredSizeDefaultZoom() {
        return new Dimension(widthDefault, heightDefault);
    }
    // END ----- GRAPH SIZES -----------------------------------------------

    // ============= MARKING =========    
    @Override
    public void doMarkComponentWithCables(Markable component, boolean marked) {
        // if component is isntance of HwComponentGraphic
        if (component instanceof HwComponentGraphic) {
            component.setMarked(marked);
            if (marked) {
                //markedComponents.add(component);
                markedAbstractHwComponents.add((HwComponentGraphic) component);
            } else {
                //markedComponents.remove(component);
                markedAbstractHwComponents.remove((HwComponentGraphic) component);
            }

            // set marked to all its cables
            List<BundleOfCablesGraphic> bundle = ((HwComponentGraphic) component).getBundleOfCableses();

            for (BundleOfCablesGraphic boc : bundle) {
                for (CableGraphic c : boc.getCables()) {
                    if (marked) {
                        c.setMarked(marked);
                        //markedComponents.add(c);
                        if (!markedCables.contains(c)) {
                            markedCables.add(c);
                        }
                    } else {
                        // if both ends of cable not marked, than unmark cable
                        if (!boc.getComponent1().isMarked() && !boc.getComponent2().isMarked()) {
                            c.setMarked(marked);
                            //markedComponents.remove(c);
                            markedCables.remove(c);
                        }
                    }
                }
            }

        } else { // component is cable
            component.setMarked(marked);
            if (marked) {
                //markedComponents.add(component);
                markedCables.add((CableGraphic) component);
            } else {
                //markedComponents.remove(component);
                markedCables.remove((CableGraphic) component);
            }

        }
    }

    @Override
    public void doMarkCable(CableGraphic cable) {
        cable.setMarked(true);
        markedCables.add(cable);
    }

    /**
     * sets all Markable components in markedComponents to marked(false) and
     * clears markedComponents list
     */
    @Override
    public void doUnmarkAllComponents() {
        for (Markable m : markedAbstractHwComponents) {
            m.setMarked(false);
        }
        markedAbstractHwComponents.clear();

        for (Markable m : markedCables) {
            m.setMarked(false);
        }
        markedCables.clear();
    }

    @Override
    public int getMarkedAbstractHWComponentsCount() {
        return markedAbstractHwComponents.size();
    }

    @Override
    public int getMarkedCablesCount() {
        return markedCables.size();
    }

    @Override
    public void doMarkAllComponents() {
        // get Collection of values contained in LinkedHashMap
        Collection<HwComponentGraphic> colection = componentsMap.values();
        // obtain an Iterator for Collection
        Iterator<HwComponentGraphic> it = colection.iterator();

        // mark all AbstractHwComponents
        while (it.hasNext()) {
            HwComponentGraphic m = it.next();
            m.setMarked(true);
            markedAbstractHwComponents.add(m);
        }

        for (BundleOfCablesGraphic boc : bundlesOfCables) {
            for (CableGraphic c : boc.getCables()) {
                c.setMarked(true);
                markedCables.add(c);
            }
        }
    }

    @Override
    public RemovedComponentsWrapper doRemoveMarkedComponents() {
        // get all marked components
        List<HwComponentGraphic> markedComponents = this.getMarkedHwComponentsCopy();

        // put all marked cables to cables toRemove
        List<CableGraphic> cablesToRemove = this.getMarkedCablesCopy();

        // if there is no marked cable and no component
        if (markedComponents.isEmpty() && cablesToRemove.isEmpty()) {
            return null;
        }


        // for all removed components
        for (HwComponentGraphic c : markedComponents) {
            // all its cables add to cablesToRemove
            for (BundleOfCablesGraphic boc : c.getBundleOfCableses()) {
                for (CableGraphic cable : boc.getCables()) {
                    // if collection doesnt contain, than add cable
                    if (!cablesToRemove.contains(cable)) {
                        cablesToRemove.add(cable);
                    }
                }
            }
            // unmark component
            //this.doMarkComponentWithCables(c, false);
        }

        // unmark all components
        this.doUnmarkAllComponents();

        // remove cables from graph
        this.removeCables(cablesToRemove);

        // remove marked components from graph
        this.removeHwComponents(markedComponents);

        this.markedAbstractHwComponents.clear();
        this.markedCables.clear();

        return new RemovedComponentsWrapper(markedComponents, cablesToRemove);
    }
    // END ============= MARKING =========  

    // ============== CHANGE POSITION AND RESIZE =======================
    @Override
    public void doChangePositionOfAbstractHwComponent(HwComponentGraphic component, Dimension offsetInDefaultZoom, boolean positive) {
        // get old position
        Point oldPosition = component.getLowerRightCornerLocation();
        // change position
        component.doChangePosition(offsetInDefaultZoom, positive);
        // get new position
        Point newPosition = component.getLowerRightCornerLocation();
        // update size of graph
        updateSizeMovePosition(oldPosition, newPosition);
        //System.out.println("tady1: oldpos="+oldPosition.x+","+oldPosition.y+"; newpos="+newPosition.x+","+newPosition.y);

        // set timestamp of edit
        editHappend();
    }

    @Override
    public void doChangePositionOfAbstractHwComponents(List<HwComponentGraphic> components, Dimension offsetInDefaultZoom, boolean positive) {
        // get old lowerRightCorner of all components
        Point oldPosition = getLowerRightBound(components);
        // change position of all components
        for (HwComponentGraphic component : components) {
            // change position of component
            component.doChangePosition(offsetInDefaultZoom, positive);
        }
        // get new lowerRightCorner of all components
        Point newPosition = getLowerRightBound(components);
        // update size of graph
        updateSizeMovePosition(oldPosition, newPosition);
        //System.out.println("tady2: oldpos="+oldPosition.x+","+oldPosition.y+"; newpos="+newPosition.x+","+newPosition.y);

        // set timestamp of edit
        editHappend();
    }

    /**
     * Updates graph's dimension. Call after HwComponentGraphic move.
     *
     * @param oldPositionLowerRightCorner
     * @param newPositionLowerRightCorner
     */
    private void updateSizeMovePosition(Point oldPositionLowerRightCorner,
            Point newPositionLowerRightCorner) {

        // 4. case - position not changed
        // if nothing changed
        if (oldPositionLowerRightCorner.x == newPositionLowerRightCorner.x
                && oldPositionLowerRightCorner.y == newPositionLowerRightCorner.y) {
            // do nothing
            //System.out.println("update size move nothingto do, sizes the same, case 4");
            return;
        }

        // 3.case - moved out of graph dimension
        // if x or y is out of current width or height
        if ((newPositionLowerRightCorner.x >= ZoomManagerSingleton.getInstance().doScaleToActual(widthDefault))
                && newPositionLowerRightCorner.y >= ZoomManagerSingleton.getInstance().doScaleToActual(heightDefault)) {
            // update like addComponent
            //System.out.println("update size move calling updateSizeAddComponent, case 3");
            updateSizeAddComponent(newPositionLowerRightCorner);
            return;
        }


        // 1.case - moved right or down in graph dimension (not out of them)
        // if (oldX <= newX <=  width) and (oldY <= newY <= height)
        if ((oldPositionLowerRightCorner.x <= newPositionLowerRightCorner.x
                && newPositionLowerRightCorner.x <= ZoomManagerSingleton.getInstance().doScaleToActual(widthDefault))
                && (oldPositionLowerRightCorner.y <= newPositionLowerRightCorner.y
                && newPositionLowerRightCorner.y <= ZoomManagerSingleton.getInstance().doScaleToActual(heightDefault))) {
            // do nothing, graph size is not changed
            //System.out.println("update size move nothingto do, case 1");
            return;
        }

        // 2. case - moved left or up in graph dimension (not out of them)
        if ((newPositionLowerRightCorner.x < oldPositionLowerRightCorner.x)
                || newPositionLowerRightCorner.y < oldPositionLowerRightCorner.y) {
            // size could change, the same asi in remove
            //System.out.println("update size move calling update size remove components, case 2");
            //updateSizeRemoveComponents(oldPositionLowerRightCorner);
            updateSizeByRecalculate();
            return;
        }

        // 5. case 
        if ((oldPositionLowerRightCorner.x <= newPositionLowerRightCorner.x
                && newPositionLowerRightCorner.x <= ZoomManagerSingleton.getInstance().doScaleToActual(widthDefault))
                || (oldPositionLowerRightCorner.y <= newPositionLowerRightCorner.y
                && newPositionLowerRightCorner.y <= ZoomManagerSingleton.getInstance().doScaleToActual(heightDefault))) {
            // update like addComponent
            //System.out.println("update size move nothingto do, case 5");
            updateSizeAddComponent(newPositionLowerRightCorner);
            return;
        }

        //System.out.println("dalsi moznost neni");
    }

    /**
     * call when need to go through all components to lower right bound
     */
    private void updateSizeByRecalculate() {
        Point p = ZoomManagerSingleton.getInstance().doScaleToDefault(getGraphLowerRightBound());
        this.widthDefault = p.x;
        this.heightDefault = p.y;
        //System.out.println("update size recalculate");
        doInformAboutSizeChange();
    }

    /**
     * Updates size of Graph. Call after HwComponentGraphic ADD only.
     *
     * @param lowerRightCorner LowerRightCorner in ActualZoom
     */
    private void updateSizeAddComponent(Point lowerRightCorner) {
        // if width changed
        if (lowerRightCorner.x > ZoomManagerSingleton.getInstance().doScaleToActual(widthDefault)) {
            // resize width
            this.widthDefault = ZoomManagerSingleton.getInstance().doScaleToDefault(lowerRightCorner.x);
        }
        // if height changed
        if (lowerRightCorner.y > ZoomManagerSingleton.getInstance().doScaleToActual(heightDefault)) {
            // resize height
            this.heightDefault = ZoomManagerSingleton.getInstance().doScaleToDefault(lowerRightCorner.y);
        }
        //System.out.println("update size add");
        doInformAboutSizeChange();
    }

    /**
     * Informs drawPanel about change of graph size
     */
    public void doInformAboutSizeChange() {
        customObservable.notifyAllObservers(ObserverUpdateEventType.GRAPH_SIZE_CHANGED);
    }

    @Override
    public HashMap<HwComponentGraphic, Dimension> doAlignComponentsToGrid() {
        return doAlignComponentsToGrid(componentsMap.values());
    }

    @Override
    public HashMap<HwComponentGraphic, Dimension> doAlignMarkedComponentsToGrid() {
        return doAlignComponentsToGrid(markedAbstractHwComponents);
    }

    /**
     * Aligns all components in List to grid. Works with default zoom sizes. In
     * returned HashMap are all components that has been moved and the dimension
     * of position change in default zoom.
     *
     * @param componentsToAlign
     * @return HashMap with all components that has been moved and the dimension
     * of position change in default zoom.
     */
    private HashMap<HwComponentGraphic, Dimension> doAlignComponentsToGrid(Collection<HwComponentGraphic> componentsToAlign) {
        HashMap<HwComponentGraphic, Dimension> movedComponentsMap = new HashMap<>();

        // obtain an Iterator for Collection
        Iterator<HwComponentGraphic> it = componentsToAlign.iterator();

        // mark all AbstractHwComponents
        while (it.hasNext()) {
            HwComponentGraphic c = it.next();

            Point originalLocation = c.getCenterLocationDefaultZoom();
            Point newLocation = grid.getNearestGridPointDefaultZoom(originalLocation);

            Dimension differenceInDefaultZoom = new Dimension(originalLocation.x - newLocation.x,
                    originalLocation.y - newLocation.y);

            // if component moved, add to moved 
            if (differenceInDefaultZoom.getWidth() != 0 || differenceInDefaultZoom.getHeight() != 0) {
                this.doChangePositionOfAbstractHwComponent(c, differenceInDefaultZoom, false);

                movedComponentsMap.put(c, differenceInDefaultZoom);
            }
        }
        return movedComponentsMap;
    }

    @Override
    public HashMap<HwComponentGraphic, Dimension> doChangePositions(GeneticGraph geneticGraph) {
        int maxX = 0;
        int maxY = 0;

        for (int i = 0; i < geneticGraph.getNodes().length; i++) {
            if (maxX < geneticGraph.getNodes()[i][0]) {
                maxX = geneticGraph.getNodes()[i][0];
            }

            if (maxY < geneticGraph.getNodes()[i][1]) {
                maxY = geneticGraph.getNodes()[i][1];
            }
        }

        HashMap<HwComponentGraphic, Dimension> movedComponentsMap = new HashMap<>();

        // get Collection of values contained in LinkedHashMap
        Collection<HwComponentGraphic> colection = componentsMap.values();
        // obtain an Iterator for Collection
        Iterator<HwComponentGraphic> it = colection.iterator();

        int i = 0;

        while (it.hasNext()) {
            HwComponentGraphic c = it.next();

            Point originalLocation = c.getCenterLocationDefaultZoom();
            Point newLocation = new Point(geneticGraph.getNodes()[i][0] * 30 + ZoomManagerSingleton.getInstance().getIconWidthDefaultZoom(),
                    geneticGraph.getNodes()[i][1] * 30 + ZoomManagerSingleton.getInstance().getIconWidthDefaultZoom());

            Dimension differenceInDefaultZoom = new Dimension(originalLocation.x - newLocation.x,
                    originalLocation.y - newLocation.y);

            this.doChangePositionOfAbstractHwComponent(c, differenceInDefaultZoom, false);

            movedComponentsMap.put(c, differenceInDefaultZoom);

            i++;
        }
        return movedComponentsMap;
    }
    // END ============== CHANGE POSITION AND RESIZE =======================

    // --------- GETTERS AND SETTERS    
    @Override
    public int getWidth() {
        return ZoomManagerSingleton.getInstance().doScaleToActual(widthDefault);
    }

    @Override
    public int getHeight() {
        return ZoomManagerSingleton.getInstance().doScaleToActual(heightDefault);
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }
    
    @Override
    public List<BundleOfCablesGraphic> getBundlesOfCables() {
        return bundlesOfCables;
    }

    @Override
    public Collection<HwComponentGraphic> getHwComponents() {
        return componentsMap.values();
    }

    @Override
    public Collection<CableGraphic> getCables() {
        return cablesMap.values();
    }

    @Override
    public List<HwComponentGraphic> getMarkedHwComponentsCopy() {
        List<HwComponentGraphic> temp = new ArrayList<>(markedAbstractHwComponents);
        return temp;
    }

    /**
     * Retruns new ArrayList with marked cables
     *
     * @return
     */
    @Override
    public List<CableGraphic> getMarkedCablesCopy() {
        List<CableGraphic> temp = new ArrayList<>(markedCables);
        return temp;
    }

    @Override
    public int getCablesCount() {
        return cablesMap.size();
    }

    @Override
    public int getAbstractHwComponentsCount() {
        return componentsMap.size();
    }

    /**
     * Returns HwComponentGraphic with ID id, or null if not found
     *
     * @param id
     * @return
     */
    @Override
    public HwComponentGraphic getAbstractHwComponent(int id) {
        return componentsMap.get(id);
    }

    /**
     * Returns cablle with ID id, or null if not found
     *
     * @param id
     * @return
     */
    public CableGraphic getCable(int id) {
        return cablesMap.get(id);
    }

   
}
