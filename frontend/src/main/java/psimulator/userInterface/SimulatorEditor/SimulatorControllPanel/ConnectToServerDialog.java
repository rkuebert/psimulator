package psimulator.userInterface.SimulatorEditor.SimulatorControllPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.Dialogs.AbstractPropertiesDialog;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Support.Validator;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.RegexFormatter;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.WaitLayerUI;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class ConnectToServerDialog extends AbstractPropertiesDialog {

    final WaitLayerUI waitLayerUI = new WaitLayerUI();
    //
    private Font boldFont;
    //
    private JButton jButtonConnectToServer;
    private JButton jButtonCancel;
    //
    private JFormattedTextField jTextFieldPsimulatorIpAddress;
    private JFormattedTextField jTextFieldPsimulatorPort;
    //
    private String connectionIpAddress;
    private String connectionPort;
    //
    private boolean connectingActive = false;

    public ConnectToServerDialog(Component mainWindow, DataLayerFacade dataLayer) {
        super(mainWindow, dataLayer);

        // set icon
        this.setIconImage(ImageFactorySingleton.getInstance().getImageIcon("/resources/toolbarIcons/32/kwifimanager.png").getImage());
        
        // set title
        this.setTitle(dataLayer.getString("CONNECT_TO_SERVER"));

        // initialize
        initialize();
    }

    /**
     * Call when connected. The dialog is disposed
     */
    public void connected() {
        connectingActive = false;

        // stop animation
        waitLayerUI.stop();

        // dispose
        thisDialog.setVisible(false);
        thisDialog.dispose();    //closes the window
    }

    /**
     * Call when connecting failed. The animation is stopped and components are
     * enabled
     */
    public void connectingFailed() {
        connectingActive = false;

        // stop animation
        waitLayerUI.stop();

        // enable buttons
        enableComponents(true);

        // inform user
        JOptionPane.showMessageDialog(parentComponent,
                dataLayer.getString("CONNECTION_ESTABLISH_NOT_SUCCESSFUL"),
                dataLayer.getString("WARNING"),
                JOptionPane.WARNING_MESSAGE);
    }

    @Override
    protected void copyValuesFromGlobalToLocal() {
        connectionIpAddress = dataLayer.getConnectionIpAddress();
        connectionPort = dataLayer.getConnectionPort();
    }

    @Override
    protected void copyValuesFromFieldsToLocal() {
        connectionIpAddress = jTextFieldPsimulatorIpAddress.getText();
        connectionPort = jTextFieldPsimulatorPort.getText();
    }

    @Override
    protected void copyValuesFromLocalToGlobal() {
        dataLayer.setConnectionIpAddress(connectionIpAddress);
        dataLayer.setConnectionPort(connectionPort);

        // save preferences 
        dataLayer.savePreferences();
    }

    @Override
    protected boolean hasChangesMade() {
        if (!connectionIpAddress.equals(dataLayer.getConnectionIpAddress())) {
            return true;
        }

        if (!connectionPort.equals(dataLayer.getConnectionPort())) {
            return true;
        }

        return false;
    }

    @Override
    protected void windowClosing() {
        if (connectingActive) {
            dataLayer.getSimulatorManager().doDisconnect();
            //
            connectingActive = false;
        }


        jButtonCancel.requestFocusInWindow();

        jTextFieldPsimulatorIpAddress.setValue(jTextFieldPsimulatorIpAddress.getValue());
        jTextFieldPsimulatorPort.setValue(jTextFieldPsimulatorPort.getValue());
    }

    @Override
    protected void setDefaultJButton() {
        jButtonDefault = jButtonConnectToServer;
    }

    @Override
    protected JPanel createMainPanel() {
        JPanel wrapPanel = new JPanel();

        JPanel mainPanel = new JPanel();
        //mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(createContentPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        mainPanel.add(createConnectCancelPanel());

        wrapPanel.add(new JLayer<>(mainPanel, waitLayerUI));

        return wrapPanel;
    }

    @Override
    protected JPanel createContentPanel() {
        JPanel addressesPanel = new JPanel();
        addressesPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("CONNECTION_PROPERTIES")));
        // set layout
        GridLayout addressesPanelLayout = new GridLayout(0, 3);
        addressesPanelLayout.setHgap(10);
        addressesPanel.setLayout(addressesPanelLayout);

        // IP address
        JLabel ipAddressName = new JLabel(dataLayer.getString("IP_ADDRESS"));
        boldFont = new Font(ipAddressName.getFont().getName(), Font.BOLD, ipAddressName.getFont().getSize());
        ipAddressName.setFont(boldFont);
        addressesPanel.add(ipAddressName);

        RegexFormatter ipMaskFormatter = new RegexFormatter(Validator.IP_PATTERN_NOT_EMPTY);
        ipMaskFormatter.setAllowsInvalid(true);         // allow to enter invalid value for short time
        ipMaskFormatter.setCommitsOnValidEdit(true);    // value is immedeatly published to textField
        ipMaskFormatter.setOverwriteMode(false);        // do notoverwrite charracters

        jTextFieldPsimulatorIpAddress = new JFormattedTextField(ipMaskFormatter);
        jTextFieldPsimulatorIpAddress.setToolTipText(dataLayer.getString("REQUIRED_FORMAT_IS") + " 192.168.1.1 (IP)");
        jTextFieldPsimulatorIpAddress.setText(connectionIpAddress);
        // add decorator that paints wrong input icon
        addressesPanel.add(new JLayer<>(jTextFieldPsimulatorIpAddress, layerUI));

        JLabel ipAddressTip = new JLabel("10.0.0.1 (IP)");
        addressesPanel.add(ipAddressTip);

        // PORT
        JLabel portName = new JLabel(dataLayer.getString("PORT"));
        portName.setFont(boldFont);
        addressesPanel.add(portName);

        RegexFormatter portFormatter = new RegexFormatter(Validator.PORT_PATTERN_NOT_EMPTY);
        portFormatter.setAllowsInvalid(true);         // allow to enter invalid value for short time
        portFormatter.setCommitsOnValidEdit(true);    // value is immedeatly published to textField
        portFormatter.setOverwriteMode(false);        // do notoverwrite charracters

        jTextFieldPsimulatorPort = new JFormattedTextField(portFormatter);
        jTextFieldPsimulatorPort.setToolTipText(dataLayer.getString("REQUIRED_FORMAT_IS") + " 1-49 999");
        jTextFieldPsimulatorPort.setText(connectionPort);
        addressesPanel.add(new JLayer<>(jTextFieldPsimulatorPort, layerUI));

        JLabel portTip = new JLabel("1-49 999");
        addressesPanel.add(portTip);

        return addressesPanel;
    }

    private JPanel createConnectCancelPanel() {
        JPanel buttonPane = new JPanel();

        jButtonConnectToServer = new JButton(dataLayer.getString("CONNECT_TO_SERVER"));
        jButtonConnectToServer.addActionListener(new JButtonConnectToServerListener());

        jButtonCancel = new JButton(dataLayer.getString("CANCEL"));
        jButtonCancel.addActionListener(new AbstractPropertiesDialog.JButtonCancelListener());

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        buttonPane.add(jButtonConnectToServer);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(jButtonCancel);
        buttonPane.add(Box.createRigidArea(new Dimension(3, 0)));

        return buttonPane;
    }

    private boolean isIpAndPortValid() {
        if (jTextFieldPsimulatorIpAddress.getText().isEmpty() || jTextFieldPsimulatorPort.getText().isEmpty()) {
            return false;
        }

        if (!Pattern.matches(Validator.IP_PATTERN_NOT_EMPTY, jTextFieldPsimulatorIpAddress.getText())) {
            return false;
        }

        if (!Pattern.matches(Validator.PORT_PATTERN_NOT_EMPTY, jTextFieldPsimulatorPort.getText())) {
            return false;
        }

        return true;
    }

    private void enableComponents(boolean enable) {
        jButtonConnectToServer.setEnabled(enable);
        jButtonCancel.setEnabled(enable);

        jTextFieldPsimulatorIpAddress.setEnabled(enable);
        jTextFieldPsimulatorPort.setEnabled(enable);
    }

    //
    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for JComboBoxInterface
     */
    class JButtonConnectToServerListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            copyValuesFromFieldsToLocal();
            if (hasChangesMade()) {
                copyValuesFromLocalToGlobal();
            }

            // if IP and port valid
            if (isIpAndPortValid()) {
                //
                connectingActive = true;

                // disable buttons
                enableComponents(false);

                // start animation
                waitLayerUI.start();

                // try to connect
                dataLayer.getSimulatorManager().doConnect();

            } else {
                // if not valid
                JOptionPane.showMessageDialog(parentComponent,
                        dataLayer.getString("FILL_IN_VALID_IP_AND_PORT"),
                        dataLayer.getString("WARNING"),
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for JComboBoxInterface
     */
    class JButtonCancelListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            closeAction();
        }
    }
}
