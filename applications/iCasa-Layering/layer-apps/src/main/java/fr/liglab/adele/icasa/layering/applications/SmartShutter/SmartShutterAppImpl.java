package fr.liglab.adele.icasa.layering.applications.SmartShutter;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer;
import fr.liglab.adele.icasa.layering.applications.lightning.HomeLightningAppImpl;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShutterServiceInterface;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShuttersServiceImpl;
import fr.liglab.adele.icasa.location.LocatedObject;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ContextEntity(coreServices = ApplicationLayer.class)
@SuppressWarnings("unused")
public class SmartShutterAppImpl implements ApplicationLayer {
    public static final String RELATION_IS_ATTACHED="app.attached.to";
    private static final Logger LOG = LoggerFactory.getLogger(SmartShutterAppImpl.class);
    private static final String LOG_PREFIX = "SMART SHUTTERS APP";

    @ContextEntity.State.Field(service = ApplicationLayer.class,state = ApplicationLayer.status,value="init")
    public String stat;

   @Requires(id="shutterservices",optional = true,specification = ShuttersServiceImpl.class)
    private List<ShutterServiceInterface> shutterService;

  /*  @Requires(id="photo",optional = true,specification = Photometer.class, filter = "(locatedobject.object.zone=${servicelayer.zone.attached})", proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    @SuppressWarnings("all")
    private List<Photometer> photos;*/


    @Override
    public boolean getStatus() {
        return false;
    }

    @Override
    public boolean setStaus() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getProvider() {
        return null;
    }
}
