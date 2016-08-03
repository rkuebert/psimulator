package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.Dialogs.AbstractPropertiesOkCancelDialog;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Support.Validator;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.InterfacesTable.InterfacesTableModel;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.InterfacesTable.JInterfacesTable;
import shared.Components.EthInterfaceModel;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class HwComponentProperties extends AbstractPropertiesOkCancelDialog {

    private HwComponentGraphic abstractHwComponent;
    private DrawPanelInnerInterface drawPanel;
    /*
     * window componenets
     */
    private JFormattedTextField jTextFieldDeviceName;

    private JButton jButtonAddInterface;
    private JButton jButtonRemoveInterface;
    /*
     * END of window components
     */
    private Font fontBold;
    private boolean showAddresses = true;
    private boolean showInterfaces = true;
    private boolean viewUniqueId = true;
    private boolean allowInterfaceCountChange = true;
    //
    private String deviceName;
    //
    private InterfacesTableModel tableInterfacesModel;
    // 

    public HwComponentProperties(Component mainWindow, DataLayerFacade dataLayer, DrawPanelInnerInterface drawPanel, HwComponentGraphic abstractHwComponent) {
        super(mainWindow, dataLayer);

        this.abstractHwComponent = abstractHwComponent;
        this.drawPanel = drawPanel;

        // set title
        this.setTitle(abstractHwComponent.getDeviceName());

        switch (abstractHwComponent.getHwType()) {
            case END_DEVICE_NOTEBOOK:
            case END_DEVICE_WORKSTATION:
            case END_DEVICE_PC:
            case LINUX_ROUTER:
            case CISCO_ROUTER:
                showAddresses = true;
                showInterfaces = true;
                allowInterfaceCountChange = true;
                break;
            case LINUX_SWITCH:
            case CISCO_SWITCH:
                showAddresses = false;
                showInterfaces = true;
                allowInterfaceCountChange = true;
                break;
            case REAL_PC:
                showAddresses = false;
                showInterfaces = false;
                allowInterfaceCountChange = false;
                break;
            default:
                System.err.println("HwComponentProperties error1");
                break;
        }
        
        // set icon to dialog
        this.setIconImage(ImageFactorySingleton.getInstance().getDialogIconForComponentProperties(abstractHwComponent.getHwType()).getImage());

        
        if(showAddresses){
            // set minimum size
            this.setMinimumSize(new Dimension(550, 300));
            // set preffered size
            this.setPreferredSize(new Dimension(580, 350)); 
        }else{
            // set minimum size
            this.setMinimumSize(new Dimension(450, 300));
            // set preffered size
            this.setPreferredSize(new Dimension(450, 350)); 
        }
        
        // if real pc dialog
        if(!showInterfaces){
            this.setResizable(false);
        }
        
        
        
        //Make textField get the focus whenever frame is activated.
        this.addWindowFocusListener(new WindowAdapter() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
                /*
                if (showAddresses) {
                    jTextFieldIpAddress.requestFocusInWindow();
                }*/

            }
        });
        
        // initialize
        initialize();
        
        // set visible true
        this.setVisible(true);
    }

    /**
     * Copies values that can be modified to local variables.
     */
    @Override
    protected void copyValuesFromGlobalToLocal() {
        // save name
        deviceName = abstractHwComponent.getDeviceName();
    }

    /**
     * Propagates changes in name and eth interfaces addresses to the component.
     */
    @Override
    protected void copyValuesFromLocalToGlobal() {
        // save name
        abstractHwComponent.setDeviceName(deviceName);

        if(showInterfaces){
            // save interface changes
            tableInterfacesModel.copyValuesFromLocalToGlobal();
        }
        
        // fire edit happend on graph
        drawPanel.getGraphOuterInterface().editHappend();
    }

    /**
     * Saves device Name, IP and MAC addresses from text fields to local maps
     */
    @Override
    protected void copyValuesFromFieldsToLocal() {
        deviceName = jTextFieldDeviceName.getText().trim();
    }

    /**
     * Finds out if any changes have been made
     *
     * @return True if changes made, false if not.
     */
    @Override
    protected boolean hasChangesMade() {
        if (!deviceName.equals(abstractHwComponent.getDeviceName())) {
            return true;
        }

        if(showInterfaces && tableInterfacesModel.hasChangesMade()){
            return true;
        }

        return false;
    }

    @Override
    protected JPanel createContentPanel() {
        JPanel mainPanel = new JPanel();
        /*
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(createDevicePanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        */
        
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL; // natural height maximum width

        cons.gridx = 0;
        cons.gridy = 0;
        mainPanel.add(createDevicePanel(), cons);
        
        cons.gridx = 0;
        cons.gridy = 1;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)), cons);

        cons.gridx = 0;
        cons.gridy = 2;
        cons.weighty = 1.0;
        cons.weightx = 1.0;
        cons.fill = GridBagConstraints.BOTH; // both width and height max
        
        if (showInterfaces) {
            mainPanel.add(createInterfaceTablePanel(), cons);
        } else {
            mainPanel.add(createRealPcPanel(), cons);
        }
        /*
         * mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));
         * mainPanel.add(createOkCancelPanel());
         */

        return mainPanel;
    }

    private JPanel createDevicePanel() {
        JPanel devicePanel = new JPanel();
        devicePanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("DEVICE")));

        GridLayout devicePanelLayout = new GridLayout(0, 2);
        devicePanelLayout.setHgap(10);
        devicePanel.setLayout(devicePanelLayout);

        //
        JLabel jLabelName = new JLabel(dataLayer.getString("NAME") + " (1-15):");
        fontBold = new Font(jLabelName.getFont().getName(), Font.BOLD, jLabelName.getFont().getSize());
        jLabelName.setFont(fontBold);
        devicePanel.add(jLabelName);

        RegexFormatter deviceNameFormatter = new RegexFormatter(Validator.NAME_PATTERN_NOT_EMPTY);
        deviceNameFormatter.setAllowsInvalid(true);        // allow to enter invalid value for short time
        deviceNameFormatter.setCommitsOnValidEdit(true);    // value is immedeatly published to textField
        deviceNameFormatter.setOverwriteMode(false);        // do not overwrite charracters


        jTextFieldDeviceName = new JFormattedTextField(deviceNameFormatter);
        jTextFieldDeviceName.setToolTipText(dataLayer.getString("REQUIRED_FORMAT_IS") + " 1-15 " + dataLayer.getString("CHARACTERS"));
        jTextFieldDeviceName.setText(abstractHwComponent.getDeviceName());
        // add decorator that paints wrong input icon
        devicePanel.add(new JLayer<JFormattedTextField>(jTextFieldDeviceName, layerUI));
        //
        JLabel typeName = new JLabel(dataLayer.getString("TYPE") + ":");
        typeName.setFont(fontBold);
        devicePanel.add(typeName);

        JLabel typeValue = new JLabel(abstractHwComponent.getHwType().toString());
        devicePanel.add(typeValue);
        //
        JLabel interfaceCountName = new JLabel(dataLayer.getString("INTERFACE_COUNT"));
        interfaceCountName.setFont(fontBold);
        devicePanel.add(interfaceCountName);

        JLabel interfaceCountValue = new JLabel("" + abstractHwComponent.getInterfaceCount());
        devicePanel.add(interfaceCountValue);
        //
        if (viewUniqueId) {
            JLabel deviceIdName = new JLabel(dataLayer.getString("DEVICE_UNIQUE_ID"));
            deviceIdName.setFont(fontBold);
            devicePanel.add(deviceIdName);

            JLabel deviceIdValue = new JLabel("" + abstractHwComponent.getId().toString());
            devicePanel.add(deviceIdValue);
        }

        return devicePanel;
    }

    private JPanel createInterfaceTablePanel() {
        JPanel interfacesTablePanel = new JPanel(new BorderLayout());
        // create border
        interfacesTablePanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("INTERFACES")));

        // create table model
        tableInterfacesModel = new InterfacesTableModel(abstractHwComponent, dataLayer, showAddresses);
        
        // create table
        JInterfacesTable table = new JInterfacesTable(tableInterfacesModel, abstractHwComponent, dataLayer);
        
        // create scroll pane
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(table);

        /*
        if(showAddresses){
            jScrollPane.setMaximumSize(new Dimension(470,150));
            jScrollPane.setPreferredSize(new Dimension(470,150));
        }else{
            jScrollPane.setMaximumSize(new Dimension(300,150));
            jScrollPane.setPreferredSize(new Dimension(300,150));
            
        }*/
            
        // set scrollbar policies
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        
        // add scroll pane to panel
        interfacesTablePanel.add(jScrollPane, BorderLayout.CENTER);

        return interfacesTablePanel;
    }

    private JPanel createRealPcPanel() {
        JPanel realPcPanel = new JPanel();
        realPcPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("REAL_PC")));

        JLabel realPcLabel = new JLabel(dataLayer.getString("THIS_IS_REAL_PC"));
        realPcLabel.setFont(fontBold);

        realPcPanel.add(realPcLabel);

        return realPcPanel;
    }

    @Override
    public JPanel createOkCancelPanel() {
        JPanel buttonPane = new JPanel();

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        
        if(allowInterfaceCountChange){
            jButtonAddInterface = new JButton(dataLayer.getString("ADD_INTERFACE"));
            jButtonAddInterface.addActionListener(new JButtonAddInterfaceListener());
            jButtonAddInterface.setToolTipText(dataLayer.getString("ADDS_INTERFACE_TO_LIST_END"));
            buttonPane.add(jButtonAddInterface);
            
//            jButtonRemoveInterface = new JButton(dataLayer.getString("REMOVE_INTERFACE"));
//            jButtonRemoveInterface.addActionListener(new JButtonRemoveInterfaceListener());
//            jButtonRemoveInterface.setToolTipText(dataLayer.getString("REMOVES_LAST_INTERFACE_FROM_LIST"));
//            buttonPane.add(jButtonRemoveInterface);
        }
        
        jButtonOk = new JButton(dataLayer.getString("SAVE"));
        jButtonOk.addActionListener(new AbstractPropertiesOkCancelDialog.JButtonOkListener());

        jButtonCancel = new JButton(dataLayer.getString("CANCEL"));
        jButtonCancel.addActionListener(new JButtonCancelListener());

        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(jButtonOk);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(jButtonCancel);
        buttonPane.add(Box.createRigidArea(new Dimension(3, 0)));

        return buttonPane;
    }
    

    @Override
    protected void windowClosing() {
        jButtonCancel.requestFocusInWindow();

        jTextFieldDeviceName.setValue(jTextFieldDeviceName.getValue());
    }

    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for JComboBoxInterface
     */
    class JComboBoxInterfaceListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            copyValuesFromFieldsToLocal();
        }
    }
    
    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for JComboBoxInterface
     */
    class JButtonAddInterfaceListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            EthInterfaceModel ethInterface = dataLayer.getNetworkFacade().createEthInterface(abstractHwComponent.getHwComponentModel(), tableInterfacesModel.getRowCount());
            
            // add interface to table
            tableInterfacesModel.addInterface(ethInterface);
        }
    }
    
    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for JComboBoxInterface
     */
    class JButtonRemoveInterfaceListener implements ActionListener {

        /**
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // get index of interface that should be removed
            int index = tableInterfacesModel.getRowCount()-1;
            // get id of interface
            int id = tableInterfacesModel.getEthInterfaceId(index);
            
            // try to get eth interface from model, it can return null when interface not found
            EthInterfaceModel ethInterface = abstractHwComponent.getHwComponentModel().getEthInterface(id);
            
            boolean hasCable = false;
            
            // if not null, find out if is connected to cable
            if(ethInterface != null){
                hasCable = ethInterface.hasCable();
            }

            if(tableInterfacesModel.getRowCount() <= abstractHwComponent.getHwComponentModel().getMinInterfaceCount()){
                // inform user that no more interfaces can be removed
                showWarningDialog(dataLayer.getString("WARNING"), dataLayer.getString("NO_MORE_INTERFACES_CAN_BE_REMOVED"));
                
                return;
            }
            
            if(hasCable){
                // inform user that remove is not possible when cable connected
                showWarningDialog(dataLayer.getString("WARNING"), dataLayer.getString("INTERFACE_WITH_CABLE_CANT_BE_REMOVED"));
                
                return;
            }
            
            if(ethInterface != null){
                // remove interface from component later
                tableInterfacesModel.removeInterface(ethInterface);
            }else{
                // do not remove from component later, just remove it from added interface, because it was added
                tableInterfacesModel.removeInterface();
            }

            //
            return;
        }
    }
    
    
    private void showWarningDialog(String title, String message) {
        //custom title, warning icon
        JOptionPane.showMessageDialog(this,
                message, title, JOptionPane.WARNING_MESSAGE);
    }
}
