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

/**
 * A listener which listens to state variable value changes.
 *
 *
 */
public interface StateVariableListener {
	
	/**
	 * Called when a variable is added to an object.
	 * 
	 * @param variable the new variable
	 * @param sourceObject the object which owns this variable. must not be null.
	 */
	public void addVariable(StateVariable variable, Object sourceObject);
	
	/**
	 * Called when a variable is removed from an object.
	 * 
	 * @param variable the removed variable
	 * @param sourceObject the object which owned this variable. must not be null.
	 */
	public void removeVariable(StateVariable variable, Object sourceObject);

	/**
	 * Called when value of a state variable changes.
	 * Old value is equal to null if old value is unknown. 
	 * 
	 * @param variable state variable with the new value
	 * @param oldValue old variable value
	 * @param sourceObject the object which owns this variable. May be null (not all variables are attached to objects).
	 */
	public void notifValueChange(StateVariable variable, Object oldValue, Object newValue, Object sourceObject);
}
