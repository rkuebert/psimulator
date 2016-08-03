package psimulator.userInterface.actionListerners;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.Dialogs.SettingsDialog;
import psimulator.userInterface.MainWindowInnerInterface;

/**
 * Action Listener for Preferences Button
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class PreferencesActionListener implements ActionListener {

    private MainWindowInnerInterface mainWindow;
    private DataLayerFacade dataLayer;

    public PreferencesActionListener(MainWindowInnerInterface mainWindow, DataLayerFacade dataLayer) {
        super();
        
        this.mainWindow = mainWindow;
        this.dataLayer = dataLayer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SettingsDialog dialog = new SettingsDialog(mainWindow.getMainWindowComponent(), dataLayer);
    }
}
