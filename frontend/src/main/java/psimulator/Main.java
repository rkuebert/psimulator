package psimulator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import psimulator.dataLayer.DataLayer;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.TimerKeeperSingleton;
import psimulator.logicLayer.Controller;
import psimulator.logicLayer.ControllerFacade;
import psimulator.userInterface.MainWindow;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // init timing source for animations
        TimerKeeperSingleton.getInstance().initTimingSource();
        
        try {

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    DataLayerFacade model = new DataLayer();
                    MainWindow view = new MainWindow(model);
                    ControllerFacade controller = new Controller(model, view);
                }
            });
        } catch (Exception ex) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Error occured. Program will exit", "Error in application!", JOptionPane.ERROR_MESSAGE);
                }
            });           
        }
    }
}
