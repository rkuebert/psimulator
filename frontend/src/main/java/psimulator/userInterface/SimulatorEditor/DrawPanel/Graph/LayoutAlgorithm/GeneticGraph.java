package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.HwComponentGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Components.BundleOfCablesGraphic;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class GeneticGraph implements Comparable<GeneticGraph> {

    private Random random = new Random();
    /*
     * n | 1 | 2 | 3 | 4 |
     * -------------------
     * x | 4 | 1 | 8 | 1 |
     * y | 5 | 2 | 5 | 1 |
     */
    private int[][] nodes;
    /*
     * | 1 | 2 | 3 | 4 |
     * | 2 | 3 | 1 | 2 |
     */
    private int[][] edges;
    private final int gridSize;
    private int nodeWithMaxNeighbours = -1;
    private double fitness;
    private int score;

    public GeneticGraph(Graph graph, int numberOfDevices) {

        /*
        if(numberOfDevices < 5){
            gridSize = (int) (numberOfDevices*1.5);
        }else if(numberOfDevices < 8){
            gridSize = numberOfDevices;
        }else if(numberOfDevices < 20){
            gridSize = (int) (numberOfDevices /1.5);
        } else if(numberOfDevices < 50){
            gridSize = (int) (numberOfDevices / 2);
        } else {
            gridSize = (int) (numberOfDevices / (numberOfDevices / 25.0));
        }*/
        gridSize = numberOfDevices * 2;
        
        //System.out.println("Grid size = "+gridSize);
        
        //this.gridSize = gridSize;

        List<HwComponentGraphic> tmpList = new ArrayList(graph.getHwComponents());
        
        int nodesCount = graph.getHwComponents().size();
        int edgesCount = graph.getBundlesOfCables().size();

        nodes = new int[nodesCount][2];
        edges = new int[edgesCount][2];

        // fill edges
        for (int i = 0; i < edgesCount; i++) {
            BundleOfCablesGraphic boc = graph.getBundlesOfCables().get(i);

            //edges[i][0] = graph.getHwComponents().indexOf(boc.getComponent1());
            //edges[i][1] = graph.getHwComponents().indexOf(boc.getComponent2());
            edges[i][0] = tmpList.indexOf(boc.getComponent1());
            edges[i][1] = tmpList.indexOf(boc.getComponent2());
        }

        nodeWithMaxNeighbours = getNodeWithMostNeighbours();
        //System.out.println("Node with max neighbours = " + nodeWithMaxNeighbours);
        
        // fill nodes randomly
        placeNodesRandomly();
    }

    private GeneticGraph(int[][] nodesToCopy, int[][] edgesToCopy, int gridSize, int nodeWithMaxNeighbours) {
        this.gridSize = gridSize;
        this.nodeWithMaxNeighbours = nodeWithMaxNeighbours;

        nodes = new int[nodesToCopy.length][2];
        edges = new int[edgesToCopy.length][2];

        // problem 2d copy
        //System.arraycopy(nodesToCopy, 0, nodes, 0, nodesToCopy.length);
        //System.arraycopy(edgesToCopy, 0, edges, 0, edgesToCopy.length);

        for (int i = 0; i < edgesToCopy.length; i++) {
            edges[i][0] = edgesToCopy[i][0];
            edges[i][1] = edgesToCopy[i][1];
        }

        for (int i = 0; i < nodesToCopy.length; i++) {
            nodes[i][0] = nodesToCopy[i][0];
            nodes[i][1] = nodesToCopy[i][1];
        }
    }

    @Override
    public GeneticGraph clone() {
        GeneticGraph clone = new GeneticGraph(nodes, edges, gridSize, nodeWithMaxNeighbours);
        return clone;
    }

    public void evaluateFitness() {
        fitness = 0.0;

        int edgeCrossings = getCrossingsCount();

        double[] array = getMinimumNodeDistanceSum();
        double minimumDistanceNodeSum = array[0];   // the bigger the sum, the more evenly are the nodes distributed
        double minimumNodeDistance = ((double) nodes.length) * (Math.pow(array[1], 2.0));
        double edgeLengthDeviation = getEdgeLengthDeviation();

        /*
        System.out.println("Crossings = "+edgeCrossings+
        ", Min node distance sum = "+minimumDistanceNodeSum+
        ", Min node distance= "+minimumNodeDistance+
        ", Edge length deviation = "+edgeLengthDeviation);
         */

        //System.out.println("crossings "+crossings);
        /*
        double award = 2.0* minimumDistanceNodeSum + 
        0.25 * minimumNodeDistance;
        
        double penalization = 2.0*edgeLengthDeviation + 
        2.5*(edgeLengthDeviation / minimumNodeDistance) + 
        edgeCrossings * (Math.pow((double)gridSize, 2.0));
         */
        double award = 2.0 * minimumDistanceNodeSum
                + 0.25 * minimumNodeDistance;

        double tmp = 2.5 * (edgeLengthDeviation / minimumNodeDistance);

        
        
        if(((int)tmp)<0){
            //System.out.println("tmp = "+ tmp +", int tmp = "+(int)tmp+", edge lenght devi="+edgeLengthDeviation);
        }
        
        double penalization = 2.0 * edgeLengthDeviation
                + tmp
                + edgeCrossings * (Math.pow((double) gridSize, 2.0));
        /*double penalization = 2.0*edgeLengthDeviation + 
        2.5*(edgeLengthDeviation / minimumNodeDistance) + 
        edgeCrossings * (Math.pow((double)gridSize, 2.0));*/

        fitness = award - penalization;
        /*
        System.out.println("Crossings = "+edgeCrossings+
        ", Min node distance sum = "+minimumDistanceNodeSum+
        ", Min node distance= "+minimumNodeDistance+
        ", Edge length deviation = "+edgeLengthDeviation);
         */
        //System.out.println("Award = "+award +", penalization = "+penalization);
        //fitness = edges.length / (double)(edges.length + edgeCrossings);
    }

    private int getNodeWithMostNeighbours() {
        int[] edgesNodes = new int[edges.length * 2];

        for (int i = 0; i < edges.length; i++) {
            edgesNodes[i * 2] = edges[i][0];
            edgesNodes[i * 2 + 1] = edges[i][1];
        }

        Arrays.sort(edgesNodes);
        /*
        for (int i = 0; i < edgesNodes.length; i++) {
        System.out.print(edgesNodes[i]+",");
        
        }
        System.out.println("");*/


        int sameCount = 0;
        int maxSameCount = 0;
        int lastElement = -1;
        int nodeWithMaxSameCount = -1;

        for (int i = 0; i < edgesNodes.length; i++) {
            if (edgesNodes[i] == lastElement) {
                sameCount++;
            } else {
                lastElement = edgesNodes[i];
                if (sameCount > maxSameCount) {
                    maxSameCount = sameCount;
                    nodeWithMaxSameCount = edgesNodes[i];
                }
                sameCount = 1;
            }
        }
        return nodeWithMaxSameCount;
    }

    public GeneticGraph[] crossoverRandomSinglePoint(GeneticGraph second) {
        GeneticGraph offspring1 = second.clone();
        GeneticGraph offspring2 = this.clone();

        int singlePoint = random.nextInt(this.nodes.length);

        for (int i = 0; i < singlePoint; i++) {
            // first offspring
            int p1x = this.nodes[i][0];
            int p1y = this.nodes[i][1];

//            if(!checkIfXPossible(offspring1, p1x)){
//                p1x = getRandomFreeX(offspring1);
//            }
//            
//            if(!checkIfYPossible(offspring1, p1y)){
//                p1y = getRandomFreeY(offspring1);
//            }
            
            if(!checkIfPointPossible(offspring1, new Point(p1x, p1y))){
                Point p1 = getRandomFreePoint(offspring1);
                p1x = p1.getX();
                p1y = p1.getY();
            }
            
            offspring1.nodes[i][0] = p1x;
            offspring1.nodes[i][1] = p1y;

            
            // second offspring
            int p2x = second.nodes[i][0];
            int p2y = second.nodes[i][1];
            
//            if(!checkIfXPossible(offspring2, p2x)){
//                p2x = getRandomFreeX(offspring2);
//            }
//            
//            if(!checkIfYPossible(offspring2, p2y)){
//                p2y = getRandomFreeY(offspring2);
//            }
            
            if(!checkIfPointPossible(offspring2, new Point(p2x, p2y))){
                Point p2 = getRandomFreePoint(offspring1);
                p2x = p2.getX();
                p2y = p2.getY();
            }
            
            offspring2.nodes[i][0] = p2x;
            offspring2.nodes[i][1] = p2y;
        }

        GeneticGraph[] array = {offspring1, offspring2};
        return array;
    }

    private boolean checkIfPointPossible(GeneticGraph graph, Point p){
        for (int j = 0; j < graph.nodes.length; j++) {
            if (graph.nodes[j][0] == p.getX() && graph.nodes[j][1] == p.getY()) {
                return false;
            }
        }
        return true;
    }
    
//    private boolean checkIfXPossible(GeneticGraph graph, int p1x) {
//        for (int j = 0; j < graph.nodes.length; j++) {
//            if (graph.nodes[j][0] == p1x) {
//                return false;
//            }
//        }
//        return true;
//    }
//    
//    private boolean checkIfYPossible(GeneticGraph graph, int p1y) {
//        for (int j = 0; j < graph.nodes.length; j++) {
//            if (graph.nodes[j][1] == p1y) {
//                return false;
//            }
//        }
//        return true;
//    }

    /**
     * random change of node position
     */
    public void singleNodeMutate() {
        int nodePos = random.nextInt(nodes.length);
        Point p = getRandomFreePoint(this);
        nodes[nodePos][0] = p.getX();
        nodes[nodePos][1] = p.getY();
    }

    /**
     * random change of line ends
     */
    public void singleEdgeMutate1() {
        int edgePos = random.nextInt(edges.length);

        Point p1 = getRandomFreePoint(this);
        nodes[edges[edgePos][0]][0] = p1.getX();
        nodes[edges[edgePos][0]][1] = p1.getY();

        Point p2 = getRandomFreePoint(this);
        nodes[edges[edgePos][1]][0] = p2.getX();
        nodes[edges[edgePos][1]][1] = p2.getY();
    }

    /**
     * preserves the length if possible
     */
    public void singleEdgeMutate2() {
        int edgePos = random.nextInt(edges.length);

        int width = Math.abs(nodes[edges[edgePos][1]][0] - nodes[edges[edgePos][0]][0]);
        int height = Math.abs(nodes[edges[edgePos][1]][1] - nodes[edges[edgePos][0]][1]);

        if (width >= gridSize) {
            width = gridSize - 1;
        }

        if (height >= gridSize) {
            height = gridSize - 1;
        }

        Point p1 = getRandomFreePoint(this);
        nodes[edges[edgePos][0]][0] = p1.getX();
        nodes[edges[edgePos][0]][1] = p1.getY();

        int p2x = nodes[edges[edgePos][0]][0] + width;
        int p2y = nodes[edges[edgePos][0]][1] + height;

//        if(!checkIfXPossible(this, p2x)){
//            p2x = getRandomFreeX(this);
//        }

//        
//        if(!checkIfYPossible(this, p2y)){
//            p2y = getRandomFreeY(this);
//        }
        
        if(!checkIfPointPossible(this, new Point(p2x, p2y))){
            Point p2 = getRandomFreePoint(this);
            p2x = p2.getX();
            p2y = p2.getY();
        }

        nodes[edges[edgePos][1]][0] = p2x;
        nodes[edges[edgePos][1]][1] = p2y;
    }

    public void nodeWithMostNeighboursMutate() {
        int node = nodeWithMaxNeighbours;

        Point p = getRandomFreePoint(this);
        nodes[node][0] = p.getX();
        nodes[node][1] = p.getY();

    }

    public double getFitness() {
        return fitness;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public final void placeNodesRandomly() {
        for (int i = 0; i < nodes.length; i++) {
            Point p = getRandomFreePoint(this);
            nodes[i][0] = p.getX();
            nodes[i][1] = p.getY();
        }
    }

    private double getEdgeLengthDeviation() {
        double minEdgeLength = Integer.MAX_VALUE;
        double maxEdgeLength = 0.0;

        for (int i = 0; i < edges.length; i++) {
            int p1x = nodes[edges[i][0]][0];
            int p1y = nodes[edges[i][0]][1];

            int p2x = nodes[edges[i][1]][0];
            int p2y = nodes[edges[i][1]][1];

            double length = Math.sqrt(Math.pow(p1x - p2x, 2.0) + Math.pow(p1y - p2y, 2.0));
            
            if(length <= 0.0){
                //System.out.println("PRUSER, delka zaporna");
            }
            
            if (length < minEdgeLength) {
                minEdgeLength = length;
            }

            if (length > maxEdgeLength) {
                maxEdgeLength = length;
            }
        }
        //System.out.println("Min edge length = "+ minEdgeLength+", max edge length ="+maxEdgeLength);

        return maxEdgeLength - minEdgeLength;
    }

    private double[] getMinimumNodeDistanceSum() {

        double sum = 0;
        double minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < nodes.length; i++) {
            double p1x = nodes[i][0];
            double p1y = nodes[i][1];
            double minDistanceActual = Integer.MAX_VALUE;

            for (int j = 0; j < nodes.length; j++) {
                if (i == j) {
                    continue;
                }
                double p2x = nodes[j][0];
                double p2y = nodes[j][1];

                double diffX = Math.pow(p1x - p2x, 2.0);
                double diffY = Math.pow(p1y - p2y, 2.0);

                double distance = Math.sqrt(diffX + diffY);

                /*
                if (distance == 0.0) {
                    System.out.println("distance = " + distance + ", diff x =" + diffX + ", diff y =" + diffY);
                    System.out.println("I=" + i + ", J=" + j);
                    System.out.println(this);
                }*/

                if (distance < minDistanceActual) {
                    minDistanceActual = distance;
                }
            }

            sum += minDistanceActual;

            if (minDistanceActual < minDistance) {
                minDistance = minDistanceActual;
            }
        }

        double[] array = {sum, minDistance};

        return array;
    }

    private int getCrossingsCount() {
        List<Line2D> lines = new ArrayList<Line2D>();

        for (int i = 0; i < edges.length; i++) {
            Line2D line = new Line2D.Double(nodes[edges[i][0]][0], nodes[edges[i][0]][1], nodes[edges[i][1]][0], nodes[edges[i][1]][1]);
            lines.add(line);
            //System.out.println("Line: x="+line.getP1()+",="+line.getP2());
        }

        int crossings = 0;
        for (int i = 0; i < lines.size(); i++) {
            for (int j = i + 1; j < lines.size(); j++) {
                if (i == j) {
                    continue;
                }
                if (lines.get(i).getP1().equals(lines.get(j).getP1())
                        || lines.get(i).getP1().equals(lines.get(j).getP2())
                        || lines.get(i).getP2().equals(lines.get(j).getP1())
                        || lines.get(i).getP2().equals(lines.get(j).getP2())) {

                    continue;
                }

                if (lines.get(i).intersectsLine(lines.get(j))) {
                    crossings++;
                }
            }
        }
        return crossings;
    }
    
    private Point getRandomFreePoint(GeneticGraph graph){
        int randomX = 0;
        int randomY = 0;
        
        boolean success = false;
        while (!success) {
            randomX = random.nextInt(graph.gridSize);
            randomY = random.nextInt(graph.gridSize);
            
            boolean isPossible = true;
            // check if it is empty field
            for (int i = 0; i < graph.nodes.length; i++) {
                if (graph.nodes[i][0] == randomX && graph.nodes[i][1] == randomY) {
                    isPossible = false;
                    break;
                }
            }
            if (isPossible == true) {
                success = true;
            }
        }
        return new Point(randomX, randomY);
    }

//    private int getRandomFreeX(GeneticGraph graph) {
//        int randomX = 0;
//        boolean success = false;
//        while (!success) {
//            randomX = random.nextInt(graph.gridSize);
//
//            boolean isPossible = true;
//            for (int i = 0; i < graph.nodes.length; i++) {
//                if (graph.nodes[i][0] == randomX) {
//                    isPossible = false;
//                    break;
//                }
//            }
//            if (isPossible == true) {
//                success = true;
//            }
//
//        }
//        return randomX;
//    }

//    private int getRandomFreeY(GeneticGraph graph) {
//        int randomY = 0;
//        boolean success = false;
//        while (!success) {
//            randomY = random.nextInt(graph.gridSize);
//
//            boolean isPossible = true;
//            for (int i = 0; i < graph.nodes.length; i++) {
//                if (graph.nodes[i][1] == randomY) {
//                    isPossible = false;
//                    break;
//                }
//            }
//            if (isPossible == true) {
//                success = true;
//            }
//        }
//        return randomY;
//    }

    public int[][] getEdges() {
        return edges;
    }

    public int[][] getNodes() {
        return nodes;
    }

    /*
     * n | 1 | 2 | 3 | 4 |
     * -------------------
     * x | 4 | 1 | 8 | 1 |
     * y | 5 | 2 | 5 | 1 |
     */
    //private int[][] nodes;
    /*
     * | 1 | 2 | 3 | 4 |
     * | 2 | 3 | 1 | 2 |
     */
    //private final int[][] edges;
    @Override
    public String toString() {
        String s = "";

        // NODES
        s += "Nodes:\n";
        // N line 

        s += "N |";
        for (int i = 0; i < nodes.length; i++) {
            s += i + " | ";
        }
        s += "\n";

        // X line
        s += "x |";
        for (int i = 0; i < nodes.length; i++) {
            s += nodes[i][0] + " | ";
        }
        s += "\n";

        // Y line
        s += "y |";
        for (int i = 0; i < nodes.length; i++) {
            s += nodes[i][1] + " | ";
        }
        s += "\n";

        // EDGES
        s += "Edges:\n";
        // 1. component line
        for (int i = 0; i < edges.length; i++) {
            s += edges[i][0] + " | ";
        }
        s += "\n";

        // 2. component line
        for (int i = 0; i < edges.length; i++) {
            s += edges[i][1] + " | ";
        }
        s += "\n";

        return s;
    }

    @Override
    public int compareTo(GeneticGraph t) {
        if (this.getFitness() < t.getFitness()) {
            return -1;
        }
        if (this.getFitness() > t.getFitness()) {
            return 1;
        }
        return 0;

    }
}
