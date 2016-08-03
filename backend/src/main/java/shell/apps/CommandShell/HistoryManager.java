/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import device.Device;
import java.util.HashMap;
import java.util.Map;

/**
 * class that manage histories objects
 *
 * @author Martin Lukáš
 */
public class HistoryManager {

	public static String defaultHistoryPath = "/home/user/history";
	/**
	 * curently used history
	 */
	private History activeHistory;
	/**
	 * internal list of available historys
	 */
	private Map<Integer, History> mapOfHistorys;
	private Device deviceReference;

	public HistoryManager(Device device) {
		int mode = CommandShell.DEFAULT_MODE;

		this.deviceReference = device;
		this.activeHistory = HistoryManager.createHistoryObject(mode, device);
		this.activeHistory.activate();

		this.mapOfHistorys = new HashMap<>();
		this.mapOfHistorys.put(mode, activeHistory);

	}

	/**
	 * GETTER
	 *
	 * @return
	 */
	public History getActiveHistory() {
		return activeHistory;
	}
	
	

	/**
	 * set current active history and activate it
	 *
	 * @param activeHistory
	 */
	private void setActiveHistory(History activeHistory) {
		this.activeHistory = activeHistory;
		activeHistory.activate();
	}

	private static History createHistoryObject(int mode, Device device){
		return new History(defaultHistoryPath + String.valueOf(mode), device);
	}
	
	// @TODO rename historys => histories, english grammar failure
	/**
	 * method, that rotate two historys.
	 *
	 * @param mode
	 */
	public void swapHistory(int mode) {
		this.saveAllHistory();
		History histObject = this.mapOfHistorys.get(mode);

		if(histObject == null) // not yet loaded
		{
			histObject = createHistoryObject(mode, deviceReference);
			mapOfHistorys.put(mode, histObject);
		}
		
		this.setActiveHistory(histObject);

	}

	/**
	 * save all(two) used historys
	 */
	public void saveAllHistory() {

		for (Map.Entry<Integer, History> entry : mapOfHistorys.entrySet()) {
			History history = entry.getValue();
			history.save();
			
		}
	}
}
