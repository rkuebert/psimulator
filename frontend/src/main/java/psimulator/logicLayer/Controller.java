package psimulator.logicLayer;

import psimulator.dataLayer.DataLayerFacade;
import psimulator.logicLayer.Simulator.SimulatorClientEventRecieverMockupThread;
import psimulator.logicLayer.Simulator.SimulatorClientEventRecieverThread;
import psimulator.logicLayer.Simulator.SimulatorPlayerThread;
import psimulator.userInterface.UserInterfaceOuterFacade;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class Controller implements ControllerFacade{

    private DataLayerFacade model;
    private UserInterfaceOuterFacade view;

   
    public Controller(DataLayerFacade model, UserInterfaceOuterFacade view) {
        this.model = model;
        this.view = view;

        view.initView((ControllerFacade)this);
        
        SimulatorClientEventRecieverThread eventReciever = new SimulatorClientEventRecieverThread(model, view);
        //SimulatorClientEventRecieverMockupThread eventReciever = new SimulatorClientEventRecieverMockupThread(model, view);
        eventReciever.startThread(new Thread(eventReciever));
 
        
        SimulatorPlayerThread simulatorPlayer = new SimulatorPlayerThread(model, view);
        simulatorPlayer.startThread(new Thread(simulatorPlayer));
        
        model.addSimulatorObserver(eventReciever);
        model.addSimulatorObserver(simulatorPlayer);
    }
   
}
