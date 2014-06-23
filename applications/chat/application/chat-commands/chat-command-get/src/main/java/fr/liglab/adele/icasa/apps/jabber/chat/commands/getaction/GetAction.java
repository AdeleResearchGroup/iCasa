package fr.liglab.adele.icasa.apps.jabber.chat.commands.getaction;

/**
 * Created by donatien on 24/04/14.
 */
import fr.liglab.adele.icasa.apps.jabber.chat.regulators.regInt.Regulator;
import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.service.command.CommandSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG= LoggerFactory.getLogger(GetAction.class);

    @Requires(optional=true, filter="(factory.name=fr.liglab.adele.icasa.apps.jabber.chat.regulators.regImpl.LightRegulator)")
    private Regulator LReg;

    @Requires(optional=true, filter="(factory.name=fr.liglab.adele.icasa.apps.jabber.chat.regulators.TempReg.TemperatureRegulator)")
    private Regulator TempReg;

    @Option(name = "-l",
            aliases = "--loc",
            required = false,
            description = "Devices' location")
    private String location = "all";

    @Option(name = "-d",
            aliases = "--device",
            required = false,
            description = "Device type")
    private String device = "light";

    public Object execute(CommandSession session) throws Exception {
        String retour=null;
        // Select the output language
        if (device.equals("light")) {
            retour=LReg.getStatus(location);
        } else if (device.equals("temperature")) {
            retour=TempReg.getStatus(location);
        } else {
            retour="Device unrecognized";
            throw new Exception("Unknown command");
        }
        return retour;
    }
}