package org.ow2.chameleon.fuchsia.importer.xmpp;


import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.chat.ChatService;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class XMPPImporter extends AbstractImporterComponent {

    @Requires(filter ="(factory.name=org.ow2.chameleon.chat.jabber)")
    private Factory jabberFactory;

    @Requires(filter ="(factory.name=fr.liglab.adele.icasa.apps.jabber.chat.message.MsgReceiver)")
    private Factory receiverFactory;

    private ComponentInstance instanceReceiver=null;

    private ComponentInstance instanceJabber=null;

    private static final Logger LOG = LoggerFactory.getLogger(XMPPImporter.class);

    private Map<String,Object> metadata;

    @ServiceProperty(name = "target", value = "(protocol=XMPP)")
    private String filter;

    @ServiceProperty(name = "instance.name")
    private String name;

    private final BundleContext context;

    private ServiceReference serviceReference;

    public XMPPImporter(BundleContext pContext) {
        context=pContext;

    }


    @Invalidate
    protected void stop() {
        LOG.info("STOP XMPP IMPORTER SERVICE");
        if (instanceJabber != null) instanceJabber.dispose();
        if (instanceJabber != null) instanceReceiver.dispose();
        super.stop();
    }


    @Validate
    protected void start() {
        LOG.info("START XMPP IMPORTER SERVICE");
        super.start();

    }


    /**
     * Return the name of the instance
     * @return name of this instance
     */
    public String getName() {
        return name;
    }

	/*--------------------------*
	 * ImporterService methods  *
	 *--------------------------*/

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) {
        metadata = importDeclaration.getMetadata();
        Properties jabberProps = new Properties();
        jabberProps.put("jabber.host",metadata.get("host"));
        jabberProps.put("jabber.service",metadata.get("service"));
        jabberProps.put("jabber.user",metadata.get("username"));
        jabberProps.put("jabber.password",metadata.get("password"));

        try {
            instanceJabber = jabberFactory.createComponentInstance(jabberProps);
            if (instanceJabber!=null){
                Properties receiverProps = new Properties();
                ServiceReference[] refs =
                        context.getServiceReferences(ChatService.class.getName(),"(instance.name=" + instanceJabber.getInstanceName() +")");
                if (refs != null) {
                    LOG.info(refs[0].toString());
                    receiverProps.put("jabber-ref",refs[0]);
                    instanceReceiver=receiverFactory.createComponentInstance(receiverProps);
                }

            }
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            unacceptableConfiguration.printStackTrace();
        } catch (MissingHandlerException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Destroy the proxy & update the map containing the registration ref
     * @param importDeclaration
     */
    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) {

    }

    protected Logger getLogger() {
        return LOG;
    }
}
