package psimulator.userInterface.SimulatorEditor.DrawPanel.Dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm.GeneticGraph;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.LayoutAlgorithm.GeneticLayoutTask;
import psimulator.userInterface.MainWindowInnerInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ProgressBarGeneticDialog extends JDialog implements ActionListener, ProgresBarGeneticInterface {
    // commands

    private static final String CANCEL_COMMAND = "cancel";
    private static final String ENOUGH_QUALITY_COMMAND = "enough";
    //
    private DataLayerFacade dataLayer;
    private GeneticGraph geneticGraph;
    private GeneticLayoutTask task;
    private boolean success = false;
    // swing
    private JPanel mainPanel;
    private JPanel controlPanel;
    private JButton cancelButton;
    private JButton enoughQuality;
    private JTextArea taskOutput;
    private JProgressBar progressBar;

    public ProgressBarGeneticDialog(MainWindowInnerInterface mainWindow, DataLayerFacade dataLayer, Graph graph) {
        super((JFrame) mainWindow, dataLayer.getString("GENETIC_ALGORITHM_RUNNING"), ModalityType.APPLICATION_MODAL);

        // create genetic graph from graph
        this.geneticGraph = new GeneticGraph(graph, graph.getAbstractHwComponentsCount()); //graph.getAbstractHwComponentsCount() * 2

        this.dataLayer = dataLayer;

        this.setLocationRelativeTo((JFrame) mainWindow);

        mainPanel = new JPanel(new BorderLayout());

        controlPanel = new JPanel();

        cancelButton = new JButton(dataLayer.getString("CANCEL"));
        cancelButton.addActionListener((ActionListener) this);
        cancelButton.setActionCommand(CANCEL_COMMAND);
        cancelButton.setEnabled(true);

        enoughQuality = new JButton(dataLayer.getString("ENOUGH_QUALITY"));
        enoughQuality.addActionListener((ActionListener) this);
        enoughQuality.setActionCommand(ENOUGH_QUALITY_COMMAND);
        enoughQuality.setEnabled(false);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        controlPanel.add(cancelButton);
        //controlPanel.add(enoughQuality);
        controlPanel.add(progressBar);

        taskOutput = new JTextArea(10, 35);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);
        DefaultCaret caret = (DefaultCaret) taskOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        taskOutput.setFont(cancelButton.getFont());


        mainPanel.add(controlPanel, BorderLayout.PAGE_START);
        mainPanel.add(new JScrollPane(taskOutput), BorderLayout.CENTER);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 20, 20));

        this.add(mainPanel);

        this.setSize(new Dimension(400, 300));
        this.setResizable(false);
        
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doCancelAction();
            }
        });

    }

    public void startGenetic() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        doStartGenetic();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand() == CANCEL_COMMAND) {
            doCancelAction();
        } else if (ae.getActionCommand() == ENOUGH_QUALITY_COMMAND) {
        }
    }

    private void doCancelAction() {
        // show dialog if really cancel

        int result = showWarningYesNoDialog(dataLayer.getString("CANCEL_COMPUTATION"), dataLayer.getString("DO_YOU_REALLY_WANT_TO_CANCEL_COMPUTATION"));

        if (result == 0) {
            doCancelGenetic();
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            this.setVisible(false);
            this.dispose();
        } else {
            cancelButton.setEnabled(true);
        }
    }

    private void doStartGenetic() {
        task = new GeneticLayoutTask(this, geneticGraph);
        task.execute();

    }

    private void doCancelGenetic() {
        task.cancel(true);
    }

    private int showWarningYesNoDialog(String title, String message) {
        Object[] options = {dataLayer.getString("YES"), dataLayer.getString("NO")};
        int n = JOptionPane.showOptionDialog(this,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, //do not use a custom Icon
                options, //the titles of buttons
                options[0]); //default button title

        return n;
    }

    @Override
    public void informProgress(int generation, double fitness) {
        taskOutput.append(dataLayer.getString("ACTUAL_GENERATION") + " : " + generation + "\n" + dataLayer.getString("BEST_FITNESS_IN_GENERATION") + " : " + fitness + "\n\n");
    }

    @Override
    public void informSuccessEnd(int generation, double fitness, GeneticGraph geneticGraph) {
        this.geneticGraph = geneticGraph;

        taskOutput.append(dataLayer.getString("FINAL_GENERATION") + " : " + generation + "\n" + dataLayer.getString("BEST_FITNESS_IN_GENERATION") + " : " + fitness + "\n");

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        cancelButton.setEnabled(false);
        progressBar.setIndeterminate(false);

        success = true;

        this.dispose();
    }

    public boolean isSuccess() {
        return success;
    }

    public GeneticGraph getGeneticGraph() {
        return this.geneticGraph;
    }
}
