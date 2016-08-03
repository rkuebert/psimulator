package shared.Serializer;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SaveLoadException extends Exception{
    
    private SaveLoadExceptionParametersWrapper parametersWrapper;
    
    public SaveLoadException(SaveLoadExceptionParametersWrapper parametersWrapper){
        this.parametersWrapper = parametersWrapper;
    }

    public SaveLoadExceptionParametersWrapper getParametersWrapper() {
        return parametersWrapper;
    }
}
