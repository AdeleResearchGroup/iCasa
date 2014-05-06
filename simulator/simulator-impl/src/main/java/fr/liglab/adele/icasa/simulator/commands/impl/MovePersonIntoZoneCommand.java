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
package fr.liglab.adele.icasa.simulator.commands.impl;


import fr.liglab.adele.icasa.commands.Signature;
import fr.liglab.adele.icasa.commands.AbstractCommand;
import fr.liglab.adele.icasa.commands.ScriptLanguage;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * 
 * Moves a person between the simulated environments 
 *
 *
 */
@Component(name = "MovePersonIntoZoneCommand")
@Provides
@Instantiate(name = "move-person-zone-command")
public class MovePersonIntoZoneCommand extends AbstractCommand {

	@Requires
	private SimulationManager simulationManager;

    public MovePersonIntoZoneCommand() {
        addSignature(new Signature(new String[]{ScriptLanguage.PERSON_ID, ScriptLanguage.ZONE_ID}));
    }


	@Override
	public Object execute(InputStream in, PrintStream out, JSONObject param, Signature signature) throws Exception {
        String personId = param.getString(ScriptLanguage.PERSON_ID);
        String zoneId = param.getString(ScriptLanguage.ZONE_ID);
        System.out.println("Moving " + personId + " to Zone " + zoneId);
		simulationManager.setPersonZone(personId, zoneId);
		return null;
	}
	
    /**
     * Get the name of the  Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return "move-person-zone";
    }

    @Override
    public String getDescription(){
        return "Move a person into a zone.\n\t" + super.getDescription();
    }
}