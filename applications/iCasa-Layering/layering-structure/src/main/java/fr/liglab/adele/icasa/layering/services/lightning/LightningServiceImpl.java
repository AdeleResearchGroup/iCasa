package fr.liglab.adele.icasa.layering.services.lightning;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.layering.services.global.ServiceLayer;
//import fr.liglab.adele.icasa.simulator.model.api.LuminosityModel;
import org.apache.felix.ipojo.annotations.*;
import fr.liglab.adele.icasa.device.light.BinaryLight;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//ToDo Remove Photometer implemantetion (it's only for simulation)
@ContextEntity(coreServices = {ServiceLayer.class,LightningServiceInterface.class})
@SuppressWarnings("unused")
public class LightningServiceImpl implements ServiceLayer, LightningServiceInterface {
    public static final String RELATION_IS_ATTACHED="model.attached.to";
    private static final Logger LOG = LoggerFactory.getLogger(LightningServiceImpl.class);
    private static final String LOG_PREFIX = "LIGHTNING_SERVICE";
    private static final Integer MIN_QOS = 34;

    //states from ServiceLayer interface
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.CURRENT_STATE,value="init")
    public String status;
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS, directAccess = true, value="0")
    public int AppQos;
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.ZONE_ATTACHED)
    public String zoneName;
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.LOCKED)
    public String lockstatus;
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.NAME,directAccess = true)
    public String name;

    //State from GenericDevice
   /* @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;*/

    //States from ServiceLayer implementations
   /* @ContextEntity.State.Field(service = Photometer.class,state=PHOTOMETER_CURRENT_ILLUMINANCE,directAccess = true)
    private Quantity<Illuminance> currentSensedIlluminance;*/
   // @ContextEntity.State.Field(service = WindowShutter.class,state = SHUTTER_LEVEL,directAccess = true)
   //private Double shutterlevel;



    private final static String taskName = "home-lighting";
    // private static TaskStateEnum taskState = TaskStateEnum.INIT;
    // private static TaskStateEnum previousTaskState = TaskStateEnum.INIT;
    private boolean isReconfiguring = false;
    private boolean needToBeReconfigured = true;

    //4 integration with context
    private static boolean registered;

    @Validate
    @SuppressWarnings("unused")
    public void start(){
        System.out.println();
        System.out.println("Lightning service started");
        //toDo
        //registered=false;
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop(){if (registered){
        //ToDO
    }
    }

    //requirements
    @Requires(id="lights",optional = true,specification = BinaryLight.class,filter = "(locatedobject.object.zone=${LightningServiceInterface.zone.attached})",proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    @SuppressWarnings("all")
    private List<BinaryLight> binaryLights;

    @Requires(id="photometers",optional = true,specification = Photometer.class, filter = "(locatedobject.object.zone=${LightningServiceInterface.zone.attached})", proxy = false)
    @SuppressWarnings("all")
    private List<Photometer> photometers;

   /* @Requires(id="windowshutters",optional = true, specification = WindowShutter.class, filter = "(locatedobject.object.zone=${LightningServiceInterface.zone.attached})", proxy = false)
    @SuppressWarnings("all")
    private List<WindowShutter> windowshutters;*/

    //relations
    @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone",specification=Zone.class,optional=false)
    Zone zone;

    @Bind(id = "zone")
    public void bindZone(Zone zone){
        pushZone(zone.getZoneName());
        System.out.println(zone.getZoneName());
        getExternalLUM();
    }
    @Modified(id = "zone")
    public void modifyZone(Zone zone){
        System.out.println("____ZONEname: "+zone.getZoneName());
        System.out.println("completion: "+getServiceQoS());
    }

    @ContextEntity.State.Push(service = ServiceLayer.class,state = ServiceLayer.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }
//LIGHTS*******************************************************************************************************************************
    @Bind(id="lights")
    @SuppressWarnings("all")
    public void bindBinaryLight(){
        getServiceQoS();
        //System.out.println("-LIGHT attached to "+zone.getZoneName());
        binaryLights.get(0).setPowerStatus(true);
    }
    @Unbind(id="lights")
    @SuppressWarnings("all")
    public synchronized void unbindBinaryLight(BinaryLight binaryLight){
        getServiceQoS();
        if(binaryLights.isEmpty()){
            LOG.info("NO lights in zone: "+zoneName+" !");
        }
    }
    @Modified(id="lights")
    @SuppressWarnings("all")
    public void modifyBinaryLight(){
        System.out.println(binaryLights.size());
        System.out.println(binaryLights);
        LightState();
    }
//PHOTOMETERS***************************************************************************************************************************
    @Bind(id="photometers")
    @SuppressWarnings("all")
    public void bindPhotometer(){
        getServiceQoS();
        //System.out.println("Number of lights: "+binaryLights.size());
       // System.out.println("binding photo to lighthing!!"+photometers);
       // System.out.println(photometers.get(0).getIlluminance());

    }
    @Unbind(id="photometers")
    @SuppressWarnings("all")
    public synchronized void unbindPhotometer(Photometer photometer){
        getServiceQoS();
    }
    @Modified(id="photometers")
    @SuppressWarnings("all")
    public void modifyIlluminance(){
       // System.out.println("ilumi"+getIlluminance());
        //return Quantities.getQuantity(illuminance, Units.LUX);
    }//@ContextEntity.State.Push(service = Photometer.class,state = Photometer.PHOTOMETER_CURRENT_ILLUMINANCE)
//WINDOWSHUTTERS*************************************************************************************************************************
  /*  @Bind(id="windowshutters")
    @SuppressWarnings("all")
    public void bindWindowShutter(){
        windowshutters.get(0).setShutterLevel(0.6);
        System.out.println("binding photo to lighthing!!"+windowshutters);
    }
    @Unbind(id="windowshutters")
    @SuppressWarnings("all")
    public void unbindWindowShutter(WindowShutter windowShutter){
        System.out.println("binding photo to lighthing!!"+windowshutters);
    }
    @Modified(id="windowshutters")
    @SuppressWarnings("all")
    public void modifyWindowShutter(){
        setState(windowshutters.size()*22);
        LOG.info("shutLVL: ");
    }*/


    public String getExternalLUM(){

        return null;
    }

    @Override
    public int getMinQos() {
        return MIN_QOS;
    }


    @Override
    public String getServiceName() {
        return null;
    }

    @Override
    public boolean getRegistrationState() {
        return false;
    }

    @Override
    public int getServiceQoS() {
        AppQos=0;
        if(binaryLights.size()>0){
            AppQos+=35;
        }
      /*  if(windowshutters.size()>0){
            AppQos+=34;
        }*/
        if(photometers.size()>0){
            AppQos+=31;
        }
        //AppQos=String.valueOf(srvCompl);
        int a;
        return AppQos;
    }

    @Override
    public String getState() {//overrides ServiceLayer Interface
        if(getServiceQoS()==35){//if only lights are present
           // status=getIlluminance().toString();
        }else if(getServiceQoS()==34){//if only shutters are present
            status="to get from External source";
        }else if(getServiceQoS()==31){//if only photometers are present
            status="min QoS not reached";
        }
        else if(getServiceQoS()==100){//everything is present
           // status=getIlluminance().toString();
        }
        return status;
    }

    @Override
    public String setState(int illumination) {
        int QoS=getServiceQoS();
        LOG.info("setting State"+illumination);
        if (illumination>100 || illumination<0){
            LOG.error("the value for illumination has to be between 0 and 100, received:"+illumination+" and QoS: "+getServiceQoS());
        }else if (QoS==35){
            for(int i=0;i<binaryLights.size();i++){
                if (i<binaryLights.size()*illumination/100){
                    binaryLights.get(i).turnOn();
                }else{
                    binaryLights.get(i).turnOff();
                }
            }

        }else if(QoS==34){
            //toDo Implement shutter oppening
        }else if(QoS==67){
            //toDo Implement shutter+lights
        }
        else{
            LOG.info("not in any case");
        }
        return null;
    }

    private int LightState(){
        int LightsOn=0;
        for(int a=0; a<binaryLights.size();a++){
            if(binaryLights.get(a).getPowerStatus()){
                LightsOn+=1;
            }
        }
        return LightsOn;
        //System.out.println("percentL: "+(((double)LightsOn/(double)binaryLights.size())*100));
        //return (((double)LightsOn/(double)binaryLights.size()));
    }

   /* @Override
    public Quantity<Illuminance> getIlluminance() {
        //gives 500 lux per light
        currentSensedIlluminance=Quantities.getQuantity(LightState()*500, Units.LUX);
        LOG.debug("get Illuminance function returns: "+currentSensedIlluminance);
        //currentSensedIlluminance= "50";
        return currentSensedIlluminance;

      //  return LightState()*500;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }*/

   /* @Override
    public double getShutterLevel() {
        return 0;
    }

    @Override
    public void setShutterLevel(double v) {

    }*/
}
