package psimulator.userInterface.SimulatorEditor.SimulatorControllPanel;

import java.text.DecimalFormat;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class JTableEventListTimeCellRenderer extends DefaultTableCellRenderer{

    private DecimalFormat fmt = new DecimalFormat("0.000");  
    
    public JTableEventListTimeCellRenderer(){
        super();
    }
    
    @Override
    public void setValue(Object value) {
        long time = (long)value;
        
        double seconds = time / 1000.0;
        
        String text = fmt.format(seconds);  

        this.setText(text);
    }  
}
