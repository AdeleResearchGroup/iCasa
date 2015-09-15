package fr.liglab.adele.icasa.context.model.example;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.context.model.RelationFactory;
import fr.liglab.adele.icasa.location.*;
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
public class ZoneDiscovery implements ZoneListener,LocatedDeviceListener {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceDiscovery.class);

    private final Map<String,ServiceRegistration> zoneEntities = new HashMap<>();

    @Requires
    private ContextManager m_manager;

    @Requires
    private RelationFactory m_relationFactory;

    @Requires(filter = "(factory.name=fr.liglab.adele.icasa.context.model.example.ZoneContextEntity)")
    private Factory zoneEntityFactory;

    @Validate
    public void start(){
        m_manager.addListener(this);
    }

    @Invalidate
    public void stop(){

    }

    @Override
    public void zoneAdded(Zone zone) {
        ComponentInstance instance;

        Hashtable properties = new Hashtable();
        properties.put("context.entity.id", zone.getId());

        try {
            instance = zoneEntityFactory.createComponentInstance(properties);
            ServiceRegistration sr = new IpojoServiceRegistration(
                    instance);
            zoneEntities.put(zone.getId(),sr);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error("Relation instantiation failed",unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error("Relation instantiation failed",e);
        } catch (ConfigurationException e) {
            LOG.error("Relation instantiation failed",e);
        }
    }

    @Override
    public void zoneRemoved(Zone zone) {
        try {
            zoneEntities.remove(zone.getId()).unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering zone Entity", e);
        }
    }

    @Override
    public void zoneMoved(Zone zone, Position oldPosition, Position newPosition) {

    }

    @Override
    public void zoneResized(Zone zone) {

    }

    @Override
    public void zoneParentModified(Zone zone, Zone oldParentZone, Zone newParentZone) {

    }

    @Override
    public void deviceAttached(Zone container, LocatedDevice child) {

    }

    @Override
    public void deviceDetached(Zone container, LocatedDevice child) {

    }

    @Override
    public void zoneVariableAdded(Zone zone, String variableName) {

    }

    @Override
    public void zoneVariableRemoved(Zone zone, String variableName) {

    }

    @Override
    public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue) {

    }

    @Override
    public void deviceAdded(LocatedDevice device) {

    }

    @Override
    public void deviceRemoved(LocatedDevice device) {

    }

    @Override
    public void deviceMoved(LocatedDevice device, Position oldPosition, Position newPosition) {
        if (getZones(oldPosition).isEmpty()){
            for (Zone zone : getZones(newPosition)){
                LOG.info(" Discovery create relation");
                m_relationFactory.createRelation("isContained",device.getSerialNumber(),zone.getId());
                m_relationFactory.createRelation("contained",zone.getId(),device.getSerialNumber());
            }
        }else {
            boolean delete = getZones(newPosition).isEmpty();
            for (Zone oldZone : getZones(oldPosition)){
                if (delete){
                    LOG.info(" Discovery delete relation");
                    m_relationFactory.deleteRelation("isContained", device.getSerialNumber(), oldZone.getId());
                    m_relationFactory.deleteRelation("contained", oldZone.getId(), device.getSerialNumber());
                }else {
                    for (Zone newZone : getZones(newPosition)) {
                        LOG.info(" Discovery update relation");
                        m_relationFactory.updateRelation("isContained", device.getSerialNumber(), oldZone.getId(), device.getSerialNumber(), newZone.getId());
                        m_relationFactory.updateRelation("contained", oldZone.getId(), device.getSerialNumber(), newZone.getId(), device.getSerialNumber());
                    }
                }
            }
        }
    }

    private Set<Zone> getZones(Position devicePosition) {
        List<Zone> zones = m_manager.getZones();
        Set<Zone> zonesToUpdate = new HashSet<Zone>();
        for (Zone zone : zones) {
            if (zone.contains(devicePosition))
                zonesToUpdate.add(zone);
        }
        return zonesToUpdate;
    }

    @Override
    public void devicePropertyModified(LocatedDevice device, String propertyName, Object oldValue, Object newValue) {

    }

    @Override
    public void devicePropertyAdded(LocatedDevice device, String propertyName) {

    }

    @Override
    public void devicePropertyRemoved(LocatedDevice device, String propertyName) {

    }

    @Override
    public void deviceAttached(LocatedDevice container, LocatedDevice child) {

    }

    @Override
    public void deviceDetached(LocatedDevice container, LocatedDevice child) {

    }

    @Override
    public void deviceEvent(LocatedDevice device, Object data) {

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

    }

}
