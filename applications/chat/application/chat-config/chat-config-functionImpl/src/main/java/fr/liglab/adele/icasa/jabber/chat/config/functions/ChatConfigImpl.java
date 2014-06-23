package fr.liglab.adele.icasa.jabber.chat.config.functions;

/**
 * Created by donatien on 24/04/14.
 */
import fr.liglab.adele.icasa.jabber.chat.config.configInt.ChatConfig;
import org.apache.felix.ipojo.annotations.*;

@Component
@Provides
public class ChatConfigImpl implements ChatConfig {

    @Property(name="config.service", mandatory=true)
    public void setService(String service) {
        m_service = service;
    }

    @Property(name="config.user", mandatory=true)
    public void setUSer(String user) {
        m_user = user;
    }

    @Property(name="config.host", mandatory=true)
    public void setHost(String host) {
        m_host = host;
    }

    /*@Property(name="config.port", mandatory=true)
public void setPort(int port) {
m_port = port;
}*/

    @Property(name="config.pwd", mandatory=true)
    public void setPwd(String pwd) {
        m_password = pwd;
    }

    @ServiceProperty(name=ChatConfig.HOST_PROPERTY)
    private String m_host;

    /*@ServiceProperty(name=ChatConfig.PORT_PROPERTY)
private int m_port;*/

    @ServiceProperty(name=ChatConfig.SERVICE_PROPERTY)
    private String m_service;

    @ServiceProperty(name=ChatConfig.USER_PROPERTY)
    private String m_user;

    @ServiceProperty(name=ChatConfig.PASSWORD_PROPERTY)
    private String m_password;
    public void saveConfig(){

    }

    @Validate
    public void start() {
      System.out.println("Chat Configuration starts");
    }

    @Invalidate
    public void stop() {
        System.out.println("Chat Configuration stopped");
    }
}