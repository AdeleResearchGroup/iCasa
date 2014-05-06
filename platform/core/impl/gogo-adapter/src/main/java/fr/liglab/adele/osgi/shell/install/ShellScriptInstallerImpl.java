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
package fr.liglab.adele.osgi.shell.install;

import java.net.URI;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.Function;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import fr.liglab.adele.osgi.shell.installer.ShellScriptInstaller;

/**
 * 
 */
@Component(immediate = true)
@Instantiate
@Provides
public final class ShellScriptInstallerImpl implements ShellScriptInstaller {

	@Requires
	protected CommandProcessor _processor;

	/**
	 * Keep a track of the registered command so as to be able to unregister
	 * them
	 */
	private final Map<String, ServiceRegistration> m_functions = new HashMap<String, ServiceRegistration>();

	private final Object _functionsLock = new Object();

	public static final String PROP_DESCRIPTION = "command.description";

	/**
	 * The bundle context used to register services.
	 */
	private final BundleContext m_context;


	ShellScriptInstallerImpl(BundleContext context) {
		m_context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.osgi.shell.installer.ShellScriptInstaller#install(java
	 * .lang.String, java.lang.String, java.net.URI)
	 */
	public void install(String scope, String name, URI scriptPath) {

		ScriptFunction function = new ScriptFunction(scriptPath, _processor);

		// Register the command
		Dictionary commandProperties = new Properties();
		commandProperties.put(CommandProcessor.COMMAND_FUNCTION,
				new String[] { name });
		commandProperties.put(CommandProcessor.COMMAND_SCOPE, scope);

		commandProperties.put(PROP_DESCRIPTION, scriptPath.toString());
		
		ServiceRegistration commandRegistration = m_context.registerService(
				new String[] { Function.class.getName() }, function,
				commandProperties);

		synchronized (_functionsLock) {
			// keep a track of the registration
			m_functions.put(scope + ":" + name, commandRegistration);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.osgi.shell.installer.ShellScriptInstaller#remove(java
	 * .lang.String, java.lang.String)
	 */
	public void remove(String scope, String name)
			throws IllegalArgumentException {


		// Unregister the adapted command
		ServiceRegistration commandRegistration = m_functions.get(scope + ":"
				+ name);
		if (commandRegistration != null) {
			commandRegistration.unregister();
			synchronized (_functionsLock) {
				m_functions.remove(scope + ":" + name);
			}
		}
	}

}
