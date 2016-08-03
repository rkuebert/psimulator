package shared.Serializer;

import shared.Components.NetworkModel;
import java.io.*;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkModelSerializer implements AbstractNetworkSerializer {

    @Override
    public void saveNetworkModelToFile(NetworkModel networkModel, File file) throws SaveLoadException {
        // get file name
        String fileName = file.getPath();

        // try if file exists
        if (!file.exists()) {
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

        OutputStream os = null;
        ObjectOutputStream oos = null;
        
        // save in autoclose stream
        try {
            os = new FileOutputStream(file);

            oos = new ObjectOutputStream(os);

            oos.writeObject(networkModel);
        } catch (IOException ex) {
            //Logger.getLogger(NetworkModelSerializer.class.getName()).log(Level.SEVERE, null, ex);
            // throw exception
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.ERROR_WHILE_WRITING, fileName, true));
        } finally{
            try {
                if(oos != null){
                    oos.close();
                }
            } catch (IOException ex) {
                // nothing to do
            }
            try {
                if(os != null){
                    os.close();
                }
            } catch (IOException ex) {
                // nothing to do
            }
        }
    }

    @Override
    public NetworkModel loadNetworkModelFromFile(File file) throws SaveLoadException {
        // get file name
        String fileName = file.getPath();

        NetworkModel networkModel = null;

        // try if file exists
        if (!file.exists()) {
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.FILE_DOES_NOT_EXIST, fileName, false));
        }

        // try if file readable
        if (!file.canRead()) {
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.CANT_READ_FROM_FILE, fileName, false));
        }

        InputStream is = null;
        ObjectInputStream ois =null;
        
        // try read in autoclose streams
        try {
            is = new FileInputStream(file);

            ois = new ObjectInputStream(is);

            //simulatorEvents = (SimulatorEventsWrapper) ois.readObject();
            networkModel = (NetworkModel) ois.readObject();
        } catch (Exception ex) {
            // throw exception
            throw new SaveLoadException(new SaveLoadExceptionParametersWrapper(SaveLoadExceptionType.ERROR_WHILE_READING, fileName, true));
        } finally{
            try {
                if(ois != null){
                    ois.close();
                }
            } catch (IOException ex) {
                // nothing to do
            }
            try {
                if(is != null){
                    is.close();
                }
            } catch (IOException ex) {
                // nothing to do
            }
        }

        return networkModel;
    }
}
