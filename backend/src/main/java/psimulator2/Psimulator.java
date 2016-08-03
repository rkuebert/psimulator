/*
 * created 2.2.2012
 */

package psimulator2;

import config.configTransformer.Saver;
import device.Device;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import logging.SystemListener;
import logging.networkEvents.EventServer;
import shared.Components.NetworkModel;
import shared.Serializer.AbstractNetworkSerializer;
import shared.Serializer.NetworkModelSerializerXML;
import shared.Serializer.SaveLoadException;
import utils.Alarm;

/**
 * Instance of Psimulator.
 * Pouzit navrhovy vzor Singleton.
 *
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomas Pitrinec
 */
public class Psimulator implements Loggable{




	public final List<Device> devices=new ArrayList<>();
	public NetworkModel configModel;
	public String lastConfigFile;
	public Alarm budik;
	public SystemListener systemListener;
	public EventServer eventServer;


	private Psimulator() {
		budik = new Alarm();
	}

	/**
	 * Metoda na ulozeni
	 * @param configFileName
	 * @return
	 */
	public int saveSimulatorToConfigFile(String configFileName) {

		int vratit=0;

		if(!configFileName.isEmpty()){
			lastConfigFile = configFileName;
		}

		//ukladani do ukladacich struktur:
		Saver saver = new Saver(configModel);
		saver.saveToModel();
		AbstractNetworkSerializer serializer = new NetworkModelSerializerXML();	// vytvori se serializer

		//ukladani do samotnyho souboru:
		try {
			serializer.saveNetworkModelToFile(configModel, new File(lastConfigFile));	// nacita se xmlko do ukladacich struktur
			Logger.log(Psimulator.getNameOfProgram(), Logger.IMPORTANT, LoggingCategory.XML_LOAD_SAVE, "File succesfully saved into " + lastConfigFile);
		} catch (SaveLoadException ex) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.XML_LOAD_SAVE, "Hozena vyjimka: ", ex);
			Logger.log(Logger.WARNING, LoggingCategory.XML_LOAD_SAVE, "Cannot save network model to: " + configFileName);
			vratit = 1;
		}

		return vratit;
	}

	@Override
	public String getDescription() {
		return "Class Psimulator";
	}

	public Device getDeviceByName(String name){
		for (Device d: devices){
			if(d.getName().equals(name)){
				return d;
			}
		}
		return null;
	}





// staticky veci:

	public static String getNameOfProgram(){
		return "psimulator2";
	}

	private static volatile Psimulator instance;

	public static Psimulator getPsimulator() {
		if (instance == null) {
			synchronized(Psimulator.class) {
				if (instance == null) {
					instance = new Psimulator();
				}
			}
		}
		return instance;
	}


}
