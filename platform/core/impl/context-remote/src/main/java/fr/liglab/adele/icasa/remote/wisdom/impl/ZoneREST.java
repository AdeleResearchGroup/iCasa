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
package fr.liglab.adele.icasa.remote.wisdom.impl;

import fr.liglab.adele.icasa.LocationManager;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import fr.liglab.adele.icasa.remote.wisdom.util.ZoneJSON;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

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
	private LocationManager m_locationManager;

	@Requires(id = "zones", specification = Zone.class,optional = true)
	List<Zone>  zones;


	@Route(method = HttpMethod.GET, uri = "/zones")
	public Result zones() {
		return ok(getZones()).as(MimeTypes.JSON);
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
			return ok(getZones()).as(MimeTypes.JSON);
		}

		boolean zoneFound = m_locationManager.getZoneIds().contains(zoneId);
		if (!zoneFound){
			return notFound();
		} else {
			for (Zone zone:zones){
				if (zone.getZoneName().equals(zoneId)){
					JSONObject zoneJSON = IcasaJSONUtil.getZoneJSON(zone);
					return ok(zoneJSON.toString()).as(MimeTypes.JSON);
				}
			}
		}
		return internalServerError();
	}

	/**
	 * Returns a JSON array containing all zones.
	 *
	 * @return a JSON array containing all zones.
	 */
	public String getZones() {
		// boolean atLeastOne = false;
		JSONArray currentZones = new JSONArray();
		for (Zone zone : zones) {
			JSONObject zoneJSON = IcasaJSONUtil.getZoneJSON(zone);
			currentZones.put(zoneJSON);
		}

		return currentZones.toString();
	}


	@Route(method = HttpMethod.PUT, uri = "/zone/{zoneId}")
	public Result updatesZone(@Parameter("zoneId") String zoneId) {
		String content = null;
		try {
			content = IcasaJSONUtil.getContent(context().reader());
		} catch (IOException e) {
			e.printStackTrace();
			return internalServerError();
		}
		if (zoneId == null || zoneId.length() < 1) {
			return notFound();
		}

		boolean zoneFound = m_locationManager.getZoneIds().contains(zoneId);
		if (zoneFound )	return notFound();

		ZoneJSON zoneJSON = ZoneJSON.fromString(content);

		Position position = new Position(zoneJSON.getLeftX(), zoneJSON.getTopY());

		for(Zone zone : zones){
			if (zone.getZoneName().equals(zoneId)){
				if (!zone.getLeftTopAbsolutePosition().equals(position)){
					zone.setLeftTopAbsolutePosition(position);
				}
				int width = zoneJSON.getRigthX() - zoneJSON.getLeftX();
				int height = zoneJSON.getBottomY() - zoneJSON.getTopY();
				if ( (zone.getXLength() != width) || (zone.getYLength() != height) ){
					zone.setLeftTopAbsolutePosition(position);
				}
			}
		}
		return ok();

	}

	@Route(method = HttpMethod.POST, uri = "/zone")
	public Result createZone() {
		String content = null;
		try {
			content = IcasaJSONUtil.getContent(context().reader());
		} catch (IOException e) {
			e.printStackTrace();
			return internalServerError();
		}

		ZoneJSON zoneJSON = ZoneJSON.fromString(content);

		int width = zoneJSON.getRigthX() - zoneJSON.getLeftX();
		int height = zoneJSON.getBottomY() - zoneJSON.getTopY();
		int depth = zoneJSON.getTopZ() - zoneJSON.getBottomZ();

		m_locationManager.createZone(zoneJSON.getId(), zoneJSON.getLeftX(), zoneJSON.getTopY(), zoneJSON.getBottomZ(), width, height, depth);

		//	return ok(IcasaJSONUtil.getZoneJSON(newZone).toString()).as(MimeTypes.JSON);
		return ok();
	}


	@Route(method = HttpMethod.DELETE, uri = "/zone/{zoneId}")
	public Result deleteZone(@Parameter("zoneId") String zoneId) {
		m_locationManager.removeZone(zoneId);
		return ok();
	}

	@Validate
	public void start(){

	}


	@Invalidate
	public void stop(){

	}
}
