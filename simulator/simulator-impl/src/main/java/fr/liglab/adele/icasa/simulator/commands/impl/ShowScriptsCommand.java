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
import fr.liglab.adele.icasa.commands.Signature;
import fr.liglab.adele.icasa.simulator.script.executor.ScriptExecutor;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

/**
 *
 */
@Component(name = "ShowScriptsCommand")
@Provides
@Instantiate(name="show-scripts-command")
public class ShowScriptsCommand extends AbstractCommand {

    @Requires
    private ScriptExecutor executor;


    private static final String[] PARAMS =  new String[]{};

    private static final String NAME= "show-scripts";

    public ShowScriptsCommand(){
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
        out.println("Scripts: ");
        List<String> scripts = executor.getScriptList();
        for (String script : scripts) {
            out.println(script);
        }
        return null;
    }


    @Override
    public String getDescription(){
        return "Shows the list of the available scripts.\n\t" + super.getDescription();
    }
}