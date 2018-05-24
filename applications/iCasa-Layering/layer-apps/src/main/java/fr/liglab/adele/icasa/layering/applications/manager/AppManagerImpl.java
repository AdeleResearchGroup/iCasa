package fr.liglab.adele.icasa.layering.applications.manager;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer;
import fr.liglab.adele.icasa.layering.applications.global.globalEnabler;
import fr.liglab.adele.icasa.layering.applications.lightning.HomeLightningAppImpl;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.layering.applications.test.*;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@CommandProvider(namespace = "Layer-applications")
@Component(immediate=true,publicFactory = false)
@Instantiate
public class AppManagerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(HomeLightningAppImpl.class);

   // @Requires(id="app",specification = ApplicationLayer.class,optional=true)
    List<String> validApps= new ArrayList<>();
    List<String> validAppsFactory= new ArrayList<>();
    List<String> appNameList = new ArrayList<>();
    List<String> appImplList = new ArrayList<>();
    List<String> appClassList = new ArrayList<>();
    Set<String> transitionappsset = new HashSet<>();

    @Requires(id="providers",specification = EntityProvider.class, optional=true)
    List<EntityProvider> provs;

    @Validate
    @SuppressWarnings("unused")
    public void start(){
        LOG.debug("AppManager Started");
    }

   @Bind(id="app", optional=true, aggregate = true)
    public void bindApp(ApplicationLayer app, Map<String, Object> properties){
        System.out.println("adding:"+(String)properties.get("instance.name"));
        System.out.println("and :"+(String)properties.get("factory.name"));
        validApps.add(0,(String)properties.get("instance.name"));
        validAppsFactory.add(0,(String)properties.get("factory.name"));
        transitionappsset.addAll(validApps);
        validApps.clear();
        validApps.addAll(transitionappsset);

       transitionappsset.clear();
       transitionappsset.addAll(validAppsFactory);
       validAppsFactory.clear();
       validAppsFactory.addAll(transitionappsset);
       transitionappsset.clear();
       /* for(int p=0;p<validApps.size();p++){
            System.out.println(validApps.get(p));
        }*/
    }
    @Unbind(id="app", optional=true, aggregate = true)
    public void unbindApp(ApplicationLayer app, Map<String, Object> properties){
        validApps.remove((String)properties.get("instance.name"));
    }
    @Modified(id = "app")
    public void modApp(ApplicationLayer app, Map<String, Object> properties){
      System.out.println("[MODED APP]:"+app.toString());
    }

    @Command
    @SuppressWarnings("unused")
    public void test(){
        testApplEnabler testApp=new testApplEnabler();
        System.out.println(testApp.getstatus());
    }


    @Command
    @SuppressWarnings("unused")
    public void apps(){
        //search all the providers...
        for (EntityProvider P:provs) {//run through every provider...
            for(String implementation:P.getProvidedEntities()){
                for (String service:P.getPotentiallyProvidedEntityServices(implementation)) {
                    if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){//get the application providers
                        System.out.println("PRovider:"+P.getName());
                        System.out.println("IMplem:"+implementation);
                        System.out.println("SErvice:"+service);
                        System.out.println(P.getProvidedEntities());
                        System.out.println("[Instances(true]"+P.getInstances(implementation,true));
                        System.out.println("[Instances[false]"+P.getInstances(implementation,false));
                        System.out.println("");
                    }
                }

            }
        }
    }

    @Command
    @SuppressWarnings("unused")
    public void stopImpl(String app){
        for (EntityProvider P:provs) {
            System.out.println(P.disable(app));

        }
    }
    @Command
    @SuppressWarnings("unused")
    public void startImpl(String app){
        for (EntityProvider P:provs) {
            P.enable(app);
        }
    }

    @Command
    @SuppressWarnings("unused")
    public void wxcmpl(String app){
        for (EntityProvider P:provs) {
            for(String implementation:P.getProvidedEntities()){
                for (String service:P.getPotentiallyProvidedEntityServices(implementation)) {
                    if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){//get the application providers
                        System.out.println("PRovider:"+P.getName());
                        System.out.println("IMplem:"+implementation);
                        System.out.println("SErvice:"+service);
                        System.out.println("[Instances(true]"+P.getInstances(implementation,true));
                        System.out.println("[Instances[false]"+P.getInstances(implementation,false));
                    }
                }

            }

        }
    }

    public List<String> getValidApps() {
        return validApps;
    }

    @Command
    @SuppressWarnings("unused")
    public List<String> getAppNames() {
        for(int p=0;p<validApps.size();p++){
            System.out.println("Vapps:"+validApps.size()+" names:"+appNameList.size());
            if(!appNameList.contains(validApps.get(p))){
                appNameList.add(p,validApps.get(p));
            }
        }
        System.out.println(appNameList);
        return appNameList; }

    @Command
    @SuppressWarnings("unused")
        public List<String> getValidAppsFactory(){ return validAppsFactory; }

        public  List<EntityProvider> getAppsInfo(){
        return provs;
        }

    public List<String> getAllApps(){
        List<String> r= new ArrayList<>();
        for(EntityProvider P:provs){
            for(String service:P.getPotentiallyProvidedEntityServices()){
                if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){//get the application providers
                    r.add(P.getProvidedEntities().toArray()[0].toString());
                }
            }
        }
        return r;
    }
}
