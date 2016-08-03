package psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class VisualizePanel extends JPanel {

    private GeneticGraph graph;
    private int multiplier = 5;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);


        Graphics2D g2 = (Graphics2D) g;

        for (int i = 0; i < graph.getEdges().length; i++) {
            int p1x = graph.getNodes()[graph.getEdges()[i][0]][0];
            int p1y = graph.getNodes()[graph.getEdges()[i][0]][1];

            int p2x = graph.getNodes()[graph.getEdges()[i][1]][0];
            int p2y = graph.getNodes()[graph.getEdges()[i][1]][1];

            g2.drawLine(p1x * multiplier, p1y * multiplier, p2x * multiplier, p2y * multiplier);
        }

        for (int i = 0; i < graph.getNodes().length; i++) {
            g2.fillRect(graph.getNodes()[i][0] * multiplier -1, graph.getNodes()[i][1] * multiplier -1, 2, 2);
        }
    }

    public void setGraph(GeneticGraph graph) {
        this.graph = graph;
    }
}
