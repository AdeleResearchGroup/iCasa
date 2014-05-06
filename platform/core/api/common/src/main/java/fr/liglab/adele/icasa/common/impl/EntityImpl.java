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
package fr.liglab.adele.icasa.common.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import fr.liglab.adele.icasa.common.Attributable;
import fr.liglab.adele.icasa.common.Identifiable;
import fr.liglab.adele.icasa.common.StateVariable;
import fr.liglab.adele.icasa.common.StateVariableExtender;
import fr.liglab.adele.icasa.common.StateVariableListener;
import fr.liglab.adele.icasa.common.VariableType;

/**
 * Represents generic implementation of Identifiable and Attributable interfaces. 
 * Most concepts in device manager represents entities which can be implemented using this class.
 *
 *
 */
public class EntityImpl implements Identifiable, Attributable {
	
	public static final String ID_PROP_NAME = "#id";
	
	private Map<String, StateVariableProxy> attributeValues = new ConcurrentHashMap<String, StateVariableProxy>();
	
	private List<StateVariableExtender> _extenders = new ArrayList<StateVariableExtender>();
	
	protected List<StateVariableListener> _listeners = new ArrayList<StateVariableListener>();
	
	protected Object _lockStructChanges = new Object();
	
	public EntityImpl(String id) {
		StateVariable var = new StateVariableImpl(ID_PROP_NAME, id, String.class, VariableType.ID, "Entity identifier", false, true, this);
		addStateVariable(var);
	}

	@Override
	public final Set<String> getVariableNames() {
		return attributeValues.keySet();
	}

	@Override
	public final Object getVariableValue(String propertyName) {
		StateVariable stateVar = attributeValues.get(propertyName);
		if (stateVar == null)
			return null;
		
		return stateVar.getValue();
	}
	
	public final StateVariable getStateVariable(String propertyName) {
		StateVariable stateVar = attributeValues.get(propertyName);
		if (stateVar == null)
			return null;
		
		return stateVar;
	}
	
	/**
	 * Returns the variable implementation of specified variable.
	 * Should not be used by client code.
	 * 
	 * @param propertyName name of an state variable
	 * @return the variable implementation of specified variable.
	 */
	protected final StateVariable getInternalVariable(String propertyName) {
		StateVariableProxy stateVar = attributeValues.get(propertyName);
		if (stateVar == null)
			return null;
		
		return stateVar.getInternalVariable();
	}

	@Override
	public final void setVariableValue(String propertyName, Object value) {
		StateVariable stateVar = attributeValues.get(propertyName);
		if (stateVar == null)
			throw new IllegalArgumentException("Property " + propertyName + " does not exist.");
		
		stateVar.setValue(value);
	}

	@Override
	public final List<StateVariable> getStateVariables() {
		final Collection<StateVariableProxy> stateVars = attributeValues.values();
		return new ArrayList<StateVariable>(stateVars);
	}
	
	public final List<StateVariable> getInternalStateVariables() {
		final Collection<StateVariableProxy> stateVars = attributeValues.values();
		List<StateVariable> internalVars = new ArrayList<StateVariable>();
		for (StateVariableProxy proxy : stateVars) {
			internalVars.add(proxy.getInternalVariable());
		}
		
		return internalVars;
	}

	@Override
	public final void addVariableExtender(StateVariableExtender extender) {
		synchronized (_extenders) {
			_extenders.add(extender);
		}
	}

	@Override
	public final void removeVariableExtender(StateVariableExtender extender) {
		synchronized (_extenders) {
			_extenders.remove(extender);
		}
	}

	@Override
	public final List<StateVariableExtender> getVariableExtenders() {
		return Collections.unmodifiableList(_extenders);
	}

	@Override
	public final String getId() {
		return (String) getVariableValue(ID_PROP_NAME);
	}
	
	/**
	 * Change the implementation of the specified variable.
	 * Client variable references will not be changed.
	 * Variables are considered as not changed, that is why there is no event sent to listeners.
	 * 
	 * @param var a state variable
	 */
	protected final void changeVariableImplem(StateVariable var) {
		synchronized (_lockStructChanges) {
			final String propName = var.getName();
			StateVariableProxy varProxy = attributeValues.get(propName);
			if (varProxy == null)
				throw new IllegalArgumentException("Property " + propName
						+ " already exists.");

			varProxy.setInternalVariable(var);
		}
	}
	
	protected final void addStateVariable(StateVariable var) {
		StateVariableProxy varProxy;
		synchronized (_lockStructChanges) {
			final String propName = var.getName();
			StateVariable stateVar = attributeValues.get(propName);
			if (stateVar != null)
				throw new IllegalArgumentException("Property " + propName
						+ " already exists.");

			varProxy = new StateVariableProxy(var);
			attributeValues.put(propName, varProxy);
		}
		
		synchronized(_listeners) {
			for (StateVariableListener listener : _listeners) {
				listener.addVariable(varProxy, this);
				varProxy.addListener(listener);
			}
		}
	}
	
	protected final void removeStateVariable(StateVariable var) {
		removeStateVariable(var.getName());
	}
	
	protected final void removeStateVariable(String varName) {
		StateVariableProxy varProxy = null;
		synchronized (_lockStructChanges) {
			final String propName = varName;
			varProxy = attributeValues.remove(propName);
		}
		
		if (varProxy == null)
			return;
		
		synchronized(_listeners) {
			for (StateVariableListener listener : _listeners) {
				listener.removeVariable(varProxy, this);
				varProxy.removeListener(listener);
			}
		}
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Identifiable))
			return false;
		
		Identifiable idObj = (Identifiable) obj;
		return getId().equals(idObj.getId());
	}

	@Override
	public void addVariableListener(StateVariableListener listener) {
		synchronized(_listeners) {
			_listeners.add(listener);
			for (StateVariable var : getStateVariables()) {
				var.addListener(listener);
			}
		}
	}

	@Override
	public void removeVariableListener(StateVariableListener listener) {
		synchronized(_listeners) {
			_listeners.remove(listener);
			for (StateVariable var : getStateVariables()) {
				var.removeListener(listener);
			}
		}
	}

	@Override
	public boolean hasStateVariable(String varName) {
		return getStateVariable(varName) != null;
	}

	
}
