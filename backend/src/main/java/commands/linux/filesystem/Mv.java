/*
 * Erstellt am 18.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.exceptions.AlreadyExistsException;
import filesystem.exceptions.FileNotFoundException;
import filesystem.exceptions.FileSystemException;

/**
 *
 * @author Tomas Pitrinec
 */
public class Mv extends MvOrCp {

	public Mv(AbstractCommandParser parser) {
		super(parser, "mv");
	}

	@Override
	protected int processFile(String source, String target) {
		try {
			
			String currentDir = getCurrentDir();
			
			String mySource = resolvePath(currentDir, source);
			String myTarget = resolvePath(currentDir, target);
			
			parser.device.getFilesystem().mv(mySource, myTarget);
			
			return 0;
		} catch (FileNotFoundException ex) {
			printLine("mv: "+ source + " to " + target + "failed. Directory or file doesnt exist" );
			return -1;
		}catch(AlreadyExistsException ex){
			printLine("mv: "+ source + " to " + target + "failed. Directory or file already exist");
			return -1;
		}catch(FileSystemException ex){}
	
		return 0;
		
	}

	



}
