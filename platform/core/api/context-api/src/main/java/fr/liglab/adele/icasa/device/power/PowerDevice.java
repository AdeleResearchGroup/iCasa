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

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Common interface for Power device service descriptions
 *
 *
 */
public interface PowerDevice extends GenericDevice {

	/**
	 * Service property indicating the attached devices name
	 * 
	 * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.String</code></b></li>
     * <li>Description : value is a String indicating the attached devices name</li>
     * </ul>
	 */
	String POWER_DEVICE_ATTACHED_DEVICE_NAME = "powerDevice.attachedDeviceName";
	
	/**
	 * Service property indicating the attached devices default Power Rating expressed in watt (W)
	 * 
	 * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.Double</code></b></li>
     * <li>Description : value is a power rating expressed in watt (W), so it is
     * <code>always positive</code>.</li>
     * </ul>
	 */
	String POWER_DEVICE_ATTACHED_DEVICE_WATT = "powerDevice.attachedDeviceWatt";
	
}
