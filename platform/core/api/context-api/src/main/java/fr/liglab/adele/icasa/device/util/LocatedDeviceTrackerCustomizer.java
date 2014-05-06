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
package fr.liglab.adele.icasa.device.util;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;

/**
 * Device Tracker Customizer.
 *
 */
public interface LocatedDeviceTrackerCustomizer {

    /**
     * A device is being added to the Tracker object.
     * This method is called before a device which matched the search parameters of the Tracker object is added to it.
     * This method must return true to be tracked for this device object.
     * @param device the device being added to the Tracker object.
     * @return The service object to be tracked for the DeviceReference object or null if the DeviceReference object should not be tracked.
     */
    boolean addingDevice(LocatedDevice device);
    
    /**
     * A device tracked by the Tracker object has been added in the list.
     * This method is called when a device has been added in the managed list (after addingDevice) and if the device has not disappeared before during the callback.
     * @param device the added device
     */
    void addedDevice(LocatedDevice device);

    /**
     * Called when a device tracked by the Tracker object has been modified.
     * A tracked device is considered modified according to tracker configuration.
     *
     * @param device the changed device
     * @param propertyName name of the property that has changed
     * @param oldValue previous value of the property
     * @param newValue new value of the property
     */
    void modifiedDevice(LocatedDevice device, String propertyName, Object oldValue, Object newValue);

    void movedDevice(LocatedDevice device, Position oldPosition, Position newPosition);

    /**
     * A device tracked by the Tracker object has been removed.
     * This method is called after a device is no longer being tracked by the Tracker object.
     * @param device the removed device.
     */
    void removedDevice(LocatedDevice device);

}
