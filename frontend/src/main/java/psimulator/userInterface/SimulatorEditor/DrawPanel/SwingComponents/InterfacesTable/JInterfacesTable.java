package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.InterfacesTable;

import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class JInterfacesTable extends JTable {

    private HwComponentGraphic abstractHwComponent;
    private DataLayerFacade dataLayer;
    //
    public JInterfacesTable(TableModel tableModel, HwComponentGraphic abstractHwComponent, DataLayerFacade dataLayer) {
        super(tableModel);

        this.abstractHwComponent = abstractHwComponent;
        this.dataLayer = dataLayer;

        // set custom cell editors
        TableColumnModel m = this.getColumnModel();
        
        // set custom cell renderer 
        this.getColumnModel().getColumn(0).setCellRenderer(new InterfacesTableCellRenderer(dataLayer));
        this.getColumnModel().getColumn(1).setCellRenderer(new InterfacesTableCellRendererCheckBoxNotFocusable(dataLayer));
        this.getColumnModel().getColumn(2).setCellRenderer(new InterfacesTableCellRenderer(dataLayer));
        
        // if show ip addreses
        if(m.getColumnCount()>=4){
            m.getColumn(4).setCellEditor(new InterfacesTableIpAddressCellEditor());
            m.getColumn(5).setCellEditor(new InterfacesTableMacAddressCellEditor());
            
            // set custom cell renderer - for tool tips
            this.getColumnModel().getColumn(3).setCellRenderer(new InterfacesTableCellRendererCheckBox(dataLayer));
            this.getColumnModel().getColumn(4).setCellRenderer(new InterfacesTableCellRenderer(dataLayer));
            this.getColumnModel().getColumn(5).setCellRenderer(new InterfacesTableCellRenderer(dataLayer));
            
            // add tool tips to header
            this.setTableHeader(createTableHeader());
        }
        
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // only single selection
        this.setRowSelectionAllowed(false);                          // row selection enabled
        this.getTableHeader().setReorderingAllowed(false);           // disable reordering columns

        // init column sizes of table
        initColumnSizes();
    }

    private JTableHeader createTableHeader() {
        return new JTableHeader(columnModel) {

            String[] columnToolTips = {
                null,
                null,
                null,
                dataLayer.getString("SETS_THAT_INTERFACE_ON_OFF"),
                dataLayer.getString("REQUIRED_FORMAT_IS") + " 192.168.1.1/24 (IP/mask)",
                dataLayer.getString("REQUIRED_FORMAT_IS") + " HH-HH-HH-HH-HH-HH (H = hexadecimal n.)"};

            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex =
                        columnModel.getColumn(index).getModelIndex();
                return columnToolTips[realIndex];
            }
        };
    }
    
    /*
     * This method picks good column sizes. If all column heads are wider than
     * the column's cells' contents, then you can just use
     * column.sizeWidthToFit().
     */
    private void initColumnSizes() {
        TableColumn column = null;
        for (int i = 0; i < this.getColumnModel().getColumnCount(); i++) {
            column = this.getColumnModel().getColumn(i);
            switch(i){
                case 0:
                    column.setPreferredWidth(90);
                    break;
                case 1:
                    column.setPreferredWidth(60);
                    break;
                case 2:
                    column.setPreferredWidth(75);
                    break;
                case 3:
                    column.setPreferredWidth(65);
                    break;
                case 4:
                    column.setPreferredWidth(120);
                    break;
                case 5:
                    column.setPreferredWidth(120);
                    break;
                default:
                    column.setPreferredWidth(100);
                    break;
                            
            }

        }
    }
}
