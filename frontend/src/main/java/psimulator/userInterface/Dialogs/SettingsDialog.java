package psimulator.userInterface.Dialogs;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.LevelOfDetailsMode;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.dataLayer.Enums.ViewDetailsType;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.PacketImageType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Support.Validator;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.RegexFormatter;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class SettingsDialog extends AbstractPropertiesOkCancelDialog {

    private Font font;
    /*
     * window componenets
     */
    private JComboBox languageList;
    private JLabel iconSizePicture;
    private JRadioButton tinyToolbarIconButton;
    private JRadioButton smallToolbarIconButton;
    private JRadioButton mediumToolbarIconButton;
    private JRadioButton largeToolbarIconButton;
    private JCheckBox jCheckBoxDeviceType;
    private JCheckBox jCheckBoxDeviceName;
    private JCheckBox jCheckBoxInterfaceName;
    private JCheckBox jCheckBoxCableDelay;
    private JCheckBox jCheckBoxIpAddresses;
    private JCheckBox jCheckBoxMacAddresses;
    private JRadioButton jRadioButtonManualLOD;
    private JRadioButton jRadioButtonAutoLOD;
    //
    private JCheckBox jCheckBoxNetworkBounds;
    
    //
    private JRadioButton envelopePacketIconButton;
    private JRadioButton classicPacketIconButton;
    private JRadioButton carPacketIconButton;
    private JLabel packetImageTypePicture;
    private JFormattedTextField jTextFieldPsimulatorIpAddress;
    private JFormattedTextField jTextFieldPsimulatorPort;
    //
    private RegexFormatter ipMaskFormatter;
    private RegexFormatter portFormatter;
    /*
     * END of window components
     */
    /*
     * variables for local store
     */
    private ToolbarIconSizeEnum toolbarIconSize;
    private PacketImageType packetImageType;
    private int currentLanguagePosition;
    //
    private boolean viewDeviceNames;
    private boolean viewDeviceTypes;
    private boolean viewInterfaceNames;
    private boolean viewCableDelay;
    private boolean viewIpAddresses;
    private boolean viewMacAddresses;
    //
    private boolean viewNetworkBounds;
    //
    private LevelOfDetailsMode levelOfDetails;
    //
    private String connectionIpAddress;
    private String connectionPort;
    /*
     * END variables for local store
     */

    public SettingsDialog(Component mainWindow, DataLayerFacade dataLayer) {
        super(mainWindow, dataLayer);

        // set icon to dialog
        this.setIconImage(ImageFactorySingleton.getInstance().getImageIcon("/resources/toolbarIcons/32/configure.png").getImage());
        
        // set title
        this.setTitle(dataLayer.getString("PREFERENCES"));

        // set minimum size
        this.setMinimumSize(new Dimension(150, 150));

        // initialize
        initialize();
        
        // update swing components
        setElementsAccordingToLocal();

        // set visible
        this.setVisible(true);
    }

    @Override
    protected void copyValuesFromGlobalToLocal() {
        currentLanguagePosition = dataLayer.getCurrentLanguagePosition();
        toolbarIconSize = dataLayer.getToolbarIconSize();
        packetImageType = dataLayer.getPackageImageType();
        //
        viewDeviceNames = dataLayer.isViewDetails(ViewDetailsType.DEVICE_NAMES);
        viewDeviceTypes = dataLayer.isViewDetails(ViewDetailsType.DEVICE_TYPES);
        viewInterfaceNames = dataLayer.isViewDetails(ViewDetailsType.INTERFACE_NAMES);
        viewCableDelay = dataLayer.isViewDetails(ViewDetailsType.CABLE_DELAYS);
        viewIpAddresses = dataLayer.isViewDetails(ViewDetailsType.IP_ADDRESS);
        viewMacAddresses = dataLayer.isViewDetails(ViewDetailsType.MAC_ADDRESS);
        viewNetworkBounds = dataLayer.isViewDetails(ViewDetailsType.NETWORK_BOUNDS);
        //
        levelOfDetails = dataLayer.getLevelOfDetails();
        //
        connectionIpAddress = dataLayer.getConnectionIpAddress();
        connectionPort = dataLayer.getConnectionPort();
    }

    @Override
    protected void copyValuesFromFieldsToLocal() {
        currentLanguagePosition = languageList.getSelectedIndex();
        //
        viewDeviceNames = jCheckBoxDeviceName.isSelected();
        viewDeviceTypes = jCheckBoxDeviceType.isSelected();
        viewInterfaceNames = jCheckBoxInterfaceName.isSelected();
        viewCableDelay = jCheckBoxCableDelay.isSelected();
        viewIpAddresses = jCheckBoxIpAddresses.isSelected();
        viewMacAddresses = jCheckBoxMacAddresses.isSelected();
        viewNetworkBounds = jCheckBoxNetworkBounds.isSelected();
        //
        
        connectionIpAddress = jTextFieldPsimulatorIpAddress.getText();
        connectionPort = jTextFieldPsimulatorPort.getText();
    }

    @Override
    protected void copyValuesFromLocalToGlobal() {
        dataLayer.setCurrentLanguage(currentLanguagePosition);
        dataLayer.setToolbarIconSize(toolbarIconSize);
        dataLayer.setPackageImageType(packetImageType);
        //
        dataLayer.setViewDetails(ViewDetailsType.DEVICE_NAMES, viewDeviceNames);
        dataLayer.setViewDetails(ViewDetailsType.DEVICE_TYPES, viewDeviceTypes);
        dataLayer.setViewDetails(ViewDetailsType.INTERFACE_NAMES, viewInterfaceNames);
        dataLayer.setViewDetails(ViewDetailsType.CABLE_DELAYS, viewCableDelay);
        dataLayer.setViewDetails(ViewDetailsType.IP_ADDRESS, viewIpAddresses);
        dataLayer.setViewDetails(ViewDetailsType.MAC_ADDRESS, viewMacAddresses);
        dataLayer.setViewDetails(ViewDetailsType.NETWORK_BOUNDS, viewNetworkBounds);
        //
        dataLayer.setLevelOfDetails(levelOfDetails);
        //
        dataLayer.setConnectionIpAddress(connectionIpAddress);
        dataLayer.setConnectionPort(connectionPort);
        
        // save preferences 
        dataLayer.savePreferences();
    }

    @Override
    protected boolean hasChangesMade() {
        if (currentLanguagePosition != dataLayer.getCurrentLanguagePosition()) {
            return true;
        }

        if (toolbarIconSize != dataLayer.getToolbarIconSize()) {
            return true;
        }

        if (packetImageType != dataLayer.getPackageImageType()) {
            return true;
        }

        if (viewDeviceNames != dataLayer.isViewDetails(ViewDetailsType.DEVICE_NAMES)) {
            return true;
        }

        if (viewDeviceTypes != dataLayer.isViewDetails(ViewDetailsType.DEVICE_TYPES)) {
            return true;
        }

        if (viewInterfaceNames != dataLayer.isViewDetails(ViewDetailsType.INTERFACE_NAMES)) {
            return true;
        }

        if (viewCableDelay != dataLayer.isViewDetails(ViewDetailsType.CABLE_DELAYS)) {
            return true;
        }

        if (viewIpAddresses != dataLayer.isViewDetails(ViewDetailsType.IP_ADDRESS)) {
            return true;
        }

        if (viewMacAddresses != dataLayer.isViewDetails(ViewDetailsType.MAC_ADDRESS)) {
            return true;
        }
        
        if (viewNetworkBounds != dataLayer.isViewDetails(ViewDetailsType.NETWORK_BOUNDS)) {
            return true;
        }

        if (levelOfDetails != dataLayer.getLevelOfDetails()) {
            return true;
        }
        
        if(!connectionIpAddress.equals(dataLayer.getConnectionIpAddress())){
            return true;
        }
        
        if(!connectionPort.equals(dataLayer.getConnectionPort())){
            return true;
        }

        return false;
    }
    
    
    @Override
    protected void windowClosing() {
        jButtonCancel.requestFocusInWindow();

        jTextFieldPsimulatorIpAddress.setValue(jTextFieldPsimulatorIpAddress.getValue());
        jTextFieldPsimulatorPort.setValue(jTextFieldPsimulatorPort.getValue());
    }

    private void setElementsAccordingToLocal() {
        // set selected language
        languageList.setSelectedIndex(currentLanguagePosition);
        // set image like in preferences
        setIconSize();
        // set packet icon size like in preferences
        setPacketIconType();
        //
        setLevelOfDetails();
    }

    @Override
    protected JPanel createContentPanel() {
        JPanel mainPanel = new JPanel();

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab(dataLayer.getString("GENERAL"), new ImageIcon(getClass().getResource("/resources/toolbarIcons/32/home.png")), createCardGeneral());

        tabbedPane.addTab(dataLayer.getString("SIMULATOR"), new ImageIcon(getClass().getResource("/resources/toolbarIcons/32/exec.png")), createCardSimulator());

        tabbedPane.addTab(dataLayer.getString("EDITOR"), new ImageIcon(getClass().getResource("/resources/toolbarIcons/32/editor.png")), createCardEditor());
        
        mainPanel.add(tabbedPane);

        return mainPanel;
    }

    private JPanel createCardGeneral() {
        JPanel card = new JPanel();

        //card.setLayout(new BorderLayout());
        card.setLayout(new BoxLayout(card, BoxLayout.PAGE_AXIS));

        card.add(createApplicationPanel());//, BorderLayout.PAGE_START);

        card.add(Box.createRigidArea(new Dimension(0, 6)));

        card.add(createViewedDetailsPanel());

        return card;
    }

    private JPanel createViewedDetailsPanel() {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("VIEWED_DETAILS")));
        //
        JPanel autoManualPanel = new JPanel();
        autoManualPanel.setLayout(new GridLayout(0, 2));

        ButtonGroup buttonGroup = new ButtonGroup();

        ActionListener lodListener = new LevelOfDetailsListener();



        jRadioButtonManualLOD = new JRadioButton(dataLayer.getString("MANUAL_LEVEL_OF_DETAILS"));
        jRadioButtonManualLOD.setActionCommand(LevelOfDetailsMode.MANUAL.toString());
        jRadioButtonManualLOD.addActionListener(lodListener);
        buttonGroup.add(jRadioButtonManualLOD);
        autoManualPanel.add(jRadioButtonManualLOD);

        jRadioButtonAutoLOD = new JRadioButton(dataLayer.getString("AUTO_LEVEL_OF_DETAILS"));
        jRadioButtonAutoLOD.setActionCommand(LevelOfDetailsMode.AUTO.toString());
        jRadioButtonAutoLOD.addActionListener(lodListener);
        buttonGroup.add(jRadioButtonAutoLOD);
        autoManualPanel.add(jRadioButtonAutoLOD);

        switch (levelOfDetails) {
            case AUTO:
                jRadioButtonAutoLOD.setSelected(true);
                break;
            case MANUAL:
                jRadioButtonManualLOD.setSelected(true);
                break;
        }

        //
        JPanel checkBoxesPanel = new JPanel();
        checkBoxesPanel.setLayout(new GridLayout(0, 2));

        jCheckBoxDeviceType = new JCheckBox(dataLayer.getString("TYPES_OF_DEVICES"));
        jCheckBoxDeviceType.setAlignmentX(Component.LEFT_ALIGNMENT);
        jCheckBoxDeviceType.setSelected(viewDeviceTypes);
        checkBoxesPanel.add(jCheckBoxDeviceType);

        jCheckBoxDeviceName = new JCheckBox(dataLayer.getString("NAMES_OF_DEVICES"));
        jCheckBoxDeviceName.setAlignmentX(Component.LEFT_ALIGNMENT);
        jCheckBoxDeviceName.setSelected(viewDeviceNames);
        checkBoxesPanel.add(jCheckBoxDeviceName);

        jCheckBoxInterfaceName = new JCheckBox(dataLayer.getString("INTERFACE_NAMES"));
        jCheckBoxInterfaceName.setAlignmentX(Component.LEFT_ALIGNMENT);
        jCheckBoxInterfaceName.setSelected(viewInterfaceNames);
        checkBoxesPanel.add(jCheckBoxInterfaceName);

        jCheckBoxCableDelay = new JCheckBox(dataLayer.getString("CABLE_DELAY"));
        jCheckBoxCableDelay.setAlignmentX(Component.LEFT_ALIGNMENT);
        jCheckBoxCableDelay.setSelected(viewCableDelay);
        checkBoxesPanel.add(jCheckBoxCableDelay);

        jCheckBoxIpAddresses = new JCheckBox(dataLayer.getString("IP_ADDRESSES"));
        jCheckBoxIpAddresses.setAlignmentX(Component.LEFT_ALIGNMENT);
        jCheckBoxIpAddresses.setSelected(viewIpAddresses);
        checkBoxesPanel.add(jCheckBoxIpAddresses);

        jCheckBoxMacAddresses = new JCheckBox(dataLayer.getString("MAC_ADDRESSES"));
        jCheckBoxMacAddresses.setAlignmentX(Component.LEFT_ALIGNMENT);
        jCheckBoxMacAddresses.setSelected(viewMacAddresses);
        checkBoxesPanel.add(jCheckBoxMacAddresses);

        //
        detailsPanel.add(autoManualPanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailsPanel.add(new JSeparator());
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailsPanel.add(checkBoxesPanel);


        return detailsPanel;
    }

    private JPanel createApplicationPanel() {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        // APPLICATION PANEL
        JPanel applicationPanel = new JPanel();
        applicationPanel.setLayout(new BoxLayout(applicationPanel, BoxLayout.Y_AXIS));
        applicationPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("APPLICATION")));

        // LANGUAGE
        JPanel languagePanel = new JPanel();

        languagePanel.setLayout(new BoxLayout(languagePanel, BoxLayout.X_AXIS));

        JLabel languageLabel = new JLabel(dataLayer.getString("LANGUAGE"));
        font = new Font(languageLabel.getFont().getName(), Font.BOLD, languageLabel.getFont().getSize());
        languageLabel.setFont(font);

        languageList = new JComboBox(dataLayer.getAvaiableLanguageNames());
        languageList.setBackground(Color.white);

        languagePanel.add(languageLabel);
        languagePanel.add(Box.createRigidArea(new Dimension(5, 0)));
        languagePanel.add(languageList);

        applicationPanel.add(languagePanel);
        applicationPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // TOOLBAR ICON SIZE
        JPanel iconSizePanel = new JPanel();
        iconSizePanel.setLayout(new BoxLayout(iconSizePanel, BoxLayout.X_AXIS));

        JLabel iconSizeLabel = new JLabel(dataLayer.getString("TOOLBAR_ICON_SIZE"));
        iconSizeLabel.setFont(font);

        iconSizePanel.add(iconSizeLabel);
        iconSizePanel.add(Box.createRigidArea(new Dimension(5, 0)));

        iconSizePanel.add(createIconSizePanel());


        applicationPanel.add(iconSizePanel);


        // END APPLICATION PANEL
        pane.add(applicationPanel);

        return pane;
    }

    private JPanel createIconSizePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        ButtonGroup buttonGroup = new ButtonGroup();
        ActionListener toolbarIconSizeListener = new IconSizeListener();

        tinyToolbarIconButton = new JRadioButton(dataLayer.getString("TINY"));
        tinyToolbarIconButton.setActionCommand(ToolbarIconSizeEnum.TINY.toString());
        tinyToolbarIconButton.addActionListener(toolbarIconSizeListener);

        smallToolbarIconButton = new JRadioButton(dataLayer.getString("SMALL"));
        smallToolbarIconButton.setActionCommand(ToolbarIconSizeEnum.SMALL.toString());
        smallToolbarIconButton.addActionListener(toolbarIconSizeListener);

        mediumToolbarIconButton = new JRadioButton(dataLayer.getString("MEDIUM"));
        mediumToolbarIconButton.setActionCommand(ToolbarIconSizeEnum.MEDIUM.toString());
        mediumToolbarIconButton.addActionListener(toolbarIconSizeListener);

        largeToolbarIconButton = new JRadioButton(dataLayer.getString("LARGE"));
        largeToolbarIconButton.setActionCommand(ToolbarIconSizeEnum.LARGE.toString());
        largeToolbarIconButton.addActionListener(toolbarIconSizeListener);

        buttonGroup.add(tinyToolbarIconButton);
        buttonGroup.add(smallToolbarIconButton);
        buttonGroup.add(mediumToolbarIconButton);
        buttonGroup.add(largeToolbarIconButton);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
        buttonPanel.add(tinyToolbarIconButton);
        buttonPanel.add(smallToolbarIconButton);
        buttonPanel.add(mediumToolbarIconButton);
        buttonPanel.add(largeToolbarIconButton);

        panel.add(buttonPanel);

        iconSizePicture = new JLabel();
        iconSizePicture.setPreferredSize(new Dimension(48, 48));

        panel.add(iconSizePicture);

        return panel;
    }

    private JPanel createCardSimulator() {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        // add packet icon change panel
        pane.add(createPacketIconPanel());

        pane.add(Box.createRigidArea(new Dimension(0, 6)));
        
        // add Connection properties panel
        pane.add(createConnectionPropertiesPanel());

        pane.add(Box.createRigidArea(new Dimension(0, 130)));
        
        return pane;
    }

    private JPanel createPacketIconPanel() {
        // PACKET ICON TYPE panel
        JPanel packetImageTypePanel = new JPanel();
        packetImageTypePanel.setLayout(new BoxLayout(packetImageTypePanel, BoxLayout.X_AXIS));
        packetImageTypePanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("PACKET_IMAGE_TYPE")));

        JLabel iconSizeLabel = new JLabel(dataLayer.getString("PACKET_IMAGE_TYPE"));
        iconSizeLabel.setFont(font);
        
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        ButtonGroup buttonGroup = new ButtonGroup();
        ActionListener packetImageTypeListener = new PacketImageTypeListener();

        envelopePacketIconButton = new JRadioButton(dataLayer.getString("ENVELOPE"));
        envelopePacketIconButton.setActionCommand(PacketImageType.ENVELOPE.toString());
        envelopePacketIconButton.addActionListener(packetImageTypeListener);
        
        classicPacketIconButton = new JRadioButton(dataLayer.getString("PACKAGE"));
        classicPacketIconButton.setActionCommand(PacketImageType.CLASSIC.toString());
        classicPacketIconButton.addActionListener(packetImageTypeListener);

        carPacketIconButton = new JRadioButton(dataLayer.getString("CARS"));
        carPacketIconButton.setActionCommand(PacketImageType.CAR.toString());
        carPacketIconButton.addActionListener(packetImageTypeListener);

        buttonGroup.add(envelopePacketIconButton);
        buttonGroup.add(classicPacketIconButton);
        buttonGroup.add(carPacketIconButton);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
        buttonPanel.add(envelopePacketIconButton);
        buttonPanel.add(classicPacketIconButton);
        buttonPanel.add(carPacketIconButton);

        panel.add(buttonPanel);

        packetImageTypePicture = new JLabel();
        packetImageTypePicture.setPreferredSize(new Dimension(48, 48));

        panel.add(packetImageTypePicture);

        packetImageTypePanel.add(iconSizeLabel);
        packetImageTypePanel.add(Box.createRigidArea(new Dimension(5, 0)));
        packetImageTypePanel.add(panel);
        
        return packetImageTypePanel;
    }

    private JPanel createConnectionPropertiesPanel() {
        JPanel addressesPanel = new JPanel();
        addressesPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("CONNECTION_PROPERTIES")));
        // set layout
        GridLayout addressesPanelLayout = new GridLayout(0, 3);
        addressesPanelLayout.setHgap(10);
        addressesPanel.setLayout(addressesPanelLayout);

        // IP address
        JLabel ipAddressName = new JLabel(dataLayer.getString("IP_ADDRESS"));
        ipAddressName.setFont(font);
        addressesPanel.add(ipAddressName);

        ipMaskFormatter = new RegexFormatter(Validator.IP_PATTERN);
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
        portName.setFont(font);
        addressesPanel.add(portName);
        
        portFormatter = new RegexFormatter(Validator.PORT_PATTERN);
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

    private JPanel createCardEditor(){
        JPanel card = new JPanel();

        card.add(createNetworkBoundsPanel());
        
        card.setLayout(new BoxLayout(card, BoxLayout.PAGE_AXIS));

        card.add(Box.createRigidArea(new Dimension(0, 250)));

        return card;
    }
    
    private JPanel createNetworkBoundsPanel(){
        JPanel displayPanel = new JPanel();
        displayPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("DETAILS")));
        
        // set layout
        GridLayout displayPanelLayout = new GridLayout(0, 2);
        displayPanelLayout.setHgap(10);
        displayPanel.setLayout(displayPanelLayout);
        
        // network bounds
        jCheckBoxNetworkBounds = new JCheckBox(dataLayer.getString("NETWORK_BOUNDS"));
        jCheckBoxNetworkBounds.setAlignmentX(Component.LEFT_ALIGNMENT);
        jCheckBoxNetworkBounds.setSelected(viewNetworkBounds);
        displayPanel.add(jCheckBoxNetworkBounds);
        
        return displayPanel;
    }
    
    private void setIconSize() {
        // set icon size
        switch (toolbarIconSize) {
            case TINY:
                tinyToolbarIconButton.setSelected(true);
                iconSizePicture.setIcon(new ImageIcon(getClass().getResource("/resources/toolbarIcons/16/home.png")));
                break;
            case SMALL:
                smallToolbarIconButton.setSelected(true);
                iconSizePicture.setIcon(new ImageIcon(getClass().getResource("/resources/toolbarIcons/22/home.png")));
                break;
            case MEDIUM:
                mediumToolbarIconButton.setSelected(true);
                iconSizePicture.setIcon(new ImageIcon(getClass().getResource("/resources/toolbarIcons/32/home.png")));
                break;
            case LARGE:
                largeToolbarIconButton.setSelected(true);
                iconSizePicture.setIcon(new ImageIcon(getClass().getResource("/resources/toolbarIcons/48/home.png")));
                break;
        }
    }

    private void setPacketIconType() {
        switch (packetImageType) {
            case ENVELOPE:
                envelopePacketIconButton.setSelected(true);
                packetImageTypePicture.setIcon(new ImageIcon(getClass().getResource("/resources/toolbarIcons/simulator/packages/envelope_pink_48.png")));
                break;
            case CLASSIC:
                classicPacketIconButton.setSelected(true);
                packetImageTypePicture.setIcon(new ImageIcon(getClass().getResource("/resources/toolbarIcons/simulator/packages/package_pink_48.png")));
                break;
            case CAR:
                carPacketIconButton.setSelected(true);
                packetImageTypePicture.setIcon(new ImageIcon(getClass().getResource("/resources/toolbarIcons/simulator/packages/delivery_truck_pink_48.png")));
                break;
        }
    }

    private void setLevelOfDetails() {
        switch (levelOfDetails) {
            case MANUAL:
                jCheckBoxDeviceName.setEnabled(true);
                jCheckBoxDeviceType.setEnabled(true);
                jCheckBoxInterfaceName.setEnabled(true);
                jCheckBoxCableDelay.setEnabled(true);
                jCheckBoxIpAddresses.setEnabled(true);
                jCheckBoxMacAddresses.setEnabled(true);
                break;
            case AUTO:
                jCheckBoxDeviceName.setEnabled(false);
                jCheckBoxDeviceType.setEnabled(false);
                jCheckBoxInterfaceName.setEnabled(false);
                jCheckBoxCableDelay.setEnabled(false);
                jCheckBoxIpAddresses.setEnabled(false);
                jCheckBoxMacAddresses.setEnabled(false);
                break;
        }
    }

    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for ToolbarIconSize
     */
    class LevelOfDetailsListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            levelOfDetails = LevelOfDetailsMode.valueOf(e.getActionCommand());
            setLevelOfDetails();
        }
    }

    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for ToolbarIconSize
     */
    class IconSizeListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            toolbarIconSize = ToolbarIconSizeEnum.valueOf(e.getActionCommand());
            setIconSize();
        }
    }

    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for ToolbarIconSize
     */
    class PacketImageTypeListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            packetImageType = PacketImageType.valueOf(e.getActionCommand());
            setPacketIconType();
        }
    }
}
