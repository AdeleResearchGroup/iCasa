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

import fr.liglab.adele.icasa.commands.Signature;
import fr.liglab.adele.icasa.location.LocatedDevice;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONObject;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.commands.AbstractCommand;
import fr.liglab.adele.icasa.commands.ScriptLanguage;
import fr.liglab.adele.icasa.location.Position;

import java.io.InputStream;
import java.io.PrintStream;


/**
 * 
 * Sets the fault state of device to "Yes"
 *
 * 
 */
@Component(name = "MoveDeviceCommandNew")
@Provides
@Instantiate(name = "move-device-command-new")
public class MoveDeviceCommand extends AbstractCommand {

	@Requires
	private ContextManager contextManager;

    private static Signature MOVE = new Signature(new String[]{ScriptLanguage.DEVICE_ID, ScriptLanguage.LEFT_X, ScriptLanguage.TOP_Y} );
    private static Signature MOVE_WZ = new Signature(new String[]{ScriptLanguage.DEVICE_ID, ScriptLanguage.LEFT_X, ScriptLanguage.TOP_Y, ScriptLanguage.BOTTOM_Z } );

    public MoveDeviceCommand(){
       addSignature(MOVE);
       addSignature(MOVE_WZ);
    }

	@Override
	public Object execute(InputStream in, PrintStream out,JSONObject param, Signature signature) throws Exception {
        String deviceId = param.getString(ScriptLanguage.DEVICE_ID);
        LocatedDevice device = contextManager.getDevice(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device ("+ deviceId +") does not exist");
        }
        int newX = param.getInt( ScriptLanguage.LEFT_X);
        int newY = param.getInt(ScriptLanguage.TOP_Y);
        int newZ = device.getCenterAbsolutePosition().z;
        if (signature.equals(MOVE_WZ)){
            newZ = param.getInt( ScriptLanguage.BOTTOM_Z);
        }
		contextManager.setDevicePosition(deviceId, new Position(newX, newY, newZ));
		return null;
	}

    /**
     * Get the name of the  Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return "move-device";
    }

    @Override
    public String getDescription(){
        return "Move a device to new X,Y positions.\n\t" + super.getDescription();
    }

}
