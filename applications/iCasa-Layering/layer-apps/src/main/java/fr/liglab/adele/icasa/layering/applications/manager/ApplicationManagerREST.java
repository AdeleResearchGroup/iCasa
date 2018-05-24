package fr.liglab.adele.icasa.layering.applications.manager;


//import fr.liglab.adele.icasa.remote.wisdom.impl.DeviceREST;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONException;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

@Component(immediate = true)
@Provides
@Instantiate
@Path("/icasa/layer")
public class ApplicationManagerREST extends DefaultController {

	@Requires(id = "manager", specification = ApplicationManager.class, optional = false)
	private ApplicationManager appManager;

	@Route(method = HttpMethod.GET, uri = "/applications")
	public Result applications() {
		try {
			
			JSONArray result = new JSONArray();
			for (ApplicationDescription application : appManager.getApplications()) {
				result.put(application.serialize());
			}
			
			return ok(result.toString()).as(MimeTypes.JSON);
			
		} catch (JSONException e) {
			return internalServerError(e);
		}
	}
 
}
