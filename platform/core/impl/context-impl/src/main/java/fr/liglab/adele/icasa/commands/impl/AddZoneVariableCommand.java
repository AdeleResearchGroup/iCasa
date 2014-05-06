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
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.PrintStream;

/**
 *
 */
@Component(name = "AddZoneVariableCommand")
@Provides
@Instantiate(name="add-zone-variable-command")
public class AddZoneVariableCommand extends AbstractCommand {

    @Requires
    private ContextManager contextManager;

    public AddZoneVariableCommand(){
        addSignature(new Signature(new String[]{ScriptLanguage.ZONE_ID, ScriptLanguage.VARIABLE}));
    }
    @Override
    public Object execute(InputStream in, PrintStream out,JSONObject param, Signature signature) throws Exception {
        contextManager.addZoneVariable(param.getString(ScriptLanguage.ZONE_ID), param.getString(ScriptLanguage.VARIABLE));
        return null;
    }


    /**
     * Get the name of the  Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return "add-zone-variable";
    }


    @Override
    public String getDescription(){
        return "Add a variable to a zone.\n\t" + super.getDescription();
    }

}