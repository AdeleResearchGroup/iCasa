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
package fr.liglab.adele.icasa.access.command;

import fr.liglab.adele.icasa.access.AccessManager;
import fr.liglab.adele.icasa.access.DeviceAccessPolicy;
import fr.liglab.adele.icasa.access.MemberAccessPolicy;
import fr.liglab.adele.icasa.commands.AbstractCommand;
import fr.liglab.adele.icasa.commands.ScriptLanguage;
import fr.liglab.adele.icasa.commands.Signature;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.PrintStream;

/**
 *
 */
@Component(name = "AccessManagerUpdateCommand")
@Provides
@Instantiate(name="access-manager-update-command-1")
public class AccessManagerUpdateCommand extends AbstractCommand {


    private static final Signature UPDATE_RIGHT = new Signature(new String[]{ScriptLanguage.APPLICATION_ID, ScriptLanguage.DEVICE_ID, ScriptLanguage.VALUE});

    private static final Signature UPDATE_RIGHT_METHOD = new Signature(new String[]{ScriptLanguage.APPLICATION_ID, ScriptLanguage.DEVICE_ID, ScriptLanguage.METHOD, ScriptLanguage.VALUE});

    private static final String NAME = "set-access-right";


    @Requires
    AccessManager manager;

    public AccessManagerUpdateCommand(){
        addSignature(UPDATE_RIGHT);
        addSignature(UPDATE_RIGHT_METHOD);
    }

    @Override
    public Object execute(InputStream in, PrintStream out, JSONObject param, Signature signature) throws Exception {
        if (signature.equals(UPDATE_RIGHT)){
            manager.setDeviceAccess(param.getString(ScriptLanguage.APPLICATION_ID), param.getString(ScriptLanguage.DEVICE_ID), DeviceAccessPolicy.fromString(param.getString(ScriptLanguage.VALUE)));
        } else {
            manager.setMethodAccess(param.getString(ScriptLanguage.APPLICATION_ID), param.getString(ScriptLanguage.DEVICE_ID), param.getString(ScriptLanguage.METHOD), MemberAccessPolicy.fromString(param.getString(ScriptLanguage.VALUE)));
        }
        return null;
    }

     /**
     * Get the name of the Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription(){
        StringBuilder description = new StringBuilder("Modify a device access right: ");
        String[] params = UPDATE_RIGHT.getParameters();
        description.append("\t(");
        for (String param: params){
            description.append(" ");
            description.append(param);
            description.append(" ");
        }
        description.append(")\n");
        description.append("\t\tPossible Values: ");
        for (DeviceAccessPolicy b : DeviceAccessPolicy.values()) {
            description.append(b.toString()).append(" ");
        }

        params = UPDATE_RIGHT_METHOD.getParameters();
        description.append("\n\tModify a method access right: ");
        description.append("\t(");
        for (String param: params){
            description.append(" ");
            description.append(param);
            description.append(" ");
        }
        description.append(")\n");
        description.append("\t\tPossible Values: ");
        for (MemberAccessPolicy b : MemberAccessPolicy.values()) {
            description.append(b.toString()).append(" ");
        }
        description.append("\n");
        return description.toString();
    }


    /**
     *
     * @param param The parameters in JSON format
     * @return true if all parameters are in the JSON object, false if not. For optional
     * parameters, this method should be override..
     * @throws Exception
     */
    @Override
    public boolean validate(JSONObject param, Signature signature) throws Exception {
        boolean validation = super.validate(param, signature);
        if (DeviceAccessPolicy.fromString(param.getString(ScriptLanguage.VALUE)) == null && MemberAccessPolicy.fromString(param.getString(ScriptLanguage.VALUE)) == null){
            return false;
        }
        return validation;
    }

}
