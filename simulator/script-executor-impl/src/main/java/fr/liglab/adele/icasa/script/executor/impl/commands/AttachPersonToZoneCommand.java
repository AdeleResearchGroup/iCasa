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
package fr.liglab.adele.icasa.script.executor.impl.commands;


import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.json.JSONObject;

import fr.liglab.adele.icasa.environment.SimulationManager;
import fr.liglab.adele.icasa.script.executor.impl.commands.AbstractCommand;

/**
 * 
 * Moves a person between the simulated environments 
 * 
 * @author Gabriel
 *
 */
@Component(name = "AttachPersonZoneCommand")
@Provides(properties = { @StaticServiceProperty(name = "osgi.command.scope", value = "icasa", type = "String"),
      @StaticServiceProperty(name = "osgi.command.function", type = "String[]", value = "{attachPersonToZone}"),
      @StaticServiceProperty(name = "name", value = "attach-person-zone", type = "String") })
@Instantiate(name = "attach-person-zone-command")
public class AttachPersonToZoneCommand extends AbstractCommand {

		
	private String person;
	
	private String zone;

	private boolean attach;
	
	@Requires
	private SimulationManager simulationManager;


	@Override
	public Object execute() throws Exception {
		if (attach)
			simulationManager.attachPersonToZone(zone, person);
		else
			simulationManager.detachPersonFromZone(zone, person);
		return null;
	}
	
	
	@Override
	public void configure(JSONObject param) throws Exception {
		this.person = param.getString("person");
		this.zone = param.getString("zone");
		this.attach = param.getBoolean("attach");
	}
	
	
	public void attachPersonToZone(String person, String zone, boolean attach) throws Exception {
	   this.person = person;
	   this.zone = zone;
	   this.attach = attach;
	   execute();
   }
	

}