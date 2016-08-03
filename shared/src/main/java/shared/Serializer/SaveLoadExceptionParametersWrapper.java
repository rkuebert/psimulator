package shared.Serializer;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SaveLoadExceptionParametersWrapper {
    
    private SaveLoadExceptionType saveLoadExceptionType;
    private String fileName;
    private boolean saving;

    public SaveLoadExceptionParametersWrapper(SaveLoadExceptionType saveLoadExceptionType, String fileName, boolean saving) {
        this.saveLoadExceptionType = saveLoadExceptionType;
    }

    public SaveLoadExceptionType getSaveLoadExceptionType() {
        return saveLoadExceptionType;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isSaving() {
        return saving;
    }
    
    
}
