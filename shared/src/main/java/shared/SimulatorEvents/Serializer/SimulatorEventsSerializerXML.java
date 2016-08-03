package shared.SimulatorEvents.Serializer;

import shared.Serializer.SaveLoadException;
import shared.Serializer.SaveLoadExceptionParametersWrapper;
import shared.Serializer.SaveLoadExceptionType;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SimulatorEventsSerializerXML implements AbstractSimulatorEventsSaveLoadInterface{

    @Override
    public void saveEventsToFile(SimulatorEventsWrapper simulatorEvents, File file) throws SaveLoadException {
         // get file name
        String fileName = file.getPath();

        // try if file exists
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.ERROR_WHILE_CREATING, fileName, true));
            }
        }
        
        // try if file readable
        if (!file.canWrite()) {
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.CANT_WRITE_TO_FILE, fileName, true));
        }
        
        
        // save in autoclose stream
        try {
            // SAVE TO XML
            JAXBContext context = JAXBContext.newInstance(SimulatorEventsWrapper.class);

            Marshaller marsh = context.createMarshaller();

            // nastavení formátování
            marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marsh.marshal(simulatorEvents, file);
        } catch (JAXBException ex) {
            // throw exception
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.ERROR_WHILE_WRITING, fileName, true));
        }
    }

    @Override
    public SimulatorEventsWrapper loadEventsFromFile(File file) throws SaveLoadException {
        // get file name
        String fileName = file.getPath();
        
        SimulatorEventsWrapper simulatorEvents = null;

        // try if file exists
        if(!file.exists()){
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.FILE_DOES_NOT_EXIST, fileName, false));
        }
        
        // try if file readable
        if (!file.canRead()) {
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.CANT_READ_FROM_FILE, fileName, false));
        }
        
        // try read
        try {
            // LOAD FROM XML
            
            JAXBContext context = JAXBContext.newInstance(SimulatorEventsWrapper.class);

            Unmarshaller unmarsh = context.createUnmarshaller();

            simulatorEvents = (SimulatorEventsWrapper) unmarsh.unmarshal(file);
        } catch (JAXBException ex) {
            // if needed, uncomment this line:
            //Logger.getLogger(AbstractNetworkAdapter.class.getName()).log(Level.SEVERE, null, ex);
            
            // throw exception
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.ERROR_WHILE_READING, fileName, false));
        }
        
        return simulatorEvents;
    }
    
}
