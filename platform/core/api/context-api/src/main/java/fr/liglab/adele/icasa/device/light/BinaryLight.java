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
package fr.liglab.adele.icasa.device.light;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Service definition of a simple binary light device.
 *
 */
public @ContextService interface BinaryLight extends GenericDevice {

	/**
	 * Service property indicating whether the binary light is is turned on or
	 * off.
	 * 
	 * <ul>
	 * <li>This property is <b>mandatory</b></li>
	 * <li>Type of values : <b><code>java.lang.Boolean</code></b></li>
	 * <li>Description : value is <code>true</code> when the light is turned on,
	 * <code>false</code> otherwise.</li>
	 * </ul>
	 * 
	 * @see #getPowerStatus()
	 * @see #setPowerStatus(boolean)
	 */
	public final static @State String BINARY_LIGHT_POWER_STATUS = "binaryLight.powerStatus";

	/**
	 * Return the current power state of this binary light.
	 *
	 * @return the current power state of this binary light.
	 * @see #setPowerStatus(boolean)
	 * @see #BINARY_LIGHT_POWER_STATUS
	 */
	boolean getPowerStatus();

	/**
	 * Change the power status of this binary light.
	 * 
	 * @param state
	 *           the new power state of this binary light.
	 * @return the previous power state of this binary light.
	 * @see #getPowerStatus()
	 * @see #BINARY_LIGHT_POWER_STATUS
	 */
	void setPowerStatus(boolean state);
	
	/**
	 * Change the power status to true
	 */
	void turnOn();
	
	/**
	 * Change the power status to false
	 */
	void turnOff();
	


}
