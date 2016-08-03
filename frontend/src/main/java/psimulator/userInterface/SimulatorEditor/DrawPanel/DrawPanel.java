package psimulator.userInterface.SimulatorEditor.DrawPanel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import psimulator.dataLayer.Singletons.ColorMixerSingleton;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.Singletons.GeneratorSingleton;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;
import psimulator.dataLayer.Enums.ViewDetailsType;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Actions.*;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.DrawPanelAction;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.GraphOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.MouseActionListeners.*;
import psimulator.userInterface.SimulatorEditor.UserInterfaceLayeredPane.UserInterfaceLayeredPaneInnerInterface;
import psimulator.userInterface.SimulatorEditor.UserInterfaceMainPanelInnerInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class DrawPanel extends DrawPanelOuterInterface implements
        DrawPanelInnerInterface, Observer {
    // mouse listeners

    private DrawPanelListenerStrategy mouseListenerHand;
    private DrawPanelListenerStrategy mouseListenerDragMove;
    private DrawPanelListenerStrategy mouseListenerAddHwComponent;
    private DrawPanelListenerStrategy mouseListenerCable;
    private DrawPanelListenerStrategy mouseListenerSimulator;
    private DrawPanelListenerStrategy currentMouseListener;
    // END mouse listenrs
    private Graph graph;
    private UndoManager undoManager = new UndoManager();
    private MainWindowInnerInterface mainWindow;
    private UserInterfaceMainPanelInnerInterface userInterface;
    private UserInterfaceLayeredPaneInnerInterface layeredPane;
    // variables for creating cables
    private boolean lineInProgress = false;
    private Point lineStartInDefaultZoom;
    private Point lineEndInActualZoom;
    // variables for marking components with transparent rectangle
    private boolean rectangleInProgress = false;
    private Rectangle rectangle;
    //
    private DataLayerFacade dataLayer;
    private EnumMap<DrawPanelAction, AbstractAction> actions;

    public DrawPanel(MainWindowInnerInterface mainWindow, UserInterfaceMainPanelInnerInterface UserInterface,
            DataLayerFacade dataLayer, UserInterfaceLayeredPaneInnerInterface layeredPane) {
        super();

        this.userInterface = UserInterface;
        this.mainWindow = mainWindow;
        this.dataLayer = dataLayer;
        this.layeredPane = layeredPane;

        this.setBackground(ColorMixerSingleton.drawPanelColor);

        createDrawPaneMouseListeners();
        createAllActions();
        createKeyBindings();

        // add as a zoom observer
        ZoomManagerSingleton.getInstance().addObserver((Observer) this);

        // add as a language observer
        dataLayer.addLanguageObserver((Observer) this);
        dataLayer.addPreferencesObserver((Observer) this);

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //System.out.println("ReapintDrawPanel");

        Graphics2D g2 = (Graphics2D) g;

        // set antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // paint line that is being currently made
        if (lineInProgress) {
            Stroke stroke = new BasicStroke(ZoomManagerSingleton.getInstance().getStrokeWidth());
            Stroke tmp = g2.getStroke();
            g2.setStroke(stroke);

            g2.drawLine(ZoomManagerSingleton.getInstance().doScaleToActual(lineStartInDefaultZoom.x),
                    ZoomManagerSingleton.getInstance().doScaleToActual(lineStartInDefaultZoom.y),
                    lineEndInActualZoom.x,
                    lineEndInActualZoom.y);

            g2.setStroke(tmp);
        }

        if (graph != null) {
            graph.paint(g2);
        }


        // DRAW makring rectangle
        if (rectangleInProgress) {
            Color tmpColor = g2.getColor();
            g2.setColor(Color.BLUE);
            //
            Rectangle rectangleInActualZoom = new Rectangle(ZoomManagerSingleton.getInstance().doScaleToActual(rectangle.getLocation()), 
                    ZoomManagerSingleton.getInstance().doScaleToActual(rectangle.getSize()));
            //g2.draw(rectangle);
            g2.draw(rectangleInActualZoom);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            //g2.fill(rectangle);
            g2.fill(rectangleInActualZoom);

            g2.setColor(tmpColor);
        }
        
        if(dataLayer.isViewDetails(ViewDetailsType.NETWORK_BOUNDS)){
            g2.setColor(Color.GRAY);
            g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
        }
        
        g2.dispose();
    }

// ====================  IMPLEMENTATION OF Observer ======================   
    /**
     * Reaction to notification from zoom manager
     *
     * @param o
     * @param o1
     */
    @Override
    public void update(Observable o, Object o1) {
        
        switch ((ObserverUpdateEventType) o1) {
            case VIEW_DETAILS:
                break;
            case LANGUAGE:
                break;
            case GRAPH_COMPONENT_CHANGED:
                break;
            case GRAPH_SIZE_CHANGED:
                break;
            case ZOOM_CHANGE:
                break;
            case NETWORK_BOUNDS:
                break;
                    
        }
        
        //this.revalidate();
        //this.repaint();
    }
// END ====================  IMPLEMENTATION OF Observer ======================      

// ================  IMPLEMENTATION OF ToolChangeInterface =================
    @Override
    public void setCurrentMouseListenerSimulator() {
        removeCurrentMouseListener();
        setCurrentMouseListener(mouseListenerSimulator);
    }

    @Override
    public void removeCurrentMouseListener() {
        if (currentMouseListener != null) {
            currentMouseListener.deInitialize();
        }

        JViewport jViewport = userInterface.getJViewport();

        jViewport.removeMouseListener(currentMouseListener);
        jViewport.removeMouseMotionListener(currentMouseListener);
        jViewport.removeMouseWheelListener(currentMouseListener);
    }

    @Override
    public DrawPanelListenerStrategy getMouseListener(MainTool tool) {
        switch (tool) {
            case DRAG_MOVE:
                return mouseListenerDragMove;
            case HAND:
                return mouseListenerHand;
            case ADD_CABLE:
                return mouseListenerCable;
            case ADD_REAL_PC:
            case ADD_END_DEVICE:
            case ADD_SWITCH:
            case ADD_ROUTER:
                return mouseListenerAddHwComponent;
            case SIMULATOR:
                return mouseListenerSimulator;
        }

        // this should never happen
        System.err.println("chyba v DrawPanel metoda getMouseListener(MainTool tool)");
        return mouseListenerHand;
    }

    @Override
    public void setCurrentMouseListener(DrawPanelListenerStrategy mouseListener) {
        currentMouseListener = mouseListener;

        currentMouseListener.initialize();

        JViewport jViewport = userInterface.getJViewport();

        jViewport.addMouseListener(currentMouseListener);
        jViewport.addMouseMotionListener(currentMouseListener);
        jViewport.addMouseWheelListener(currentMouseListener);
    }
// END ==============  IMPLEMENTATION OF ToolChangeInterface ===============

// ============== IMPLEMENTATION OF DrawPanelInnerInterface ================
    @Override
    public DataLayerFacade getDataLayerFacade() {
        return dataLayer;
    }

    @Override
    public GraphOuterInterface getGraphOuterInterface() {
        return graph;
    }

    @Override
    public void setLineInProgras(boolean lineInProgres, Point startInDefaultZoom, Point endInActualZoom) {
        this.lineInProgress = lineInProgres;
        lineStartInDefaultZoom = startInDefaultZoom;
        lineEndInActualZoom = endInActualZoom;
    }

    @Override
    public void setTransparetnRectangleInProgress(boolean rectangleInProgress, Rectangle rectangle) {
        this.rectangleInProgress = rectangleInProgress;
        this.rectangle = rectangle;
    }

    @Override
    public void doSetTollInEditorToolBar(MainTool mainTool) { 
        userInterface.doSetToolInToolBar(mainTool);
    }

    @Override
    public JScrollPane getJScrollPane() {
        return userInterface.getJScrollPane();
    }

// END ============ IMPLEMENTATION OF DrawPanelInnerInterface ==============
// ============== IMPLEMENTATION OF DrawPanelOuterInterface ================
    @Override
    public Graph removeGraph() {
        if (graph != null) {
            graph.deleteObserver(this);
        }

        Graph tmp = graph;
        graph = null;

        ImageFactorySingleton.getInstance().clearTextBuffers();

        undoManager.discardAllEdits();
        //ZoomManagerSingleton.getInstance().zoomReset();
        return tmp;
    }

    @Override
    public void setGraph(Graph graph) {
        if (this.graph != null) {
            removeGraph();
        }

        this.graph = graph;
        graph.addObserver(this);

        // if graph is empty - new project is created
        if (graph.getAbstractHwComponentsCount() == 0) {
            // get network coutner model
            // initialize generator singleton
            
        }
        
        GeneratorSingleton.getInstance().initialize(dataLayer.getNetworkFacade().getNetworkCounterModel());

        graph.initialize(this, dataLayer);
    }

    @Override
    public boolean hasGraph() {
        if (graph == null) {
            return false;
        }
        return true;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public AbstractAction getAbstractAction(DrawPanelAction action) {
        return actions.get(action);
    }

    @Override
    public boolean canUndo() {
        return undoManager.canUndo();
    }

    @Override
    public boolean canRedo() {
        return undoManager.canRedo();
    }

    @Override
    public void undo() {
        undoManager.undo();
        update(null, ObserverUpdateEventType.UNDO_REDO);
    }

    @Override
    public void redo() {
        undoManager.redo();
        update(null, ObserverUpdateEventType.UNDO_REDO);
    }


    @Override
    public void doFitToGraphSize() {
        layeredPane.doFitToGraphSize();
    }
// END ============ IMPLEMENTATION OF DrawPanelOuterInterface ==============

    // -------- PRIVATE -------------------
    private void createKeyBindings() {
        InputMap inputMap = mainWindow.getRootPane().getInputMap();
        ActionMap actionMap = mainWindow.getRootPane().getActionMap();



        // add key binding for delete
        KeyStroke keyDel = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        inputMap.put(keyDel, DrawPanelAction.DELETE);
        actionMap.put(DrawPanelAction.DELETE, getAbstractAction(DrawPanelAction.DELETE));

        // add key binding for H - switch to hand tool
        KeyStroke keyH = KeyStroke.getKeyStroke(KeyEvent.VK_H, 0);

        inputMap.put(keyH, DrawPanelAction.SWITCH_TO_HAND_TOOL);
        actionMap.put(DrawPanelAction.SWITCH_TO_HAND_TOOL, getAbstractAction(DrawPanelAction.SWITCH_TO_HAND_TOOL));
        
        // add key binding for M - switch to move tool
        KeyStroke keyM = KeyStroke.getKeyStroke(KeyEvent.VK_M, 0);
        inputMap.put(keyM, DrawPanelAction.SWITCH_TO_MOVE_TOOL);
        actionMap.put(DrawPanelAction.SWITCH_TO_MOVE_TOOL, getAbstractAction(DrawPanelAction.SWITCH_TO_MOVE_TOOL));
        
    }

    /**
     * creates all actions according to DrawPanelAction Enum
     */
    private void createAllActions() {
        actions = new EnumMap<DrawPanelAction, AbstractAction>(DrawPanelAction.class);

        for (DrawPanelAction drawPanelAction : DrawPanelAction.values()) {
            switch (drawPanelAction) {
                case ALIGN_COMPONENTS_TO_GRID:
                    actions.put(drawPanelAction, new ActionAlignComponentsToGrid(undoManager, this, mainWindow));
                    break;
                case DELETE:
                    actions.put(drawPanelAction, new ActionOnDelete(undoManager, this, mainWindow));
                    break;
                case PROPERTIES:
                    actions.put(drawPanelAction, new ActionOpenProperties(undoManager, this, mainWindow, dataLayer));
                    break;
                case FIT_TO_SIZE:
                    actions.put(drawPanelAction, new ActionFitToSize(undoManager, this, mainWindow));
                    break;
                case SELECT_ALL:
                    actions.put(drawPanelAction, new ActionSelectAll(undoManager, this, mainWindow));
                    break;
                case AUTOMATIC_LAYOUT:
                    actions.put(drawPanelAction, new ActionAutomaticLayout(undoManager, this, mainWindow, dataLayer));
                    break;
                case SWITCH_TO_HAND_TOOL:
                    actions.put(drawPanelAction, new ActionSwitchToToolAction(undoManager, this, mainWindow, MainTool.HAND));
                    break;
                case SWITCH_TO_MOVE_TOOL:
                    actions.put(drawPanelAction, new ActionSwitchToToolAction(undoManager, this, mainWindow, MainTool.DRAG_MOVE));
                    break;
            }
        }
    }

    /**
     * Creates mouse listeners for all tools
     */
    private void createDrawPaneMouseListeners() {
        mouseListenerHand = new DrawPanelListenerStrategyHand(this, undoManager, mainWindow, dataLayer);
        mouseListenerDragMove = new DrawPanelListenerStrategyDragMove(this, undoManager, mainWindow, dataLayer);
        mouseListenerAddHwComponent = new DrawPanelListenerStrategyAddHwComponent(this, undoManager, mainWindow, dataLayer);
        mouseListenerCable = new DrawPanelListenerStrategyAddCable(this, undoManager, mainWindow, dataLayer);
        mouseListenerSimulator = new DrawPanelListenerStrategySimulator(this, undoManager, mainWindow, dataLayer);
    }
}
