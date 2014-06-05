package fr.liglab.adele.icasa.zigbee.dongle.importer;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component(name = "fr.liglab.adele.icasa.zigbee.dongle.importer.ZigbeeDongleImporter")
@Instantiate(name = "fr.liglab.adele.icasa.zigbee.dongle.importer.ZigbeeDongleImporter-0")
public class ZigbeeDongleImporter extends AbstractImporterComponent {


    private static final Logger LOG = LoggerFactory.getLogger(ZigbeeDongleImporter.class);

    private final Map<String,ServiceRegistration> zigbeeDongles = new HashMap<String, ServiceRegistration>();

    //TODO FILTER
    @ServiceProperty(name = "target", value = "(=*)")
    private String filter;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @Requires(filter = "(factory.name=fr.liglab.adele.icasa.zigbee.dongle.factory.ZigbeeDongleFactory)")
    private Factory zigbeeDongleFactory;

    @Override
    public String getName() {
        return null;
    }

    public ZigbeeDongleImporter() {
        super();
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("Zigbee Dongle Discovery importer triggered");

        ComponentInstance instance;

        Hashtable properties = new Hashtable();
        properties.put("zigbee.dongle.id", importDeclaration.getMetadata().get("usb.discovery.id"));
        properties.put("zigbee.dongle.object", importDeclaration.getMetadata().get("usb.discovery.device.object"));

        try {
            instance = zigbeeDongleFactory.createComponentInstance(properties);
            ServiceRegistration sr = new IpojoServiceRegistration(
                    instance);
            super.handleImportDeclaration(importDeclaration);

            zigbeeDongles.put((String)importDeclaration.getMetadata().get("usb.discovery.id"),sr);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error("Proxy instantiation failed",unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error("Proxy instantiation failed",e);
        } catch (ConfigurationException e) {
            LOG.error("Proxy instantiation failed",e);
        }
    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        try {
            zigbeeDongles.remove(importDeclaration.getMetadata().get("usb.discovery.id")).unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering lamp", e);
        }

        unhandleImportDeclaration(importDeclaration);

    }


    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void start() {
        LOG.info("ZigbeeDongleImporter is up and running");
    }

    @Invalidate
    public void stop() {
        LOG.info("ZigbeeDongleImporter is stopping");
    }


    class IpojoServiceRegistration implements ServiceRegistration {

        ComponentInstance instance;

        public IpojoServiceRegistration(ComponentInstance instance) {
            super();
            this.instance = instance;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.osgi.framework.ServiceRegistration#getReference()
         */
        public ServiceReference getReference() {
            try {
                ServiceReference[] references = instance.getContext()
                        .getServiceReferences(
                                instance.getClass().getCanonicalName(),
                                "(instance.name=" + instance.getInstanceName()
                                        + ")");
                if (references.length > 0)
                    return references[0];
            } catch (InvalidSyntaxException e) {
                LOG.error(" Invalid syntax Exception " , e);
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.osgi.framework.ServiceRegistration#setProperties(java.util.Dictionary
         * )
         */
        public void setProperties(Dictionary properties) {
            instance.reconfigure(properties);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.osgi.framework.ServiceRegistration#unregister()
         */
        public void unregister() {
            instance.dispose();
        }

    }



}
