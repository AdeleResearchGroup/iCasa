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

import java.io.InputStream;
import java.io.PrintStream;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONObject;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.commands.Signature;
import fr.liglab.adele.icasa.commands.AbstractCommand;
import fr.liglab.adele.icasa.commands.ScriptLanguage;
import fr.liglab.adele.icasa.location.Zone;

/**
 * 
 * Command to Create a Zone
 *
 * 
 */
@Component(name = "MoveZoneCommand")
@Provides
@Instantiate(name="move-zone-command")
public class MoveZoneCommand extends AbstractCommand {

    private static final String NAME= "move-zone";

	@Requires
	private ContextManager simulationManager;

    private static Signature MOVE = new Signature(new String[]{ScriptLanguage.ZONE_ID, ScriptLanguage.LEFT_X, ScriptLanguage.TOP_Y});
    private static Signature MOVE_WZ = new Signature(new String[]{ScriptLanguage.ZONE_ID, ScriptLanguage.LEFT_X, ScriptLanguage.TOP_Y, ScriptLanguage.BOTTOM_Z});

    public MoveZoneCommand(){
        addSignature(MOVE);
        addSignature(MOVE_WZ);
    }

	@Override
	public Object execute(InputStream in, PrintStream out,JSONObject param, Signature signature) throws Exception {
        String zoneId = param.getString(ScriptLanguage.ZONE_ID);
        Zone zone = simulationManager.getZone(zoneId);
        if (zone == null){
            throw new IllegalArgumentException("Zone ("+ zoneId +") does not exist");
        }
        int leftX = param.getInt(ScriptLanguage.LEFT_X);
        int topY = param.getInt(ScriptLanguage.TOP_Y);
        int bottomZ = zone.getLeftTopAbsolutePosition().z;
        if (signature.equals(MOVE_WZ)){
            bottomZ = param.getInt(ScriptLanguage.BOTTOM_Z);
        }
		simulationManager.moveZone(zoneId, leftX, topY, bottomZ);
		return null;
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
    public String getDescription(){
        return "Change the position of a zone.\n\t" + super.getDescription();
    }
}