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
package fr.liglab.adele.icasa.service.preferences.impl.commands;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.icasa.commands.Signature;
import fr.liglab.adele.icasa.commands.ScriptLanguage;
import fr.liglab.adele.icasa.service.preferences.Preferences;

@Component(name = "SetGlobalPropertyCommand")
@Provides
@Instantiate(name = "set-global-property-command")
public class SetGlobalPropertyCommand extends AbstractSetPropertyCommand {

	@Requires
	private Preferences preferenceService;

	public SetGlobalPropertyCommand() {
		addSignature(new Signature(new String[] { ScriptLanguage.NAME, ScriptLanguage.VALUE }));
		addSignature(new Signature(new String[] { ScriptLanguage.NAME, ScriptLanguage.VALUE, ScriptLanguage.TYPE }));
	}

	@Override
	public String getName() {
		return "set-global-property";
	}


	@Override
	public String getDescription() {
		return "Sets a platform configuration property.\n\t" + super.getDescription();
	}

	@Override
   protected String getPreferencesType() {
	   return GLOBAL_PREFERENCE;
   }

	@Override
   protected String getExtraParameterName() {
	   return "no-name";
   }

	@Override
   protected Preferences getPreferenceService() {
	   return preferenceService;
   }
	
	

}
