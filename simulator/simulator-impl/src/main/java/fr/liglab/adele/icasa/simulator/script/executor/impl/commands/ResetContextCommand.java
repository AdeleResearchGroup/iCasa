/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa-Simulator/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.simulator.script.executor.impl.commands;


import fr.liglab.adele.icasa.Signature;
import fr.liglab.adele.icasa.commands.impl.AbstractCommand;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONObject;

import fr.liglab.adele.icasa.simulator.SimulationManager;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * 
 * Moves a person between the simulated environments 
 * 
 * @author Gabriel
 *
 */
@Component(name = "ResetContextCommand")
@Provides
@Instantiate(name = "reset-context-command")
public class ResetContextCommand extends AbstractCommand {

	@Requires
	private SimulationManager simulationManager;


    private static final String NAME= "reset-context";

    public ResetContextCommand(){
        addSignature(EMPTY_SIGNATURE);
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
		simulationManager.resetContext();
		return null;
	}

    @Override
    public String getDescription(){
        return "Remove all the zones, persons and devices from the iCasa Context.\n\t" + super.getDescription();
    }
}