package fr.liglab.adele.icasa.apps.demo.light.follow.me.context.controllers;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.icasa.apps.demo.global.Util;
import fr.liglab.adele.icasa.apps.demo.global.context.controllers.LocationRelationsController;
import fr.liglab.adele.icasa.apps.demo.light.follow.me.app.AppManager;
import fr.liglab.adele.icasa.apps.demo.light.follow.me.context.entities.DemoBinaryLightImpl;
import fr.liglab.adele.icasa.device.GenericDevice;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class LightControllerImpl implements LightController {

    private final static Logger LOG = LoggerFactory.getLogger(PushButtonControllerImpl.class);

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.local)
    Creator.Entity<DemoBinaryLightImpl> demoBinaryLightCreator;

    @Requires
    private LocationRelationsController locationRelationsController;

    @Override
    public Set<String> getLights() {
        return demoBinaryLightCreator.getInstances();
    }

    @Override
    public boolean addLight(String id) {
        if(Util.checkIfItExists(demoBinaryLightCreator, id))
            return false;

        Map<String,Object> propertiesInit = new HashMap<>();
        propertiesInit.put(ContextEntity.State.id(GenericDevice.class,GenericDevice.DEVICE_SERIAL_NUMBER),id);
        demoBinaryLightCreator.create(id, propertiesInit);
        LOG.info(AppManager.LOG_PREFIX + "Add light: " + id );
        return true;
    }

    @Override
    public void removeLight(String id) {
        if(!Util.checkIfItExists(demoBinaryLightCreator, id))
            return;

        locationRelationsController.removeLocationRelatedToLocatedObject(id);
        demoBinaryLightCreator.delete(id);
        LOG.info(AppManager.LOG_PREFIX + "Remove light: " + id );
    }
}
