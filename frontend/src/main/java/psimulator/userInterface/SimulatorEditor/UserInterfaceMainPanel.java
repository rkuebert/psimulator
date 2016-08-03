package psimulator.userInterface.SimulatorEditor;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.BevelBorder;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.AnimationPanelOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.DrawPanelAction;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;
import psimulator.userInterface.SimulatorEditor.DrawPanel.ZoomEventWrapper;
import psimulator.userInterface.SimulatorEditor.SimulatorControllPanel.SimulatorControlPanel;
import psimulator.userInterface.SimulatorEditor.UserInterfaceLayeredPane.UserInterfaceLayeredPane;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class UserInterfaceMainPanel extends UserInterfaceMainPanelOuterInterface implements UserInterfaceMainPanelInnerInterface,
        Observer {

    private MainWindowInnerInterface mainWindow;
    private DataLayerFacade dataLayer;
    //
    //
    private UserInterfaceMainPanelState userInterfaceState;
    //
    private EditorToolBar jToolBarEditor;   // certical tool bar with hand, computer, switches
    //
    //private DrawPanelOuterInterface jPanelDraw; // draw panel
    //private AnimationPanelOuterInterface jPanelAnimation; // animation panel
    private JScrollPane jScrollPane;            // scroll pane with draw panel
    private JViewport jViewPort;
    //
    private UserInterfaceLayeredPane jLayeredPane;
    //
    private SimulatorControlPanel jPanelSimulator;
    //
    private WelcomePanel jPanelWelcome;

    public UserInterfaceMainPanel(MainWindowInnerInterface mainWindow, DataLayerFacade dataLayer, 
            UserInterfaceMainPanelState userInterfaceState) {
        super(new BorderLayout());

        this.mainWindow = mainWindow;
        this.dataLayer = dataLayer;
        
        // set border
        this.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //
        jViewPort = new JViewport() {

            private boolean flag = false;

            @Override
            public void revalidate() {
                if (flag) {
                    return;
                }
                
                super.revalidate();
            }

            @Override
            public void setViewPosition(Point p) {
                flag = true;
                super.setViewPosition(p);
                flag = false;

            }
        };
        // create layered pane
        jLayeredPane = new UserInterfaceLayeredPane(mainWindow, this, dataLayer);
        
        //AnimationPanel panel  = new AnimationPanel(mainWindow, this, imageFactory, dataLayer, null, null);
        
        // add layered pane to viewport
        jViewPort.add(jLayeredPane);
        //jViewPort.add(panel);

        // create scrollpane
        jScrollPane = new JScrollPane();
        // add viewport to scroll pane
        jScrollPane.setViewport(jViewPort);

        // add scroll bars
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        // ----------- EDITOR STUFF CREATION -----------------------
        // create tool bar
        jToolBarEditor = new EditorToolBar(dataLayer, jLayeredPane);

        // add listener for FitToSize button in tool bar
        jToolBarEditor.addToolActionFitToSizeListener(jLayeredPane.getAbstractAction(DrawPanelAction.FIT_TO_SIZE));

        // add listener for AlignToGrid button in tool bar
        jToolBarEditor.addToolActionAlignToGridListener(jLayeredPane.getAbstractAction(DrawPanelAction.ALIGN_COMPONENTS_TO_GRID));


        // ----------- SIMULATOR STUFF CREATION -----------------------
        // create simulator panel
        jPanelSimulator = new SimulatorControlPanel(mainWindow, dataLayer);

        // add as a language observer
        dataLayer.addLanguageObserver((Observer) jPanelSimulator);

        // add as a simulator observer
        dataLayer.addSimulatorObserver((Observer) jPanelSimulator);

        // add as an icon size observer
        dataLayer.addPreferencesObserver((Observer) jPanelSimulator);
        

        // ----------- WELCOME STUFF CREATION -----------------------
        // create welcome panel
        jPanelWelcome = new WelcomePanel(dataLayer);

        // add as a language observer
        dataLayer.addLanguageObserver((Observer) jPanelWelcome);


        // ----------- rest of constructor -----------------------
        // add this to zoom Manager as Observer
        //jLayeredPane.addObserverToZoomManager((Observer) this);
        
        ZoomManagerSingleton.getInstance().addObserver((Observer) this);

        doChangeMode(userInterfaceState);
    }

    @Override
    public void stopSimulatorActivities(){
         // turn of activities in simulator
        jPanelSimulator.setTurnedOff();
    }
    
    @Override
    public final void doChangeMode(UserInterfaceMainPanelState userInterfaceState) {
        this.userInterfaceState = userInterfaceState;

        this.removeAll();

        // turn of activities in simulator
        jPanelSimulator.setTurnedOff();

        switch (userInterfaceState) {
            case WELCOME:
                this.add(jPanelWelcome, BorderLayout.CENTER);
                break;
            case EDITOR:
                this.add(jScrollPane, BorderLayout.CENTER);
                this.add(jToolBarEditor, BorderLayout.WEST);

                // set default tool in ToolBar
                jLayeredPane.removeCurrentMouseListener();
                doSetToolInToolBar(MainTool.HAND);
                break;
            case SIMULATOR:
                this.add(jScrollPane, BorderLayout.CENTER);
                this.add(jPanelSimulator, BorderLayout.EAST);

                // set SIMULATOR mouse listener in draw panel
                jLayeredPane.removeCurrentMouseListener();
                jLayeredPane.setCurrentMouseListenerSimulator();
                break;
        }

        // repaint
        this.revalidate();
        this.repaint();
    }

    /**
     * reaction to zoom event
     *
     * @param o
     * @param o1
     */
    @Override
    public void update(Observable o, Object o1) {
       switch ((ObserverUpdateEventType) o1) {
            case ZOOM_CHANGE:
                ZoomEventWrapper zoomEventWrapper = ZoomManagerSingleton.getInstance().getZoomEventWrapper();
                zoomChangeUpdate(zoomEventWrapper);
                break;
            default:
                break;
        }
    }

    private void zoomChangeUpdate(ZoomEventWrapper zoomEventWrapper) {
        switch (zoomEventWrapper.getZoomType()) {
            case MOUSE:
                //System.out.println("Mouse");
                doZoomAccordingToMouse(zoomEventWrapper);
                break;
            case CENTER:
                //System.out.println("Center");
                doZoomAccordingToCenter(zoomEventWrapper);
                break;
        }

        // update zoom buttons in main window
        mainWindow.updateZoomButtons();
        // repaint
        this.revalidate();
        this.repaint();
    }

    private void doZoomAccordingToCenter(ZoomEventWrapper zoomEventWrapper) {
        Point oldPosition = jScrollPane.getViewport().getViewPosition();

        //System.out.println("width " +jScrollPane.getViewport().getWidth() + ", heigth " +jScrollPane.getViewport().getHeight() );

        int viewportWidth = jScrollPane.getViewport().getWidth();
        int viewportHeight = jScrollPane.getViewport().getHeight();

        if (jLayeredPane.hasGraph() && jLayeredPane.getGraph().getWidth() < viewportWidth) {
            viewportWidth = jLayeredPane.getGraph().getWidth();
        }

        if (jLayeredPane.hasGraph() && jLayeredPane.getGraph().getHeight() < viewportHeight) {
            viewportHeight = jLayeredPane.getGraph().getHeight();
        }

        // calculate center position 
        int centerXOldZoom = (int) (jScrollPane.getViewport().getViewPosition().x + ((viewportWidth / 2.0)));
        int centerYOldZoom = (int) (jScrollPane.getViewport().getViewPosition().y + ((viewportHeight / 2.0)));

        // count distance of old mouse from old viewport
        int width = centerXOldZoom - oldPosition.x;
        int height = centerYOldZoom - oldPosition.y;

        // count new mouse coordinates
        int centerXNewZoom = (int) ((centerXOldZoom / zoomEventWrapper.getOldScale()) * zoomEventWrapper.getNewScale());
        int centerYNewZoom = (int) ((centerYOldZoom / zoomEventWrapper.getOldScale()) * zoomEventWrapper.getNewScale());

        Point newPosition = new Point();
        // new viewport position has to be in same distance from mouse as before
        newPosition.x = centerXNewZoom - width;
        newPosition.y = centerYNewZoom - height;

        //System.out.println("New viewport x="+newPosition.x+", y="+newPosition.y);

        // do not allow position below 0,0
        if (newPosition.x < 0) {
            newPosition.x = 0;
        }

        if (newPosition.y < 0) {
            newPosition.y = 0;
        }

        // set new viewport
        jScrollPane.getViewport().setViewPosition(newPosition);
    }

    private void doZoomAccordingToMouse(ZoomEventWrapper zoomEventWrapper) {
        // -------------- ZOOM ACCORDING TO MOUSE POSITION  ---------------------
        Point oldPosition = jScrollPane.getViewport().getViewPosition();

        // get old mouse position
        int mouseXOldZoom = zoomEventWrapper.getMouseXInOldZoom();
        int mouseYOldZoom = zoomEventWrapper.getMouseYInOldZoom();

        // count distance of old mouse from old viewport
        int width = mouseXOldZoom - oldPosition.x;
        int height = mouseYOldZoom - oldPosition.y;

        // count new mouse coordinates
        int mouseXNewZoom = (int) ((mouseXOldZoom / zoomEventWrapper.getOldScale()) * zoomEventWrapper.getNewScale());
        int mouseYNewZoom = (int) ((mouseYOldZoom / zoomEventWrapper.getOldScale()) * zoomEventWrapper.getNewScale());


        Point newPosition = new Point();
        // new viewport position has to be in same distance from mouse as before
        newPosition.x = mouseXNewZoom - width;
        newPosition.y = mouseYNewZoom - height;

        // do not allow position below 0,0
        if (newPosition.x < 0) {
            newPosition.x = 0;
        }

        if (newPosition.y < 0) {
            newPosition.y = 0;
        }

        /*
         * System.out.println("Old viewport x="+oldPosition.x+",
         * y="+oldPosition.y); System.out.println("Old mouse x =
         * "+mouseXOldZoom+", y= "+mouseYOldZoom+". New zoom
         * x="+mouseXNewZoom+", y="+mouseYNewZoom);
         *
         * System.out.println("New viewport x="+newPosition.x+",
         * y="+newPosition.y);
         */

        // set new viewport
        jScrollPane.getViewport().setViewPosition(newPosition);

        // END -------------- ZOOM ACCORDING TO MOUSE POSITION  ---------------------
    }

    @Override
    public void removeAllSimulatorEvents() {
        jPanelSimulator.clearEvents();
    }

    @Override
    public Graph removeGraph() {
        return jLayeredPane.removeGraph();
    }

    @Override
    public void setGraph(Graph graph) {
        jLayeredPane.setGraph(graph);

        // initialize viewport to beginning
        jViewPort.setViewPosition(new Point(0, 0));
    }

    @Override
    public Graph getGraph() {
        return jLayeredPane.getGraph();
    }

    @Override
    public boolean hasGraph() {
        return jLayeredPane.hasGraph();
    }

    @Override
    public boolean canUndo() {
        return jLayeredPane.canUndo();
    }

    @Override
    public boolean canRedo() {
        return jLayeredPane.canRedo();
    }

    @Override
    public void undo() {
        jLayeredPane.undo();
    }

    @Override
    public void redo() {
        jLayeredPane.redo();
    }
    
    @Override
    public final void doSetToolInToolBar(MainTool mainTool){
         jToolBarEditor.setTool(mainTool);
    }

    @Override
    public UserInterfaceMainPanelState getUserInterfaceState() {
        return userInterfaceState;
    }

    @Override
    public void addNewProjectActionListener(ActionListener listener) {
        jPanelWelcome.addNewProjectActionListener(listener);
    }

    @Override
    public void addOpenProjectActionListener(ActionListener listener) {
        jPanelWelcome.addOpenProjectActionListener(listener);
    }

    @Override
    public JScrollPane getJScrollPane() {
        return jScrollPane;
    }

    @Override
    public JViewport getJViewport() {
        return jViewPort;
    }

    @Override
    public AnimationPanelOuterInterface getAnimationPanelOuterInterface() {
        return jLayeredPane.getAnimationPanelOuterInterface();
    }
}
