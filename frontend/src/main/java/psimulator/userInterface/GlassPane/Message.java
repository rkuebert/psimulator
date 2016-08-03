package psimulator.userInterface.GlassPane;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class Message {

    private String title;
    private String messageName;
    private String messageValue;

    public Message(String title, String messageName, String messageValue) {
        this.title = title;
        this.messageName = messageName;
        this.messageValue = messageValue;
    }

    public String getMessageName() {
        return messageName;
    }

    public String getMessageValue() {
        return messageValue;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Title: " + title + ", message name: " + messageName + ", message value: " + messageValue;
    }
}
