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

import fr.liglab.adele.icasa.clockservice.Clock;

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

import static fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil.*;

import java.io.IOException;


/**
 *
 * 
 */
@Component
@Provides
@Instantiate
@Path("/icasa/clocks")
public class ClockREST  extends DefaultController {

    public static final String DEFAULT_INSTANCE_NAME = "default";

    @Requires(optional = true)
	private Clock clock;


    /**
     * Returns a JSON array containing all clocks.
     */
    private String clocks() throws JSONException {
        
    	JSONArray currentClocks = new JSONArray();

		if (clock != null) {
			currentClocks.put(serialize(clock));
		}

        return currentClocks.toString();
    }

    @Route(method = HttpMethod.GET, uri = "/clocks")
    public Result getClocks() {
        try {
			return ok(clocks()).as(MimeTypes.JSON);
		} catch (JSONException e) {
			return internalServerError(e);
		}
    }

    private Clock clock(String id) {
        
    	if ((clock == null) || (id == null) || (! DEFAULT_INSTANCE_NAME.equals(id))) {
        	return null;
        }
        
        return clock;
    }
    
    @Route(method = HttpMethod.GET, uri = "/clock/{clockId}")
	public Result getClock(@Parameter("clockId") String clockId) {
    	
    	Clock clock = clock(clockId);
        if (clock == null) {
            return notFound();
        }

		try {
			return ok(serialize(clock).toString()).as(MimeTypes.JSON);
		} catch (JSONException e) {
			return internalServerError(e);
		}
	}



    @Route(method = HttpMethod.PUT, uri = "/clock/{clockId}")
	public Result updateClock(@Parameter("clockId") String clockId) {
    	
    	Clock clock = clock(clockId);
        if (clock == null) {
            return notFound();
        }

		try {
			
			JSONObject update	= new JSONObject(content(context()));
			int factor 			= update.getInt("factor");
			boolean pause		= update.getBoolean("pause");
			long startDate		= update.getLong("startDate");
			
			synchronized (clock) {

				if (pause && !clock.isPaused()) {
					clock.pause();
				} 

				if (clock.getStartDate() != startDate) {
					clock.setStartDate(startDate);
				}

				if (clock.getFactor() != factor) {
					clock.setFactor(factor);
					
				}
				
				if (!pause && clock.isPaused()) {
					clock.resume();
				}

			}

			return ok(serialize(clock).toString()).as(MimeTypes.JSON);

		} catch (JSONException | IOException e) {
			return internalServerError(e);
		}
	}




}
