package fr.liglab.adele.icasa.context.model.example;


import fr.liglab.adele.icasa.device.GenericDevice;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
@Instantiate
public class DeviceDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceDiscovery.class);

    private final Map<String,ServiceRegistration> deviceEntities = new HashMap<>();

    @Requires(filter = "(factory.name=fr.liglab.adele.icasa.context.model.example.DeviceContextEntity)")
    Factory deviceEntityFactory;

    @Validate
    public void start(){

    }

    @Invalidate
    public void stop(){

    }


    @Bind(id = "devices",optional = true,aggregate = true)
    public synchronized void bindDevices(GenericDevice device){
        ComponentInstance instance;

        Hashtable properties = new Hashtable();
        properties.put("context.entity.id", device.getSerialNumber());

        List<List<String>> state = new ArrayList<>();
        List<String> property_array;

        for (String property : device.getProperties()){
            property_array = new ArrayList<>();
            property_array.add(property);
            property_array.add(device.getPropertyValue(property).toString());
            state.add(property_array);
        }
        properties.put("context.entity.state", state);

        List<List<String>> array_test = new ArrayList<>();
        List<String> a = new ArrayList<>();
        a.add("aiefhauf");
        a.add("spduivspbv");
        array_test.add(a);
        a = new ArrayList<>();
        a.add("isContained");
        a.add("kitchen");
        array_test.add(a);
        properties.put("context.entity.array", array_test);

        try {
            instance = deviceEntityFactory.createComponentInstance(properties);
            ServiceRegistration sr = new IpojoServiceRegistration(
                    instance,
                    state);

            deviceEntities.put(device.getSerialNumber(),sr);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error("Relation instantiation failed",unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error("Relation instantiation failed",e);
        } catch (ConfigurationException e) {
            LOG.error("Relation instantiation failed",e);
        }

    }

    @Unbind(id = "devices",optional = true,aggregate = true)
    public synchronized void unbindDevices(GenericDevice device){
        try {
            deviceEntities.remove(device.getSerialNumber()).unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering device", e);
        }
    }


    class IpojoServiceRegistration implements ServiceRegistration {

        ComponentInstance instance;
        private final List<List<String>> state;

        public IpojoServiceRegistration(ComponentInstance instance, List<List<String>> state) {
            super();
            this.instance = instance;
            this.state = state;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.osgi.framework.ServiceRegistration#getReference()
         */
        public ServiceReference getReference() {
            try {
                ServiceReference[] references;
                references = instance.getContext().getServiceReferences(
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

        //TODO : To implement
        public void updateState(){}

    }
}


