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
import java.util.Set;

/**
 *
 */
@Component(name = "ShowZonesVariablesCommand")
@Provides
@Instantiate(name="show-zones-variables-command")
public class ShowZoneVariablesCommand extends AbstractCommand {

    @Requires
    private ContextManager manager;

    private static final String[] PARAMS =  new String[]{ScriptLanguage.ZONE_ID};

    private static final String NAME= "show-zone";

    public ShowZoneVariablesCommand(){
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
    public Object execute(InputStream in, PrintStream out, JSONObject param, Signature signature) throws Exception {
        String zoneId = param.getString(PARAMS[0]);
        Zone zone = manager.getZone(zoneId);
        if (zone == null){
            throw new IllegalArgumentException("Zone ("+ zoneId +") does not exist");
        }
        out.print(zone);
        out.println("\nVariables: ");
        Set<String> variables = manager.getZoneVariables(zoneId);
        for (String variable : variables) {
            out.println("Variable: " + variable + " - Value: " + manager.getZoneVariableValue(zoneId, variable));
        }
        return null;
    }

    @Override
    public String getDescription(){
        return "Shows the information of a zone.\n\t" + super.getDescription();
    }
}