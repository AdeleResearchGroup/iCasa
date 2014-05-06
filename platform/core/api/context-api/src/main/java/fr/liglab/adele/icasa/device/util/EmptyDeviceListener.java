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

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Empty implementation of a device listener. This is class is intended to ease implementation of Device Listeners.
 *
 */
public class EmptyDeviceListener<T extends GenericDevice> implements DeviceListener<T> {

	@Override
	public void deviceAdded(GenericDevice device) {
		// do nothing
	}

	@Override
	public void deviceRemoved(GenericDevice device) {
		// do nothing
	}

	@Override
	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {
		// do nothing
	}

	@Override
	public void devicePropertyAdded(GenericDevice device, String propertyName) {
		// do nothing
	}

	@Override
	public void devicePropertyRemoved(GenericDevice device, String propertyName) {
		// do nothing
	}

    /**
     * Callback notifying when the device want to trigger an event.
     *
     * @param device the device triggering the event.
     * @param data   the content of the event.
     */
    @Override
    public void deviceEvent(GenericDevice device, Object data) {
        // do nothing
    }
}
