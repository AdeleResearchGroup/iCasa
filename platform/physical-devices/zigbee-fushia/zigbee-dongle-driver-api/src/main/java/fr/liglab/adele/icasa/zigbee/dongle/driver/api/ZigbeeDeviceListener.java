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
package fr.liglab.adele.icasa.zigbee.dongle.driver.api;


/**
 * Provides this service to be notified of zigbee device events.
 * You can specify a filter to define what are the events you are interested in:
 * example : zigbee.listener.filter="(module.address =1234)"
 *
 */
public interface ZigbeeDeviceListener {


    /**
     * Called when a device data has changed.
     *
     * @param newData new device data
     */
	void deviceDataChanged(String newData);

    /**
     * Called when a device battery level has changed.
     *
     * @param newBatteryLevel new device battery level
     */
	void deviceBatteryLevelChanged(float newBatteryLevel);
}
