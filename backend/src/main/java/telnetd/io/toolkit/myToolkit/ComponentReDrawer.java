/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetd.io.toolkit.myToolkit;

import telnetd.io.toolkit.Component;

/**
 * queue of components to be drawn
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public interface ComponentReDrawer {
	
	public void addComponentDraw(Component component);
	
	public void drawComponents();
	
}
