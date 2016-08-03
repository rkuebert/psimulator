package shared.Serializer;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public enum SaveLoadExceptionType {
    FILE_DOES_NOT_EXIST,
    CANT_READ_FROM_FILE,
    CANT_WRITE_TO_FILE,
    ERROR_WHILE_READING,
    ERROR_WHILE_WRITING,
    ERROR_WHILE_CREATING,
    CANNOT_OVERWRITE;
}
