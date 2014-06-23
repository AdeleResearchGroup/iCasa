package fr.liglab.adele.icasa.apps.jabber.chat.message;

import fr.liglab.adele.icasa.jabber.chat.commands.parser.ChatCommandParser;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.chat.ChatService;
import org.ow2.chameleon.chat.Discussion;
import org.ow2.chameleon.chat.DiscussionListener;
import org.ow2.chameleon.chat.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

//import org.ow2.shelbie.core.console.JLineConsole;

/**
 * Created by donatien on 27/02/2014.
 */

/**
 *
 */
@Component
public class MsgReceiver implements MessageReceiver, DiscussionListener {

    private ChatService chat;

    private static final Logger LOG = LoggerFactory.getLogger(MsgReceiver.class);

    private Discussion conversation;

    private BundleContext context;

    @Property(name="jabber-ref")
    private ServiceReference reference;

    protected CommandSession cmd;

    @Requires
    private ChatCommandParser parser;

    @Requires
    protected CommandProcessor cmp;

    public MsgReceiver(BundleContext bundleContext){
        this.context=bundleContext;
    }

    @Validate
    public void start() {

        chat= getChat();
        cmd = cmp.createSession(System.in, System.out, System.err);
        chat.addDiscussionListener(this);
        LOG.info("Messages Receiver starts");

    }

    @Invalidate
    public void stop() {
        if (conversation != null) {
            conversation.unregisterMessageReceiver(this);
            conversation.close();
        }
        cmd.close();
    }

    public void onNewDiscussion(Discussion newConversation, List<String> participants) {
        LOG.info("On new discussion...");
        conversation = newConversation;
        newConversation.registerMessageReceiver(this);
    }

    public void onReceivedMessage(Discussion conversation, String from, String message, Map<String, Object> properties) {
        try {
            LOG.info("Has received " + message); // Do an echo
            String command = parser.parse(message);
            if(command==null){
                conversation.sendMessage("Unknown command");}
            else{
            String reponse= (String)cmd.execute(command);
            if(reponse==null){
                conversation.sendMessage("Erreur aucun device connecte!");
            }else{
                conversation.sendMessage(reponse);
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatService getChat(){
           return (ChatService)context.getService(reference);
    }
}