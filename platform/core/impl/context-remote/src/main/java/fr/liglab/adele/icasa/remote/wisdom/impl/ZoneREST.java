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

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import fr.liglab.adele.icasa.remote.wisdom.util.ZoneJSON;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.IOException;


/**
 *
 */
@Component
@Provides
@Instantiate
@Path("/icasa/zones")
public class ZoneREST extends DefaultController {

	@Requires
	private ContextManager _simulationMgr;


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

		Zone zoneFound = _simulationMgr.getZone(zoneId);
		if (zoneFound == null) {
			return notFound();
		} else {
			JSONObject zoneJSON = IcasaJSONUtil.getZoneJSON(zoneFound);

			return ok(zoneJSON.toString()).as(MimeTypes.JSON);
		}
	}

	/**
	 * Returns a JSON array containing all zones.
	 * 
	 * @return a JSON array containing all zones.
	 */
	public String getZones() {
		// boolean atLeastOne = false;
		JSONArray currentZones = new JSONArray();
		for (String envId : _simulationMgr.getZoneIds()) {
			Zone zone = _simulationMgr.getZone(envId);
			if (zone == null)
				continue;

			JSONObject zoneJSON = IcasaJSONUtil.getZoneJSON(zone);
			if (zoneJSON == null)
				continue;

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

		Zone zoneFound = _simulationMgr.getZone(zoneId);
		if (zoneFound == null)
			return notFound();

		ZoneJSON zoneJSON = ZoneJSON.fromString(content);

		Position position = new Position(zoneJSON.getLeftX(), zoneJSON.getTopY());

		// TODO: Review for children zones
		if (!position.equals(zoneFound.getLeftTopAbsolutePosition())){ //move zone
			try {
                _simulationMgr.moveZone(zoneFound.getId(),position.x,position.y,position.z);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        }

		int width = zoneJSON.getRigthX() - zoneJSON.getLeftX();
		int height = zoneJSON.getBottomY() - zoneJSON.getTopY();

		if (zoneFound.getXLength() != width || zoneFound.getYLength() != height){
			try {
                _simulationMgr.resizeZone(zoneFound.getId(), width, height,zoneFound.getZLength());
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

		return ok(IcasaJSONUtil.getZoneJSON(zoneFound).toString()).as(MimeTypes.JSON);

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

		Zone newZone = _simulationMgr
		      .createZone(zoneJSON.getId(), zoneJSON.getLeftX(), zoneJSON.getTopY(), zoneJSON.getBottomZ(), width, height, depth);

		return ok(IcasaJSONUtil.getZoneJSON(newZone).toString()).as(MimeTypes.JSON);

	}


    @Route(method = HttpMethod.DELETE, uri = "/zone/{zoneId}")
	public Result deleteZone(@Parameter("zoneId") String zoneId) {

		Zone zone = _simulationMgr.getZone(zoneId);

		if (zone == null)
			return notFound();
		try {
			_simulationMgr.removeZone(zoneId);
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError();
		}
		return ok();
	}

}
