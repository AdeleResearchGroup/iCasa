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
import java.util.HashMap;

/**
 * 
 * Create a new device instance
 *
 *
 */
@Component(name = "CreateDeviceCommand")
@Provides
@Instantiate(name="create-device-command")
public class CreateDeviceCommand extends AbstractCommand {

	@Requires	
	private SimulationManager simulationManager;

    public CreateDeviceCommand(){
        addSignature(new Signature(new String[]{ScriptLanguage.ID, ScriptLanguage.TYPE}));
    }

	@Override
   public Object execute(InputStream in, PrintStream out, JSONObject param, Signature signature) throws Exception {
        String deviceId = param.getString(ScriptLanguage.ID);
        String deviceType = param.getString(ScriptLanguage.TYPE);
		simulationManager.createDevice(deviceType, deviceId, new HashMap<String, Object>());
		return null;
   }
    /**
     * Get the name of the  Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return "create-device";
    }

    @Override
    public String getDescription(){
        return "Creates a new simulated device instance.\n\t" + super.getDescription();
    }
}
