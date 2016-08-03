package shared.Serializer;

import shared.Components.NetworkModel;
import java.io.File;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface AbstractNetworkSerializer {
    /**
     * Saves network mdel to specified file. If error occours, Exception is thrown.
     * @param networkModel Model to save
     * @param file File to save model to
     * @throws SaveLoadException 
     */
    public void saveNetworkModelToFile(NetworkModel networkModel, File file) throws SaveLoadException;
    /**
     * Loads network model from specified file. If error occours, Exception is thrown.
     * @param file File to save model into.
     * @return
     * @throws SaveLoadException 
     */
    public NetworkModel loadNetworkModelFromFile(File file) throws SaveLoadException;
}
