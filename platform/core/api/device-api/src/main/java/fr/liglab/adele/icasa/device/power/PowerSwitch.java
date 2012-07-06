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
package fr.liglab.adele.icasa.device.power;


/**
 * Service definition for a simple powerswitch device
 * 
 * @author gunalp
 *
 */
public interface PowerSwitch {
	
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
	String POWERSWITCH_CURRENT_STATUS = "powerswitch.currentStatus";
	
	/**
	 * Return the current status of the powerswitch
	 * returns 'true' if switch is on,
	 * 'false' if switch is off
	 * 
	 * @return the current status of the powerswitch
	 * @see #POWERSWITCH_CURRENT_STATUS
	 */
	boolean getStatus();
	
	/**
	 * Switches the power on
	 * 
	 * @return 'true' if the switch was off and now on, 'false' if the switch was already on
	 * @see #switchOff()
	 */
	boolean switchOn();
	
	/**
	 * Switches the power off
	 * 
	 * @return 'true' if the switch was on and now off, 'false' if the switch was already off
	 * @see #switchOn()
	 */
	boolean switchOff();
	
}
