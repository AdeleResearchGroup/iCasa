package fr.liglab.adele.icasa.layering.applications.manager.rest;


//import fr.liglab.adele.icasa.remote.wisdom.impl.DeviceREST;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import fr.liglab.adele.icasa.layering.applications.manager.ApplicationDescription;
import fr.liglab.adele.icasa.layering.applications.manager.ApplicationManager;

@Component(immediate = true)
@Provides
@Instantiate
@Path("/icasa/layers")
public class ApplicationManagerController extends DefaultController {

	@Requires(id = "manager", specification = ApplicationManager.class, optional = false)
	private ApplicationManager manager;

	@Route(method = HttpMethod.GET, uri = "/applications")
	public Result applications() {
		try {
			
			JSONArray result = new JSONArray();
			for (ApplicationDescription application : manager.getApplications()) {
				result.put(serialize(application));
			}
			
			return ok(result.toString()).as(MimeTypes.JSON);
			
		} catch (JSONException e) {
			return internalServerError(e);
		}
	}
 
    @Route(method = HttpMethod.GET, uri = "/applications/enable/{appid}")
    public synchronized Result enable(@Parameter("appid") String appid) {
    	boolean found = manager.enable(appid);
        return found ? ok("<h3>Enabling implementation ("+appid+")</h3>" ).as(MimeTypes.HTML) : notFound();
    }

	@Route(method = HttpMethod.GET, uri = "/applications/enable")
	public synchronized Result enable() {
		manager.enable();
		return ok("<h3>Enabling implementations</h3>");
	}

    @Route(method = HttpMethod.GET, uri = "/applications/disable/{appid}")
    public synchronized Result disableApplication(@Parameter("appid") String appid) {
    	boolean found = manager.disable(appid);
        return found ? ok("<h3>Disabling implementation ("+appid+")</h3>" ).as(MimeTypes.HTML) : notFound();
    }

	@Route(method = HttpMethod.GET, uri = "/applications/disable")
	public synchronized Result disableAllApplications() {
		manager.disable();
		return ok("<h3>Disabling implementations</h3>");
	}
	
    public static JSONObject serialize(ApplicationDescription application) throws JSONException {
    	
        JSONObject content = new JSONObject();
       
        content.putOnce("implementation",application.implementation);
        content.putOnce("instances",application.instances);
        content.putOnce("enabled",application.enabled);
        
        return content;
    }

}
