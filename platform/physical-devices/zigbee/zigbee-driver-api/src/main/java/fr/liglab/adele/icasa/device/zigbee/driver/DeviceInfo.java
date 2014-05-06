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
package fr.liglab.adele.icasa.device.zigbee.driver;

import java.util.Date;

/**
 * Represents general information about a device.
 *
 */
public interface DeviceInfo {

    /**
     * Returns the module address of the device.
     *
     * @return the module address of the device.
     */
    String getModuleAddress();
	
	/**
	 * Returns a battery percentage represented by a float number between 0 and 1.
	 * 
	 * @return a battery percentage represented by a float number between 0 and 1.
	 */
	float getBatteryLevel();

    /**
     * Returns device type code.
     *
     * @return device type code.
     */
	TypeCode getTypeCode();
	
	/**
	 * Returns device data.
	 * @return device data.
	 */
	Data getDeviceData();
	
	/**
	 * return last connexion date.
	 * @return last connexion date.
	 */
	Date getLastConnexionDate();
}
