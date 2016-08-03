package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.InterfacesTable;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import psimulator.dataLayer.DataLayerFacade;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class InterfacesTableCellRendererCheckBox extends JCheckBox implements TableCellRenderer {

    private DataLayerFacade dataLayer;

    public InterfacesTableCellRendererCheckBox(DataLayerFacade dataLayer) {
        super();
        this.dataLayer = dataLayer;
        setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setSelected((value != null && ((Boolean) value).booleanValue()));
   
        if (isSelected) {
            setForeground(jTable.getSelectionForeground());
            setBackground(jTable.getSelectionBackground());
        } else {
            setForeground(jTable.getForeground());
            setBackground(jTable.getBackground());
        }
        
        this.setToolTipText(dataLayer.getString("SETS_THAT_INTERFACE_ON_OFF"));

        return this;
    }
}