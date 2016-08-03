/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem.dataStructures.jobs;

import java.io.OutputStream;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public interface OutputFileJob {
    
	/**
	 * 
	 * @param output autoclosed outputstream
	 * @return
	 * @throws Exception 
	 */
        public int workOnFile(OutputStream output) throws Exception;
    
}
