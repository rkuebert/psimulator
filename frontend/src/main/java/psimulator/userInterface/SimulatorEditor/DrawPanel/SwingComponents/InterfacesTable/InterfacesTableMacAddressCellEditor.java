package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.InterfacesTable;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JLayer;
import javax.swing.JTable;
import javax.swing.plaf.LayerUI;
import javax.swing.text.DefaultFormatterFactory;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Support.Validator;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.RegexFormatter;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.ValidationLayerUI;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class InterfacesTableMacAddressCellEditor extends DefaultCellEditor {

    protected LayerUI<JFormattedTextField> layerUI = new ValidationLayerUI();

    public InterfacesTableMacAddressCellEditor() {
        super(new JFormattedTextField());

        setClickCountToStart(1);


    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JFormattedTextField editor = (JFormattedTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
        editor.setValue(value);

        RegexFormatter macMaskFormatter = new RegexFormatter(Validator.MAC_PATTERN);
        macMaskFormatter.setAllowsInvalid(true);         // allow to enter invalid value for short time
        macMaskFormatter.setCommitsOnValidEdit(true);    // value is immedeatly published to textField
        macMaskFormatter.setOverwriteMode(false);         // do notoverwrite charracters

        editor.setFormatterFactory(new DefaultFormatterFactory(macMaskFormatter));


        editor.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
            }

            // this function successfully provides cell editing stop
            // on cell losts focus (but another cell doesn't gain focus)
            @Override
            public void focusLost(FocusEvent e) {
                if (getCellEditorValue() != null) {
                    stopCellEditing();
                }
            }
        });

        JLayer layer = new JLayer<>(editor, layerUI);
        return layer;
    }

    @Override
    public boolean stopCellEditing() {
        Object o = this.getCellEditorValue();

        if (o == null) {
            return false;
        }

        return super.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        // get content of textField
        String str = (String) super.getCellEditorValue();

        if (!Validator.validateMacAddress(str)) {
            return null;
        }

        return str;
    }
}
