package psimulator.userInterface.SimulatorEditor.SimulatorControllPanel;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz> Švihlík <svihlma1 at fit.cvut.cz>
 */
public class JTableEventListColorCellRenderer extends JLabel implements TableCellRenderer{

    public JTableEventListColorCellRenderer(){
        setOpaque(true); //MUST do this for background to show up.
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Configure the component with the specified value
        Color color = (Color) value;
        
        this.setBackground(color);
        
        this.setBorder(BorderFactory.createMatteBorder(2,5,2,5,jTable.getBackground()));
        
        return this;
    }
    
}
