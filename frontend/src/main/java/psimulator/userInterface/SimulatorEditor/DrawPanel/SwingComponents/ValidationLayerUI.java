package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLayer;
import javax.swing.plaf.LayerUI;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ValidationLayerUI extends LayerUI<JFormattedTextField> {
    
  
    
  @Override
  public void paint (Graphics g, JComponent c) {
    super.paint (g, c);

    JLayer jlayer = (JLayer)c;
    JFormattedTextField ftf = (JFormattedTextField)jlayer.getView();
    if (!ftf.isEditValid()) {
      Graphics2D g2 = (Graphics2D)g.create();

      // Paint the red X.
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      int w = c.getWidth();
      int h = c.getHeight();
      int s = 8;
      int pad = 4;
      int x = w - pad - s;
      int y = (h - s) / 2;
      g2.setPaint(Color.red);
      g2.fillRect(x, y, s + 1, s + 1);
      g2.setPaint(Color.white);
      g2.drawLine(x, y, x + s, y + s);
      g2.drawLine(x, y + s, x + s, y);

      g2.dispose();
    }
  }
}