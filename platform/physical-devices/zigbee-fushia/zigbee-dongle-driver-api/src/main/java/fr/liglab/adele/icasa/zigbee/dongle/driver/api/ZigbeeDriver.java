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
 * Provides this service to implement a Zigbee driver.
 *
 */
public interface ZigbeeDriver {


    /**
     * Returns the current data of the device with specified module address.
     * The returned data could be cached data from last data sent from the device.
     *
     * @param moduleAddress a device module address
     * @return the current data of the device with specified module address.
     */
	Data getData(String moduleAddress);

    /**
     * Sets specified value to the device with corresponding module address.
     *
     * @param moduleAddress a device module address
     * @param dataToSet value to set
     */
	void setData(String moduleAddress, String dataToSet);

    void addListener(ZigbeeSerialPortListener listener);

    void removeListener(ZigbeeSerialPortListener listener);
}
