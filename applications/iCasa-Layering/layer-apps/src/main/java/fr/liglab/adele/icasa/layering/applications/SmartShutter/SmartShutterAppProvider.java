package fr.liglab.adele.icasa.layering.applications.SmartShutter;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShutterServiceInterface;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShuttersServiceImpl;
import fr.liglab.adele.icasa.location.LocatedObject;
import org.apache.felix.ipojo.annotations.*;

import java.util.List;

@Component(immediate = true,publicFactory = false)
@Instantiate
public class SmartShutterAppProvider {
    @Creator.Field Creator.Entity<SmartShutterAppImpl> smtrShuttApp;
   // @Creator.Field Creator.Entity<ShuttersServiceImpl> shuttsrv;

    @Creator.Field(SmartShutterAppImpl.RELATION_IS_ATTACHED) Creator.Relation<SmartShutterAppImpl,ShuttersServiceImpl> attachedSrv;

    @Validate
    public void start(){
        smtrShuttApp.create("smartShutterApp");
        System.out.println("STARTING SMART SHUTTER APP");
    }
    @Invalidate
    public void stop(){
        smtrShuttApp.delete("smartShutterApp");
    }

    @Requires(id="photo",optional = true,specification = Photometer.class, filter = "(locatedobject.object.zone=*)", proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    @SuppressWarnings("all")
    private List<Photometer> photos;



    @Bind(id="shuttersServiceImpl",specification = ShutterServiceInterface.class,aggregate = true, optional = true)
    @SuppressWarnings("all")
    public void bindShutterSrv(ShutterServiceInterface shuttersServiceImpl){
        System.out.println("smart shutter from APP");
      //  System.out.println(shuttsrv);
    }

    @Bind(id="photo",optional=true)
    public void bindPhotometer(Photometer photo){
        System.out.println("PHOTOMETER!!!");
        System.out.println(photo);
    }
}
