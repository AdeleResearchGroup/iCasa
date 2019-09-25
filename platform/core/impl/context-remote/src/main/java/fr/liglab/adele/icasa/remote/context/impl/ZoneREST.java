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
package fr.liglab.adele.icasa.remote.context.impl;

import fr.liglab.adele.icasa.ZoneProvider;

import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.remote.context.serialization.SerializedZone;

import org.apache.felix.ipojo.annotations.*;

import org.json.JSONArray;
import org.json.JSONException;

import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;

import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;

import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import static fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil.*;

import java.io.IOException;
import java.util.List;


/**
 *
 */

@Component
@Provides
@Instantiate
@Path("/icasa/zones")
public class ZoneREST extends DefaultController implements Controller {

	@Requires
	private ZoneProvider m_locationManager;

	@Requires(id = "zones", specification = Zone.class, optional = true)
	List<Zone>  zones;

	private Zone zone(String zoneId) {

		if (! m_locationManager.getZoneIds().contains(zoneId)) {
			return null;
		} 

		for (Zone zone : zones) {
			if (zone.getZoneName().equals(zoneId)) {
				return zone;
			}
		}
		
		return null;
	}
	

	@Route(method = HttpMethod.GET, uri = "/zones")
	public Result getZones() {

		JSONArray result = new JSONArray();

		for (Zone zone : zones) {
			try {
				result.put(new SerializedZone(zone).toJson());
			} catch (JSONException ignored) {
			}
		}

		return ok(result.toString()).as(MimeTypes.JSON);
	}

	/**
	 * Retrieves a zone.
	 *
	 * @param zoneId
	 *           The ID of the zone to retrieve
	 * @return The required zone
	 */

	@Route(method = HttpMethod.GET, uri = "/zone/{zoneId}")
	public Result getZone(@Parameter("zoneId") String zoneId) {
		
		if (zoneId == null || zoneId.length() < 1) {
			return getZones();
		}

		Zone zone = zone(zoneId);
		
		if (zone == null) {
			return notFound();
		} 

		try {
			return ok(new SerializedZone(zone).toJson().toString()).as(MimeTypes.JSON);
		} 
		catch (JSONException e) {
			return internalServerError(e);
		}	
		
	}



	@Route(method = HttpMethod.PUT, uri = "/zone/{zoneId}")
	public Result updatesZone(@Parameter("zoneId") String zoneId) {

		if (zoneId == null || zoneId.length() < 1) {
			return notFound();
		}

		Zone zone = zone(zoneId);
		
		if (zone == null) {
			return notFound();
		} 

		try {
			
			SerializedZone update = new SerializedZone(content(context()));
			update.updateZone(zone);
			
			return ok();

		} catch (IOException | JSONException e) {
			return internalServerError(e);
		}

	}

	@Route(method = HttpMethod.POST, uri = "/zone")
	public Result createZone() {
		try {

			SerializedZone serialization = new SerializedZone(content(context()));
			
			serialization.createZone(m_locationManager);
			return ok();


		} catch (IOException | JSONException e) {
			return internalServerError(e);
		}


	}


	@Route(method = HttpMethod.DELETE, uri = "/zone/{zoneId}")
	public Result deleteZone(@Parameter("zoneId") String zoneId) {
		m_locationManager.removeZone(zoneId);
		return ok();
	}

}
