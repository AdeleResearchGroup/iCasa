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
package fr.liglab.adele.icasa.command.handler;

import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The shell handler allows to expose commands to the felix shell based on
 * annotations. The present version is targeting gogo shell.
 * 
 * Usage :
 * 
 * \@CommandProvider(namespace = "namespaceofthecommand") : declares a new
 * component.
 * 
 * \@Command before each command to add the command to the command list (be
 * aware that it will also consider each method with the same name as a
 * command).
 *
 * 
 */

@Handler(name = "CommandProvider", namespace = CommandHandler.NAMESPACE)
public class CommandHandler extends PrimitiveHandler {

	/**
	 * the handler namespace :
	 */
	public static final String NAMESPACE = "fr.liglab.adele.icasa.command.handler";

	/**
	 * The bundle context used to register the gogo service.
	 */
	private static BundleContext m_context;

	/**
	 * The command service once the pojo has been registered as a command.
	 */
	private static ServiceRegistration m_commandServiceReg;

	/**
	 * The command namespace given by the CommandProvider annotation
	 */
	private String m_commandNameSpace;

	private final String DEFAULT_NAMESPACE = "default";

    private static final Logger L = LoggerFactory
            .getLogger(CommandHandler.class.getName());

	/**
	 * the commandNames set contains the name of the command to be
	 * published.
	 **/
	private Set<String> m_commandNames;

	/**
	 * Get the bundleContext from iPOJO
	 * 
	 * @param context
	 *            : the bundleContext.
	 */
	private CommandHandler(BundleContext context) {
		m_context = context;
	}

	private InstanceManager instanceManager;

	/**
	 * Parses the component's metadata to retrieve the namespace
	 * 
	 * @param metadata
	 *            component's metadata
	 * @param configuration
	 *            instance configuration
	 * @throws ConfigurationException
	 *             the configuration is inconsistent
	 */
	@Override
	public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {

		// Get all Namespace:CommandProvider element from the metadata
		Element[] commandProviderElements = metadata.getElements("CommandProvider", NAMESPACE);

		// If an element match, parse the logLevel attribute of the first found
		// element
		if (commandProviderElements[0].containsAttribute("namespace")) {
			m_commandNameSpace = commandProviderElements[0].getAttribute("namespace");
		} else {
			// set a default namespace
			m_commandNameSpace = DEFAULT_NAMESPACE;
		}

		instanceManager = getInstanceManager();

		Object pojo = instanceManager.getPojoObject();

		// get the methods (including the private methods as gogo supports
		// private methods).
		Method[] methods = pojo.getClass().getDeclaredMethods();

		// initialize the set of commands :
		m_commandNames = new HashSet<String>();

		// for each methods of the pojo object :
		for (Method method : methods) {
			// checks if the method is a command (i.e. as been marked as a
			// command via the annotation)
			if (method.getAnnotation(Command.class) != null) {
				// if yes, add it to the list of commands.
				m_commandNames.add(method.getName());
			}
		}

	}

	/**
	 * The instance is starting.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void start() {
		L.info("start the shell handler");

		Object pojo = instanceManager.getPojoObject();

		// if the component implements some command :
		if (m_commandNames.size() > 0) {

			// create the dictionary for the commands with the information
			// retrieved in the configuration.
			@SuppressWarnings("rawtypes")
			Dictionary properties = new Properties();
			properties.put("osgi.command.scope", m_commandNameSpace);
			properties.put("osgi.command.function",
			        m_commandNames.toArray(new String[m_commandNames.size()]));

			// register the pojo as a gogo shell command provider.
			m_commandServiceReg = m_context.registerService(pojo.getClass().getName(), pojo,
			        properties);
		}
	}

	/**
	 * The instance is stopping.
	 */
	@Override
	public void stop() {
		L.info("stop the shell handler");

		// unregister the command service when stopped
		if (m_commandServiceReg != null) {
			m_commandServiceReg.unregister();
		}
	}

}
