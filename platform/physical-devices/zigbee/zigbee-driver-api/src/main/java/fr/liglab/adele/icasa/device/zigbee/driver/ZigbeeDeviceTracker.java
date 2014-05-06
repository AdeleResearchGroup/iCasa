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

import java.util.EventListener;


/**
 * Provides this service to be notified of zigbee device events.
 * You can specify a filter to define what are the events you are interested in:
 * example : zigbee.listener.filter="(module.address =1234)"
 *
 */
public interface ZigbeeDeviceTracker extends EventListener {
	
	static final String FILTER_PROP_NAME = "zigbee.listener.filter";

    /**
     * Called when a new device has been discovered by the driver.
     *
     * @param deviceInfo information about the device
     */
    void deviceAdded(DeviceInfo deviceInfo);

    /**
     * Called when a device has been discovered by the driver.
     *
     * @param deviceInfo information about the device
     */
	void deviceRemoved(DeviceInfo deviceInfo);

    /**
     * Called when a device data has changed.
     *
     * @param moduleAddress a device module address
     * @param oldData previous device data
     * @param newData new device data
     */
	void deviceDataChanged(String moduleAddress, Data oldData, Data newData);

    /**
     * Called when a device battery level has changed.
     *
     * @param moduleAddress a device module address
     * @param oldBatteryLevel previous device battery level
     * @param newBatteryLevel new device battery level
     */
	void deviceBatteryLevelChanged(String moduleAddress, float oldBatteryLevel, float newBatteryLevel);
}
