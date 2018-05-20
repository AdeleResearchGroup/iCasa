package fr.liglab.adele.icasa.layering.applications.lightning;


import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.layering.services.lightning.LightningServiceImpl;
import org.apache.felix.ipojo.annotations.*;

@Component(immediate =true,publicFactory = false)
@Instantiate
public class LightningAppProvider {

    @Creator.Field Creator.Entity<HomeLightningAppImpl> lightapp;
   // @Creator.Field Creator.Entity<AccuweatherServiceLayer> accu;
    @Creator.Field Creator.Entity<LightningServiceImpl> lightserv;
    //@Creator.Field Creator.Entity<ShuttersServiceImpl> shutterserv;
    @Creator.Field(HomeLightningAppImpl.RELATION_IS_ATTACHED) Creator.Relation<HomeLightningAppImpl,LightningServiceImpl> attachedLighthingApplicationCreator;
    //@Creator.Field(HomeLightningAppImpl.RELATION_IS_ATTACHED) Creator.Relation<HomeLightningAppImpl,AccuweatherServiceLayer> attachedLighthingApplicationCreator2;
    //@Creator.Field(HomeLightningAppImpl.RELATION_IS_ATTACHED) Creator.Relation<HomeLightningAppImpl,ShuttersServiceImpl> attachedLighthingApplicationCreator3;

    @Validate
    public void start(){
       // System.out.println("is accu there?: "+accu.getInstances().isEmpty());

        lightapp.create("LightApp");
       // shutterserv.create("shutterTest");

    /*    if(accu.getInstances().isEmpty()){
         //   System.out.println("trying 2 create accu srv");
            accu.create("AccuweatherSrv");
        }*/

    }

    @Invalidate
    public void stop(){
        lightapp.delete("LightApp");
    };

    @Bind(id="lightningServiceImpl",specification = LightningServiceImpl.class,aggregate = true,optional=true)
    @SuppressWarnings("all")
    public void bindLightSrv(LightningServiceImpl lightningServiceImpl){
       // lightapp.getInstance("LigthApp").aze();
        System.out.println("lightSrv form APP");
        System.out.println(lightserv);

    }


  /*  @Bind(id="shuttersServiceLayer",specification = ShuttersServiceImpl.class,aggregate = true,optional=true)
    @SuppressWarnings("all")
    public void bindShutterSrv(ShuttersServiceImpl shuttersServiceLayer){
        System.out.println("lightSrv form APP");
       // System.out.println(shutterserv);

    }*/

    //private String AppName="LightApp";

    //private Boolean state=startApp();



   /* public Boolean startApp(){
        System.out.println("startApp: app empty?"+lightapp.getInstances().isEmpty());
        if(lightapp.getInstances().isEmpty()){
            lightapp.create(AppName);
            return true;
        }
        return true;
    }*/
 /*  @Bind(id="accuweatherServiceLayers",specification = AccuweatherServiceLayer.class,aggregate = true,optional=true)
    public void bindAcc(AccuweatherServiceLayer accuweatherServiceLayers){
        System.out.println("CREATING ACCU SERVICE...");
        if(lightapp.getInstances().isEmpty()){
            lightapp.create("LightApp");
        }
        attachedLighthingApplicationCreator2.create("LightApp", (AccuweatherServiceLayer) accu);
    }
   @Bind(id="zones",specification = Zone.class,aggregate = true,optional = true)
    @SuppressWarnings("all")
    public void bindZ(Zone zone){
        String name=getEntityName(zone);
        System.out.println("appppp empty?"+lightapp.getInstances().isEmpty());
        if(lightapp.getInstances().isEmpty()){
            lightapp.create(name);
        }
        System.out.println(zone.getZoneName());
        attachedLighthingApplicationCreator.create(name,zone.getZoneName()+".lightning");

    }*/

  /* @Bind(id="lightningServiceLayer",specification = LightningServiceImpl.class,aggregate = true,optional=false)
    @SuppressWarnings("all")
    public void bindLightSrv(LightningServiceImpl lightningServiceLayer){
        System.out.println("lightSrv form APP");
        System.out.println(lightserv);

    }*/

   /* private String getEntityName(Zone zone){return "LightApp";}*/
}
