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

/**
 * 
 * Command to Create a Zone
 *
 * 
 */
@Component(name = "CreateZoneCommand")
@Provides
@Instantiate(name = "create-zone-command")
public class CreateZoneCommand extends AbstractCommand {

	@Requires
	private ContextManager simulationManager;

    private static Signature CREATE_ZONE = new Signature(new String[]{ScriptLanguage.ID, ScriptLanguage.LEFT_X, ScriptLanguage.TOP_Y, ScriptLanguage.Y_LENGTH,
            ScriptLanguage.X_LENGTH} );
    private static Signature CREATE_ZONE_WZ = new Signature(new String[]{ScriptLanguage.ID, ScriptLanguage.LEFT_X, ScriptLanguage.TOP_Y, ScriptLanguage.BOTTOM_Z, ScriptLanguage.Y_LENGTH,
            ScriptLanguage.X_LENGTH, ScriptLanguage.Z_LENGTH} );

    public CreateZoneCommand(){
        addSignature(CREATE_ZONE);
        addSignature(CREATE_ZONE_WZ);
    }

	@Override
	public Object execute(InputStream in, PrintStream out,JSONObject param, Signature signature) throws Exception {
		String id = param.getString(ScriptLanguage.ID);
		int leftX = param.getInt(ScriptLanguage.LEFT_X);
		int topY = param.getInt(ScriptLanguage.TOP_Y);
		int height = param.getInt(ScriptLanguage.Y_LENGTH);
		int width = param.getInt(ScriptLanguage.X_LENGTH);
        int depth = Zone.DEFAULT_Z_LENGTH;
        int bottomZ = Zone.DEFAULT_Z_BOTTOM;
        if (signature.equals(CREATE_ZONE_WZ)){
            depth = param.getInt(ScriptLanguage.Z_LENGTH);
            bottomZ = param.getInt(ScriptLanguage.BOTTOM_Z);
        }
		simulationManager.createZone(id, leftX, topY, bottomZ, width, height, depth);
		return null;
	}

	/**
	 * Get the name of the Script and command gogo.
	 * 
	 * @return The command name.
	 */
	@Override
	public String getName() {
		return "create-zone";
	}

	@Override
	public String getDescription() {
		return "Creates a new zone.\n\t" + super.getDescription();
	}
}