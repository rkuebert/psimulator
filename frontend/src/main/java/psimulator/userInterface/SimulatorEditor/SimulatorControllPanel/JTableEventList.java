package psimulator.userInterface.SimulatorEditor.SimulatorControllPanel;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import psimulator.dataLayer.Simulator.EventTableModel;
import psimulator.dataLayer.Singletons.ColorMixerSingleton;
import shared.SimulatorEvents.SerializedComponents.EventType;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz> Švihlík <svihlma1 at
 * fit.cvut.cz>
 */
public class JTableEventList extends JTable {

    private EventTableModel tableModel;

    public JTableEventList(EventTableModel tableModel) {
        super(tableModel);

        this.tableModel = tableModel;

        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // only single selection
        this.setRowSelectionAllowed(true);                           // row selection enabled
        this.setFocusable(true);                                    // dont display focus on cells
        this.getTableHeader().setReorderingAllowed(false);           // disable reordering columns

        // set custom cell renderer for color column
        this.getColumnModel().getColumn(4).setCellRenderer(new JTableEventListColorCellRenderer());

        // set custom cell renderer for time column
        this.getColumnModel().getColumn(0).setCellRenderer(new JTableEventListTimeCellRenderer());


        // init column sizes of table
        initColumnSizes();
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if(column == 4){
            return c;
        }
        
        if (!isRowSelected(row)){
            c.setBackground(getBackground());
            c.setForeground(Color.BLACK);
        }else{
            if(tableModel.getSimulatorEvent(row).getEventType() != EventType.SUCCESSFULLY_TRANSMITTED){
                c.setForeground(Color.WHITE);
            }
        }
                
        if(tableModel.getSimulatorEvent(row).getEventType() != EventType.SUCCESSFULLY_TRANSMITTED){
            if (!isRowSelected(row)) {
                c.setBackground(ColorMixerSingleton.tableLostEventColor);
            }else{
                c.setBackground(ColorMixerSingleton.tableLostEventMarkedColor);
            }
        }
        
        return c;
    }

    private void initColumnSizes() {

        TableColumn column;
        for (int i = 0; i < this.getColumnModel().getColumnCount(); i++) {
            column = this.getColumnModel().getColumn(i);
            switch (i) {
                case 0:
                    column.setPreferredWidth(5);
                    break;
                case 1:
                    column.setPreferredWidth(20);
                    break;
                case 2:
                    column.setPreferredWidth(20);
                    break;
                case 3:
                    column.setPreferredWidth(10);
                    break;
                case 4:
                    column.setPreferredWidth(5);
                    break;
                default:
                    column.setPreferredWidth(10);
                    break;

            }

        }
    }
}
