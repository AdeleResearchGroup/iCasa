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

import fr.liglab.adele.icasa.listener.IcasaListener;

/**
 * A listener to the events related with device modifications.
 * 
 * Events may be notified more than once per event. The only guaranty is that at least one event will be sent to
 * listeners.
 *
 * 
 */
public interface DeviceListener<T extends GenericDevice> extends IcasaListener {

	/**
	 * Callback notifying the addition of a device to the platform.
	 * 
	 * @param device The device added. 
	 */
	public void deviceAdded(T device);

	/**
	 * Callback notifying the elimination of a device to the platform.
	 * 
	 * @param device The device removed. 
	 */
	public void deviceRemoved(T device);

	/**
	 * Callback notifying the modification of a property on the device listened.
	 * 
	 * @param device The device
	 * @param propertyName The name of the modified property
	 * @param oldValue The previous value of the property
	 * @param newValue The new value of the property
	 */
	public void devicePropertyModified(T device, String propertyName, Object oldValue, Object newValue);

	/**
	 * Callback notifying the addition of a property on the device listened.
	 *  
	 * @param device The device
	 * @param propertyName The name of the added property
	 */
	public void devicePropertyAdded(T device, String propertyName);

	/**
	 * Callback notifying the elimination of a property on the device listened.
	 *  
	 * @param device The device
	 * @param propertyName The name of the removed property
	 */
	public void devicePropertyRemoved(T device, String propertyName);

    /**
     * Callback notifying when the device want to trigger an event.
     * @param device the device triggering the event.
     * @param data the content of the event.
     */
    public void deviceEvent(T device, Object data);

	
}
