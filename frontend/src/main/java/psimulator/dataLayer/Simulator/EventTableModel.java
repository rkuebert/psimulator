package psimulator.dataLayer.Simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import psimulator.dataLayer.SimulatorEvents.SimulatorEventWithDetails;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class EventTableModel extends AbstractTableModel {

    private List<SimulatorEventWithDetails> eventList;
    /**
     * Flag set to true when all events deleted. 
     */
    private volatile boolean timeReset = true;
    /**
     * Current selected row in table.
     */
    private volatile int currentPositionInList;
    /**
     * Flag whether user is in the list = if any row in table selected.
     */
    private volatile boolean isInTheList;
    /**
     * Lock for synchronized methods.
     */
    private final Object lock = new Object();

    public EventTableModel() {
        eventList = Collections.synchronizedList(new ArrayList<SimulatorEventWithDetails>());

        isInTheList = false;
        currentPositionInList = 0;
    }
    
    /**
     * Gets current position in the list
     * @return 
     */
    public int getCurrentPositionInList(){
        return currentPositionInList;
    }

    /**
     * Gets if any row selected.
     * @return 
     */
    public boolean isInTheList() {
        synchronized (lock) {
            return isInTheList;
        }
    }

    @Override
    public int getRowCount() {
        return eventList.size();
    
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    /**
     * Gets value from row i, column i1
     * @param i
     * @param i1
     * @return 
     */
    @Override
    public Object getValueAt(int i, int i1) {
        synchronized (lock) {
            return eventList.get(i).getValueAt(i1);
        }
    }

    /**
     * Gets class of column c.
     * @param c
     * @return 
     */
    @Override
    public Class getColumnClass(int c) {
        synchronized (lock) {
            return getValueAt(0, c).getClass();
        }
    }

    /**
     * Sets current position in list on position in parameter.
     * If less than zero, than isInTheList is set to false.
     * @param position 
     */
    public void setCurrentPositionInList(int position) {
        synchronized (lock) {
            if (position < 0) {
                currentPositionInList = 0;
                isInTheList = false;
            } else if(position <= eventList.size() - 1){
                currentPositionInList = position;
                isInTheList = true;
            }
        }
    }
    
    /**
     * Returns true if next event exists.
     * @return 
     */
    public boolean canMoveToNextEvent(){
         synchronized (lock) {
             if(currentPositionInList < eventList.size() - 1){
                 return true;
             }else{
                 return false;
             }
         }
    }

    /**
     * Moves to next event. If not inTheList, than it gets into list.
     */
    public void moveToNextEvent() {
        synchronized (lock) {
            // next when not in the list (start playing)
            if(isInTheList == false && currentPositionInList == 0 && eventList.size() > 0){
                currentPositionInList = 0;
                isInTheList = true;
                return;
            }
            // classical next 
            if (currentPositionInList < eventList.size() - 1) {
                currentPositionInList++;
                isInTheList = true;
            }
        }
    }
    
    /**
     * Gets next event or null if no next event.
     * @return 
     */
    public SimulatorEventWithDetails getNextEvent(){
        synchronized (lock) {
            if(eventList.size() > 0 && currentPositionInList < eventList.size() - 1){
                return eventList.get(currentPositionInList+1);
            }else{
                return null;
            }
        }
    }

    /**
     * Moves to previous event if any.
     */
    public void moveToPreviousEvent() {
        synchronized (lock) {
            if (currentPositionInList > 0) {
                currentPositionInList--;
                isInTheList = true;
            }
        }
    }

    /**
     * Moves to first event if any.
     */
    public void moveToFirstEvent() {
        synchronized (lock) {
            if (eventList.size() > 0) {
                currentPositionInList = 0;
                isInTheList = true;
            }
        }
    }

    /**
     * Moves to last event if any.
     */
    public void moveToLastEvent() {
        synchronized (lock) {
            if (eventList.size() > 0) {
                currentPositionInList = eventList.size() - 1;
                isInTheList = true;
            }
        }
    }
    
    /**
     * Moves to last event and return the event. Use in realtime mode.
     * If no event in list, return null.
     * @return 
     */
    public SimulatorEventWithDetails moveToLastEventAndReturn(){
        synchronized (lock) {
            if (eventList.size() > 0) {
                currentPositionInList = eventList.size() - 1;
                isInTheList = true;
                return eventList.get(currentPositionInList);
            }
            return null;
        }
    }

    /**
     * Adds simulator event.
     * @param simulatorEvent 
     */
    public void addSimulatorEvent(SimulatorEventWithDetails simulatorEvent) {
        synchronized (lock) {
            eventList.add(simulatorEvent);
            timeReset = false;

            this.fireTableRowsInserted(eventList.size() - 1, eventList.size() - 1);
        }
    }

    /**
     * Removes all events. isInTheList is set to false and timeReset to true.
     */
    public void deleteAllSimulatorEvents() {
        synchronized (lock) {
            timeReset = true;

            int listSize = eventList.size();
            eventList.clear();

            currentPositionInList = 0;
            isInTheList = false;

            this.fireTableRowsDeleted(0, listSize);
        }
    }

    /**
     * Returns simulator event from specified position i.
     * @param i
     * @return 
     */
    public SimulatorEventWithDetails getSimulatorEvent(int i) {
        synchronized (lock) {
            if(eventList.size()>i){
                return eventList.get(i);
            }else{
                return null;
            }
        }
    }

    /**
     * Returns true if has any event. False if no events.
     * @return 
     */
    public boolean hasEvents() {
        synchronized (lock) {
            return !eventList.isEmpty();
        }
    }

    /**
     * Returns true if should reset time.
     * @return 
     */
    public boolean isTimeReset() {
        synchronized (lock) {
            return timeReset;
        }
    }

    /**
     * Gets copy of event list. Simualtor events are not copied deeply.
     * @return 
     */
    public List<SimulatorEventWithDetails> getEventListCopy() {
        synchronized (lock) {
            List<SimulatorEventWithDetails> copy = new ArrayList<>(eventList);
            return copy;
        }
    }

    /**
     * Sets event list.
     * @param eventList 
     */
    public void setEventList(List<SimulatorEventWithDetails> eventList) {
        synchronized (lock) {
            // set event list
            this.eventList = eventList;
            // fire event
            this.fireTableRowsInserted(0, eventList.size());
        }
    }
}
