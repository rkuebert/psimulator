package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class Generation {
    
    private List<GeneticGraph> graphList;
    private GeneticGraph bestFitnessGraph;
    private double bestFitness;

    public Generation(){
        graphList = new ArrayList<GeneticGraph>();
    }
    
    public void addGeneticGraph(GeneticGraph geneticGraph){
        graphList.add(geneticGraph);
    }

    public List<GeneticGraph> getGraphList() {
        return graphList;
    }

    public void evaluateFitness(){
        bestFitnessGraph = null;
        bestFitness = Integer.MIN_VALUE;
        
        for(GeneticGraph gg : graphList){
            gg.evaluateFitness();
            //System.out.println("fitness "+gg.getFitness());
            if(bestFitnessGraph == null || gg.getFitness() > bestFitness){
                bestFitness = gg.getFitness();
                bestFitnessGraph = gg;
            }
        }
        
        Collections.sort(graphList);
        
        int score = 1;
        
        for(GeneticGraph gg : graphList){
            gg.setScore(score);
            score ++;
        }
    }
    
    public double getBestFitness(){
        return bestFitness;
    }
    
    public GeneticGraph getBestFitnessGraph(){
        return bestFitnessGraph;
    }
    
    
}
