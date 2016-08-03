package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm;

import java.util.Random;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class GeneticAutomaticLayout {

    private int populationSize;
    private int generationCount;
    
    private GeneticGraph elitisticGraph;
    private Random random = new Random();
    private double lastFitnessRemembered = Integer.MIN_VALUE;
    private int lastFitnessRememberedGeneration = 0;
    
    private boolean countingFinished = false;
    
    private Generation oldGeneration;

    public GeneticAutomaticLayout() {
    }

    /**
     * Initalizes GA with graph.
     * @param graph
     * @param populationSize size of population in each generation
     */
    public void initGenetic(GeneticGraph graph, int populationSize) {
        this.populationSize = populationSize;

        generationCount = 0;

        GeneticGraph initialGeneticGraph = graph;

        oldGeneration = initPopulation(initialGeneticGraph);
        oldGeneration.evaluateFitness();

        //System.out.println("Best fitness = "+generation.getBestFitness()+", graph:");
        //System.out.println(generation.getBestFitnessGraph());
    }
    
    /**
     * Runs numberOGenerations iterations of genetic algorithm
     * @param numerOfGenerations how many generations compute in this part
     */
    public void runGenericPart(int numerOfGenerations) {
 
        int counter = 0;
        
        while (!termConditionSatisfied(oldGeneration ) && counter < numerOfGenerations) {
            generationCount++;
            counter ++;

            // DO SELECTION
            Generation newGeneration = doSelection(oldGeneration);

            // DO CROSSOVER
            newGeneration = doCrossover(newGeneration);

            // DO MUTATE
            doMutate(newGeneration);

            newGeneration.addGeneticGraph(elitisticGraph);

            // EVALUATE FITNESS
            newGeneration.evaluateFitness();

            //System.out.println("Best fitness in genereation " + generationCount + " is " + newGeneration.getBestFitness());
            //System.out.println("Generation size = " + newGeneration.getGraphList().size());

            oldGeneration = newGeneration;
        }
        
    }
    
    public void printResult(){
  
        //visualize(getBestGraph());
        
        //System.out.println("best genetic graph from generation: " + lastFitnessRememberedGeneration + ", fitness = " + lastFitnessRemembered
        //        + "fitness int:" + (int) lastFitnessRemembered);
    }
    
    
    public GeneticGraph getBestGraph(){
        return oldGeneration.getBestFitnessGraph();
    }
    
    public int getActualGeneration(){
        return generationCount;
    }
    
    public double getActualFitness(){
        return lastFitnessRemembered;
    }

    public boolean isCountingFinished(){
        return countingFinished;
    }

 
    /**
     * Inits initial Generation will be cloned from initialGeneticGraph populationSize times and nodes in
     * the clones will be placed randomly
     * @param initialGeneticGraph 
     * @return Generation of populationSize size
     */
    private Generation initPopulation(GeneticGraph initialGeneticGraph) {
        Generation generation = new Generation();

        // add first geneticGraph
        generation.addGeneticGraph(initialGeneticGraph);

        // clone 49 graphs
        for (int i = 0; i < populationSize - 1; i++) {
            // clone
            GeneticGraph tmp = initialGeneticGraph.clone();
            // random init
            tmp.placeNodesRandomly();

            // add to generation
            generation.addGeneticGraph(tmp);
            //System.out.println("tmp:");
            //System.out.println(tmp.toString());
        }

        return generation;
    }
    
    /**
     * roulette wheel selection
     * @param oldGeneration
     * @return 
     */
    private Generation doSelection(Generation oldGeneration) {

        Generation newGeneration = new Generation();

        int sum = 0;

        for (int i = 0; i < oldGeneration.getGraphList().size(); i++) {
            sum += oldGeneration.getGraphList().get(i).getScore();
        }

        // elitisizm - preserve the best sollution
        elitisticGraph = oldGeneration.getGraphList().get(oldGeneration.getGraphList().size() - 1).clone();

        while (newGeneration.getGraphList().size() < populationSize) {
            int counter = 0;
            int randomNumber = random.nextInt(sum) + 1;

            for (int i = 0; i < oldGeneration.getGraphList().size(); i++) {
                counter += oldGeneration.getGraphList().get(i).getScore();
                if (randomNumber < counter) {
                    GeneticGraph tmp = oldGeneration.getGraphList().get(i).clone();
                    newGeneration.addGeneticGraph(tmp);
                    break;
                }
            }


        }
        return newGeneration;
    }
    
    /**
     * crossovers by pairs - two parents creates two offsprings and offsprings replace parents
     * @param oldGeneration
     * @return Generation with offsprings
     */
    private Generation doCrossover(Generation oldGeneration) {
        Generation generation = new Generation();

        int listSize = oldGeneration.getGraphList().size();

        for (int i = 0; i < listSize - 1; i = i + 2) {
            GeneticGraph[] tmp = oldGeneration.getGraphList().get(i).crossoverRandomSinglePoint(oldGeneration.getGraphList().get(i + 1));
            generation.addGeneticGraph(tmp[0]);
            generation.addGeneticGraph(tmp[1]);

        }
        return generation;
    }
    /**
     * Goes through Graphs in generation and mutates with probability of 10%
     * @param newGeneration 
     */
    private void doMutate(Generation newGeneration) {

        for (GeneticGraph gg : newGeneration.getGraphList()) {
            double rouletteRandom = random.nextDouble();
            if (rouletteRandom <= 0.1) { // 40% probabbility
                randomMutateExecute(gg);
            }
        }
    }
    
    /**
     * Chooses randomly one mutation type and executes it
     * @param gg 
     */ 
    private void randomMutateExecute(GeneticGraph gg) {
        int number = random.nextInt(5);
        if (number == 0) {
            gg.singleNodeMutate();
        } else if (number == 1) {
            gg.singleEdgeMutate1();
        } else if (number == 3) {
            gg.nodeWithMostNeighboursMutate();
        } else if (number == 4) {
            gg.singleEdgeMutate2();
        }
    }
    
    /**
     * Checks if term condition satisfied
     * @param generation
     * @return true if satisfied, false if not
     */
    private boolean termConditionSatisfied(Generation generation) {
        double bestFitness = generation.getBestFitness();

        if (bestFitness > lastFitnessRemembered) {
            lastFitnessRemembered = bestFitness;
            lastFitnessRememberedGeneration = generationCount;
        }


        if (lastFitnessRememberedGeneration < generationCount - 1500) {
            countingFinished = true;
            return true;
        }
        return false;
    }
}
