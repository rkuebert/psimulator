package psimulator.dataLayer.Enums;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public enum ObserverUpdateEventType {
    GRAPH_SIZE_CHANGED,             // when Graph size is changed
    GRAPH_COMPONENT_CHANGED,        //
    
    NETWORK_BOUNDS,                 // when network bounds display boolean changed
    VIEW_DETAILS,                   // when view details changed
    LANGUAGE,                       // when language changed
    ICON_SIZE,                      // when icon size changed
    ZOOM_CHANGE,                    // when zoom changed
    UNDO_REDO,                      // when undo/redo
    PACKET_IMAGE_TYPE_CHANGE,       // when packet icon changed
    RECENT_OPENED_FILES_CHANGED,    // when file added to recently opened
    
    SIMULATOR_PLAYER_LIST_MOVE,     // when manually changed current event(NEXT, PREV, FIRST, LAST or DOUBLE CLICK on some event)
    SIMULATOR_PLAYER_PLAY,          // when playing starts
    SIMULATOR_PLAYER_STOP,          // when playing stops (maunal, or hitting end of list = automatic)
    SIMULATOR_PLAYER_NEXT,          // automatic move to next event when playing
    SIMULATOR_SPEED,                // when speed is changed
    SIMULATOR_DELAY,                // when delay is changed
    SIMULATOR_RECORDER_ON,          // when recorder is turned on
    SIMULATOR_RECORDER_OFF,         // when recorder is turned off
    SIMULATOR_CONNECTED,            // when connected
    SIMULATOR_DISCONNECTED,         // when disconnected
    SIMULATOR_REALTIME_ON,          // when realtime enabled
    SIMULATOR_REALTIME_OFF,         // when realtime disabled
    SIMULATOR_NEW_PACKET,           // when new packet recieved
    SIMULATOR_DETAILS,
    //
    CONNECTION_DO_CONNECT,          // when do connect called
    CONNECTION_DO_DISCONNECT,       // when do disconnect called
    CONNECTION_CONNECTION_FAILED,   // when connection failed called
    CONNECTION_CONNECTING_FAILED,   // when connecting failed
 
    PACKET_RECIEVER_WRONG_PACKET;   // when wrong packet recieved
}
