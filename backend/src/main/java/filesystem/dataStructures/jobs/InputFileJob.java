/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem.dataStructures.jobs;

import java.io.InputStream;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public interface InputFileJob {
    
    /**
	 * 
	 * @param input autoclosed inputstream, no need to close it
	 * @return
	 * @throws Exception 
	 */
    public int workOnFile(InputStream input) throws Exception;
    
}
