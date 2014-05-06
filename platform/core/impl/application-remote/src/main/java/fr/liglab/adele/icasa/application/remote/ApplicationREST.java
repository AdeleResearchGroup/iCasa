/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.application.remote;

import java.util.List;



import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.liglab.adele.icasa.application.Application;
import fr.liglab.adele.icasa.application.ApplicationManager;
import fr.liglab.adele.icasa.application.remote.util.ApplicationJSONUtil;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

@Component
@Instantiate
@Provides
@Path("/icasa/apps")
public class ApplicationREST extends DefaultController {

	@Requires
	private ApplicationManager _applicationMgr;

    @Route(method = HttpMethod.GET, uri = "/apps")
	public Result applications() {
		return ok(getApplications()).as(MimeTypes.JSON);
	}



	/**
	 * Retrieves a zone.
	 * 
	 * @param appId
	 *           The ID of the zone to retrieve
	 * @return The required zone
	 */
    @Route(method = HttpMethod.GET, uri = "/app/{appId}")
	public Result getApplication(@Parameter("appId") String appId) {
		if (appId == null || appId.length() < 1) {
			return badRequest();
		}

		Application app = _applicationMgr.getApplication(appId);
		
		if (app == null) {
			return notFound();
		} else {
			JSONObject appJSON = ApplicationJSONUtil.getApplicationJSON(app);
			return ok(appJSON.toString()).as(MimeTypes.JSON);
		}
	}

	/**
	 * Returns a JSON array containing all applications.
	 * 
	 * @return a JSON array containing all applications.
	 */
	private String getApplications() {
		JSONArray currentApps = new JSONArray();
		
		List<Application>  apps = _applicationMgr.getApplications();
        currentApps.put(ApplicationJSONUtil.getEmptyApplication());//add at least one application: with NONE
		for (Application app : apps) {
			JSONObject appJSON = ApplicationJSONUtil.getApplicationJSON(app);
			if (appJSON==null) {
				continue;
			}
			currentApps.put(appJSON);
      }
		return currentApps.toString();
	}


}
