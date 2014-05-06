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
@Component(name = "ModifyZoneVariableCommand")
@Provides
@Instantiate(name = "modify-zone-variable-command")
public class ModifyZoneVariableCommand extends AbstractCommand {

	@Requires
	private ContextManager simulationManager;

    private final Signature typedSignature;
    private final Signature unTypedSignature;

    public ModifyZoneVariableCommand(){
        unTypedSignature = new Signature(new String[]{ScriptLanguage.ZONE_ID, ScriptLanguage.VARIABLE, ScriptLanguage.VALUE});
        typedSignature =  new Signature(new String[]{ScriptLanguage.ZONE_ID, ScriptLanguage.VARIABLE, ScriptLanguage.VALUE, ScriptLanguage.TYPE});
        addSignature(unTypedSignature);
        addSignature(typedSignature);
    }

	@Override
	public Object execute(InputStream in, PrintStream out,JSONObject param, Signature signature) throws Exception {
        String zoneId = param.getString(ScriptLanguage.ZONE_ID);
        String variableName = param.getString(ScriptLanguage.VARIABLE);
        Object value = getValue(param,signature);
		simulationManager.setZoneVariable(zoneId, variableName, value);
		return null;
	}

    private Object getValue(JSONObject param, Signature signature) throws Exception{
        Object value = null;
        if (signature.equals(typedSignature)){
            String type = param.getString(ScriptLanguage.TYPE);
            if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("Integer") || type.equalsIgnoreCase("i")){
                value = Integer.valueOf(param.getString(ScriptLanguage.VALUE));
            } else if (type.equalsIgnoreCase("float") || type.equalsIgnoreCase("f")){
                value = Float.valueOf(param.getString(ScriptLanguage.VALUE));
            } else if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("d")){
                value = Double.valueOf(param.getString(ScriptLanguage.VALUE));
            } else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("b")){
                value = Boolean.valueOf(param.getString(ScriptLanguage.VALUE));
            } else if (type.equalsIgnoreCase("String") || type.equalsIgnoreCase("s")){
                value = param.getString(ScriptLanguage.VALUE);
            } else {
                throw new Exception("Unknown Data type for zone variable " + type);
            }
            return value;
        }
        //If type is not given, it will try to see if it is Double or String
        try{
            value = param.getDouble(ScriptLanguage.VALUE);//See if is a number.
        }catch (Exception ex){
            value = param.getString(ScriptLanguage.VALUE);//If not, it will be treated as a string.
        }
        return value;

    }

    /**
     * Get the name of the  Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return "modify-zone-variable";
    }

    @Override
    public String getDescription(){
        return "Modify the value of a variable in a given zone \n\tAccepted types [int, float, double, boolean, string].\n\t" + super.getDescription();
    }
}
