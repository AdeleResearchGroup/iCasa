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
package fr.liglab.adele.icasa.location;

import fr.liglab.adele.icasa.listener.IcasaListener;

/**
 * Listener on {@link fr.liglab.adele.icasa.location.LocatedDevice} objects
 *
 *
 */
public interface LocatedDeviceListener extends IcasaListener {

	/**
	 * Called callback when a device property has been added.
	 * @param device The device added.
	 */
    public void deviceAdded(LocatedDevice device);

    /**
     * Called callback when a device property has been removed.
     * @param device The device removed.
     */
    public void deviceRemoved(LocatedDevice device);

    /**
     * Called callback when a device has been moved.
     * 
     * @param device The device moved.
     * @param oldPosition The previous position.
     * @param oldPosition The new position.
     */
    public void deviceMoved(LocatedDevice device, Position oldPosition, Position newPosition);

    /**
     * Called callback when a device property has been modified.
     * 
     * @param device The device modified.
     * @param propertyName The property modified.
     * @param oldValue The property previous value.
     * @param newValue The property new value.  
     */
    public void devicePropertyModified(LocatedDevice device, String propertyName, Object oldValue, Object newValue);

    /**
     * Called callback when a device property has been added.
     * @param device The device modified
     * @param propertyName The name of the property added.
     */
    public void devicePropertyAdded(LocatedDevice device, String propertyName);

    /**
     * Called callback when a device property has been removed.
     * @param device The device modified
     * @param propertyName The name of the property removed.
     */
    public void devicePropertyRemoved(LocatedDevice device, String propertyName);

    /**
     * Called callback when a device has been attached to another device
     * @param container The container device
     * @param child The child device
     */
    void deviceAttached(LocatedDevice container, LocatedDevice child);

    /**
     * Called callback when a device has been detached from another device
     * @param container The container device
     * @param child The child device
     */
    void deviceDetached(LocatedDevice container, LocatedDevice child);

    /**
     * Callback notifying when the device want to trigger an event.
     * @param device the device triggering the event.
     * @param data the content of the event.
     */
    public void deviceEvent(LocatedDevice device, Object data);

}
