package psimulator.dataLayer.Simulator;

import psimulator.dataLayer.Enums.SimulatorPlayerCommand;
import psimulator.dataLayer.SimulatorEvents.SimulatorEventWithDetails;
import psimulator.logicLayer.Simulator.ConnectionFailtureReason;
import shared.SimulatorEvents.SerializedComponents.SimulatorEvent;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;
import shared.telnetConfig.TelnetConfig;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface SimulatorManagerInterface {

    /**
     * Adds event to list. Throws exception if event cannot be played at current network.
     * @param simulatorEvent
     * @throws ParseSimulatorEventException 
     */
    public void addSimulatorEvent(SimulatorEvent simulatorEvent) throws ParseSimulatorEventException;
    /**
     * Removes all simulator events.
     */
    public void deleteAllSimulatorEvents();

    /**
     * Connects to server. Notifies observers with CONNECTION_DO_CONNECT.
     */
    public void doConnect();
    /**
     * Disconnects from server. Stops realtime and recording. 
     * Notifies observers with CONNECTION_DO_DISCONNECT.
     */
    public void doDisconnect();
    
    /**
     * Notifies observers that connecting was succesfull. 
     * SIMULATOR_CONNECTED
     */
    public void connected();
    /**
     * Nothing happens. Prepared to implement reaction if needed.
     */
    public void disconnected();
    /**
     * Notifies observers that connecting failed.
     * CONNECTION_CONNECTING_FAILED
     */
    public void connectingFailed();
    /**
     * Notifies observers that connection failed for some reason.
     * CONNECTION_CONNECTING_FAILED
     * @param connectionFailtureReason 
     */
    public void connectionFailed(ConnectionFailtureReason connectionFailtureReason);
    
    /**
     * Notifies observers that  wrong packet was recieved.
     * PACKET_RECIEVER_WRONG_PACKET
     */
    public void recievedWrongPacket();
    
    // -------------------- SETTERS --------------------------
    /**
     * Sets speed and informs observers.
     * @param speed 
     */
    public void setPlayerSpeed(int speed);
    /**
     * Sets delay and informs observers.
     * @param speed 
     */
    public void setDelayLength(int delay);
    
    /**
     * Sets playing function actiavated and informs observers.
     * @param speed 
     */
    public void setPlayerFunctionActivated(SimulatorPlayerCommand simulatorPlayerState);
    /**
     * Select concrete row and inform observers.
     * @param row 
     */
    public void setConcreteRowSelected(int row);
    
    /**
     * Turns recording on and informs observers.
     */
    public void setRecordingActivated();
    /**
     * Turns recording off and informs observers.
     */
    public void setRecordingDeactivated();
    
    /**
     * Turns realtime on and informs observers.
     */
    public void setRealtimeActivated();
    /**
     * Turns realtime off and informs observers.
     */
    public void setRealtimeDeactivated();
    
    /**
     * Turns playing on and informs observers.
     */
    public void setPlayingActivated();
    /**
     * Turns playing off and informs observers.
     */
    public void setPlayingStopped();
    
    /**
     * Set playing sequentially.
     */
    public void setPlayingSequentially();
    /**
     * Set playing by timestamps.
     */
    public void setPlayingByTimestamps();
   
    /**
     * Get coppy of simulator events in wrapper. Copies only references.
     * @return 
     */
    public SimulatorEventsWrapper getSimulatorEventsCopy();
    /**
     * Sets events from wrapper.
     * @param simulatorEvents
     * @throws ParseSimulatorEventException 
     */
    public void setSimulatorEvents(SimulatorEventsWrapper simulatorEvents) throws ParseSimulatorEventException;
    
    // -------------------- GETTERS --------------------------
    public EventTableModel getEventTableModel();
    public boolean isConnectedToServer();
    //
    public int getSimulatorPlayerSpeed();
    public int getSimulatorDelayLength();
    public boolean isRecording();
    public boolean isPlaying();
    public boolean isRealtime();
    public boolean isPlayingSequentially();
    public boolean isPlayingByTimestamps();
    
    /**
     * Returns current position in lsit.
     * @return 
     */
    public int getCurrentPositionInList();
    /**
     * Returns list size.
     * @return 
     */
    public int getListSize();
     
    /**
     * Finds whether there are some events.
     * @return 
     */
    public boolean hasEvents();
    
    /**
     * Moves to next event if any.
     */
    public void moveToNextEvent();
    /**
     * Moves to event at index. (current row)
     * @param index 
     */
    public void moveToEvent(final int index);
    /**
     * Moves to last event in list and returns it. Null if no events in list.
     * @return 
     */
    public SimulatorEventWithDetails moveToLastEventAndReturn();
    
    /**
     * Gets simulator event from current position or null if no event in the list.
     * @return 
     */
    public SimulatorEventWithDetails getSimulatorEventAtCurrentPosition();
    
    /**
     * Returns next event. Null if no next event.
     * @return 
     */
    public SimulatorEventWithDetails getNextEvent();
     
    /**
     * Retruns if time should be reset.
     * @return 
     */
    public boolean isTimeReset();
    
    /**
     * Finds if any row selected.
     * @return 
     */
    public boolean isInTheList();
    
    /**
     * Checks if all events in event table can be played on opened network.
     * @return 
     */
    public boolean hasAllEventsItsComponentsInModel();

    /**
     * Sets current telnet config.
     * @param telnetConfig 
     */
    public void setTelnetConfig(TelnetConfig telnetConfig);
}
