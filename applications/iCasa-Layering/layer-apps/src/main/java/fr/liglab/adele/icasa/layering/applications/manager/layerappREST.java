package fr.liglab.adele.icasa.layering.applications.manager;

import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.global.ServiceLayer;
//import fr.liglab.adele.icasa.remote.wisdom.impl.DeviceREST;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


@Component(immediate = true)
@Provides
@Instantiate
@Path("/icasa/layerapps")
public class layerappREST extends DefaultController {

    private List<ApplicationLayer> apps = new ArrayList<>();

   // private final static Logger LOG = LoggerFactory.getLogger(DeviceREST.class);

    @Requires(id="app",specification = AppManagerImpl.class,optional=false)
    private AppManagerImpl appManager;

    @Route(method = HttpMethod.GET, uri = "/layerapps")
    public Result applications(){
        try{
            return ok(getLayeapps()).as(MimeTypes.JSON);
        }catch(JSONException e){
            return internalServerError(e);
        }
        }

    @Route(method = HttpMethod.GET, uri = "/layerapps/enable/{appid}")
    public synchronized Result enableApplication(@Parameter("appid") String appid){
        return ok(applicationStateChanger(appid,true)).as(MimeTypes.HTML);
    }


    @Route(method = HttpMethod.GET, uri = "/layerapps/disable/{appid}")
    public synchronized Result disableApplication(@Parameter("appid") String appid){
        return ok(applicationStateChanger(appid,false)).as(MimeTypes.HTML);
    }

    @Route(method = HttpMethod.GET, uri = "/layerapps/disableall")
    public synchronized Result disableAllApplications(){
        return ok(applicationStateChanger("ALL",false)).as(MimeTypes.HTML);
    }

    /**
     *
     * @param appId The Global name of the application extracted from the provided entities
     * @param appState true toenablme application, false to disable it
     * @return an html formated String with the
     */
    private String applicationStateChanger(String appId, boolean appState){
        for (EntityProvider P:appManager.getAppsInfo()){
            String GlobalAppName="";
            if(!P.getProvidedEntities().isEmpty()){
                GlobalAppName=extractAppName(P.getProvidedEntities().toArray()[0].toString());
            }

            for(String implementation:P.getProvidedEntities()){
                for (String service:P.getPotentiallyProvidedEntityServices(implementation)) {
                    if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){//get the application providers
                        if(appId.equals("ALL")){
                            P.disable(implementation);
                        }else if(GlobalAppName.equals(appId)){//App name found
                            if(appState==true){
                                String a="<h3>Enabling implementation("+implementation+")...  Last result:"+P.enable(implementation)+"</h3>";
                                return a;
                            }else{
                                String a="<h3>Disabling implementation("+implementation+")...  result:"+P.disable(implementation)+"</h3>";
                                return a;
                            }
                        }
                    }
                }

            }
        }
        if(appId.equals("ALL")){
            return "<h3>all apps disabled</h3>";
        }else{
            return "<h3>Application not found</h3>";
        }
    }


    /**
     * Returns a JSON array with all the Layer Apps.
     *
     * @return a JSON array with all the Layer Apps.
     */

    private static JSONObject serialize(String Name, String Implem, String Factory, String State)throws JSONException{
        JSONObject content = new JSONObject();
        content.putOnce("name",Name);
        content.putOnce("id",Implem);
        content.putOnce("version",Factory);
        content.putOnce("state",State);
        return content;
    }
    private synchronized AppManagerImpl app(String serialNumber){
        return null;
    }

    public String getLayeapps() throws JSONException{

        JSONObject attributes = new JSONObject();
        JSONArray currentLayerapps = new JSONArray();

        for (EntityProvider P:appManager.getAppsInfo()){
            for(String implementation:P.getProvidedEntities()){
                for (String service:P.getPotentiallyProvidedEntityServices(implementation)) {
                    if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){//get the application providers
                        /*System.out.println("PRovider:"+P.getName());
                        System.out.println("IMplem:"+implementation);
                        System.out.println("SErvice:"+service);
                        System.out.println("[Instances(true]"+P.getInstances(implementation,true));
                        System.out.println("[Instances[false]"+P.getInstances(implementation,false));*/
                        String GlobalAppName=extractAppName(P.getProvidedEntities().toArray()[0].toString());
                        String instances = P.getInstances(implementation,true).toString();
                        String State="unknown";
                        if(instances.equals("[]")){
                            State="Stranded";
                        }else if(P.getInstances(implementation,false).isEmpty()){
                            State="Stopped";
                        }else {
                            State="Started";
                        }
                        currentLayerapps.put(serialize(GlobalAppName,implementation,instances,State));
                    }
                }

            }
        }



       return currentLayerapps.toString();
    }

    private String extractAppName(String implementation){
        String[] temp=implementation.split(Pattern.quote("."));
        String[] temp2=temp[temp.length-1].split(Pattern.quote("Impl"));
        return temp2[0];
    }

}
