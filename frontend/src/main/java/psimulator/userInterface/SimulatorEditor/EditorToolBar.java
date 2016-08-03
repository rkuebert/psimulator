package psimulator.userInterface.SimulatorEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToolBar;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.dataLayer.Singletons.ColorMixerSingleton;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelToolChangeOuterInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.SecondaryTool;
import psimulator.userInterface.SimulatorEditor.SwingComponents.MenuToggleButton;
import psimulator.userInterface.SimulatorEditor.Tools.ToolsFactory;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class EditorToolBar extends JToolBar implements Observer {

    private DataLayerFacade dataLayer;
    
    private JButton jButtonFitToSize;
    private JButton jButtonAlignToGrid;
    
    private ButtonGroup toolsButtonGroup;
    
    private MenuToggleButton toggleButtonDragMove;
    private MenuToggleButton toggleButtonHand;
    private MenuToggleButton toggleButtonRouters;
    private MenuToggleButton toggleButtonSwitches;
    private MenuToggleButton toggleButtonEndDevices;
    private MenuToggleButton toggleButtonRealPC;
    private MenuToggleButton toggleButtonCable;

    public EditorToolBar(DataLayerFacade dataLayer, DrawPanelToolChangeOuterInterface toolChangeInterface) {
        super();
        this.dataLayer = dataLayer;
        
        // add this ToolBar as observer to languageManager
        dataLayer.addLanguageObserver((Observer)this);
        
        dataLayer.addPreferencesObserver((Observer)this);

        // tool bar is not possible to move
        this.setFloatable(false);

        // set orientation to vertical
        this.setOrientation(VERTICAL);

        this.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // BUTTONS
        jButtonFitToSize = new JButton();
        jButtonFitToSize.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        jButtonAlignToGrid = new JButton();
        jButtonAlignToGrid.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        toolsButtonGroup = new ButtonGroup();
        
        toggleButtonDragMove = new MenuToggleButton(ToolsFactory.getTools(MainTool.DRAG_MOVE, toolChangeInterface), dataLayer);
        toggleButtonHand = new MenuToggleButton(ToolsFactory.getTools(MainTool.HAND, toolChangeInterface), dataLayer);
        toggleButtonRouters = new MenuToggleButton(ToolsFactory.getTools(MainTool.ADD_ROUTER, toolChangeInterface), dataLayer);
        toggleButtonSwitches = new MenuToggleButton(ToolsFactory.getTools(MainTool.ADD_SWITCH, toolChangeInterface), dataLayer);
        toggleButtonEndDevices = new MenuToggleButton(ToolsFactory.getTools(MainTool.ADD_END_DEVICE, toolChangeInterface), dataLayer);
        toggleButtonRealPC = new MenuToggleButton(ToolsFactory.getTools(MainTool.ADD_REAL_PC, toolChangeInterface), dataLayer);
        toggleButtonCable = new MenuToggleButton(ToolsFactory.getTools(MainTool.ADD_CABLE, toolChangeInterface), dataLayer);
                
        toolsButtonGroup.add(toggleButtonDragMove);
        toolsButtonGroup.add(toggleButtonHand);
        toolsButtonGroup.add(toggleButtonEndDevices);
        toolsButtonGroup.add(toggleButtonRouters);
        toolsButtonGroup.add(toggleButtonSwitches);
        toolsButtonGroup.add(toggleButtonRealPC);
        toolsButtonGroup.add(toggleButtonCable);
 
        
        this.add(toggleButtonHand);
        this.add(toggleButtonDragMove);
        this.addSeparator();
        this.add(toggleButtonEndDevices);
        this.add(toggleButtonRouters);
        this.add(toggleButtonSwitches);
        this.add(toggleButtonRealPC);
        this.addSeparator();
        this.add(toggleButtonCable);
        this.addSeparator();
        this.add(jButtonFitToSize);
        this.add(jButtonAlignToGrid);
        
        
        // set texts
        setTextsToComponents();

        // apply background color
        this.setBackground(ColorMixerSingleton.editToolbarColor);
        Component[] comp = this.getComponents();
        for (Component c : comp) {
            c.setBackground(ColorMixerSingleton.editToolbarColor);
            // tool icon cannot be marked (ugly frame)
            c.setFocusable(false);
        }

        updateIconSize(dataLayer.getToolbarIconSize());
    }
 
    /**
     * reaction to update from LanguageManager
     */ 
    @Override
    public void update(Observable o, Object o1) {
        switch ((ObserverUpdateEventType) o1) {
            case LANGUAGE:
                setTextsToComponents();
                break;
            case ICON_SIZE:
                updateIconSize(dataLayer.getToolbarIconSize());
                break;
        }
    }

    /**
     * Updates images on toolbar buttons according to size
     * @param size 
     */
    public final void updateIconSize(ToolbarIconSizeEnum size){
        jButtonFitToSize.setIcon(ImageFactorySingleton.getInstance().getImageIconForToolbar(SecondaryTool.FIT_TO_SIZE, dataLayer.getToolbarIconSize()));
        jButtonAlignToGrid.setIcon(ImageFactorySingleton.getInstance().getImageIconForToolbar(SecondaryTool.ALIGN_TO_GRID, dataLayer.getToolbarIconSize()));
        
        toggleButtonDragMove.updateIconSize();
        toggleButtonHand.updateIconSize();
        toggleButtonEndDevices.updateIconSize();
        toggleButtonRouters.updateIconSize();
        toggleButtonSwitches.updateIconSize();
        toggleButtonRealPC.updateIconSize();
        toggleButtonCable.updateIconSize();
    }
    
    /**
     * Enables deafult tool of this toolbar
     */
    public void setTool(MainTool tool){
        switch(tool){
            case HAND:
                toggleButtonHand.setCurrentToolEnabled();
                toggleButtonHand.setSelected(true);
                break;
            case DRAG_MOVE:
                toggleButtonDragMove.setCurrentToolEnabled();
                toggleButtonDragMove.setSelected(true);
                break;
        }
        
        
    }
    
    /**
     * adds action listener to jButtonFitToSize
     * @param listener 
     */
    public void addToolActionFitToSizeListener(ActionListener listener) {
        jButtonFitToSize.addActionListener(listener);
    }
    
    /**
     * adds action listener to jButtonAlignToGrid
     * @param listener 
     */
    public void addToolActionAlignToGridListener(ActionListener listener) {
        jButtonAlignToGrid.addActionListener(listener);
    }

    ////////------------ PRIVATE------------///////////
    private void setTextsToComponents() {
        // set text only to Tools that cant be changed
        toggleButtonDragMove.setToolTipText(dataLayer.getString("DRAG_MOVE")+" (M)");
        toggleButtonHand.setToolTipText(dataLayer.getString("HAND")+" (H)");
        toggleButtonRealPC.setToolTipText(dataLayer.getString("REAL_PC"));
        //toggleButtonCable.setToolTipText(dataLayer.getString("CABLE"));
        jButtonFitToSize.setToolTipText(dataLayer.getString("FIT_TO_SIZE"));
        jButtonAlignToGrid.setToolTipText(dataLayer.getString("ALIGN_TO_GRID"));
        //
        toggleButtonRouters.updateToolTip();
        toggleButtonSwitches.updateToolTip();
        toggleButtonEndDevices.updateToolTip();
        toggleButtonRealPC.updateToolTip();
        toggleButtonCable.updateToolTip();
    }
}
