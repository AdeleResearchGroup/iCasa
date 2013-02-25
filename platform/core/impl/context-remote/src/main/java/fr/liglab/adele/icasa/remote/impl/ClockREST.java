/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.remote.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.json.JSONException;
import org.json.JSONObject;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.remote.AbstractREST;
import fr.liglab.adele.icasa.remote.util.IcasaJSONUtil;

/**
 * 
 * @author Gabriel Pedraza Ferreira
 * 
 */
@Component(name = "remote-rest-clock")
@Instantiate(name = "remote-rest-clock-0")
@Provides(specifications = { ClockREST.class }, properties = {@StaticServiceProperty(name = AbstractREST.ICASA_REST_PROPERTY_NAME, value="true", type="java.lang.Boolean")} )
@Path(value = "/clock/")
public class ClockREST extends AbstractREST {

	@Requires
	private Clock clock;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response clock() {
		return makeCORS(Response.ok(IcasaJSONUtil.getClockJSON(clock).toString()));
	}

	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	public Response clockOptions() {
		return makeCORS(Response.ok());
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateClock(String content) {
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
		return makeCORS(Response.ok(IcasaJSONUtil.getClockJSON(clock).toString()));
	}




}
