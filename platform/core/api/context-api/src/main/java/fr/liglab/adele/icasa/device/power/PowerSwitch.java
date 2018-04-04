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
package fr.liglab.adele.icasa.device.power;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.device.GenericDevice;


/**
 * Service definition for a simple powerswitch device
 *
 */
public @ContextService interface PowerSwitch extends GenericDevice {
	
	/**
	 * Service Property indicating the current Status of the power switch
	 * 
     * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.Boolean</code></b></li>
     * <li>Description : value is expressed in boolean so it is <code>true</code> or <code>false</code>.</li>
     * </ul>
	 * @see #getStatus()
	 */
	@State String CURRENT_STATUS = "currentStatus";
	
	/**
	 * Return the current status of the powerswitch
	 * returns 'true' if switch is on,
	 * 'false' if switch is off
	 * 
	 * @return the current status of the powerswitch
	 * @see #POWER_SWITCH_CURRENT_STATUS
	 */
	public boolean getStatus();

}
