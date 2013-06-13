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
package fr.liglab.adele.icasa.service.preferences.impl.commands;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.icasa.Signature;
import fr.liglab.adele.icasa.commands.impl.ScriptLanguage;
import fr.liglab.adele.icasa.service.preferences.Preferences;

@Component(name = "SetAppPreferenceCommand")
@Provides
@Instantiate(name = "set-app-preference-command")
public class SetApplicationPreferenceCommand extends AbstractSetPreferencesCommand {

	@Requires
	private Preferences preferenceService;

	public SetApplicationPreferenceCommand() {
		setSignature(new Signature(new String[] { ScriptLanguage.APPLICATION_ID, ScriptLanguage.NAME, ScriptLanguage.VALUE }));
		setSignature(new Signature(new String[] { ScriptLanguage.APPLICATION_ID, ScriptLanguage.NAME, ScriptLanguage.VALUE, ScriptLanguage.TYPE }));
	}

	@Override
	public String getName() {
		return "set-app-preference";
	}


	@Override
	public String getDescription() {
		return "Sets a application preference.\n\t" + super.getDescription();
	}

	@Override
   protected String getPreferencesType() {
	   return APPLICATION_PREFERENCE;
   }

	@Override
   protected String getExtraParameterName() {
	   return ScriptLanguage.APPLICATION_ID;
   }

	@Override
   protected Preferences getPreferenceService() {
	   return preferenceService;
   }



}
