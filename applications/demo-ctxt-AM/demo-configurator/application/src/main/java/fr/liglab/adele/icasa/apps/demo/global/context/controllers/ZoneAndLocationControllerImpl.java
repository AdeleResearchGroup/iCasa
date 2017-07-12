package fr.liglab.adele.icasa.apps.demo.global.context.controllers;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.cream.model.Relation;
import fr.liglab.adele.icasa.apps.demo.global.Util;
import fr.liglab.adele.icasa.apps.demo.global.context.entities.DemoZoneImpl;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.impl.ZoneImpl;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class ZoneAndLocationControllerImpl implements ZoneController, LocationRelationsController {

    private final static Logger LOG = LoggerFactory.getLogger(ZoneAndLocationControllerImpl.class);

    private @Creator.Field(origin = OriginEnum.local)
    Creator.Entity<DemoZoneImpl> demoZoneCreator;

    private @Creator.Field(origin = OriginEnum.local, value = ZoneImpl.RELATION_CONTAINS)
    Creator.Relation<Zone,LocatedObject> containsCreator;

    private @Creator.Field(origin = OriginEnum.local, value = LocatedObjectBehaviorProvider.IS_IN_RELATION)
    Creator.Relation<LocatedObject,Zone> isContainedCreator;

    @Requires(id = "zones", specification = Zone.class, optional = true)
    @SuppressWarnings("all")
    List<Zone> zones;

    @Requires(id = "locatedObjects",specification = LocatedObject.class,optional = true,proxy = false)
    @SuppressWarnings("all")
    List<LocatedObject> locatedObjects;


    @Override
    public Set<String> getZones() {
        return demoZoneCreator.getInstances();
    }

    @Override
    public boolean addZone(String id) {
        if(Util.checkIfItExists(demoZoneCreator, id))
            return false;

        Map<String,Object> propertiesInit = new HashMap<>();
        propertiesInit.put(ContextEntity.State.id(Zone.class,Zone.NAME),id);
        /*Specific location to avoid autonomic matching from simulator*/
        propertiesInit.put(ContextEntity.State.id(Zone.class,Zone.X),100);
        propertiesInit.put(ContextEntity.State.id(Zone.class,Zone.Y),100);
        propertiesInit.put(ContextEntity.State.id(Zone.class,Zone.Z),0);
        propertiesInit.put(ContextEntity.State.id(Zone.class,Zone.X_LENGHT),20);
        propertiesInit.put(ContextEntity.State.id(Zone.class,Zone.Y_LENGHT),20);
        propertiesInit.put(ContextEntity.State.id(Zone.class,Zone.Z_LENGHT),5);
        demoZoneCreator.create(id, propertiesInit);
        LOG.info(Util.LOG_PREFIX + "Add zone: " + id );
        return true;
    }

    @Override
    public void removeZone(String id) {
        if(!Util.checkIfItExists(demoZoneCreator, id))
           return;

        removeLocationRelatedToZone(id);
        demoZoneCreator.delete(id);
        LOG.info(Util.LOG_PREFIX + "Remove zone: " + id );
    }


    @Override
    public Set<String> getLocatedObjectRelations() {
        Set<String> result = new HashSet<>();
        result.addAll(containsCreator.getInstances());
        result.addAll(isContainedCreator.getInstances());
        return result;
    }

    @Override
    public boolean attachLocatedObjectToZone(String zoneId, String locatedObjectId) {
        if(!Util.checkIfItExists(demoZoneCreator, zoneId))
            return false;
        /*ToDo check if locatedobject exist ? (really hard :( )*/

        containsCreator.create(zoneId,locatedObjectId);
        isContainedCreator.create(locatedObjectId, zoneId);
        LOG.info(Util.LOG_PREFIX + "Attach located object to zone: " + locatedObjectId + " - " + zoneId);
        return true;
    }

    @Override
    public boolean detachLocatedObjectFromZone(String zoneId, String locatedObjectId) {
        if(!Util.checkIfItExists(demoZoneCreator, zoneId))
            return false;
        /*ToDo check if locatedobject exist ? (really hard :( )*/

        containsCreator.delete(zoneId,locatedObjectId);
        isContainedCreator.delete(locatedObjectId, zoneId);
        LOG.info(Util.LOG_PREFIX + "Detach located object from zone: " + locatedObjectId + " - " + zoneId);
        return true;
    }

    @Override
    public void removeLocationRelatedToLocatedObject(String locatedObjectId) {
        List<Relation> relations = isContainedCreator.getInstancesRelatedTo(locatedObjectId);
        for (Relation relation:relations){
            String source = relation.getSource();
            String end = relation.getTarget();
            isContainedCreator.delete(source,end);
            containsCreator.delete(end,source);
        }
        LOG.info(Util.LOG_PREFIX + "Remove location relations linked to located object: " + locatedObjectId);
    }

    private void removeLocationRelatedToZone(String zoneId){
        List<Relation> relations = containsCreator.getInstancesRelatedTo(zoneId);
        for (Relation relation:relations){
            String source = relation.getSource();
            String end = relation.getTarget();
            containsCreator.delete(source,end);
            isContainedCreator.delete(end,source);
        }
        LOG.info(Util.LOG_PREFIX + "Remove location relations linked to zone: " + zoneId);
    }
}
