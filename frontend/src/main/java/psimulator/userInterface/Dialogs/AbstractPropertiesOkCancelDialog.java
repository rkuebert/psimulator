package psimulator.userInterface.Dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public abstract class AbstractPropertiesOkCancelDialog extends AbstractPropertiesDialog {

    /*
     * window componenets
     */
    protected JButton jButtonOk;
    protected JButton jButtonCancel;

    public AbstractPropertiesOkCancelDialog(Component mainWindow, DataLayerFacade dataLayer) {
        super(mainWindow, dataLayer);
    }

    @Override
    protected JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        //mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(createContentPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        mainPanel.add(createOkCancelPanel());

        return mainPanel;
    }

    public JPanel createOkCancelPanel() {
        JPanel buttonPane = new JPanel();

        jButtonOk = new JButton(dataLayer.getString("SAVE"));
        jButtonOk.addActionListener(new JButtonOkListener());

        jButtonCancel = new JButton(dataLayer.getString("CANCEL"));
        jButtonCancel.addActionListener(new JButtonCancelListener());

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(jButtonOk);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(jButtonCancel);
        buttonPane.add(Box.createRigidArea(new Dimension(3, 0)));

        return buttonPane;
    }

    @Override
    protected void setDefaultJButton(){
        jButtonDefault = jButtonOk;
    }

    //
    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for JComboBoxInterface
     */
    public class JButtonOkListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            copyValuesFromFieldsToLocal();
            if (hasChangesMade()) {
                copyValuesFromLocalToGlobal();
            }
            thisDialog.setVisible(false);
            thisDialog.dispose();    //closes the window
        }
    }

}
