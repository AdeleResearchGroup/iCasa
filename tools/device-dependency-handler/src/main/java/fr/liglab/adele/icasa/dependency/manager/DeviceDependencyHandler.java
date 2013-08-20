/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under the Apache License, Version 2.0 (the "License");
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
package fr.liglab.adele.icasa.dependency.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.ContextSource;
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.IPojoContext;
import org.apache.felix.ipojo.PolicyServiceContext;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.handlers.dependency.DependencyCallback;
import org.apache.felix.ipojo.handlers.dependency.DependencyHandler;
import org.apache.felix.ipojo.handlers.dependency.DependencyHandlerDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.apache.felix.ipojo.util.DependencyMetadataHelper;
import org.apache.felix.ipojo.util.DependencyModel;
import org.apache.felix.ipojo.util.InstanceConfigurationSource;
import org.apache.felix.ipojo.util.SystemPropertiesSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import fr.liglab.adele.icasa.access.AccessManager;
import fr.liglab.adele.icasa.application.Application;
import fr.liglab.adele.icasa.application.ApplicationManager;

@org.apache.felix.ipojo.annotations.Handler(name = "requiresdevice", namespace = "fr.liglab.adele.icasa.dependency.handler.annotations")
public class DeviceDependencyHandler extends DependencyHandler {

	public static final String NAME_SPACE = "fr.liglab.adele.icasa.dependency.handler.annotations";

	/**
	 * Proxy settings property.
	 */
	public static final String PROXY_SETTINGS_PROPERTY = "ipojo.proxy";

	/**
	 * Proxy type property.
	 */
	public static final String PROXY_TYPE_PROPERTY = "ipojo.proxy.type";

	/**
	 * Proxy type value: smart.
	 */
	public static final String SMART_PROXY = "smart";

	/**
	 * Proxy type value: dynamic-proxy.
	 */
	public static final String DYNAMIC_PROXY = "dynamic-proxy";

	/**
	 * Proxy settings value: enabled.
	 */
	public static final String PROXY_ENABLED = "enabled";

	/**
	 * Proxy settings value: disabled.
	 */
	public static final String PROXY_DISABLED = "disabled";

	/**
	 * Dependency field type : Vector The dependency will be injected as a vector.
	 */
	protected static final int VECTOR = 2;

	/**
	 * Dependency Field Type : List. The dependency will be injected as a list.
	 */
	protected static final int LIST = 1;

	/**
	 * Dependency Field Type : Set. The dependency will be injected as a set.
	 */
	protected static final int SET = 3;

	/**
	 * List of dependencies of the component.
	 */
	private final List<DeviceDependency> m_dependencies = new ArrayList<DeviceDependency>();

	/**
	 * Is the handler started.
	 */
	private boolean m_started;

	/**
	 * The handler description.
	 */
	private DependencyHandlerDescription m_description;

	/**
	 * The instance configuration context source, updated once reconfiguration.
	 */
	private InstanceConfigurationSource m_instanceConfigurationSource;

	@Requires
	private AccessManager accessManager;

	@Requires
	private ApplicationManager applicationManager;

	/**
	 * Get the list of managed dependency.
	 * 
	 * @return the dependency list
	 */
	public Dependency[] getDependencies() {
		return m_dependencies.toArray(new Dependency[m_dependencies.size()]);
	}

	/**
	 * Validate method. This method is invoked by an AbstractServiceDependency when this dependency becomes RESOLVED.
	 * 
	 * @param dep : the dependency becoming RESOLVED.
	 * @see org.apache.felix.ipojo.util.DependencyStateListener#validate(org.apache.felix.ipojo.util.DependencyModel)
	 */
	public void validate(DependencyModel dep) {
		checkContext();
	}

	/**
	 * Invalidate method. This method is invoked by an AbstractServiceDependency when this dependency becomes UNRESOLVED
	 * or BROKEN.
	 * 
	 * @param dep : the dependency becoming UNRESOLVED or BROKEN.
	 * @see org.apache.felix.ipojo.util.DependencyStateListener#invalidate(org.apache.felix.ipojo.util.DependencyModel)
	 */
	public void invalidate(DependencyModel dep) {
		setValidity(false);
	}

	/**
	 * Check the validity of the dependencies.
	 */
	protected void checkContext() {
		if (!m_started) {
			return;
		}
		synchronized (m_dependencies) {
			// Store the initial state
			boolean initialState = getValidity();

			boolean valid = true;
			for (Dependency dep : m_dependencies) {
				if (dep.getState() != Dependency.RESOLVED) {
					valid = false;
					break;
				}
			}

			// Check the component dependencies
			if (valid) {
				// The dependencies are valid
				if (!initialState) {
					// There is a state change
					setValidity(true);
				}
				// Else do nothing, the component state stay VALID
			} else {
				// The dependencies are not valid
				if (initialState) {
					// There is a state change
					setValidity(false);
				}
				// Else do nothing, the component state stay UNRESOLVED
			}

		}
	}

	/**
	 * Check if the dependency given is valid in the sense that metadata are consistent.
	 * 
	 * @param dep : the dependency to check
	 * @param manipulation : the component-type manipulation metadata
	 * @return true if the dependency is valid
	 * @throws ConfigurationException : the checked dependency is not correct
	 */
	private boolean checkDependency(DeviceDependency dep, PojoMetadata manipulation) throws ConfigurationException {
		// Check the internal type of dependency
		String field = dep.getField();
		DependencyCallback[] callbacks = dep.getCallbacks();
		int index = dep.getConstructorParameterIndex();

		if (callbacks == null && field == null && index == -1) {
			throw new ConfigurationException("A service requirement requires at least binding methods, "
			      + "a field or a constructor parameter");
		}

		for (int i = 0; callbacks != null && i < callbacks.length; i++) {
			MethodMetadata[] mets = manipulation.getMethods(callbacks[i].getMethodName());
			if (mets.length == 0) {
				debug("A requirement callback " + callbacks[i].getMethodName()
				      + " does not exist in the implementation class, will try the super classes");
			} else {
				if (mets[0].getMethodArguments().length > 2) {
					throw new ConfigurationException("Requirement Callback : A requirement callback "
					      + callbacks[i].getMethodName() + " must have 0, 1 or 2 arguments");
				}

				callbacks[i].setArgument(mets[0].getMethodArguments());

				if (mets[0].getMethodArguments().length == 1) {
					if (!mets[0].getMethodArguments()[0].equals(ServiceReference.class.getName())) {
						// The callback receives the service object.
						setSpecification(dep, mets[0].getMethodArguments()[0], false); // Just warn if a mismatch is
						                                                               // discovered.
					}
				} else if (mets[0].getMethodArguments().length == 2) {
					// The callback receives service object, service reference. Check that the second argument is a service
					// reference
					if (!(mets[0].getMethodArguments()[1].equals(ServiceReference.class.getName()) // callback with (service
					                                                                               // object, service
					                                                                               // reference)
					      || mets[0].getMethodArguments()[1].equals(Dictionary.class.getName()) // callback with (service
					                                                                            // object, service properties
					                                                                            // in a dictionary)
					|| mets[0].getMethodArguments()[1].equals(Map.class.getName()))) { // callback with (service object,
						                                                                // service properties in a map)
						String message = "The requirement callback " + callbacks[i].getMethodName()
						      + " must have a ServiceReference, a Dictionary or a Map as the second argument";
						throw new ConfigurationException(message);
					}
					setSpecification(dep, mets[0].getMethodArguments()[0], false); // Just warn if a mismatch is discovered.
				}
			}

		}

		if (field != null) {
			FieldMetadata meta = manipulation.getField(field);
			if (meta == null) {
				throw new ConfigurationException("Requirement Callback : A requirement field " + field
				      + " does not exist in the implementation class");
			}
			String type = meta.getFieldType();
			if (type.endsWith("[]")) {
				if (dep.isProxy()) {
					info("Arrays cannot be used for proxied dependencies - Disabling the proxy mode");
					// TODO: in Icasa arrays can be proxied
					dep.setProxy(false);
				}
				// Set the dependency to multiple
				dep.setAggregate(true);
				type = type.substring(0, type.length() - 2);
			} else if (type.equals(List.class.getName()) || type.equals(Collection.class.getName())) {
				dep.setType(LIST);
				type = null;
			} else if (type.equals(Vector.class.getName())) {
				dep.setType(VECTOR);
				if (dep.isProxy()) {
					warn("Vectors cannot be used for proxied dependencies - Disabling the proxy mode");
					// TODO: in Icasa arrays can be proxied
					dep.setProxy(false);
				}
				type = null;
			} else if (type.equals(Set.class.getName())) {
				dep.setType(SET);
				type = null;
			} else {
				if (dep.isAggregate()) {
					throw new ConfigurationException("A required service is not correct : the field " + meta.getFieldName()
					      + " must be an array to support aggregate injections");
				}
			}
			setSpecification(dep, type, true); // Throws an exception if the field type mismatch.
		}

		// Constructor parameter
		if (index != -1) {
			if (!dep.isProxy()) {
				throw new ConfigurationException("Services injected into constructor must be proxied");
			}

			MethodMetadata[] cts = manipulation.getConstructors();
			// If we don't have a type, try to get the first constructor and get the type of the parameter
			// we the index 'index'.
			if (cts.length > 0 && cts[0].getMethodArguments().length > index) {
				String type = cts[0].getMethodArguments()[index];
				if (type.endsWith("[]")) {
					throw new ConfigurationException("Services injected into constructor cannot be arrays");
				} else if (type.equals(List.class.getName()) || type.equals(Collection.class.getName())) {
					dep.setType(LIST);
					type = null;
				} else if (type.equals(Vector.class.getName())) {
					throw new ConfigurationException("Services injected into constructor cannot be Vectors");
				} else if (type.equals(Set.class.getName())) {
					dep.setType(SET);
					type = null;
				} else {
					if (dep.isAggregate()) {
						throw new ConfigurationException("A required service is not correct : the constructor parameter "
						      + index + " must be an aggregate type to support aggregate injections");
					}
				}
				setSpecification(dep, type, true); // Throws an exception if the field type mismatch.
			} else {
				throw new ConfigurationException("Cannot determine the specification of the dependency " + index
				      + ", please use the specification attribute");
			}
		}

		// At this point we must have discovered the specification, it it's null, throw a ConfiguraitonException
		if (dep.getSpecification() == null) {
			String id = dep.getId();
			if (id == null) {
				dep.getField();
			}
			if (id == null && dep.getCallbacks() != null && dep.getCallbacks().length > 0) {
				id = dep.getCallbacks()[0].getMethodName();
			}
			throw new ConfigurationException("Cannot determine the targeted service specification for the dependency '"
			      + id + "'");
		}

		// Disable proxy on scalar dependency targeting non-interface specification
		if (!dep.isAggregate() && dep.isProxy()) {
			if (!dep.getSpecification().isInterface()) {
				warn("Proxies cannot be used on service dependency targeting non interface " + "service specification "
				      + dep.getSpecification().getName());
				dep.setProxy(false);
			}
		}

		// Disables proxy on null (nullable=false)
		// if (dep.isProxy() && dep.isOptional() && ! dep.supportsNullable()) {
		// dep.setProxy(false);
		// warn("Optional Null Dependencies do not support proxying - Disable the proxy mode");
		// }

		// Check that all required info are set
		return dep.getSpecification() != null;
	}

	/**
	 * Check if we have to set the dependency specification with the given class name.
	 * 
	 * @param dep : dependency to check
	 * @param className : class name
	 * @param error : set to true to throw an error if the set dependency specification and the given specification are
	 *           different.
	 * @throws ConfigurationException : the specification class cannot be loaded correctly
	 */
	private void setSpecification(Dependency dep, String className, boolean error) throws ConfigurationException {
		if (className == null) {
			// No found type (list and vector)
			if (dep.getSpecification() == null) {
				if (error) {
					String id = dep.getId();
					if (id == null) {
						id = dep.getField();
						if (id == null) {
							id = Integer.toString(dep.getConstructorParameterIndex());
						}
					}
					throw new ConfigurationException("Cannot discover the required specification for " + id);
				} else {
					// If the specification is different, warn that we will override it.
					info("Cannot discover the required specification for " + dep.getField());
				}
			}
		} else { // In all other case, className is not null.
			if (dep.getSpecification() == null || !dep.getSpecification().getName().equals(className)) {
				if (dep.getSpecification() != null) {
					if (error) {
						throw new ConfigurationException("A required service is not correct : the discovered type ["
						      + className + "] and the specified (or already discovered)  service interface ["
						      + dep.getSpecification().getName() + "] are not the same");
					} else {
						// If the specification is different, warn that we will override it.
						warn("[" + getInstanceManager().getInstanceName() + "] The field type [" + className
						      + "] and the required service interface [" + dep.getSpecification() + "] are not the same");
					}
				}

				Bundle bundle = getInstanceManager().getContext().getBundle();
				try {
					dep.setSpecification(bundle.loadClass(className));
				} catch (ClassNotFoundException e) {
					throw new ConfigurationException("The required service interface (" + className
					      + ") cannot be loaded from bundle " + bundle.getBundleId(), e);
				}
			}
		}
	}

	/**
	 * Configure the handler.
	 * 
	 * @param componentMetadata : the component type metadata
	 * @param configuration : the instance configuration
	 * @throws ConfigurationException : one dependency metadata is not correct.
	 * @see org.apache.felix.ipojo.Handler#configure(org.apache.felix.ipojo.metadata.Element, java.util.Dictionary)
	 */
	public void configure(Element componentMetadata, Dictionary configuration) throws ConfigurationException {
		PojoMetadata manipulation = getFactory().getPojoMetadata();
		boolean atLeastOneField = false;

		// Create the dependency according to the component metadata
		// Element[] deps = componentMetadata.getElements("Requires");
		Element[] deps = componentMetadata.getElements("requiresdevice", NAME_SPACE);

		// Get instance filters.
		Dictionary filtersConfiguration = getRequiresFilters(configuration.get("requires.filters"));
		Dictionary fromConfiguration = (Dictionary) configuration.get("requires.from");

		Map<String, TemporalDependency> tempDependencies = new HashMap<String, TemporalDependency>();

		for (int i = 0; deps != null && i < deps.length; i++) {
			// Create the dependency metadata
			final Element dependencyElement = deps[i];

			String mandatoryProps = dependencyElement.getAttribute("mandatoryprops");

			String identity = dependencyElement.getAttribute("id");
			String type = dependencyElement.getAttribute("type");
			String method = dependencyElement.getAttribute("method");
			String field = dependencyElement.getAttribute("field");

			if (identity == null) {
				throw new ConfigurationException("The id attributte is mandatory in requires-device configuration");
			}
			if (type == null) {
				throw new ConfigurationException("The type attributte is mandatory in requires-device configuration");
			}
			if (type.equals("bind") || type.equals("unbind")) {
				if (method == null) {
					throw new ConfigurationException(
					      "A method must be specified in a dependency of type bind or unbind in requires-device");
				}
			} else if (type.equals("field")) {
				if (field == null) {
					throw new ConfigurationException(
					      "A field must be specified in a dependency of type field in requires-device");
				}
			} else {
				throw new ConfigurationException("A requires-device dependency must contain a type field, bind or unbind");
			}

			String strSpecification = getServiceSpecificationAttribute(dependencyElement);

			String opt = dependencyElement.getAttribute("optional");
			boolean optional = opt != null && opt.equalsIgnoreCase("true");
			String defaultImpl = dependencyElement.getAttribute("default-implementation");

			String agg = dependencyElement.getAttribute("aggregate");
			boolean aggregate = agg != null && agg.equalsIgnoreCase("true");

			// String nul = dependencyElement.getAttribute("nullable");
			// boolean nullable = nul == null || nul.equalsIgnoreCase("true");
			
			boolean nullable = false;

			boolean isProxy = isProxy(dependencyElement);

			BundleContext context = getFacetedBundleContext(dependencyElement);

			String strFilter = computeFilter(dependencyElement, filtersConfiguration, fromConfiguration, aggregate,
			      identity);
			Filter filter = createAndCheckFilter(strFilter);

			Class specification = null;
			if (strSpecification != null) {
				specification = DependencyMetadataHelper.loadSpecification(strSpecification, getInstanceManager()
				      .getContext());
			}

			int policy = DependencyMetadataHelper.getPolicy(dependencyElement);
			Comparator comparator = DependencyMetadataHelper.getComparator(dependencyElement, getInstanceManager()
			      .getGlobalContext());

			TemporalDependency existingDependency = tempDependencies.get(identity);

			if (existingDependency != null) {
				if (existingDependency.type.equals("field")) {
					if (type.equals("bind") || type.equals("unbind")) { // bind or unbind
						existingDependency.addCallback(type, method);
					} else if (type.equals("field")) {
						throw new ConfigurationException(
						      "A existing requires-device dependency using type field has been declared yet");
					}
				} else if (existingDependency.type.equals("bind")) {
					if (type.equals("field")) {
						existingDependency.completeDependency(field, type, aggregate, specification, filter, context,
						      comparator, policy, nullable, optional, defaultImpl, mandatoryProps);
					} else if (type.equals("bind") || type.equals("unbind")) {
						existingDependency.addCallback(type, method);
					}
				} else  { // Existing dependency of type unbind
					if (type.equals("field")) {
						existingDependency.completeDependency(field, type, aggregate, specification, filter, context,
						      comparator, policy, nullable, optional, defaultImpl, mandatoryProps);
					} else if (type.equals("bind")) {
						existingDependency.completeDependency(field, type, aggregate, specification, filter, context,
						      comparator, policy, nullable, optional, defaultImpl, mandatoryProps);
						existingDependency.addCallback(type, method);
					} else if (type.equals("unbind")) {
						existingDependency.addCallback(type, method);
					}
				} 
			} else {
				TemporalDependency temporalDependency = new TemporalDependency(identity);
				temporalDependency.completeDependency(field, type, aggregate, specification, filter, context,
				      comparator, policy, nullable, optional, defaultImpl, mandatoryProps);
				if (type.equals("bind") || type.equals("unbind")) { // bind or unbind dependency
					temporalDependency.addCallback(type, method);
				}
				tempDependencies.put(temporalDependency.id, temporalDependency);
			}

		}

		for (TemporalDependency temporalDependency : tempDependencies.values()) {

			DeviceDependency dep = new DeviceDependency(this, temporalDependency.field, temporalDependency.specification,
			      temporalDependency.filter, temporalDependency.optional, temporalDependency.aggregate,
			      temporalDependency.nullable, true, temporalDependency.id, temporalDependency.context,
			      temporalDependency.policy, temporalDependency.comparator, temporalDependency.defaultImpl, temporalDependency.mandatoryProps);

			for (String method : temporalDependency.bindCallbacks) {
				dep.addDependencyCallback(new DependencyCallback(dep, method, DeviceDependency.BIND_ICASA));
			}
			for (String method : temporalDependency.unbindCallbacks) {
				dep.addDependencyCallback(new DependencyCallback(dep, method, DeviceDependency.UNBIND_ICASA));
			}

			// Check the dependency :
			if (checkDependency(dep, manipulation)) {
				m_dependencies.add(dep);
				if (dep.getField() != null) {
					getInstanceManager().register(manipulation.getField(dep.getField()), dep);
					atLeastOneField = true;
				}
			}

		}

		if (atLeastOneField) { // Does register only if we have fields
			MethodMetadata[] methods = manipulation.getMethods();
			for (MethodMetadata method : methods) {
				for (Dependency dep : m_dependencies) {
					getInstanceManager().register(method, dep);
				}
			}
		}

		m_description = new DependencyHandlerDescription(this, getDependencies()); // Initialize the description.

		manageContextSources(configuration);
	}

	/**
	 * Add internal context source to all dependencies.
	 * 
	 * @param configuration the instance configuration to creates the instance configuration source
	 */
	private void manageContextSources(Dictionary<String, Object> configuration) {
		m_instanceConfigurationSource = new InstanceConfigurationSource(configuration);
		SystemPropertiesSource systemPropertiesSource = new SystemPropertiesSource();

		for (Dependency dependency : m_dependencies) {
			if (dependency.getFilter() != null) {
				dependency.getContextSourceManager().addContextSource(m_instanceConfigurationSource);
				dependency.getContextSourceManager().addContextSource(systemPropertiesSource);

				for (Handler handler : getInstanceManager().getRegisteredHandlers()) {
					if (handler instanceof ContextSource) {
						dependency.getContextSourceManager().addContextSource((ContextSource) handler);
					}
				}
			}
		}
	}

	private String computeFilter(Element dependencyElement, Dictionary filtersConfiguration,
	      Dictionary fromConfiguration, boolean aggregate, String identity) {
		String filter = dependencyElement.getAttribute("filter");
		// Get instance filter if available
		if (filtersConfiguration != null && identity != null && filtersConfiguration.get(identity) != null) {
			filter = (String) filtersConfiguration.get(identity);
		}

		// Compute the 'from' attribute
		filter = updateFilterIfFromIsEnabled(fromConfiguration, dependencyElement, filter, aggregate, identity);
		return filter;
	}

	private String updateFilterIfFromIsEnabled(Dictionary fromConfiguration, Element dependencyElement, String filter,
	      boolean aggregate, String identity) {
		String from = dependencyElement.getAttribute("from");
		if (fromConfiguration != null && identity != null && fromConfiguration.get(identity) != null) {
			from = (String) fromConfiguration.get(identity);
		}
		if (from != null) {
			String fromFilter = "(|(instance.name=" + from + ")(service.pid=" + from + "))";
			if (aggregate) {
				warn("The 'from' attribute is incompatible with aggregate requirements: only one provider will match : "
				      + fromFilter);
			}
			if (filter != null) {
				filter = "(&" + fromFilter + filter + ")"; // Append the two filters
			} else {
				filter = fromFilter;
			}
		}
		return filter;
	}

	private boolean isProxy(Element dependencyElement) {
		boolean isProxy = true;
		String setting = getProxySetting();

		if (setting == null || PROXY_ENABLED.equals(setting)) { // If not set => Enabled
			isProxy = true;
		} else if (PROXY_DISABLED.equals(setting)) {
			isProxy = false;
		}

		String proxy = dependencyElement.getAttribute("proxy");
		// If proxy == null, use default value
		if (proxy != null) {
			if (proxy.equals("false")) {
				isProxy = false;
			} else if (proxy.equals("true")) {
				if (!isProxy) { // The configuration overrides the system setting
					warn("The configuration of a service dependency overrides the proxy mode");
				}
				isProxy = true;
			}
		}
		return isProxy;
	}

	private String getProxySetting() {
		// Detect proxy default value.
		String setting = getInstanceManager().getContext().getProperty(PROXY_SETTINGS_PROPERTY);

		// Felix also includes system properties in the bundle context property, however it is not the case of the
		// other frameworks, so if it's null we should call System.getProperty.

		if (setting == null) {
			setting = System.getProperty(PROXY_SETTINGS_PROPERTY);
		}
		return setting;
	}

	/*
	 * private void addCallbacksToDependencyNew(Element dependencyElement, DeviceDependency dep) throws
	 * ConfigurationException {
	 * 
	 * if (!dependencyElement.containsAttribute("type")) { throw new ConfigurationException(
	 * "Device Dependency : a device dependency type (field, bind or unbind) attribute"); }
	 * 
	 * String method = dependencyElement.getAttribute("method"); String type = dependencyElement.getAttribute("type");
	 * 
	 * int methodType = DeviceDependency.BIND_ICASA; if (type.equals("bind")) { methodType = DeviceDependency.BIND_ICASA;
	 * } else if (type.equals("unbind")) { methodType = DeviceDependency.UNBIND_ICASA; } else { return; // No callbackas
	 * must be added }
	 * 
	 * dep.addDependencyCallback(createDependencyHandler(dep, method, methodType)); }
	 * 
	 * 
	 * private void addCallbacksToDependency(Element dependencyElement, DeviceDependency dep) throws
	 * ConfigurationException { Element[] cbs = dependencyElement.getElements("Callback"); for (int j = 0; cbs != null &&
	 * j < cbs.length; j++) { if (!cbs[j].containsAttribute("method") && cbs[j].containsAttribute("type")) { throw new
	 * ConfigurationException("Requirement Callback : a dependency callback must contain a method " +
	 * "and a type (bind or unbind) attribute"); } String method = cbs[j].getAttribute("method"); String type =
	 * cbs[j].getAttribute("type");
	 * 
	 * int methodType = DependencyCallback.UNBIND; if ("bind".equalsIgnoreCase(type)) { methodType =
	 * DependencyCallback.BIND; } else if ("modified".equalsIgnoreCase(type)) { methodType = DependencyCallback.MODIFIED;
	 * }
	 * 
	 * dep.addDependencyCallback(createDependencyHandler(dep, method, methodType)); } }
	 * 
	 * 
	 * protected DependencyCallback createDependencyHandler(final Dependency dep, final String method, final int type) {
	 * return new DependencyCallback(dep, method, type); }
	 */

	private Filter createAndCheckFilter(String filter) throws ConfigurationException {
		Filter fil = null;
		if (filter != null) {
			try {
				fil = getInstanceManager().getContext().createFilter(filter);
			} catch (InvalidSyntaxException e) {
				throw new ConfigurationException("A requirement filter is invalid : " + filter, e);
			}
		}
		return fil;
	}

	private BundleContext getFacetedBundleContext(Element dep) {
		String scope = dep.getAttribute("scope");
		BundleContext context = getInstanceManager().getContext(); // Get the default bundle context.
		if (scope != null) {
			// If we are not in a composite, the policy is set to global.
			if (scope.equalsIgnoreCase("global")
			      || ((((IPojoContext) getInstanceManager().getContext()).getServiceContext()) == null)) {
				context = new PolicyServiceContext(getInstanceManager().getGlobalContext(), getInstanceManager()
				      .getLocalServiceContext(), PolicyServiceContext.GLOBAL);
			} else if (scope.equalsIgnoreCase("composite")) {
				context = new PolicyServiceContext(getInstanceManager().getGlobalContext(), getInstanceManager()
				      .getLocalServiceContext(), PolicyServiceContext.LOCAL);
			} else if (scope.equalsIgnoreCase("composite+global")) {
				context = new PolicyServiceContext(getInstanceManager().getGlobalContext(), getInstanceManager()
				      .getLocalServiceContext(), PolicyServiceContext.LOCAL_AND_GLOBAL);
			}
		}
		return context;
	}

	private String getServiceSpecificationAttribute(Element dep) {
		String serviceSpecification = dep.getAttribute("interface");
		// the 'interface' attribute is deprecated
		if (serviceSpecification != null) {
			warn("The 'interface' attribute is deprecated, use the 'specification' attribute instead");
		} else {
			serviceSpecification = dep.getAttribute("specification");
		}
		return serviceSpecification;
	}

	/**
	 * Gets the requires filter configuration from the given object. The given object must come from the instance
	 * configuration. This method was made to fix FELIX-2688. It supports filter configuration using an array:
	 * <code>{"myFirstDep", "(property1=value1)", "mySecondDep", "(property2=value2)"});</code>
	 * 
	 * @param requiresFiltersValue the value contained in the instance configuration.
	 * @return the dictionary. If the object in already a dictionary, just returns it, if it's an array, builds the
	 *         dictionary.
	 * @throws ConfigurationException the dictionary cannot be built
	 */
	private Dictionary getRequiresFilters(Object requiresFiltersValue) throws ConfigurationException {
		if (requiresFiltersValue != null && requiresFiltersValue.getClass().isArray()) {
			String[] filtersArray = (String[]) requiresFiltersValue;
			if (filtersArray.length % 2 != 0) {
				throw new ConfigurationException("A requirement filter is invalid : " + requiresFiltersValue);
			}
			Dictionary<String, Object> requiresFilters = new Hashtable<String, Object>();
			for (int i = 0; i < filtersArray.length; i += 2) {
				requiresFilters.put(filtersArray[i], filtersArray[i + 1]);
			}
			return requiresFilters;
		}

		return (Dictionary) requiresFiltersValue;
	}

	/**
	 * Handler start method.
	 * 
	 * @see org.apache.felix.ipojo.Handler#start()
	 */
	public void start() {
		// Start the dependencies
		for (Dependency dep : m_dependencies) {
			dep.start();
		}
		// Check the state
		m_started = true;
		setValidity(false);
		checkContext();
	}

	/**
	 * Handler stop method.
	 * 
	 * @see org.apache.felix.ipojo.Handler#stop()
	 */
	public void stop() {
		m_started = false;
		for (Dependency dep : m_dependencies) {
			dep.stop();
		}
	}

	/**
	 * Handler createInstance method. This method is override to allow delayed callback invocation.
	 * 
	 * @param instance : the created object
	 * @see org.apache.felix.ipojo.PrimitiveHandler#onCreation(Object)
	 */
	public void onCreation(Object instance) {
		for (DeviceDependency dep : m_dependencies) {
			dep.onObjectCreation(instance);
		}
	}

	/**
	 * Get the dependency handler description.
	 * 
	 * @return the dependency handler description.
	 * @see org.apache.felix.ipojo.Handler#getDescription()
	 */
	public HandlerDescription getDescription() {
		return m_description;
	}

	/**
	 * The instance is reconfigured.
	 * 
	 * @param configuration the new instance configuration.
	 */
	@Override
	public void reconfigure(Dictionary configuration) {
		m_instanceConfigurationSource.reconfigure(configuration);
	}

	@Override
	public void stateChanged(int state) {
		if (state == ComponentInstance.DISPOSED) {
			// Cleanup all dependencies
			for (Dependency dep : m_dependencies) {
				dep.cleanup();
			}
		}
		super.stateChanged(state);
	}

	// ------------------------- Added methods --------------------------------//

	/*
	public AccessRight getAccessRight(BundleContext context, String deviceId) {
		String appId = getApplicationId(context);
		if (appId != null) {
			return accessManager.getAccessRight(appId, deviceId);
		}
		return null;
	}
	*/

	public String getApplicationId(BundleContext context) {
		String bundleName = context.getBundle().getSymbolicName();
		Application app = applicationManager.getApplicationOfBundle(bundleName);
		if (app != null) {
			return app.getId();
		}
		return null;
	}

	private class TemporalDependency {

		String id;
		String field;
		String type;
		boolean aggregate;
		Class specification;
		Filter filter;
		BundleContext context;
		Comparator comparator;
		int policy;
		boolean nullable;
		boolean optional;
		String defaultImpl;
		String mandatoryProps;

		List<String> bindCallbacks = new ArrayList<String>();
		List<String> unbindCallbacks = new ArrayList<String>();

		public TemporalDependency(String id) {
			this.id = id;
		}

		public void completeDependency(String field, String type, boolean aggregate, Class specification, Filter filter,
		      BundleContext context, Comparator comparator, int policy, boolean nullable, boolean optional,
		      String defaultImpl, String mandatoryProps) {
			this.field = field;
			this.type = type;
			this.aggregate = aggregate;
			this.specification = specification;
			this.filter = filter;
			this.context = context;
			this.comparator = comparator;
			this.policy = policy;
			this.nullable = nullable;
			this.optional = optional;
			this.defaultImpl = defaultImpl;
			this.mandatoryProps = mandatoryProps;
		}

		void addCallback(String type, String callback) {
			if (type.equals("bind")) {
				bindCallbacks.add(callback);
			} else if (type.equals("unbind")) {
				unbindCallbacks.add(callback);
			}
		}

	}

}
