package psimulator.userInterface.Dialogs;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.ValidationLayerUI;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractPropertiesDialog extends JDialog{
    
    protected JDialog thisDialog;
    protected JButton jButtonDefault;
    
    // A single LayerUI for all the fields.
    protected LayerUI<JFormattedTextField> layerUI = new ValidationLayerUI();
    
    protected Component parentComponent;
    protected DataLayerFacade dataLayer;
    
    public AbstractPropertiesDialog(Component mainWindow, DataLayerFacade dataLayer) {
        this.dataLayer = dataLayer;
        this.parentComponent = (Component) mainWindow;

        // set of JDialog parameters
        this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        //this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
    protected final void initialize(){
        // copy values to local
        copyValuesFromGlobalToLocal();
        
        // add content to panel
        addContent();
        
        // set default jbutton
        setDefaultJButton();
        
        // initialize
        setDialogParameters();

    }
    
    
    private void setDialogParameters() {

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                
                windowClosing(e);
                
                closeAction();
            }
        });

        // set OK button as default button
        this.getRootPane().setDefaultButton(jButtonDefault);

        //
        this.thisDialog = (JDialog) this;

        this.pack();
        
        // place in middle of parent window
        int y = parentComponent.getY() + (parentComponent.getHeight() / 2) - (this.getHeight() / 2);
        int x = parentComponent.getX() + (parentComponent.getWidth() / 2) - (this.getWidth() / 2);
        this.setLocation(x, y);
    }
    
    /**
     * Override this method to react on windowClosing event
     */
    protected void windowClosing(){
        
    }
        
    /**
     * Add key events reactions to root pane
     *
     * @return
     */
    @Override
    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(new JButtonCancelListener(), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }
    
    
    protected void closeAction() {
        boolean close = true;
        
        copyValuesFromFieldsToLocal();
        
        if (hasChangesMade()) {
            if (checkUserAndSave() == false) {
                close = false;
            }
        }

        if (close) {
            this.setVisible(false);
            this.dispose();    //closes the window
        }
    }
    
    
    /**
     * Checks user if he wants to save changes and saves it.
     */
    protected boolean checkUserAndSave() {
        // want to save?
        int i = showWarningSave(dataLayer.getString("WARNING"), dataLayer.getString("DO_YOU_WANT_TO_SAVE_CHANGES"));

        // if YES
        if (i == 0) {
            copyValuesFromLocalToGlobal();
        }

        if (i == -1) {
            return false;
        } else {
            return true;
        }
    }
    
    

    protected int showWarningSave(String title, String message) {
        Object[] options = {dataLayer.getString("SAVE"), dataLayer.getString("DONT_SAVE")};
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

    protected void addContent(){
        // add Content
        this.getContentPane().add(createMainPanel());
    }
    
    protected abstract void setDefaultJButton();
    
    protected abstract JPanel createContentPanel();
    
    protected abstract JPanel createMainPanel();
    
    /**
     * Copies values from model to local variables.
     */
    protected abstract void copyValuesFromGlobalToLocal();

    /**
     * Call to copy values from swingg components to local variables.
     */
    protected abstract void copyValuesFromFieldsToLocal();

    /**
     * Call only when changes made. Copies values from local variables to model.
     */
    protected abstract void copyValuesFromLocalToGlobal();

    /**
     * True if changes made. False if no value or property changed.
     * @return 
     */
    protected abstract boolean hasChangesMade();
    
    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for JComboBoxInterface
     */
    public class JButtonCancelListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            closeAction();
        }
    }
    
    
}
