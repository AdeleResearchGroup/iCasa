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
package fr.liglab.adele.icasa.location;

import fr.liglab.adele.icasa.listener.IcasaListener;

/**
 * Listener on {@link fr.liglab.adele.icasa.location.LocatedDevice} objects
 * 
 * @author Gabriel Pedraza Ferreira
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
     * @param device The device moved.
     * @param oldPosition The previous position.
     */
    public void deviceMoved(LocatedDevice device, Position oldPosition);

    /**
     * Called callback when a device property has been modified.
     * @param device The device modified.
     * @param propertyName The property modified.
     * @param oldValue The property previous value.
     */
    public void devicePropertyModified(LocatedDevice device, String propertyName, Object oldValue);

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

}
