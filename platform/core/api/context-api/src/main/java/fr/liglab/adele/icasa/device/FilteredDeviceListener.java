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
package fr.liglab.adele.icasa.device;

/**
 * A specialized device listener which
 *
 */
public interface FilteredDeviceListener<T extends GenericDevice> extends DeviceListener<T> {

    /**
     * Called every time a new device arrives.
     * If returns true, the listener will not be notified for events coming from corresponding device.
     *
     * @param device a device
     * @return true if the listener must not be notified for events coming from corresponding device.
     */
    boolean isFiltered(T device);
}
