package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm;

import javax.swing.SwingWorker;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Dialogs.ProgresBarGeneticInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class GeneticLayoutTask extends SwingWorker<Void, Void> {

    private ProgresBarGeneticInterface progresBarGeneticInterface;
    private GeneticGraph graph;
    private GeneticAutomaticLayout genetic;
    private static final int numberOfGenerations = 200;

    public GeneticLayoutTask(ProgresBarGeneticInterface progresBarGeneticInterface, GeneticGraph graph) {
        super();
        this.progresBarGeneticInterface = progresBarGeneticInterface;
        this.graph = graph;
    }

    /*
     * Main task. Executed in background thread.
     */
    @Override
    public Void doInBackground() {
        genetic = new GeneticAutomaticLayout();
        genetic.initGenetic(graph, 30);
        
        while (!genetic.isCountingFinished()) {
            
            if(isCancelled()){
                return null;
            }

            genetic.runGenericPart(numberOfGenerations);

            progresBarGeneticInterface.informProgress(genetic.getActualGeneration(), genetic.getActualFitness());
        }

        if (genetic.isCountingFinished()) {
            genetic.printResult();
        }

        return null;
    }

    @Override
    public void done() {
        if (!isCancelled()) {
            progresBarGeneticInterface.informSuccessEnd(genetic.getActualGeneration(), genetic.getActualFitness(), genetic.getBestGraph());
        }
    }
}
