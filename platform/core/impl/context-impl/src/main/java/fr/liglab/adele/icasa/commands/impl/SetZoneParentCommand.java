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
package fr.liglab.adele.icasa.commands.impl;


import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.commands.Signature;
import fr.liglab.adele.icasa.commands.AbstractCommand;
import fr.liglab.adele.icasa.commands.ScriptLanguage;
import fr.liglab.adele.icasa.location.Zone;
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
@Component(name = "SetZoneParentCommand")
@Provides
@Instantiate(name = "set-parent-zone-command")
public class SetZoneParentCommand extends AbstractCommand {

	@Requires
	private ContextManager simulationManager;

    private static final String[] PARAMS =  new String[]{ScriptLanguage.ZONE, ScriptLanguage.PARENT_ZONE, ScriptLanguage.USE_PARENT_VARIABLES};

    private static final String NAME= "set-zone-parent";

    public SetZoneParentCommand(){
        addSignature(new Signature(PARAMS));
    }

    /**
     * Get the name of the  Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return NAME;
    }



	@Override
	public Object execute(InputStream in, PrintStream out,JSONObject param, Signature signature) throws Exception {
        String zoneId = param.getString(PARAMS[0]);
        String parentId = param.getString(PARAMS[1]);
        boolean useParentVariables = param.getBoolean(PARAMS[2]);
		simulationManager.setParentZone(zoneId, parentId);
		Zone zone = simulationManager.getZone(zoneId);
		if (zone!=null)
			zone.setUseParentVariables(useParentVariables);
		return null;
	}
    @Override
    public String getDescription(){
        return "Set the parent of a zone.\n\t" + super.getDescription();
    }


}