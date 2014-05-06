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



import fr.liglab.adele.icasa.commands.AbstractCommand;
import fr.liglab.adele.icasa.commands.ScriptLanguage;
import fr.liglab.adele.icasa.commands.Signature;
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
@Component(name = "AttachDeviceToPersonCommand")
@Provides
@Instantiate(name = "attach-device-person-command")
public class AttachDeviceToPersonCommand extends AbstractCommand {

	@Requires
	private SimulationManager simulationManager;

    public AttachDeviceToPersonCommand(){
        addSignature(new Signature(new String[]{ScriptLanguage.PERSON, ScriptLanguage.DEVICE, ScriptLanguage.ATTACH}));
    }

    /**
     * Get the name of the  Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return "attach-device-person";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object execute(InputStream in, PrintStream out, JSONObject param, Signature signature) throws Exception {
        String person = param.getString(ScriptLanguage.PERSON);
        String device = param.getString(ScriptLanguage.DEVICE);
        boolean attach = param.getBoolean(ScriptLanguage.ATTACH);
        if (attach)
            simulationManager.attachDeviceToPerson(device, person);
        else
            simulationManager.detachDeviceFromPerson(device, person);
        return null;
    }


    @Override
    public String getDescription(){
        return "Attach or detach a device to a person.\n\t" + super.getDescription();
    }
}