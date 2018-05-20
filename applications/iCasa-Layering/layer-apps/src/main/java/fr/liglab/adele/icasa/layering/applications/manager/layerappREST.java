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


    /**
     * Returns a JSON array with all the Layer Apps.
     *
     * @return a JSON array with all the Layer Apps.
     */

    private static JSONObject serialize(String Name, String Class, String Factory, String State)throws JSONException{
        JSONObject content = new JSONObject();
        content.putOnce("name",Class);
        content.putOnce("id",Name);
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
            for (String service:P.getPotentiallyProvidedEntityServices()){
                if(service.equals("fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer")){
                    String temp =P.getProvidedEntities().toArray()[0].toString();
                    String fct=P.getInstances((String) P.getProvidedEntities().toArray()[0],true).toString();
                    String[] temp2=temp.split(Pattern.quote("."));
                    String[] temp3=temp2[temp2.length-1].split(Pattern.quote("Impl"));
                    System.out.println(temp+"+"+temp2+" l: "+temp2.length+"++"+temp3.length+temp3[0]);
                    System.out.println(temp2[temp2.length-1]);
                    currentLayerapps.put(serialize(temp3[0],temp,fct,"valid"));
                }
            }
        }





       /* System.out.println("apppppppps:");
        System.out.println(appManager);*/

       System.out.println(appManager.getValidApps());

        System.out.println("forrrrrrrrrrrrrr:"+appManager.getValidApps().size());
        //List<String>  apps =;

       // currentLayerapps.put(attributes);


      /* for(String curApp: appManager.getValidApps()){
           attributes.putOnce("name",currentLayerapps.put(Collections.singleton(curApp)));
           attributes.putOnce("factory",currentLayerapps.put("test"));
           System.out.println("att:");
           System.out.println(curApp);
           System.out.println(attributes);
           System.out.println("attttttt:");
           currentLayerapps.put(attributes);
        }*/
        System.out.println("NOOOOOOOOOOforrrrrrrrrrrrrr:");
        //currentLayerapps.put("you");
       /* for (ApplicationLayer app : apps){
            JSONObject appJSON = 3;
        }*/
       return currentLayerapps.toString();
    }

}
