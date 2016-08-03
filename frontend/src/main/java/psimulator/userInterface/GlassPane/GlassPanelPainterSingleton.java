package psimulator.userInterface.GlassPane;

import javax.swing.SwingUtilities;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class GlassPanelPainterSingleton {

    private MainWindowGlassPane mainWindowGlassPane;

    private GlassPanelPainterSingleton() {
    }

    public static GlassPanelPainterSingleton getInstance() {
        return GlassPanelPainterSingletonHolder.INSTANCE;
    }

    private static class GlassPanelPainterSingletonHolder {

        private static final GlassPanelPainterSingleton INSTANCE = new GlassPanelPainterSingleton();
    }

    public void initialize(MainWindowGlassPane mainWindowGlassPane) {
        this.mainWindowGlassPane = mainWindowGlassPane;
    }

    /**
     * Adds message with title name and value to glass pane. Use when you want to inform user but
     * do you do not require user interaction. For example succesfull save/load
     * @param title
     * @param messageName
     * @param messageValue 
     */
    public void addAnnouncement(final String title, final String messageName, final String messageValue) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Message message = new Message(title, messageName, messageValue);

                mainWindowGlassPane.addMessage(message);
            }
        });

    }
}
