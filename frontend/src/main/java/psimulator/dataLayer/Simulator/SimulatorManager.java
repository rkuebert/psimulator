package psimulator.dataLayer.Simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import javax.swing.SwingUtilities;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.Enums.SimulatorPlayerCommand;
import psimulator.dataLayer.SimulatorEvents.SimulatorEventWithDetails;
import psimulator.logicLayer.Simulator.ConnectionFailtureReason;
import shared.Components.CableModel;
import shared.Components.EthInterfaceModel;
import shared.Components.HwComponentModel;
import shared.SimulatorEvents.SerializedComponents.EventType;
import shared.SimulatorEvents.SerializedComponents.SimulatorEvent;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;
import shared.telnetConfig.TelnetConfig;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SimulatorManager extends Observable implements SimulatorManagerInterface {

    private static boolean DEBUG = false;
    private DataLayerFacade dataLayerFacade;
    // player speeds
    public static final int SPEED_MIN = 10;
    public static final int SPEED_MAX = 100;
    public static final int SPEED_INIT = 50;
    // delay lengths
    public static final int DELAY_MIN = 0;
    public static final int DELAY_MAX = 4000;
    public static final int DELAY_INIT = 1000;
    // simulator state variables
    private volatile boolean isConnectedToServer = false;
    private volatile boolean isRecording = false;
    private volatile boolean isRealtime = false;
    private volatile boolean isPlaying = false;
    private volatile boolean isSequential = true;
    private volatile int currentSpeed = SPEED_INIT;
    private volatile int currentDelay = DELAY_INIT;
    //
    //private volatile int currentPositionInList = 0;
    //
    private EventTableModel eventTableModel;

    public SimulatorManager(DataLayerFacade dataLayerFacade) {
        this.dataLayerFacade = dataLayerFacade;
        eventTableModel = new EventTableModel();
        isPlaying = false;
    }

    // ----- OBSERVERS notify methods
    /**
     * Used from another thread
     */
    @Override
    public void connected() {
        isConnectedToServer = true;

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // notify all observers
                setChanged();
                notifyObservers(ObserverUpdateEventType.SIMULATOR_CONNECTED);
            }
        });
    }

    /**
     * Used from another thread
     */
    @Override
    public void disconnected() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
            }
        });
    }

    /**
     * Used from another thread
     */
    @Override
    public void recievedWrongPacket() {
        this.isRealtime = false;
        this.isRecording = false;
        
        // realtime turns of recording too
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setRealtimeDeactivated();
                
                setChanged();
                notifyObservers(ObserverUpdateEventType.PACKET_RECIEVER_WRONG_PACKET);
            }
        });    
    }

    /**
     * Used from another thread
     */
    @Override
    public void connectingFailed() {
        isConnectedToServer = false;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // notify all observers
                setChanged();
                notifyObservers(ObserverUpdateEventType.CONNECTION_CONNECTING_FAILED);
            }
        });
    }

    /**
     * Used from another thread
     */
    @Override
    public void connectionFailed(ConnectionFailtureReason connectionFailtureReason) {
        isConnectedToServer = false;

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // turn off recording and realtime
                if (isRecording) {
                    setRecordingDeactivated();
                }
                if (isRealtime) {
                    setRealtimeDeactivated();
                }
                // notify all observers
                setChanged();
                notifyObservers(ObserverUpdateEventType.CONNECTION_CONNECTION_FAILED);
            }
        });
    }

    @Override
    public void doConnect() {
        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.CONNECTION_DO_CONNECT);
    }

    @Override
    public void doDisconnect() {
        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.CONNECTION_DO_DISCONNECT);

        isConnectedToServer = false;

        if (isRealtime) {
            setRealtimeDeactivated();
        }

        if (isRecording) {
            setRecordingDeactivated();
        }

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_DISCONNECTED);
    }

    @Override
    public void setPlayerSpeed(int speed) {
        currentSpeed = speed;

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_SPEED);
    }
    
    @Override
    public void setDelayLength(int delay){
        currentDelay = delay;
        
        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_DELAY);
    }

    @Override
    public void setPlayerFunctionActivated(SimulatorPlayerCommand simulatorPlayerState) {
        //this.simulatorPlayerState = simulatorPlayerState;

        if (DEBUG) {
            System.out.println("State=" + simulatorPlayerState);
        }

        switch (simulatorPlayerState) {
            case FIRST:
                eventTableModel.moveToFirstEvent();
                break;
            case PREVIOUS:
                eventTableModel.moveToPreviousEvent();
                break;
            case NEXT:
                eventTableModel.moveToNextEvent();
                break;
            case LAST:
                eventTableModel.moveToLastEvent();
                break;
        }

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_PLAYER_LIST_MOVE);
    }

    @Override
    public void setRecordingActivated() {
        this.isRecording = true;
        if (DEBUG) {
            System.out.println("Recording " + true);
        }

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_RECORDER_ON);
    }

    @Override
    public void setRecordingDeactivated() {
        this.isRecording = false;
        if (DEBUG) {
            System.out.println("Recording " + false);
        }

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_RECORDER_OFF);
    }

    @Override
    public void setRealtimeActivated() {
        // if playing active and realtime activated, turn playing off
        if (isPlaying) {
            setPlayingStopped();
        }

        // start recording
        setRecordingActivated();

        this.isRealtime = true;
        if (DEBUG) {
            System.out.println("Realtime " + true);
        }

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_REALTIME_ON);
    }

    @Override
    public void setRealtimeDeactivated() {
        // start recording
        setRecordingDeactivated();

        this.isRealtime = false;
        if (DEBUG) {
            System.out.println("Realtime " + false);
        }

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_REALTIME_OFF);
    }

    @Override
    public void setPlayingActivated() {
        if (eventTableModel.getRowCount() <= 0) {
            // if nothing to play - stop playing (the toggle button is deselected )
            setPlayingStopped();
            return;
        }

        this.isPlaying = true;
        if (DEBUG) {
            System.out.println("START Playing ");
        }

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_PLAYER_PLAY);
    }

    @Override
    public void setPlayingStopped() {
        this.isPlaying = false;
        if (DEBUG) {
            System.out.println("STOP Playing ");
        }

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_PLAYER_STOP);
    }

    @Override
    public void setConcreteRowSelected(int row) {
        //currentPositionInList = row;
        eventTableModel.setCurrentPositionInList(row);

        // notify all observers
        setChanged();
        notifyObservers(ObserverUpdateEventType.SIMULATOR_PLAYER_LIST_MOVE);
    }

    @Override
    public void deleteAllSimulatorEvents() {
        // stop playing and notify all observers
        //setPlayingStopped();

        // delete items
        eventTableModel.deleteAllSimulatorEvents();
        //currentPositionInList = 0;
    }

    private void setNewPacketRecieved() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // notify all observers
                setChanged();
                notifyObservers(ObserverUpdateEventType.SIMULATOR_NEW_PACKET);
            }
        });
    }

    /**
     * Used from another thread
     */
    @Override
    public void addSimulatorEvent(final SimulatorEvent simulatorEvent) throws ParseSimulatorEventException {

        // set details to event
        SimulatorEventWithDetails eventWithDetails = createSimulatorEventWithDetails(simulatorEvent);

        // add to table
        eventTableModel.addSimulatorEvent(eventWithDetails);

        // new packet recieved
        setNewPacketRecieved();
    }

    @Override
    public void setSimulatorEvents(SimulatorEventsWrapper simulatorEvents) throws ParseSimulatorEventException {
        // delete items
        deleteAllSimulatorEvents();

        // get simulator event list
        List<SimulatorEvent> simulatorEventsList = simulatorEvents.getSimulatorEvents();

        // add details to events
        List<SimulatorEventWithDetails> simulatorEventsWithDetails = createSimulatorEventsWithDetails(simulatorEventsList);

        // add events to table model
        eventTableModel.setEventList(simulatorEventsWithDetails);
    }

    /**
     * Used from another thread
     */
    @Override
    public void moveToNextEvent() {
        if (eventTableModel.canMoveToNextEvent()) {
            eventTableModel.moveToNextEvent();

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    // notify all observers
                    setChanged();
                    notifyObservers(ObserverUpdateEventType.SIMULATOR_PLAYER_NEXT);
                }
            });
        } else {
            isPlaying = false;
            if (DEBUG) {
                System.out.println("Playing automaticly set to " + isPlaying);
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    // notify all observers
                    setChanged();
                    notifyObservers(ObserverUpdateEventType.SIMULATOR_PLAYER_STOP);
                }
            });
        }
    }

    /**
     * used from Controll panel
     */
    @Override
    public void moveToEvent(final int index) {
        eventTableModel.setCurrentPositionInList(index);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // notify all observers
                setChanged();
                notifyObservers(ObserverUpdateEventType.SIMULATOR_PLAYER_NEXT);
            }
        });
    }

    /**
     * Used from another thread
     */
    @Override
    public SimulatorEventWithDetails moveToLastEventAndReturn() {
        SimulatorEventWithDetails simulatorEvent = eventTableModel.moveToLastEventAndReturn();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // notify all observers
                setChanged();
                notifyObservers(ObserverUpdateEventType.SIMULATOR_PLAYER_NEXT);
            }
        });

        return simulatorEvent;
    }
    
    /**
     * used from another thread...player
     * @return 
     */
    @Override
    public SimulatorEventWithDetails getNextEvent(){
        return eventTableModel.getNextEvent();
    }
            

    /**
     * used from another thread...player
     * @return 
     */
    @Override
    public boolean isConnectedToServer() {
        return isConnectedToServer;
    }

    @Override
    public EventTableModel getEventTableModel() {
        return eventTableModel;
    }

    @Override
    public int getSimulatorPlayerSpeed() {
        return currentSpeed;
    }

    @Override
    public int getSimulatorDelayLength(){
        return currentDelay;
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public int getCurrentPositionInList() {
        return eventTableModel.getCurrentPositionInList();
    }

    /**
     * Used from another thread
     */
    @Override
    public int getListSize() {
        return eventTableModel.getRowCount();
    }

    /**
     * Used from another thread
     */
    @Override
    public SimulatorEventWithDetails getSimulatorEventAtCurrentPosition() {
        return eventTableModel.getSimulatorEvent(getCurrentPositionInList());
    }

    @Override
    public boolean isRealtime() {
        return isRealtime;
    }

    @Override
    public boolean isPlayingSequentially() {
        return isSequential;
    }

    @Override
    public boolean isPlayingByTimestamps() {
        return !isSequential;
    }

    @Override
    public void setPlayingSequentially() {
        isSequential = true;
    }

    @Override
    public void setPlayingByTimestamps() {
        isSequential = false;
    }

    @Override
    public boolean hasEvents() {
        return eventTableModel.hasEvents();
    }

    @Override
    public boolean isTimeReset() {
        return eventTableModel.isTimeReset();
    }

    @Override
    public boolean isInTheList() {
        return eventTableModel.isInTheList();
    }

    @Override
    public boolean hasAllEventsItsComponentsInModel() {
        return checkSimulatorEvents(eventTableModel.getEventListCopy());
    }

    @Override
    public SimulatorEventsWrapper getSimulatorEventsCopy() {
        List<SimulatorEvent> simulatorEvents = new ArrayList<>();

        for (SimulatorEventWithDetails eventWithDetails : eventTableModel.getEventListCopy()) {
            simulatorEvents.add(eventWithDetails.getSimulatorEvent());
        }

        SimulatorEventsWrapper simulatorEventsWrapper = new SimulatorEventsWrapper(simulatorEvents);

        return simulatorEventsWrapper;
    }

    /**
     * Throws exception if details could not be found in NetworkModel
     *
     * @param simulatorEvents
     * @throws ParseSimulatorEventException
     */
    private List<SimulatorEventWithDetails> createSimulatorEventsWithDetails(List<SimulatorEvent> simulatorEvents) throws ParseSimulatorEventException {
        List<SimulatorEventWithDetails> simulatorEventsWithDetails = new ArrayList<>();

        for (SimulatorEvent simulatorEvent : simulatorEvents) {
            simulatorEventsWithDetails.add(createSimulatorEventWithDetails(simulatorEvent));
        }

        return simulatorEventsWithDetails;
    }

    /**
     * Throws exception if details could not be found in NetworkModel
     *
     * @param simulatorEvent
     * @throws ParseSimulatorEventException
     */
    private SimulatorEventWithDetails createSimulatorEventWithDetails(SimulatorEvent simulatorEvent) throws ParseSimulatorEventException {
        if(simulatorEvent.getEventType() == EventType.LOST_IN_DEVICE){
            HwComponentModel c1 = dataLayerFacade.getNetworkFacade().getHwComponentModelById(simulatorEvent.getSourcceId());
            
            if (c1 == null) {
                throw new ParseSimulatorEventException();
            }
            
            return new SimulatorEventWithDetails(simulatorEvent, c1.getName(), "", c1, null, null, null);
        }
        
        
        // set details to event
        HwComponentModel c1 = dataLayerFacade.getNetworkFacade().getHwComponentModelById(simulatorEvent.getSourcceId());
        HwComponentModel c2 = dataLayerFacade.getNetworkFacade().getHwComponentModelById(simulatorEvent.getDestId());

        CableModel cable = dataLayerFacade.getNetworkFacade().getCableModelById(simulatorEvent.getCableId());

        if (cable == null || c1 == null || c2 == null) {
            throw new ParseSimulatorEventException();
        }

        EthInterfaceModel eth1 = cable.getInterface1();
        EthInterfaceModel eth2 = cable.getInterface2();

        return new SimulatorEventWithDetails(simulatorEvent, c1.getName(), c2.getName(), c1, c2, eth1, eth2);
    }

    /**
     * Checks if all simulator events has hw components and cables in
     * NetworkModel
     *
     * @param simulatorEventsWithDetails
     * @return true if OK, flase if ERROR
     */
    private boolean checkSimulatorEvents(List<SimulatorEventWithDetails> simulatorEventsWithDetails) {
        for (SimulatorEventWithDetails eventWithDetails : simulatorEventsWithDetails) {
            if (dataLayerFacade.getNetworkFacade().getHwComponentModelById(eventWithDetails.getSourcceId()) == null) {
                return false;
            }
            
            if(eventWithDetails.getEventType() != EventType.LOST_IN_DEVICE){
                if (dataLayerFacade.getNetworkFacade().getHwComponentModelById(eventWithDetails.getDestId()) == null) {
                    return false;
                }
                if (dataLayerFacade.getNetworkFacade().getCableModelById(eventWithDetails.getCableId()) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Used from another thread - player
     */
    @Override
    public void setTelnetConfig(TelnetConfig telnetConfig) {
        dataLayerFacade.setTelnetConfig(telnetConfig);
    }
}
