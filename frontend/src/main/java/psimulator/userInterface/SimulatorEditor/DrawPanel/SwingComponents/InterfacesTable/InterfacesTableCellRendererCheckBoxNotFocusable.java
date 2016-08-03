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
public class InterfacesTableCellRendererCheckBoxNotFocusable extends JCheckBox implements TableCellRenderer {

    private DataLayerFacade dataLayer;

    public InterfacesTableCellRendererCheckBoxNotFocusable(DataLayerFacade dataLayer) {
        this.dataLayer = dataLayer;
        setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setFocusable(false);

        this.setSelected((boolean) value);

        if (isSelected) {
            setForeground(jTable.getSelectionForeground());
            setBackground(jTable.getSelectionBackground());
        } else {
            setForeground(jTable.getForeground());
            setBackground(jTable.getBackground());
        }


        return this;
    }
}