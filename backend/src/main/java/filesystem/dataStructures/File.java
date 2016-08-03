

package filesystem.dataStructures;

import javax.print.attribute.standard.MediaSize;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class File extends Node{

	@Override
	public String toString() {
		return "file: "+super.getName();
	}
	
	

}
