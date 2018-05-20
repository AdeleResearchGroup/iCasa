package fr.liglab.adele.icasa.layering.applications.shutterMgr;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShutterServiceInterface;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShuttersServiceImpl;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.*;

import java.util.*;

//@CommandProvider(namespace = "shutter-app")
@Component(immediate =true,publicFactory = false)
@Instantiate
public class ShutterAppProvider {

 /*   @Creator.Field Creator.Entity<ShutterAppImpl> shutterApp;
    @Creator.Field Creator.Entity<ShuttersServiceImpl> shutterSrv;

    @Creator.Field (ShutterAppImpl.RELATION_IS_ATTACHED) Creator.Relation<ShutterAppImpl,ShuttersServiceImpl> attachedShutterSrv;

    boolean initLocalPhotometer=false;
    List<String> ZoneDeviceCollection=new ArrayList<>();
    List<String> measuredZones=new ArrayList<>();
    List<String> locatedLocalShutters=new ArrayList<>();
    Position zoneposition;
    boolean firstIOP=false;

    Set<String> transition = new HashSet<>();*/


  //  @Creator.Field(ShutterAppImpl.RELATION_IS_ATTACHED) Creator.Relation<ShutterAppImpl,ShuttersServiceImpl> shutterConn;



    @Validate
    public void start(){
       // shutterApp.create("ShutterManagementApp");

       // shutterSrv.create("ShutterSrv");
    }
    @Invalidate
    public void stop(){
        // shutterApp.delete("ShutterManagementApp");
        }


  /*  @Bind(id="shuttersServiceImpl",specification = ShuttersServiceImpl.class,aggregate = true,optional = true)
    @SuppressWarnings("all")
    public void bindShutterSrv(ShutterServiceInterface shuttersServiceImpl){
       System.out.println("added Shutter srv from ShutterApp");
        // shutterConn.create("ShutterManagementApp", shuttersServiceImpl);
    }*/

    /* @Requires(id="photometers",optional = true,specification = Photometer.class, filter = "(locatedobject.object.zone=${servicelayer.zone.attached})", proxy = false)//shutterservices
    @ContextRequirement(spec = {LocatedObject.class})
    @SuppressWarnings("all")
    private List<Photometer> photometers;*/

    //relations
   // @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
   /* @Requires(id="zone",specification=Zone.class,optional=false)
    Zone zone;*/

/*    @Requires(id="zone", specification = Zone.class,optional=true)
    private List<Zone> zone;*/

   /* @Requires(id="shutterservices", optional=true)
    private List<ServiceLayer> shutterServiceLayer;*/

/*    @Requires(optional = true, proxy = false, id="iop")
    IOPLookupService lookup;

    @Requires(optional = true, proxy = false)
    LocationService location;


    private List<String> photosZ;


    @Bind(id="iop")
    public void bindIop(IOPLookupService lookup){
        System.out.println("iop binded...");
        lookup.consider(new String[]{LocationService.class.getName()}, Collections.emptyMap());
    }*/

   /* @Bind(id="shutterservices")
    public void bindShuttersServiceLayer(ServiceLayer shutterservices){
        System.out.println("shutter binded: "+shutterservices);
        System.out.println(shutterservices.getServiceName());
        //shutterSrv.create(shutterservices.getServiceName());
       // shutterConn.create("ShutterManagementApp",shutterservices.getServiceName());
        //ZoneDeviceCollection.add(shutterservices.getServiceName());
//        shutterConn.create("ShutterManagementApp",shutterservices.getServiceName());

    }*/


  /* @Bind(id="zone")
    @SuppressWarnings("all")
    public void bindZone(){
       // System.out.println(zone.get(0).getZoneName());
    }
    @Modified(id="zone")
    @SuppressWarnings("all")
    public void modifyzone(Zone zone){
        System.out.println("x:"+zone.getXLength()+" y:"+zone.getXLength()+" z:"+zone.getZLength()+" LTap:"+zone.getLeftTopAbsolutePosition()+" RBap:"+zone.getRightBottomAbsolutePosition());
        zoneposition=zone.getLeftTopAbsolutePosition();
    }*/



    //PHOTOMETERS***************************************************************************************************************************
/*    @Bind(id="photometers")
    @SuppressWarnings("all")
    public void bindPhotometer(){
      //  System.out.println("----------------------BINDING photometer!!"+photometers);
/*        System.out.println("-------->"+photometers.get(photometers.size()-1).getClass().getClassLoader());

        if(photometers.get(photometers.size()-1).getClass().getClassLoader().equals("iop-device-importer [106]")){
            System.out.println("--binding Photometer from IOP: "+zoneposition.x+","+zoneposition.y);
            ((LocatedObject)photometers.get(photometers.size()-1)).setPosition(zoneposition);
        }else if (iopPresent()&&firstIOP){
            System.out.println("trying to remove IOP photometer...");
            lookup.discard(new String[]{Photometer.class.getName()});
            firstIOP=false;
        }else{
            System.out.println("bind photometer");
        }
       // System.out.println(photometers.get(0).getIlluminance());
       // System.out.println(((LocatedObject)photometers.get(0)).getZone());
        ZoneDeviceCollection.add(photometers.get(photometers.size()-1).toString());*/



  //  }
 /*   @Unbind(id="photometers")
    @SuppressWarnings("all")
    public synchronized void unbindPhotometer(Photometer photometer){
       /*     System.out.println("**UNBINDING PHOTOMETER");
            System.out.println("remove from "+((LocatedObject)photometer).getZone()+" is it inside and empty? "+(!((LocatedObject)photometer).getZone().equals("unknown")&& !zonehasPhotometers(((LocatedObject)photometer).getZone())));
        System.out.println(" is it inside? "+(!((LocatedObject)photometer).getZone().equals("unknown")));
        System.out.println(" is it empty? "+(!zonehasPhotometers(((LocatedObject)photometer).getZone())));*/
       //!((LocatedObject)photometer).getZone().equals("unknown") &&
/*            if( !zonehasPhotometers(((LocatedObject)photometer).getZone())){
                firstIOP=true;
                System.out.println(zonehasPhotometers(((LocatedObject)photometer).getZone()));
                lookup.consider(new String[]{Photometer.class.getName()}, Collections.singletonMap("location","appartment1"));
            }*/
            //check if the zone is empty


  //  }
 /*   @Modified(id="photometers")
    @SuppressWarnings("all")
    public void modifyPhotometers(Photometer photometer){
        //If it's the first time a photometer is binded to a zone, then the app beggins
/*       if (iopPresent()){
            System.out.println("trying to remove IOP photometer...");
            lookup.discard(new String[]{Photometer.class.getName()});
            firstIOP=false;
        }
        System.out.println("-------->"+photometer.getClass().getClassLoader());
        System.out.println(((LocatedObject)photometer).getZone());
        if(!initLocalPhotometer&&!((LocatedObject)photometer).getZone().equals("unknown")){
            initLocalPhotometer=true;
            measuredZones.add(((LocatedObject)photometer).getZone());
            System.out.println("--**--**--**--");
            System.out.println(measuredZones);
        }//else if(initLocalPhotometer)*/
//System.out.println("----------****************************************/////////////");
//System.out.println(ZoneDeviceCollection);
//System.out.println(((LocatedObject)photometer).getZone());

  /*      for (Photometer photo:photometers) {
           // System.out.println("the  one?: "+photo.getClass().getClassLoader());
            System.out.println(photo);
            System.out.println(")))))zone: "+((LocatedObject)photo).getZone());

        }*/
        //System.out.println("ilumi"+getIlluminance());
        //return Quantities.getQuantity(illuminance, Units.LUX);
  //  }

 /*   @Command
    public boolean zonehasPhotometers(String zone){

       /* for (Photometer pht:photometers) {
            if(((LocatedObject)pht).getZone().equals(zone)){
                return true;
            }
        }*/
    /*    return false;
    }*/

/*   @Command
    public boolean iopPresent() {
       /* for (Photometer pht:photometers) {

            if(pht instanceof IOPService){

                return true;
            }
        }*/
/*        return false;
    }*/




}
