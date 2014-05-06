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
package fr.liglab.adele.icasa.simulator.remote.wisdom.impl;


import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.liglab.adele.icasa.simulator.remote.wisdom.util.IcasaSimulatorJSONUtil;
import fr.liglab.adele.icasa.simulator.remote.wisdom.util.ScriptJSON;
import fr.liglab.adele.icasa.simulator.script.executor.ScriptExecutor;
import fr.liglab.adele.icasa.simulator.script.executor.ScriptExecutor.State;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;

import java.io.IOException;

/**
 *
 */
@Component
@Provides
@Instantiate
@Path("/icasa/scriptPlayer")
public class ScriptPlayerREST extends DefaultController {


	@Requires
	private ScriptExecutor _scriptExecutor;



    @Route(method = HttpMethod.GET, uri = "/scripts")
	public Result scripts() {
		return ok(getScripts()).as(MimeTypes.JSON);
	}

	/*
	 * private JSONObject getScriptJSON(String scriptName) {
	 * 
	 * State scriptState = _scriptExecutor.getCurrentScriptState(); ScriptState
	 * scriptStateJSON = ScriptState.STOPPED; String selectedScriptName =
	 * _scriptExecutor.getCurrentScript(); if
	 * (scriptName.equals(selectedScriptName)) { if
	 * (State.EXECUTING.equals(scriptState)) scriptStateJSON =
	 * ScriptState.STARTED; else if (State.PAUSED.equals(scriptState))
	 * scriptStateJSON = ScriptState.PAUSED; else if
	 * (State.STOPPED.equals(scriptState)) scriptStateJSON = ScriptState.STOPPED;
	 * }
	 * 
	 * JSONObject scriptJSON = null; try { scriptJSON = new JSONObject();
	 * scriptJSON.putOnce(ScriptJSON.ID_PROP, scriptName);
	 * scriptJSON.putOnce(ScriptJSON.NAME_PROP, scriptName);
	 * scriptJSON.putOnce(ScriptJSON.STATE_PROP, scriptStateJSON.toString());
	 * scriptJSON.putOnce(ScriptJSON.COMPLETE_PERCENT_PROP,
	 * _scriptExecutor.getExecutedPercentage());
	 * scriptJSON.putOnce("actionNumber",
	 * _scriptExecutor.getActionsNumber(scriptName)); //
	 * scriptJSON.putOnce("startDate", //
	 * _scriptExecutor.getStartDate(scriptName)); scriptJSON.putOnce("factor",
	 * _scriptExecutor.getFactor(scriptName));
	 * scriptJSON.putOnce("executionTime",
	 * _scriptExecutor.getExecutionTime(scriptName));
	 * 
	 * } catch (JSONException e) { e.printStackTrace(); scriptJSON = null; }
	 * 
	 * return scriptJSON; }
	 */

	/**
	 * Returns a JSON array containing all scripts.
	 * 
	 * @return a JSON array containing all scripts.
	 */
	public String getScripts() {
		JSONArray scripts = new JSONArray();
		for (String scriptName : _scriptExecutor.getScriptList()) {
			JSONObject script = IcasaSimulatorJSONUtil.getScriptJSON(scriptName, _scriptExecutor);
			if (script == null)
				continue;
			scripts.put(script);
		}
		return scripts.toString();
	}

	/**
	 * Retrieve a script.
	 * 
	 * @param scriptId
	 *           The ID of the script to retrieve
	 * @return The required script, return
	 *         <code>null<code> if the script does not exist.
	 */
    @Route(method = HttpMethod.GET, uri = "/script/{scriptId}")
    public Result script(@Parameter("scriptId") String scriptId) {
		if (scriptId == null || scriptId.length() < 1) {
			return ok(getScripts()).as(MimeTypes.JSON);
		}

		boolean scriptFound = _scriptExecutor.getScriptList().contains(scriptId);
		if (!scriptFound) {
			return notFound();
		} else {
			JSONObject scriptJSON = IcasaSimulatorJSONUtil.getScriptJSON(scriptId, _scriptExecutor);
			return ok(scriptJSON.toString()).as(MimeTypes.JSON);
		}
	}




    @Route(method = HttpMethod.PUT, uri = "/script/{scriptId}")
    public Result updatesScriptPut(@Parameter("scriptId") String scriptId) {
        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
		if (scriptId == null || scriptId.length() < 1) {
			return notFound();
		}

		boolean scriptFound = _scriptExecutor.getScriptList().contains(scriptId);
		if (!scriptFound) {
			return notFound();
		}

		ScriptJSON script = ScriptJSON.fromString(content);

		if (script == null)
			return notFound();

		String currentScriptName = _scriptExecutor.getCurrentScript();
		State currentScriptState = _scriptExecutor.getCurrentScriptState();
		State newState = State.fromString(script.getState());
		
		if (scriptId.equals(currentScriptName)) { // Same script
			if (currentScriptState == State.STARTED) {				
				if (State.STOPPED == newState)
					_scriptExecutor.stop();
				if (State.PAUSED == newState)
					_scriptExecutor.pause();
			} else if (currentScriptState == State.PAUSED) {
				if (State.STARTED == newState)
					_scriptExecutor.resume();
			} else if (currentScriptState == State.STOPPED) {
				if (State.STARTED == newState)
					_scriptExecutor.execute(scriptId);
			} else {
				return status(Status.SERVICE_UNAVAILABLE);
			}				
		} else { // New script
			if (currentScriptState == State.STOPPED)
				if (State.STARTED == newState)
					_scriptExecutor.execute(scriptId);
			// else
			//	return makeCORS(Response.status(Response.Status.SERVICE_UNAVAILABLE));
		}

		JSONObject scriptJSON = IcasaSimulatorJSONUtil.getScriptJSON(scriptId, _scriptExecutor);

		return ok(scriptJSON.toString()).as(MimeTypes.JSON);
	}


    @Route(method = HttpMethod.POST, uri = "/script")
    public Result createScript() {

        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
		ScriptJSON scriptJSON = ScriptJSON.fromString(content);
		String fileName = scriptJSON.getId();

		//_simulationMgr.saveSimulationState(fileName);
		_scriptExecutor.saveSimulationScript(fileName);

		return ok(scriptJSON.toString()).as(MimeTypes.JSON);
	}

}
