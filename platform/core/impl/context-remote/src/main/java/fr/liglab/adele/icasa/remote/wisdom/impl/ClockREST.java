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

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.IOException;


/**
 *
 * 
 */
@Component
@Provides
@Instantiate
@Path("/icasa/clocks")
public class ClockREST  extends DefaultController{

    public static final String DEFAULT_INSTANCE_NAME = "default";

    @Requires(optional = true)
	private Clock clock;


    @Route(method = HttpMethod.GET, uri = "/clocks")
    public Result clocks() {
        return ok(getClocks()).as(MimeTypes.JSON);
    }

    /**
     * Returns a JSON array containing all clocks.
     *
     * @return a JSON array containing all clocks.
     */
    private String getClocks() {
        JSONArray currentClocks = new JSONArray();

        if (clock != null) {
            JSONObject currentClock = IcasaJSONUtil.getClockJSON(clock);
            if (currentClock != null)
                currentClocks.put(currentClock);
        }

        return currentClocks.toString();
    }

    @Route(method = HttpMethod.GET, uri = "/clock/{clockId}")
	public Result clock(@Parameter("clockId") String clockId) {
        if ((clock == null) || (clockId == null) || (! DEFAULT_INSTANCE_NAME.equals(clockId)))
            return notFound();

		return ok(IcasaJSONUtil.getClockJSON(clock).toString()).as(MimeTypes.JSON);
	}



    @Route(method = HttpMethod.PUT, uri = "/clock/{clockId}")
	public Result updateClock(@Parameter("clockId") String clockId) {
        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
        if ((clock == null) || (clockId == null) || (! "default".equals(clockId)))
            return notFound();

		try {
			JSONObject clockObject = new JSONObject(content);
			int factor = clockObject.getInt("factor");
			boolean pause = clockObject.getBoolean("pause");
			long startDate = clockObject.getLong("startDate");
			
			synchronized (clock) {
				if (clock.getStartDate() != startDate)
					clock.setStartDate(startDate);

				if (clock.getFactor() != factor)
					clock.setFactor(factor);

				if (pause) {
					if (!clock.isPaused()) {
						clock.pause();
					}
				} else {
					if (clock.isPaused())
						clock.resume();
				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ok(IcasaJSONUtil.getClockJSON(clock).toString()).as(MimeTypes.JSON);
	}




}
