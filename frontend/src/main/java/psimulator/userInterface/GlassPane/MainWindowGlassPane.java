package psimulator.userInterface.GlassPane;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class MainWindowGlassPane extends JPanel implements TimingTarget{
    
    private GridBagConstraints c = new GridBagConstraints();
    //

    private Animator f_animator;
    //
    private List<Message> messageList;
    
    private boolean animationInProgress  = false;
    private MessageGraphic messageGraphic = null;
    
    
    public MainWindowGlassPane() {
        messageList = new ArrayList<>();
        
        // create animator
        f_animator = new Animator.Builder()
                .setDuration(3, TimeUnit.SECONDS)
                .setStartDirection(Animator.Direction.FORWARD)
                .addTarget((TimingTarget)this).build();

        // create message displayer
        messageGraphic = new MessageGraphic();

        
        JPanel bottom = new JPanel();
        bottom.setAlignmentX(LEFT_ALIGNMENT);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.LINE_AXIS));

        bottom.add(messageGraphic);
        
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(Box.createVerticalGlue());
        this.add(bottom);
 
        messageGraphic.setVisible(false);
    }

    /**
     * Glass pane never takes focus.
     * @param x
     * @param y
     * @return 
     */
    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    /**
     * Used by glas panel painter singleton to add messages
     * @param message 
     */
    public void addMessage(Message message){
        messageList.add(message);
        
        // if animation in progress, stop it
        if(animationInProgress){
            stopAnimation();
        }
        
        // start animation
        startAnimation();
    }
   
    /**
     * Call when you want to start animation
     */
    private void startAnimation(){
        // if nothing in list, return
        if(messageList.isEmpty()){
            return;
        }
        
        animationInProgress = true;

        Message message = messageList.get(0);
        messageGraphic.setMessage(message);
        
        messageList.remove(0);
        
        //System.out.println("Animation: "+currentGrahicMessage.getMessage());
        
        //System.out.println("Start Animation");
        f_animator.start();
    }
    
    /**
     * Call when you want to stop animation.
     */
    private void stopAnimation(){
        animationInProgress = false;
        
        //System.out.println("Stop Animation");
        f_animator.stop();
        
        // start next
        startAnimation();
    }


    /**
     * Reaction on animation start.
     * @param source 
     */
    @Override
    public void begin(Animator source) {
        messageGraphic.setVisible(true);
    }

    /**
     * Reaction on animation end.
     * @param source 
     */
    @Override
    public void end(Animator source) {
        messageGraphic.setVisible(false);
        stopAnimation();
    }

    @Override
    public void repeat(Animator source) {
    }

    @Override
    public void reverse(Animator source) {
    }

    @Override
    public void timingEvent(Animator source, double fraction) {
    }

}
