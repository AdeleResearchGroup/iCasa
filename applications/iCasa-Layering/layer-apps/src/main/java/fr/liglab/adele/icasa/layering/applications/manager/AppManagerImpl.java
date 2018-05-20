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
    public String apps(){
        for(int p=0;p<validApps.size();p++){
            System.out.println("[app "+p+"] "+validApps.get(p));
            System.out.println("[factory "+p+"] "+validAppsFactory.get(p));
        }
        return validApps.get(0).toString();
    }

    @Command
    @SuppressWarnings("unused")
    public void dataFill(){
        int cur=0;
            //System.out.println("cleaning...");
            appClassList.clear();
            appImplList.clear();
            appNameList.clear();
           // System.out.println(appImplList);

        for (EntityProvider P:provs) {//run through every provider...
             for(String service:P.getPotentiallyProvidedEntityServices()){
 //                System.out.println("---**"+service);
                if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){//get the application providers
                    String temp=String.valueOf(P.getInstances(P.getProvidedEntities().toArray()[0].toString(),true));
                    temp=temp.substring(1,temp.length()-1);
                    String temp2=(String) P.getProvidedEntities().toArray()[0];
                    String temp3= getAppClassFromImpl(temp2);
                    //System.out.println(temp);
                   // System.out.println(temp2);
                   // System.out.println(temp3);
                    if(!appNameList.contains(temp)){
                        appNameList.add(temp);
                    }
                    if(!appImplList.contains(temp2)){
                        appImplList.add(temp2);
                        //System.out.println("L1");
                        if(!appClassList.contains(temp3)){
                            //System.out.println("L1.5");
                            appClassList.add(getAppClassFromImpl(temp2));
                        }
                    }else{
                        //System.out.println("L2");
                        if(appClassList.contains("notFound")){
                            //System.out.println("L2.5");
                            appClassList.remove("notFound");
                        }
                        if(!appClassList.contains(temp3)){
                            //System.out.println("L2.8");
                            appClassList.add(getAppClassFromImpl(temp2));
                        }
                    }

                }
            }
        }

        System.out.println(appNameList);
        System.out.println(appImplList);
        System.out.println(appClassList);
    }

    @Command
    @SuppressWarnings("unused")
    public void providers(){
        //search all the providers...
        for (EntityProvider P:provs) {//run through every provider...
            for(String service:P.getPotentiallyProvidedEntityServices()){
                if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){//get the application providers
                    System.out.println("[ALIAS]"+P.getInstances((String) P.getProvidedEntities().toArray()[0],true));
                    System.out.println("[PROVIDER] "+P.getProvidedEntities().toArray()[0]);


                    System.out.println("    [provider Name]"+P.getName());
                    System.out.println("    [provider Obj]"+P);
                    System.out.println("    [prv Entities]"+P.getProvidedEntities());
                    for(String r:P.getProvidedEntities()){
                        System.out.println("    [prv ent]"+P.getInstances(r,true));
                        System.out.println("    [prv Entities2]"+P.getPotentiallyProvidedEntityServices(r));
                    }
                }
            }
        }
    }

    @Command
    @SuppressWarnings("unused")
    public void stap(String i){
        String e = getAppImplementationFromClass(i); //i: fr.liglab.adele.icasa.layering.applications.lightning.LightApp  e:fr.liglab.adele.icasa.layering.applications.lightning.LightApp
       // System.out.println(">>"+e);
        for (EntityProvider P:provs) {//run through every provider...
            for(String service:P.getPotentiallyProvidedEntityServices()){
                if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){
                    System.out.println("srv: "+service);
                    System.out.println("      PEne : "+e);
                    System.out.println("      PEnt0: "+P.getProvidedEntities().toArray()[0]);
                    System.out.println("      PEnt : "+P.getProvidedEntities());
                }
                if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer") && (P.getProvidedEntities().toArray()[0].equals(e))){//get the application providers
                    System.out.println("stopping app");
                    P.disable(e);
                }
            }
        }
    }

    @Command
    @SuppressWarnings("unused")
    public void stort(String i){
        String e = getAppImplementationFromClass(i);
        System.out.println(">>"+e);
        for (EntityProvider P:provs) {//run through every provider...
            for(String service:P.getPotentiallyProvidedEntityServices()){
                System.out.println(service);
                if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer") && (P.getProvidedEntities().toArray()[0].equals(i))){//get the application providers
                    System.out.println("starting app");
                    P.enable(i);
                }
            }
        }
    }



    @Command
    @SuppressWarnings("unused")
    public String getAppImplementationFromClass(String P){
        String e = "notFound";
        for(int i=0;i<validApps.size();i++){
            if(validApps.get(i).equals(P)){
                return validAppsFactory.get(i);
            }
        }
        return e;
    }

    @Command
    @SuppressWarnings("unused")
    public String getAppClassFromImpl(String P){
        String e = "notFound";
        for(int i=0;i<validAppsFactory.size();i++){
            if(validAppsFactory.get(i).equals(P)){
                return validApps.get(i);
            }
        }
        return e;
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
