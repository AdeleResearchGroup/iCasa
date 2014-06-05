package fr.liglab.adele.icasa.zigbee.device.importer;

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

@Component(name = "fr.liglab.adele.icasa.zigbee.device.importer.ZigbeeDeviceImporter")
@Instantiate(name = "fr.liglab.adele.icasa.zigbee.device.importer.ZigbeeDeviceImporter-0")
public class ZigbeeDeviceImporter extends AbstractImporterComponent {


    private static final Logger LOG = LoggerFactory.getLogger(ZigbeeDeviceImporter.class);

    //TODO FILTER
    @ServiceProperty(name = "target", value = "(=*)")
    private String filter;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @Override
    public String getName() {
        return null;
    }

    public ZigbeeDeviceImporter() {
        super();
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {


    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {


    }


    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void start() {
        LOG.info("ZigbeeDeviceImporter is up and running");
    }

    @Invalidate
    public void stop() {
        LOG.info("ZigbeeDeviceImporter is stopping");
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
