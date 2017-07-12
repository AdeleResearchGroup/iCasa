package fr.liglab.adele.icasa.apps.demo.light.follow.me.context.controllers;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.icasa.apps.demo.global.Util;
import fr.liglab.adele.icasa.apps.demo.global.context.controllers.LocationRelationsController;
import fr.liglab.adele.icasa.apps.demo.light.follow.me.app.AppManager;
import fr.liglab.adele.icasa.apps.demo.light.follow.me.context.entities.DemoPushButtonImpl;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.location.LocatedObject;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class PushButtonControllerImpl implements PushButtonController {

    private final static Logger LOG = LoggerFactory.getLogger(PushButtonControllerImpl.class);

    @Requires(id="buttons", optional = true, specification = PushButton.class)
    @SuppressWarnings("all")
    private List<PushButton> buttons;

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.local)
    Creator.Entity<DemoPushButtonImpl> demoPushButtonCreator;

    @Requires
    private LocationRelationsController locationRelationsController;

    @Override
    public Set<String> getButtons() {
        return demoPushButtonCreator.getInstances();
    }

    @Override
    public boolean addButton(String id) {
        if(Util.checkIfItExists(demoPushButtonCreator, id))
            return false;

        Map<String,Object> propertiesInit = new HashMap<>();
        propertiesInit.put(ContextEntity.State.id(GenericDevice.class,GenericDevice.DEVICE_SERIAL_NUMBER),id);
        demoPushButtonCreator.create(id, propertiesInit);
        LOG.info(AppManager.LOG_PREFIX + "Add push button: " + id );
        return true;
    }

    @Override
    public void removeButton(String id) {
        if(!Util.checkIfItExists(demoPushButtonCreator, id))
            return;

        locationRelationsController.removeLocationRelatedToLocatedObject(id);
        demoPushButtonCreator.delete(id);
        LOG.info(AppManager.LOG_PREFIX + "Remove push button: " + id );
    }

    @Override
    public void pushAllButtons(){
        buttons.forEach(PushButton::push);
    }

    @Override
    public void pushButton(String id) {
        if(id == null)
            return;
        for(PushButton b : buttons){
            if(id.equals(b.getSerialNumber()))
                if(b instanceof DemoPushButtonImpl)
                    b.push();
        }
    }

    @Override
    public void pushButtonInZone(String zone){
        if(zone == null)
            return;
        for(PushButton b : buttons){
            if(b instanceof LocatedObject){
                if(zone.equals(((LocatedObject)b).getZone()))
                    if(b instanceof DemoPushButtonImpl){
                        b.push();
                        break;
                    }
            }
        }
    }
}
