package psimulator.userInterface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.dataLayer.Singletons.ZoomManagerSingleton;
import psimulator.logicLayer.ControllerFacade;
import psimulator.userInterface.GlassPane.GlassPanelPainterSingleton;
import psimulator.userInterface.GlassPane.MainWindowGlassPane;
import psimulator.userInterface.SaveLoad.SaveLoadManagerEvents;
import psimulator.userInterface.SaveLoad.SaveLoadManagerNetworkModel;
import psimulator.userInterface.SaveLoad.SaveLoadManagerUserReaction;
import psimulator.userInterface.SimulatorEditor.AnimationPanel.AnimationPanelOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.UndoRedo;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.Zoom;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;
import psimulator.userInterface.SimulatorEditor.UserInterfaceMainPanel;
import psimulator.userInterface.SimulatorEditor.UserInterfaceMainPanelOuterInterface;
import psimulator.userInterface.SimulatorEditor.UserInterfaceMainPanelState;
import psimulator.userInterface.actionListerners.PreferencesActionListener;
import shared.Components.NetworkModel;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class MainWindow extends JFrame implements MainWindowInnerInterface, UserInterfaceOuterFacade, Observer {

    private SaveLoadManagerNetworkModel saveLoadManagerGraph;
    private SaveLoadManagerEvents saveLoadManagerEvents;
    //
    private DataLayerFacade dataLayer;
    private ControllerFacade controller;
    /*
     * window componenets
     */
    private MenuBar jMenuBar;
    private ToolBar jToolBar;
    private UserInterfaceMainPanelOuterInterface jPanelUserInterfaceMain;
    /*
     * end of window components
     */
    private JFrame mainWindow;
    private MainWindowGlassPane glassPane;
    private Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    //
    //private Map<Integer, JFrame> openedTelnetWindows;
    
    public MainWindow(DataLayerFacade dataLayer) {
        this.dataLayer = dataLayer;

        try {
            //if OS is Windows we set the windows look and feel
            if (isWindows()) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else { // otherwise we set metal look and feel
                //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
        }

        // create save load managers
        saveLoadManagerGraph = new SaveLoadManagerNetworkModel((Component) this, dataLayer);
        saveLoadManagerEvents = new SaveLoadManagerEvents((Component) this, dataLayer);

        // create menu bar
        jMenuBar = new MenuBar(dataLayer);
        // create tool bar
        jToolBar = new ToolBar(dataLayer);

        this.setTitle(dataLayer.getString("WINDOW_TITLE"));

        // set menu bar
        this.setJMenuBar(jMenuBar);
        this.add(jToolBar, BorderLayout.PAGE_START);

        // save reference to this for inner classes of ActionListeners
        this.mainWindow = (JFrame) this;

        // crate and add main panel
        jPanelUserInterfaceMain = new UserInterfaceMainPanel(this, dataLayer, UserInterfaceMainPanelState.WELCOME);
        this.add(jPanelUserInterfaceMain, BorderLayout.CENTER);


        // set this as Observer to LanguageManager
        dataLayer.addLanguageObserver((Observer) this);

        // set icon
        this.setIconImage(ImageFactorySingleton.getInstance().getImageIcon("/resources/toolbarIcons/32/home.png").getImage());

        // create glass pane and glass pane painter
        glassPane = new MainWindowGlassPane();

        // initialize glass pane painter singleton
        GlassPanelPainterSingleton.getInstance().initialize(glassPane);

        this.setGlassPane(glassPane);
        glassPane.setOpaque(false);
        getGlassPane().setVisible(true);

    }

    @Override
    public void initView(ControllerFacade controller) {
        this.controller = controller;

        updateProjectRelatedButtons();

        updateToolBarIconsSize(dataLayer.getToolbarIconSize());

        addActionListenersToViewComponents();

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                // if data can be lost after check
                if (!checkDataLoss()) {
                    mainWindow.setCursor(defaultCursor);
                    return;
                }
                mainWindow.setCursor(defaultCursor);
                
                refreshUserInterfaceMainPanel(null, null, UserInterfaceMainPanelState.WELCOME, false);

                dataLayer.savePreferences();
                
                System.exit(0);
            }
        });

        // set of window properties
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //this.setMinimumSize(new Dimension(987, 740));
        this.setMinimumSize(new Dimension(933, 700));
        this.setSize(new Dimension(1024, 768));
        this.setVisible(true);

        
//        // Get the size of the screen
//        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//
//        // Determine the new location of the window
//        int w = this.getSize().width;
//        int h = this.getSize().height;
//        int x = (dim.width - w) / 2;
//        int y = (dim.height - h) / 2;
//
//        // Move the window
//        this.setLocation(x, y);
        
        this.setVisible(true);
    }

    @Override
    public void updateUndoRedoButtons() {
        // if in simulator mode
        if (jPanelUserInterfaceMain.getUserInterfaceState() == UserInterfaceMainPanelState.EDITOR) {
            jMenuBar.setUndoEnabled(jPanelUserInterfaceMain.canUndo());
            jToolBar.setUndoEnabled(jPanelUserInterfaceMain.canUndo());

            jMenuBar.setRedoEnabled(jPanelUserInterfaceMain.canRedo());
            jToolBar.setRedoEnabled(jPanelUserInterfaceMain.canRedo());
        } else {
            jMenuBar.setUndoEnabled(false);
            jToolBar.setUndoEnabled(false);

            jMenuBar.setRedoEnabled(false);
            jToolBar.setRedoEnabled(false);
        }
    }

    @Override
    public void updateZoomButtons() {
        jMenuBar.setZoomInEnabled(ZoomManagerSingleton.getInstance().canZoomIn());
        jToolBar.setZoomInEnabled(ZoomManagerSingleton.getInstance().canZoomIn());

        jMenuBar.setZoomOutEnabled(ZoomManagerSingleton.getInstance().canZoomOut());
        jToolBar.setZoomOutEnabled(ZoomManagerSingleton.getInstance().canZoomOut());

        jMenuBar.setZoomResetEnabled(true);
        jToolBar.setZoomResetEnabled(true);
    }

    @Override
    public void updateToolBarIconsSize(ToolbarIconSizeEnum size) {
        jToolBar.updateIconSize(size);
    }

    /**
     * Reaction to Language Observable update
     *
     * @param o
     * @param o1
     */
    @Override
    public void update(Observable o, Object o1) {
        saveLoadManagerGraph.updateTextsOnFileChooser();
        saveLoadManagerEvents.updateTextsOnFileChooser();
    }

    @Override
    public AnimationPanelOuterInterface getAnimationPanelOuterInterface() {
        return jPanelUserInterfaceMain.getAnimationPanelOuterInterface();
    }

    @Override
    public Component getMainWindowComponent() {
        return this;
    }

    /**
     * Saves events. Exceptions handled inside.
     *
     * @param simulatorEventsWrapper
     */
    @Override
    public void saveEventsAction(SimulatorEventsWrapper simulatorEventsWrapper) {
        saveEventsAndInformAboutSuccess(simulatorEventsWrapper);
    }
    
    private boolean saveEventsAndInformAboutSuccess(SimulatorEventsWrapper simulatorEventsWrapper){
        boolean success = saveLoadManagerEvents.doSaveAsEventsAction(simulatorEventsWrapper);

        if (success) {
            // inform user
            String file = saveLoadManagerEvents.getFile().getPath();
            GlassPanelPainterSingleton.getInstance().
                    addAnnouncement(dataLayer.getString("EVENT_LIST_SAVE_ACTION"), dataLayer.getString("SAVED_TO"), file);
        }
        
        this.setCursor(defaultCursor);
        return success;
    }

    /**
     * Returns loaded events or null if it could not be loaded. Exceptions are
     * handled inside.
     *
     * @return
     */
    @Override
    public SimulatorEventsWrapper loadEventsAction() {
        SimulatorEventsWrapper simulatorEventsWrapper = saveLoadManagerEvents.doLoadEventsAction();

        if (simulatorEventsWrapper != null) {
            // inform user
            String file = saveLoadManagerEvents.getFile().getPath();
            GlassPanelPainterSingleton.getInstance().
                    addAnnouncement(dataLayer.getString("EVENT_LIST_OPEN_ACTION"), dataLayer.getString("OPENED_FROM"), file);
        }

        this.setCursor(defaultCursor);
        return simulatorEventsWrapper;
    }

    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for Undo and Redo button
     */
    class JMenuItemUndoRedoListener implements ActionListener {

        /**
         * calls undo or redo and repaint on jPanelEditor, updates Undo and Redo
         * buttons
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (UndoRedo.valueOf(e.getActionCommand())) {
                case UNDO:
                    jPanelUserInterfaceMain.undo();
                    break;
                case REDO:
                    jPanelUserInterfaceMain.redo();
                    break;
            }
            updateUndoRedoButtons();
            jPanelUserInterfaceMain.repaint();
        }
    }

/////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for Zoom buttons
     */
    class JMenuItemZoomListener implements ActionListener {

        /**
         * calls zoom operation on jPanelEditor according to actionCommand
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (Zoom.valueOf(e.getActionCommand())) {
                case IN:
                    ZoomManagerSingleton.getInstance().zoomIn();
                    break;
                case OUT:
                    ZoomManagerSingleton.getInstance().zoomOut();
                    break;
                case RESET:
                    ZoomManagerSingleton.getInstance().zoomReset();
                    break;
            }
        }
    }

/////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for Simulator and Editor buttons
     */
    class JMenuItemSimulatorEditorListener implements ActionListener {

        /**
         * 
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (UserInterfaceMainPanelState.valueOf(e.getActionCommand())) {
                case EDITOR:
                    // if nothing changed, do nothing
                    if (jPanelUserInterfaceMain.getUserInterfaceState() == UserInterfaceMainPanelState.EDITOR) {
                        return;
                    }

                    // change state to editor without changing or removing the graph
                    refreshUserInterfaceMainPanel(null, null, UserInterfaceMainPanelState.EDITOR, true);

                    break;
                case SIMULATOR:
                    // if nothing changed, do nothing
                    if (jPanelUserInterfaceMain.getUserInterfaceState() == UserInterfaceMainPanelState.SIMULATOR) {
                        return;
                    }
                    
                    // check if we can change to simulator (wrong events in list)
                    if(!dataLayer.getSimulatorManager().hasAllEventsItsComponentsInModel()){
                        // if there is a problem, ask user what to do
                        int result = showWarningEventsInListHaventComponents(dataLayer.getString("WARNING"), dataLayer.getString("EVENTS_CANT_BE_APPLIED_WHAT_TO_DO"));

                        if(result == 0){    // save events and celar list
                            //System.out.println("save events and celar list");
                            // save events
                            boolean success = saveEventsAndInformAboutSuccess(dataLayer.getSimulatorManager().getSimulatorEventsCopy());
                            // if save wasnt succesfull
                            if(!success){
                                // go back to editor
                                refreshUserInterfaceMainPanel(null, null, UserInterfaceMainPanelState.EDITOR, true);
                                return;
                            }
                            // if save succesfull clear list
                            jPanelUserInterfaceMain.removeAllSimulatorEvents();
                        } else if (result == 1){    // celar events
                            //System.out.println("Clear list");
                            // clear list
                            jPanelUserInterfaceMain.removeAllSimulatorEvents();
                        } else {    // go back to editor
                            //System.out.println("Cancel");
                            // get back to editor
                            // change state to editor without changing or removing the graph
                            refreshUserInterfaceMainPanel(null, null, UserInterfaceMainPanelState.EDITOR, true);
                            return;
                        }     
                    }
                    

                    // change state to editor without changing or removing the graph
                    refreshUserInterfaceMainPanel(null, null, UserInterfaceMainPanelState.SIMULATOR, true);
                    break;
            }
        }
    }
    
    private int showWarningEventsInListHaventComponents(String title, String message) {
        //Object[] options = {dataLayer.getString("SAVE"), dataLayer.getString("DONT_SAVE"), dataLayer.getString("CANCEL")};
        Object[] options = {dataLayer.getString("SAVE_EVENTS_AND_CLEAR_LIST"), 
            dataLayer.getString("DELETE_EVENTS"),dataLayer.getString("GO_BACK_TO_EDITOR")};
        int n = JOptionPane.showOptionDialog(this,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, //do not use a custom Icon
                options, //the titles of buttons
                options[0]); //default button title

        return n;
    }
    
    /**
     * Returns true if checked and we should continue. False if do not continue.
     *
     * @return
     */
    private boolean checkDataLoss() {
        // if data can be lost
        if (saveLoadManagerGraph.doCheckIfPossibleDataLoss(jPanelUserInterfaceMain.getGraph())) {

            SaveLoadManagerUserReaction userReaction = saveLoadManagerGraph.doAskUserIfSave(jPanelUserInterfaceMain.getGraph());

            switch (userReaction) {
                case DO_NOT_SAVE:
                    // user dont want to save, we should proceed
                    return true;
                case DO_SAVE:
                    // save 
                    boolean success = saveLoadManagerGraph.doSaveGraphAction();
                    if (success) {
                        String file = saveLoadManagerGraph.getFile().getPath();
                        GlassPanelPainterSingleton.getInstance().
                                addAnnouncement(dataLayer.getString("NETWORK_SAVE_ACTION"), dataLayer.getString("SAVED_TO"), file);
                    }

                    // return true if save successfull, false if not succesfull
                    return success;
                case CANCEL:
                    return false;
            }
        }
        // data cant be lost
        return true;
    }

/////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for NewProject button
     */
    class JMenuItemNewListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // if data can be lost after check
            if (!checkDataLoss()) {
                mainWindow.setCursor(defaultCursor);
                return;
            }
            mainWindow.setCursor(defaultCursor);

            // create new network model
            NetworkModel networkModel = dataLayer.getNetworkFacade().createNetworkModel();

            // create new graph
            Graph graph = new Graph();

            // refresh UI
            refreshUserInterfaceMainPanel(graph, networkModel, UserInterfaceMainPanelState.EDITOR, false);

            // set saved timestamp
            saveLoadManagerGraph.setLastSavedTimestamp();
            saveLoadManagerGraph.setLastSavedFile(null);
        }
    }

/////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for NewProject button
     */
    class JMenuItemCloseListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // if data can be lost after check
            if (!checkDataLoss()) {
                mainWindow.setCursor(defaultCursor);
                return;
            }
            mainWindow.setCursor(defaultCursor);

            // turn off playing recording and etc
            //jPanelUserInterfaceMain.stopSimulatorActivities();

            refreshUserInterfaceMainPanel(null, null, UserInterfaceMainPanelState.WELCOME, false);
        }
    }

/////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for Open button
     */
    class JMenuItemOpenListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            openAction(null);
        }
    }
    
        
    /**
     * Action Listener for Open recent file button
     */
    class JMenuItemOpenRecentFileListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            openAction(e.getActionCommand());
        }
    }
    
      
        ////////////////////////////////////////// JMenuItemAboutListener
    class JMenuItemAboutListener implements ActionListener {
        /**
         * Opens about dialog
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            final String ABOUT =
                "<HTML>" +
                "<B>"+dataLayer.getString("WINDOW_TITLE")+"</B></BR>" +
                "<P><BR>"+dataLayer.getString("ABOUT_author")+": <FONT COLOR=\"#0000ff\">Martin Švihlík</FONT></P>" +
                "<P><BR></P>"+dataLayer.getString("ABOUT_text")+
                "</HTML>";
            JOptionPane.showMessageDialog(mainWindow, ABOUT, dataLayer.getString("ABOUT_aboutProgram"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void openAction(String filePath){
        // if data can be lost after check
        if (!checkDataLoss()) {
            this.setCursor(defaultCursor);
            return;
        }
        mainWindow.setCursor(defaultCursor);
        
        // turn off playing recording and etc
        jPanelUserInterfaceMain.stopSimulatorActivities();

        
        // load network model
        NetworkModel networkModel;
        
        if(filePath == null){
            networkModel = saveLoadManagerGraph.doLoadNetworkModel();
        }else{
            networkModel = saveLoadManagerGraph.doLoadNetworkModel(filePath);
        }

        if (networkModel == null) {
            this.setCursor(defaultCursor);
            return;
        }

        // set wait cursor
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
        // create graph from model
        Graph graph = saveLoadManagerGraph.buildGraphFromNetworkModel(networkModel);

        if (graph != null) {
            // removeAllSimulatorEvents graph (set edit timestamp)
            refreshUserInterfaceMainPanel(graph, networkModel, UserInterfaceMainPanelState.EDITOR, false);

            // set saved timestamp
            saveLoadManagerGraph.setLastSavedTimestamp();

            // inform user
            String file = saveLoadManagerGraph.getFile().getPath();
            GlassPanelPainterSingleton.getInstance().
                    addAnnouncement(dataLayer.getString("NETWORK_OPEN_ACTION"), dataLayer.getString("OPENED_FROM"), file);
        }
        
        this.setCursor(defaultCursor);
    }

/////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for Save button
     */
    class JMenuItemSaveListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean success = saveLoadManagerGraph.doSaveGraphAction();

            // inform user
            if (success) {
                String file = saveLoadManagerGraph.getFile().getPath();
                GlassPanelPainterSingleton.getInstance().
                        addAnnouncement(dataLayer.getString("NETWORK_SAVE_ACTION"), dataLayer.getString("SAVED_TO"), file);
            }
            
            mainWindow.setCursor(defaultCursor);
        }
    }

/////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for SaveAs button
     */
    class JMenuItemSaveAsListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean success = saveLoadManagerGraph.doSaveAsGraphAction();

            // inform user
            if (success) {
                String file = saveLoadManagerGraph.getFile().getPath();
                GlassPanelPainterSingleton.getInstance().
                        addAnnouncement(dataLayer.getString("NETWORK_SAVE_AS_ACTION"), dataLayer.getString("SAVED_TO"), file);
            }
            
            mainWindow.setCursor(defaultCursor);
        }
    }

/////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for Exit Button
     */
    class JMenuItemExitListener implements ActionListener {

        /**
         * Saves config data and exits application.
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            // if data can be lost after check
            if (!checkDataLoss()) {
                mainWindow.setCursor(defaultCursor);
                return;
            }
            mainWindow.setCursor(defaultCursor);

            refreshUserInterfaceMainPanel(null, null, UserInterfaceMainPanelState.WELCOME, false);

            dataLayer.savePreferences();
            
            System.exit(0);
        }
    }
    
    
////////------------ PRIVATE------------///////////

    /**
     * Updates jPanelUserInterfaceMain according to userInterfaceState. If
     * changing to SIMULATOR or EDITOR state, graph cannot be null.
     *
     * @param graph Graph to set into jPanelUserInterfaceMain, can be null if
     * userInterfaceState will be WELCOME
     * @param userInterfaceState State to change to.
     * @param changingSimulatorEditor if true, the graph is kept untouched
     */
    private void refreshUserInterfaceMainPanel(Graph graph, NetworkModel networkModel, UserInterfaceMainPanelState userInterfaceState, boolean changingSimulatorEditor) {

        // turn off playing recording and etc
        jPanelUserInterfaceMain.stopSimulatorActivities();

        if (!changingSimulatorEditor) {
            // delete events from simulator
            jPanelUserInterfaceMain.removeAllSimulatorEvents();
        }

        switch (userInterfaceState) {
            case WELCOME:
                // remove graph
                jPanelUserInterfaceMain.removeGraph();
                break;
            case EDITOR:
                // if not only changing from simulator to editor or back
                if (!changingSimulatorEditor) {
                    // remove graph
                    jPanelUserInterfaceMain.removeGraph();
                    // set network model to network facade
                    dataLayer.getNetworkFacade().setNetworkModel(networkModel);
                    // set another graph
                    jPanelUserInterfaceMain.setGraph(graph);
                }

                // set Editor selected in tool bar
                jToolBar.setEditorSelected(true);
                break;
            case SIMULATOR:
                // if not only changing from simulator to editor or back
                if (!changingSimulatorEditor) {
                    // remove graph
                    jPanelUserInterfaceMain.removeGraph();
                    // set network model to network facade
                    dataLayer.getNetworkFacade().setNetworkModel(networkModel);
                    // set another graph
                    jPanelUserInterfaceMain.setGraph(graph);
                }

                // set Simulator selected in tool bar
                jToolBar.setSimulatorSelected(true);
                break;
        }

        jPanelUserInterfaceMain.doChangeMode(userInterfaceState);

        // update buttons
        updateProjectRelatedButtons();
    }

    private void updateProjectRelatedButtons() {
        if (!jPanelUserInterfaceMain.hasGraph()) {
            jMenuBar.setProjectRelatedButtonsEnabled(false);
            jToolBar.setProjectRelatedButtonsEnabled(false, jPanelUserInterfaceMain.getUserInterfaceState());

            jMenuBar.setUndoEnabled(false);
            jToolBar.setUndoEnabled(false);

            jMenuBar.setRedoEnabled(false);
            jToolBar.setRedoEnabled(false);

            jMenuBar.setZoomInEnabled(false);
            jToolBar.setZoomInEnabled(false);

            jMenuBar.setZoomOutEnabled(false);
            jToolBar.setZoomOutEnabled(false);

            jMenuBar.setZoomResetEnabled(false);
            jToolBar.setZoomResetEnabled(false);

            return;
        }

        jMenuBar.setProjectRelatedButtonsEnabled(true);
        jToolBar.setProjectRelatedButtonsEnabled(true, jPanelUserInterfaceMain.getUserInterfaceState());

        updateZoomButtons();
        updateUndoRedoButtons();
    }

    /**
     * Adds action listeners to View Components
     */
    private void addActionListenersToViewComponents() {

        // add listeners to Menu Bar - FILE
        ActionListener newListener = new JMenuItemNewListener();
        jMenuBar.addNewProjectActionListener(newListener);
        jToolBar.addNewProjectActionListener(newListener);
        jPanelUserInterfaceMain.addNewProjectActionListener(newListener);

        ActionListener closeListener = new JMenuItemCloseListener();
        jMenuBar.addCloseActionListener(closeListener);
        jToolBar.addCloseActionListener(closeListener);

        ActionListener openListener = new JMenuItemOpenListener();
        jMenuBar.addOpenActionListener(openListener);
        jToolBar.addOpenActionListener(openListener);
        jPanelUserInterfaceMain.addOpenProjectActionListener(openListener);
        
        ActionListener openRecentFileListener = new JMenuItemOpenRecentFileListener();
        jMenuBar.addOpenRecentFileListener(openRecentFileListener);

        ActionListener saveListener = new JMenuItemSaveListener();
        jMenuBar.addSaveActionListener(saveListener);
        jToolBar.addSaveActionListener(saveListener);

        ActionListener saveasListener = new JMenuItemSaveAsListener();
        jMenuBar.addSaveAsActionListener(saveasListener);
        jToolBar.addSaveAsActionListener(saveasListener);

        jMenuBar.addExitActionListener(new JMenuItemExitListener());
        // END add listeners to Menu Bar - FILE

        // add listeners to Menu Bar - EDIT
        ActionListener udnoListener = new JMenuItemUndoRedoListener();
        jMenuBar.addUndoRedoActionListener(udnoListener);
        jToolBar.addUndoRedoActionListener(udnoListener);

        // END add listeners to Menu Bar - EDIT

        // add listeners to Menu Bar - VIEW
        ActionListener zoomListener = new JMenuItemZoomListener();
        jMenuBar.addZoomActionListener(zoomListener);
        jToolBar.addZoomActionListener(zoomListener);
        // END add listeners to Menu Bar - VIEW

        // add listeners to Menu Bar - OPTIONS
        ActionListener preferencesListener = new PreferencesActionListener((MainWindowInnerInterface) this, dataLayer);
        jMenuBar.addPreferencesActionListener(preferencesListener);
        jToolBar.addPreferencesActionListener(preferencesListener);

        // END add listeners to Menu Bar - OPTIONS
        
        // add listeners to Menu Bar - HELP
        ActionListener aboutListener = new JMenuItemAboutListener();
        jMenuBar.addAboutListener(aboutListener);
        
        // END add listeners to Menu Bar - HELP

        // add listeners to ToolBar editor and simulator toggle buttons
        jToolBar.addSimulatorEditorActionListener(new JMenuItemSimulatorEditorListener());
        // END add listeners to ToolBar editor and simulator toggle buttons

    }

    /**
     * Finds whether OS is windows
     *
     * @return true if windows, false otherwise
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }
}
