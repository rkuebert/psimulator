package psimulator.userInterface.SimulatorEditor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class WelcomePanel extends JPanel implements Observer{
    
    private DataLayerFacade dataLayer;
    //
    private JPanel jPanelMainButtons;
    private JButton jButtonNewProject;
    private JButton jButtonOpenProject;
    //
    private Font font;
    
    
    
    public WelcomePanel(DataLayerFacade dataLayer){
        super();
                
        this.dataLayer = dataLayer;
        
        font = new Font("Tahoma", 0, 18);
        
        // create graphic layout with components
        initComponents();
        
    }
    
    /**
     * Adds action listener to jMenuItemNew
     * @param listener Action listener
     */
    public void addNewProjectActionListener(ActionListener listener){
        jButtonNewProject.addActionListener(listener);
    }
    
    /**
     * Adds action listener to jMenuItemOpen
     * @param listener Action listener
     */
    public void addOpenProjectActionListener(ActionListener listener){
        jButtonOpenProject.addActionListener(listener);
    }
    
    
    private void initComponents(){
        this.setLayout(new GridBagLayout());
        //
        jPanelMainButtons = new JPanel();
        jPanelMainButtons.setLayout(new BoxLayout(jPanelMainButtons, BoxLayout.X_AXIS));
        //
        jButtonNewProject = new JButton();
        jButtonNewProject.setFont(font);
        jButtonNewProject.setIcon(ImageFactorySingleton.getInstance().getImageIcon(ImageFactorySingleton.ICON_FILENEW_128_PATH));// NOI18N
        jButtonNewProject.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonNewProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        //
        jButtonOpenProject = new JButton();
        jButtonOpenProject.setFont(font); 
        jButtonOpenProject.setIcon(ImageFactorySingleton.getInstance().getImageIcon(ImageFactorySingleton.ICON_OPENFILE_GREEN_128_PATH));// NOI18N
        jButtonOpenProject.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonOpenProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        //
        jPanelMainButtons.add(jButtonNewProject);
        jPanelMainButtons.add(Box.createRigidArea(new Dimension(10, 0)));
        jPanelMainButtons.add(jButtonOpenProject);
        //
        this.add(jPanelMainButtons);
        //
        setTextsToComponents();
    }
    
    private void setTextsToComponents(){
        jButtonNewProject.setText(dataLayer.getString("NEW_PROJECT"));
        jButtonNewProject.setToolTipText(dataLayer.getString("NEW_PROJECT"));
        jButtonOpenProject.setText(dataLayer.getString("OPEN"));
        jButtonOpenProject.setToolTipText(dataLayer.getString("OPEN"));
    }
    
    
    @Override
    public void update(Observable o, Object o1) {
        // update texts on components
        setTextsToComponents();
    }
   
    
}
