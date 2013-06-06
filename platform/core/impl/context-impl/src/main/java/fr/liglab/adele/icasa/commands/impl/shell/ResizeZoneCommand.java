/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.commands.impl.shell;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.Signature;
import fr.liglab.adele.icasa.commands.impl.AbstractCommand;
import fr.liglab.adele.icasa.commands.impl.ScriptLanguage;
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
 * @author Gabriel
 * 
 */
@Component(name = "ResizeZoneCommand")
@Provides
@Instantiate(name="resize-zone-command")
public class ResizeZoneCommand extends AbstractCommand {
	@Requires
	private ContextManager simulationManager;

    private static final Signature RESIZE = new Signature(new String[]{ScriptLanguage.ZONE_ID, ScriptLanguage.WIDTH, ScriptLanguage.HEIGHT});
    private static final Signature RESIZE_WZ = new Signature(new String[]{ScriptLanguage.ZONE_ID, ScriptLanguage.WIDTH, ScriptLanguage.HEIGHT, ScriptLanguage.DEPTH});

    private static final String NAME= "resize-zone";

    public ResizeZoneCommand(){
        setSignature(RESIZE);
        setSignature(RESIZE_WZ);
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
	public Object execute(InputStream in, PrintStream out,JSONObject param, Signature signature) throws Exception {
        String zoneId = param.getString(signature.getParameters()[0]);
        Zone zone = simulationManager.getZone(zoneId);
        if (zone == null){
            throw new IllegalArgumentException("Zone ("+ zoneId +") does not exist");
        }
        int width = param.getInt(signature.getParameters()[1]);
        int height = param.getInt(signature.getParameters()[2]);
        int depth = zone.getZLength();
        if (signature.equals(RESIZE_WZ)){
            depth = param.getInt(signature.getParameters()[3]);
        }
		simulationManager.resizeZone(zoneId, width, height, depth);
		return null;
	}
    @Override
    public String getDescription(){
        return "Resize a zone.\n\t" + super.getDescription();
    }

}