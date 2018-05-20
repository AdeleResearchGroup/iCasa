package fr.liglab.adele.icasa.layering.applications.lightning;



import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.global.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.lightning.LightningServiceInterface;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShuttersServiceImpl;
import fr.liglab.adele.icasa.layering.services.shutterOpening.ShuttersServiceProvider;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@ContextEntity(coreServices = {ApplicationLayer.class})
@SuppressWarnings("unused")
public class HomeLightningAppImpl implements ApplicationLayer{
    public static final String RELATION_IS_ATTACHED="app.attached.to";
    private static final Logger LOG = LoggerFactory.getLogger(HomeLightningAppImpl.class);
    private static final String LOG_PREFIX = "LIGHTNING APP";

   // AccuweatherServiceLayer gueb = new AccuweatherServiceLayer();

    @ContextEntity.State.Field(service = ApplicationLayer.class, state = ApplicationLayer.status,value = "init")
    public String stat;


   // @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="lightningservices",optional=false)
    private List<LightningServiceInterface> lightningServiceLayer;
    //relations
    public void aze(){
        System.out.println("servs: "+lightningServiceLayer.size());
    }


   // @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
  /*  @Requires(id="accuweatherservices",specification = AccuweatherService.class)
    AccuweatherService accuweatherService;*/

   /* @Requires(id="shuttersServiceLayer",specification = ShuttersServiceImpl.class)
    ShuttersServiceImpl shuttersServiceLayer;*/

  /*  @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="lightningservice",specification = LightningServiceImpl.class)
    LightningServiceImpl lightningservice;*/

   /* @Bind(id="accuweatherservices")
    public void bindAccuservice(AccuweatherService accuweatherService){

    }*/

   /* @Bind(id="shuttersServiceLayer", aggregate = true,optional = true)
    public void bindShuttersServiceLayer(ShuttersServiceImpl shuttersServiceLayer){

    }*/

    @Bind(id="lightningservices")
    public void bindLightningservice(LightningServiceInterface lightningservices){
        System.out.println(lightningservices instanceof ShuttersServiceImpl);
        System.out.println(lightningservices instanceof ShuttersServiceProvider);
        System.out.println(lightningServiceLayer.get(lightningServiceLayer.size()-1).getClass().getGenericInterfaces());
        System.out.println(lightningServiceLayer.get(lightningServiceLayer.size()-1).getClass().getClassLoader());
        System.out.println(lightningServiceLayer.get(lightningServiceLayer.size()-1).getClass().getClasses());
        System.out.println(lightningServiceLayer.get(lightningServiceLayer.size()-1).getClass().getClass());
        System.out.println("binding LIGHT from LIGHTAPP.."+lightningservices.getServiceName());
      /*  try {
            System.out.println("WEEEEEB ANS: ");
            System.out.println(gueb.getHTML("136555","UIIBdfTX91vNlgsZFdROiiW7uMflMrUP").getClass());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("errooooor: ",e);
        }*/

    }



    public void setStat(String stat) {
        this.stat = stat;
    }

    @Override
    public boolean getStatus() {
        if(stat!="0"){
            return false;
        }else{
            return true;
        }

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
