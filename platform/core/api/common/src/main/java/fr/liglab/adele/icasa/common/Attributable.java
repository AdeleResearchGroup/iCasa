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
package fr.liglab.adele.icasa.common;

import java.util.List;
import java.util.Set;

/**
 * Allows an object to define attributes with their corresponding values.
 * Property value can be null.
 *
 *
 */
public interface Attributable {

	/**
	 * Returns a set containing all variable names.
	 * 
	 * @return a set containing all variable names.
	 */
	public Set<String> getVariableNames();
	
	/**
	 * Returns value of specified variable.
	 * Returns null if there is no specified variable
	 * 
	 * @param variableName name of considered variable
	 * @return value of specified variable.
	 */
	public Object getVariableValue(String variableName);
	
	/**
	 * Returns variable with specified name.
	 * Returns null if there is no such variable
	 * 
	 * @param variableName name of considered variable
	 * @return variable with specified name.
	 */
	public StateVariable getStateVariable(String variableName);
	
	/**
	 * Sets value of specified variable.
	 * 
	 * @param variableName name of considered variable
	 * @param value variable value to set
	 */
	public void setVariableValue(String variableName, Object value);
	
	/**
	 * Returns list of state variables which represent all properties.
	 * 
	 * @return list of state variables which represent all properties.
	 */
	public List<StateVariable> getStateVariables();
	
	/**
	 * Adds a variable extender.
	 * 
	 * @param extender a variable extender.
	 */
	public void addVariableExtender(StateVariableExtender extender);
	
	/**
	 * Removes a variable extender.
	 * 
	 * @param extender a variable extender.
	 */
	public void removeVariableExtender(StateVariableExtender extender);
	
	/**
	 * Returns all associated variable extenders.
	 * 
	 * @return all associated variable extenders.
	 */
	public List<StateVariableExtender> getVariableExtenders();
	
	/**
	 * Add a listener which listens of value changes and variable addition and removal.
	 * 
	 * @param listener a listener
	 */
	public void addVariableListener(StateVariableListener listener);
	
	/**
	 * Removes specified listener.
	 * 
	 * @param listener a listener
	 */
	public void removeVariableListener(StateVariableListener listener);

	/**
	 * Returns true if a variable with specified name exists.
	 * 
	 * @param varName a variable name
	 * @return true if a variable with specified name exists.
	 */
	public boolean hasStateVariable(String varName);
}
