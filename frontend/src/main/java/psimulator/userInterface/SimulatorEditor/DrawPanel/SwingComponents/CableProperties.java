package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.Dialogs.AbstractPropertiesOkCancelDialog;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.CableGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Support.Validator;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class CableProperties extends AbstractPropertiesOkCancelDialog {

    private CableGraphic cable;
    private DrawPanelInnerInterface drawPanel;
    /*
     * window componenets
     */
    private JFormattedTextField jTextFieldDelay;
    /*
     * END of window components
     */
    private Font fontBold;
    private boolean viewUniqueId = true;
    //
    private int delay;

    public CableProperties(Component mainWindow, DataLayerFacade dataLayer, DrawPanelInnerInterface drawPanel, CableGraphic cable) {
        super(mainWindow, dataLayer);
        
        this.drawPanel = drawPanel;
        this.cable = cable;

        // set title
        this.setTitle(dataLayer.getString("CABLE_PROPERTIES"));

        
        // set icon to dialog
        this.setIconImage(ImageFactorySingleton.getInstance().getDialogIconForComponentProperties(cable.getHwType()).getImage());
        
        // set minimum size
        this.setMinimumSize(new Dimension(300, 100));

        // initialize
        initialize();
        
        //
        this.setResizable(false);
        
        // set visible true
        this.setVisible(true);
    }

    @Override
    protected void copyValuesFromGlobalToLocal() {
        // save delay
        this.delay = cable.getDelay();
    }

    @Override
    protected void copyValuesFromLocalToGlobal() {
        // save delay to global
        cable.setDelay(delay);
        
        // fire edit happend on graph
        drawPanel.getGraphOuterInterface().editHappend();
    }

    @Override
    protected void copyValuesFromFieldsToLocal() {
        // get delay from text field
        try {
            this.delay = Integer.parseInt(jTextFieldDelay.getText());
        } catch (NumberFormatException ex) {
        }
    }

    @Override
    protected boolean hasChangesMade() {
        if (this.delay != cable.getDelay()) {
            return true;
        }

        return false;
    }

    @Override
    protected JPanel createContentPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(createInfoPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        mainPanel.add(createParametersPanel());

        return mainPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("INFO")));

        GridLayout infoPanelLayout = new GridLayout(0, 2);
        infoPanelLayout.setHgap(10);
        infoPanelLayout.setVgap(5);
        infoPanel.setLayout(infoPanelLayout);
        //
        // -- TYPE -------------------------------------------------------------
        JLabel typeName = new JLabel(dataLayer.getString("TYPE") + ":");
        fontBold = new Font(typeName.getFont().getName(), Font.BOLD, typeName.getFont().getSize());
        typeName.setFont(fontBold);
        infoPanel.add(typeName);

        JLabel typeValue = new JLabel(cable.getHwType().toString());
        infoPanel.add(typeValue);
        //
        // -- DEVICE 1 name and interface --------------------------------------
        JLabel device1Name = new JLabel(dataLayer.getString("COMPONENT") + " 1:");
        device1Name.setFont(fontBold);
        infoPanel.add(device1Name);

        JLabel device1Value = new JLabel(cable.getComponent1().getDeviceName());
        infoPanel.add(device1Value);
        //
        JLabel interface1Name = new JLabel(dataLayer.getString("INTERFACE") + " 1:");
        interface1Name.setFont(fontBold);
        infoPanel.add(interface1Name);

        JLabel interface1Value = new JLabel(cable.getEth1().getName());
        infoPanel.add(interface1Value);
        //
        // -- DEVICE 2 name and interface --------------------------------------
        JLabel device2Name = new JLabel(dataLayer.getString("COMPONENT") + " 2:");
        device2Name.setFont(fontBold);
        infoPanel.add(device2Name);

        JLabel device2Value = new JLabel(cable.getComponent2().getDeviceName());
        infoPanel.add(device2Value);
        //
        JLabel interface2Name = new JLabel(dataLayer.getString("INTERFACE") + " 2:");
        interface2Name.setFont(fontBold);
        infoPanel.add(interface2Name);

        JLabel interface2Value = new JLabel(cable.getEth2().getName());
        infoPanel.add(interface2Value);
        //
        // -- UNIQUE ID --------------------------------------------------------
        if (viewUniqueId) {
            JLabel deviceIdName = new JLabel(dataLayer.getString("DEVICE_UNIQUE_ID") + ":");
            deviceIdName.setFont(fontBold);
            infoPanel.add(deviceIdName);

            JLabel deviceIdValue = new JLabel("" + cable.getId().toString());
            infoPanel.add(deviceIdValue);
        }

        return infoPanel;
    }

    private JPanel createParametersPanel() {
        JPanel parametersPanel = new JPanel();
        parametersPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("PARAMETERS")));

        GridLayout parametersPanelLayout = new GridLayout(0, 3);
        parametersPanelLayout.setHgap(10);
        parametersPanelLayout.setVgap(5);
        parametersPanel.setLayout(parametersPanelLayout);
        //
        // -- DELAY ------------------------------------------------------------
        JLabel delayName = new JLabel(dataLayer.getString("DELAY") + ":");
        delayName.setFont(fontBold);
        parametersPanel.add(delayName);

        // create formatter
        RegexFormatter delayFormatter = new RegexFormatter(Validator.DELAY_PATTERN);
        delayFormatter.setAllowsInvalid(true);        // allow to enter invalid value for short time
        delayFormatter.setCommitsOnValidEdit(true);    // value is immedeatly published to textField
        delayFormatter.setOverwriteMode(false);        // do not overwrite charracters

        jTextFieldDelay = new JFormattedTextField(delayFormatter);
        jTextFieldDelay.setText("" + cable.getDelay());
        jTextFieldDelay.setToolTipText(dataLayer.getString("REQUIRED_FORMAT_IS") + " 1-99");
        // add decorator that paints wrong input icon
        parametersPanel.add(new JLayer<JFormattedTextField>(jTextFieldDelay, layerUI));

        JLabel delayTip = new JLabel("1-99");
        parametersPanel.add(delayTip);

        // --  ------------------------------------------------------------
        return parametersPanel;
    }

    @Override
    protected void windowClosing() {
        jButtonCancel.requestFocusInWindow();

        jTextFieldDelay.setValue(jTextFieldDelay.getValue());
    }


}
