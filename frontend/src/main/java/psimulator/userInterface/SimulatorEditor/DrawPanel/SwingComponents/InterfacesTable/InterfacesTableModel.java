package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.InterfacesTable;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import shared.Components.EthInterfaceModel;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class InterfacesTableModel extends AbstractTableModel {

    private HwComponentGraphic abstractHwComponent;
    private DataLayerFacade dataLayer;
    //
    private boolean showAddresses;
    //
    private String[] columnNames;
    private Object[][] data;// = ...//same as before...
    //
    private List<EthInterfaceModel> addedInterfaces = new ArrayList<>();
    private List<EthInterfaceModel> removedInterfaces = new ArrayList<>();

    public InterfacesTableModel(HwComponentGraphic abstractHwComponent, DataLayerFacade dataLayer, boolean showAddresses) {
        this.dataLayer = dataLayer;
        this.abstractHwComponent = abstractHwComponent;
        this.showAddresses = showAddresses;

        if (showAddresses) {
            String[] names = {dataLayer.getString("INTERFACE"), dataLayer.getString("CONNECTED"),
                dataLayer.getString("CONNECTED_TO"), dataLayer.getString("IS_UP"),
                dataLayer.getString("IP_ADDRESS_MASK"), dataLayer.getString("MAC_ADDRESS")};
            columnNames = names;
        } else {
            String[] names = {dataLayer.getString("INTERFACE"), dataLayer.getString("CONNECTED"),
                dataLayer.getString("CONNECTED_TO")};
            columnNames = names;
        }

        //

        int interfacesCount = abstractHwComponent.getInterfaceCount();

        data = new Object[interfacesCount][columnNames.length + 1];   // extra one for the ID

        for (int i = 0; i < interfacesCount; i++) {
            EthInterfaceModel ethInterface = abstractHwComponent.getEthInterfaceAtIndex(i);

            addInterfaceToArray(data, i, ethInterface);
        }
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col < 3) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void addInterface(EthInterfaceModel ethInterface) {
        addedInterfaces.add(ethInterface);

        // copy array to +1 row bigger one
        data = copyRowsToNewArray(data, data.length + 1, data.length);

        // ADD NEW INTERFACE TO DATA ARRAY 
        int i = data.length - 1;

        // add interface to array
        addInterfaceToArray(data, i, ethInterface);

        // fire about change
        fireTableRowsInserted(i, i);
    }

    /**
     * Use when removing real interface, that was already registered in model
     * (in component)
     *
     * @param ethInterface
     */
    public void removeInterface(EthInterfaceModel ethInterface) {
        removedInterfaces.add(ethInterface);

        // copy array to -1 row bigger one
        data = copyRowsToNewArray(data, data.length - 1, data.length - 1);

        // cancel removed rows in added rows

        // fire about change
        fireTableRowsDeleted(data.length - 1, data.length - 1);
    }

    /**
     * Use when removing added interface, than the last one is removed
     */
    public void removeInterface() {
        // remove from added interfaces
        addedInterfaces.remove(addedInterfaces.size() - 1);

        // copy array to -1 row bigger one
        data = copyRowsToNewArray(data, data.length - 1, data.length - 1);

        // fire about change
        fireTableRowsDeleted(data.length - 1, data.length - 1);
    }

    /**
     * Gets ID of ethInterface at row position
     *
     * @param row
     * @return
     */
    public int getEthInterfaceId(int row) {
        return (Integer) (data[row][columnNames.length]);
    }

    public boolean hasChangesMade() {
        if (!addedInterfaces.isEmpty()) {
            return true;
        }

        if (!removedInterfaces.isEmpty()) {
            return true;
        }

        if (showAddresses) {
            for (int i = 0; i < getRowCount(); i++) {
                // if IS UP is different
                if (!abstractHwComponent.getInterfaces().get(i).isIsUp() == (boolean) getValueAt(i, 3)) {
                    return true;
                }

                // if IP address is different
                if (!abstractHwComponent.getInterfaces().get(i).getIpAddress().equals(getValueAt(i, 4))) {
                    return true;
                }

                // if MAC address is different
                if (!abstractHwComponent.getInterfaces().get(i).getMacAddress().equals(getValueAt(i, 5))) {
                    return true;
                }

            }
        }
        return false;
    }

    public void copyValuesFromLocalToGlobal() {
        // add added interfaces to model
        if (!addedInterfaces.isEmpty()) {
            for (EthInterfaceModel ethInterface : addedInterfaces) {
                abstractHwComponent.addInterface(ethInterface);
            }
        }

        // remove removed interfaces from model
        if (!removedInterfaces.isEmpty()) {
            for (EthInterfaceModel ethInterface : removedInterfaces) {
                abstractHwComponent.removeInterface(ethInterface);
            }
        }

        if (showAddresses) {
            for (int i = 0; i < getRowCount(); i++) {
                // save IS UP
                abstractHwComponent.getInterfaces().get(i).setIsUp((boolean) getValueAt(i, 3));

                // save IP
                abstractHwComponent.getInterfaces().get(i).setIpAddress(getValueAt(i, 4).toString());

                // save MAC
                String mac = getValueAt(i, 5).toString();
                mac = mac.replaceAll(":", "-");

                abstractHwComponent.getInterfaces().get(i).setMacAddress(mac);
            }
        }
    }

    private void addInterfaceToArray(Object[][] array, int i, EthInterfaceModel ethInterface) {
        // fill interface names
        array[i][0] = ethInterface.getName();

        // fill connected status
        array[i][1] = new Boolean(ethInterface.hasCable());

        // fill connected to
        if (ethInterface.hasCable()) {
            if (ethInterface.getCable().getComponent1().getId().intValue() != abstractHwComponent.getId().intValue()) {
                // set name from component1
                array[i][2] = ethInterface.getCable().getComponent1().getName();
            } else {
                // set name from component2
                array[i][2] = ethInterface.getCable().getComponent2().getName();
            }
        } else {
            array[i][2] = "";
        }


        if (showAddresses) {
            //
            // fill IS UP
            array[i][3] = new Boolean(ethInterface.isIsUp());

            // fill IP addresses
            array[i][4] = ethInterface.getIpAddress();

            // fill MAC addresses
            array[i][5] = ethInterface.getMacAddress();

            // fill ID
        }

        // fill ID in the last slot
        array[i][columnNames.length] = ethInterface.getId().intValue();


    }

    private Object[][] copyRowsToNewArray(Object[][] old, int rowsNumberOfNewArray, int rowsNumberToCopy) {
        // create array
        Object[][] tmpData = new Object[rowsNumberOfNewArray][old[0].length];


        for (int i = 0; i < rowsNumberToCopy; i++) {
            System.arraycopy(old[i], 0, tmpData[i], 0, old[i].length);
        }

        return tmpData;
    }
}
