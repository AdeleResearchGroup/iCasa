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
import fr.liglab.adele.icasa.access.AccessRight;
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
@Component(name = "AccessManagerPrintCommand")
@Provides
@Instantiate(name="access-manager-print-command-0")
public class AccessManagerPrintCommand extends AbstractCommand{

    private static final Signature SEE_RIGHT = new Signature (new String[]{ScriptLanguage.APPLICATION_ID, ScriptLanguage.DEVICE_ID});

    private static final Signature SEE_APP_RIGHT = new Signature(new String[]{ScriptLanguage.APPLICATION_ID});

    private static final String NAME = "show-access-right";

    @Requires
    AccessManager manager;

    public AccessManagerPrintCommand(){
        addSignature(SEE_APP_RIGHT);
        addSignature(SEE_RIGHT);
    }

    @Override
    public Object execute(InputStream in, PrintStream out, JSONObject param, Signature signature) throws Exception {
        if (signature.equals(SEE_RIGHT)){
            out.print(seeAppDeviceRight(param));
        }else {
            out.print(seeAppRight(param));
        }

        return null;
    }

    public String seeAppRight(JSONObject param) throws Exception{
        StringBuffer info = new StringBuffer();
        info.append("Application: ").append(param.getString(ScriptLanguage.APPLICATION_ID)).append('\n');
        AccessRight[]rights =  manager.getAccessRight(param.getString(ScriptLanguage.APPLICATION_ID));
        if (rights != null && rights.length > 0){
            info.append("Device List: ").append('\n');
        }
        for (AccessRight right: rights){
            info.append("\tDevice: ").append(right.getDeviceId()).append(" Access: ").append(right.isVisible()).append('\n');
        }
        return info.toString();
    }

    public String seeAppDeviceRight(JSONObject param) throws Exception{
        AccessRight accessRight = manager.getAccessRight(param.getString(ScriptLanguage.APPLICATION_ID), param.getString(ScriptLanguage.DEVICE_ID));
        StringBuffer info = new StringBuffer();
        info.append("Application: ").append(param.getString(ScriptLanguage.APPLICATION_ID)).append('\n');
        info.append("Device: ").append(param.getString(ScriptLanguage.DEVICE_ID)).append('\n');
        info.append("Right Access: ").append(accessRight.isVisible()).append('\n');
        String[] methods = accessRight.getMethodList();
        if (methods != null && methods.length>0){
            info.append("Method Access: ").append('\n');
        }
        for (String method: methods){
            info.append('\t').append(method).append(": ").append(accessRight.hasMethodAccess(method)).append('\n');
        }
        return info.toString();
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
}
