package psimulator.userInterface.GlassPane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class MessageGraphic extends JPanel{
    
    //
    private Font titleFont;
    private Font nameFont;
    private Font valueFont;
    //
    private JLabel jLabelTitle;
    private JLabel jLabelName;
    private JLabel jLabelValue;
    
    public MessageGraphic(){
        titleFont = new Font("Tahoma", Font.BOLD, 10); // NOI18N
        nameFont = new Font("Tahoma", Font.PLAIN,  11);
        valueFont = new Font("Tahoma", Font.ITALIC,  11);

        initComponents();
    }
    
    public void setMessage(Message message){
        jLabelTitle.setText(message.getTitle());
        jLabelName.setText(message.getMessageName()+":");
        jLabelValue.setText(message.getMessageValue()+" ");
    }
    
    private void initComponents() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // set background color to jpanel
        this.setBackground(new Color(255, 255, 204));
        
        JPanel topPanel = new JPanel();
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setOpaque(false);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
        bottomPanel.setOpaque(false);

        jLabelTitle = new JLabel();       
        jLabelTitle.setFont(titleFont); // NOI18N
        jLabelTitle.setFocusable(false);
        
        jLabelName = new JLabel();
        jLabelName.setFont(nameFont); // NOI18N
        jLabelName.setFocusable(false);
        
        jLabelValue = new JLabel();
        jLabelValue.setFont(valueFont); // NOI18N
        jLabelValue.setFocusable(false);

        topPanel.add(Box.createRigidArea(new Dimension(5,0)));
        topPanel.add(jLabelTitle);

        bottomPanel.add(Box.createRigidArea(new Dimension(8,0)));
        bottomPanel.add(jLabelName);
        bottomPanel.add(Box.createRigidArea(new Dimension(5,0)));
        bottomPanel.add(jLabelValue);
        bottomPanel.add(Box.createRigidArea(new Dimension(8,0)));
        
        this.add(topPanel);
        this.add(Box.createRigidArea(new Dimension(0,5)));
        this.add(bottomPanel);
        this.add(Box.createRigidArea(new Dimension(0,5)));

    }
}
