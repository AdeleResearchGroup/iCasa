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

import java.util.Map;

/**
 * Represents a state with a defined type.
 * A state variable explicitly defines if it is able to push notifications of state value changes.
 *
 *
 */
public interface StateVariable {

	/**
	 * Returns variable name.
	 * 
	 * @return variable name.
	 */
	public String getName();
	
	/**
	 * Returns true if it is able to push notifications of state value changes.
	 * This does not mean that it is able to push one notification for every value change but that 
	 * a value change will be sent at least with a certain latency.
	 * 
	 * @return true if it is able to push notifications of state value changes.
	 */
	public boolean canSendNotifications();
	
	/**
	 * Returns true if its value can be modified using the setValue method.
	 * If false, this does not mean that the value cannot be changed outside current digital service.
	 * 
	 * @return true if its value can be modified using the setValue method.
	 */
	public boolean canBeModified();
	
	/**
	 * Add a listener which listens of value changes.
	 * 
	 * @param listener a listener of value changes.
	 */
	public void addListener(StateVariableListener listener);
	
	/**
	 * Removes specified listener.
	 * 
	 * @param listener a listener of value changes.
	 */
	public void removeListener(StateVariableListener listener);
	
	/**
	 * Returns variable value.
	 * Null means that there is no value defined.
	 * 
	 * @return variable value.
	 */
	public Object getValue();
	
	/**
	 * Returns variable description.
	 * 
	 * @return variable description.
	 */
	public String getDescription();
	
	/**
	 * Returns variable type.
	 * 
	 * @return variable type.
	 */
	public VariableType getType();
	
	/**
	 * Returns variable value type.
	 * 
	 * @return variable value type.
	 */
	public Class getValueType();
	
	/**
	 * Sets specified value.
	 * 
	 * @param value the new value to set
	 * @throws IllegalArgumentException if this value cannot be modified.
	 */
	public void setValue(Object value);

	/**
	 * Returns true if this variable has a defined value.
	 * 
	 * @return true if this variable has a defined value.
	 */
	public boolean hasValue();

	/**
	 * Returns object which owns this variable.
	 * 
	 * @return object which owns this variable.
	 */
	public Object getOwner();
	
	/**
	 * Returns true if a metadata with specified has been set.
	 * 
	 * @param name a metadata name
	 * @return true if a metadata with specified has been set.
	 */
	public boolean hasMetadata(String name);
	
	/**
	 * Returns value of specified metadata.
	 * Returns null if there is no such metadata.
	 * 
	 * @param name a metadata name
	 * @return value of specified metadata.
	 */
	public Object getMetadataValue(String name);
	
	/**
	 * Sets specified value to corresponding metadata. 
	 * 
	 * @param name a metadata name
	 * @param value metadata value to set
	 */
	public void setMetadataValue(String name, Object value);
	
	/**
	 * Returns a map containing all metadata of this variable. 
	 * 
	 * @return a map containing all metadata of this variable.
	 */
	public Map<String, Object> getMetadataValues();
}
