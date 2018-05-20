package fr.liglab.adele.icasa.layering.applications.shutterMgr;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.global.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShutterServiceInterface;
import fr.liglab.adele.icasa.location.LocatedObject;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ContextEntity(coreServices = {ApplicationLayer.class})

@SuppressWarnings("unused")
public class ShutterAppImpl implements ApplicationLayer{
  /*  public static final String RELATION_IS_ATTACHED="app.attached.to";
    private static final Logger LOG = LoggerFactory.getLogger(ShutterAppImpl.class);
    private static final String LOG_PREFIX = "SHUTTERS APP";

    @ContextEntity.State.Field(service = ApplicationLayer.class, state = ApplicationLayer.status, value = "init")
    public String stat;


    @Requires(id="shutterservices", optional=false)
    private List<ShutterServiceInterface> shutterServiceLayer;

    @Requires(id="photometers",optional = true,specification = Photometer.class, filter = "(locatedobject.object.zone=${servicelayer.zone.attached})", proxy = false)//shutterservices
    @ContextRequirement(spec = {LocatedObject.class})
    @SuppressWarnings("all")
    private List<Photometer> photometers;




    @Bind(id="shutterservices")
    public void bindShuttersServiceLayer(ShutterServiceInterface shutterservices){
        System.out.println("shutter binded: "+shutterservices);
        System.out.println(shutterservices.getServiceName());
        //shutterSrv.create(shutterservices.getServiceName());
        // shutterConn.create("ShutterManagementApp",shutterservices.getServiceName());
        //ZoneDeviceCollection.add(shutterservices.getServiceName());
//        shutterConn.create("ShutterManagementApp",shutterservices.getServiceName());

    }*/

  /* @Requires(id="photometersNL",optional = false,specification = Photometer.class)
   private List<Photometer> photometersNL;*/



/*
    @Bind(id="photometersNL", aggregate=true,optional = true)
    public void bindPhotometersNL(Photometer photometer){
        System.out.println("photo");
        System.out.println(photometer);
    }*/

   /* @Bind(id="photometers", aggregate=true,optional = true)
    public void bindPhotometers(Photometer photometer){
        System.out.println("LocatedPhoto");
        System.out.println(photometer);
    }*/



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
