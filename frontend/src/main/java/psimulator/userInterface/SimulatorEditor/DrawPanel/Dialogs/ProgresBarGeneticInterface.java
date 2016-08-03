
package psimulator.userInterface.SimulatorEditor.DrawPanel.Dialogs;

import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm.GeneticGraph;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface ProgresBarGeneticInterface {
    
    public void informProgress(int generation, double fitness);
    
    public void informSuccessEnd(int generation, double fitness, GeneticGraph geneticGraph);
    
    
}
