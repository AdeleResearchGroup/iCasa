package fr.liglab.adele.icasa.layering.services.shutterOpening;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.layering.services.global.ServiceLayer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//@CommandProvider(namespace = "Layer-applications")
@ContextEntity(coreServices = {ServiceLayer.class,ShutterServiceInterface.class})
@SuppressWarnings("unused")
public class ShuttersServiceImpl implements ServiceLayer,ShutterServiceInterface{


    public static final String RELATION_IS_ATTACHED="model.attached.to";
    private static final Logger LOG = LoggerFactory.getLogger(ShuttersServiceImpl.class);
    private static final String LOG_PREFIX = "SHUTTER_SERVICE";
    private static final Integer MIN_QOS = 100;

    //states from ServiceLayer interface
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.CURRENT_STATE,value="init")
    public String status;
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS, directAccess = true, value="0")
    public int AppQos;
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.ZONE_ATTACHED)
    public String zoneName;
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.LOCKED)
    public String lockstatus;
    @ContextEntity.State.Field(service=ServiceLayer.class,state = ServiceLayer.NAME,directAccess = true)
    public String name;

    //State from GenericDevice
   /* @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;*/

    //State from Devices
   /* @ContextEntity.State.Field(service = WindowShutter.class,state = SHUTTER_LEVEL,directAccess = true)
    private Double shutterlevel;*/


    private final static String taskName = "home-shutters";
    private boolean isReconfiguring = false;
    private boolean needToBeReconfigured = true;

    //4 integration with context
    private static boolean registered;

    @Validate
    @SuppressWarnings("unused")
    public void start(){
       System.out.println("maname"+this.name);
        //toDo
        //registered=false;
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop(){if (registered){
        //ToDO
    }
    }

    //REQUIREMENTS
    @Requires(id="windowshutters",optional = true, specification = WindowShutter.class, filter = "(locatedobject.object.zone=${servicelayer.zone.attached})", proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    @SuppressWarnings("all")
    private List<WindowShutter> windowshutters;

    //relations
    @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone",specification=Zone.class,optional=false)
    Zone zone;

    @Bind(id = "zone")
    public void bindZone(Zone zone){
        pushZone(zone.getZoneName());
        name=zone.getZoneName()+".shutters";
        System.out.println("wzone:"+name);
        //System.out.println(zoneName+"IN SHUTTER APP!!");

    }

    @ContextEntity.State.Push(service = ServiceLayer.class,state = ServiceLayer.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    //WINDOWSHUTTERS*************************************************************************************************************************
    @Bind(id="windowshutters")
    @SuppressWarnings("all")
    public void bindWindowShutter(){
        System.out.println(windowshutters.get(0).getShutterLevel());
        windowshutters.get(0).setShutterLevel(0.6);

        System.out.println(windowshutters.get(0).getShutterLevel());

        System.out.println("binded shutter to service in zone!!"+windowshutters);
    }
    @Unbind(id="windowshutters")
    @SuppressWarnings("all")
    public void unbindWindowShutter(WindowShutter windowShutter){
        System.out.println("unbinding photo to service in zone!!"+windowshutters);
    }
    @Modified(id="windowshutters")
    @SuppressWarnings("all")
    public void modifyWindowShutter(){
       // setState(windowshutters.size()*22);
       // LOG.info("shutLVL: "+this.getShutterLevel());
    }

   // @Command
   @Override
    @SuppressWarnings("unused")
    public boolean setShutterSLevel(Double level){
        if((0 <= level) && (level <= 1)){
            System.out.println(windowshutters);
            for(int p=0;p<windowshutters.size();p++){
                windowshutters.get(p).setShutterLevel(level);
            }
            return true;
        }else{
            LOG.error("Binder level should be between 0 and 1, not: "+level);
            return false;
        }
    }

    public boolean setIndividualState(int index, Double level){
        if((0 <= level) && (level <= 1)&&(index<windowshutters.size())){
            windowshutters.get(index).setShutterLevel(level);
            return true;
        }else{
            LOG.error("Malformed function call parameters (index, level)index should be less than "+windowshutters.size()+"Binder level should be between 0 and 1, not: "+level);
            return false;
        }
    }

    public Double getShutterSlevel(int index) {
        Double averagelevel=0.0;
        if (index<windowshutters.size()){
            for(WindowShutter ws:windowshutters){
                averagelevel+=ws.getShutterLevel();
            }
            return averagelevel;
        }
        return averagelevel;
    }

  /*  @Override
    public double getShutterLevel() {
        return shutterlevel;
    }

    @Override
    public void setShutterLevel(double lvl) {
        if((0 <= lvl) && (lvl <= 1)){
            shutterlevel = lvl;
        }else{
            LOG.error("Binder level should be between 0 and 1, not: "+lvl);
        }
    }

    @Override
    public String getSerialNumber() {
        return null;
    }*/

    @Override
    public int getMinQos() {
        return MIN_QOS;
    }


    @Override
    public String getServiceName() {
        return name;
    }

    @Override
    public boolean getRegistrationState() {
        return false;
    }

    @Override
    public int getServiceQoS() {
        if(windowshutters.isEmpty()){
            AppQos=0;
            return AppQos;
        }else{
            AppQos=100;
            return AppQos;
        }
    }

    @Override
    public String getState() {
        if(getServiceQoS()==100){
            status="Shutters found";
        }else{
            status="No shutters found";
        }
        return status;
    }

    @Override
    public String setState(int Openness) {
        int QoS=getServiceQoS();
        if((0 <= Openness) && (Openness <= 1)){
            LOG.error("The value has to be between 0 &nd 1, not: "+Openness);
            return null;
        }else if(QoS==100){
            for (int p=0; p<windowshutters.size();p++){
                windowshutters.get(p).setShutterLevel(Openness);
            }
            return "set to "+Openness;
        }else{
            return "unknown state";
        }
    }

}
