package fr.liglab.adele.icasa.apps.jabber.chat.commands;

/**
 * Created by donatien on 24/04/14.
 */
import fr.liglab.adele.icasa.device.GenericDevice;
import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;

@Component
@Command(name="get",
        scope="jabber",
        description="A simple get command")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class GetAction implements Action {

    /**
     * An option is a named command parameter that should be valued (except if the type is boolean).
     * Example usage: hello --lang fr
     */
    @Option(name = "-o",
            aliases = "--Option",
            required = false,
            description = "option on or off")
    private String option = "on";
    @Option(name = "-d",
            aliases = "--device",
            required = false,
            description = "targeted device")
    private String device = "light";
    @Option(name = "-l",
            aliases = "--location",
            required = false,
            description = "targeted location")
    private String location = "all";

    public GetAction() {
    }

    public Object execute(CommandSession session) throws Exception {

        // Select the output language
        if ("en".equals(option)) {

            // Directly print the message using System.out or System.err
            System.out.println("Hello ");
        } else if ("fr".equals(option)) {

            // Really easy, isn't it ?
            System.out.println("Bonjour " );
        } else {
            throw new Exception("Unknown language");
        }
        return null;
    }

    /**
     * Callback notifying the addition of a device to the platform.
     *
     * @param device The device added.
     */
    public void deviceAdded(GenericDevice device) {

    }

    /**
     * Callback notifying the elimination of a device to the platform.
     *
     * @param device The device removed.
     */
    public void deviceRemoved(GenericDevice device) {

    }

    /**
     * Callback notifying the modification of a property on the device listened.
     *
     * @param device       The device
     * @param propertyName The name of the modified property
     * @param oldValue     The previous value of the property
     * @param newValue     The new value of the property
     */
    public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {

    }

    /**
     * Callback notifying the addition of a property on the device listened.
     *
     * @param device       The device
     * @param propertyName The name of the added property
     */
    public void devicePropertyAdded(GenericDevice device, String propertyName) {

    }

    /**
     * Callback notifying the elimination of a property on the device listened.
     *
     * @param device       The device
     * @param propertyName The name of the removed property
     */
    public void devicePropertyRemoved(GenericDevice device, String propertyName) {

    }

    /**
     * Callback notifying when the device want to trigger an event.
     *
     * @param device the device triggering the event.
     * @param data   the content of the event.
     */
    public void deviceEvent(GenericDevice device, Object data) {

    }
}