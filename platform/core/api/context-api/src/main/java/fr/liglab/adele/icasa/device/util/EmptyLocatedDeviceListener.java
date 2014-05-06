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

import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.LocatedDeviceListener;
import fr.liglab.adele.icasa.location.Position;

/**
 * Empty implementation of a located device listener.
 *
 */
public class EmptyLocatedDeviceListener implements LocatedDeviceListener {


    @Override
    public void deviceAdded(LocatedDevice device) {
        //do nothing
    }

    @Override
    public void deviceRemoved(LocatedDevice device) {
        //do nothing
    }

    @Override
    public void deviceMoved(LocatedDevice device, Position oldPosition, Position newPosition) {
        //do nothing
    }

    @Override
    public void devicePropertyModified(LocatedDevice device, String propertyName, Object oldValue, Object newValue) {
        //do nothing
    }

    @Override
    public void devicePropertyAdded(LocatedDevice device, String propertyName) {
        //do nothing
    }

    @Override
    public void devicePropertyRemoved(LocatedDevice device, String propertyName) {
        //do nothing
    }

    @Override
    public void deviceAttached(LocatedDevice container, LocatedDevice child) {
        //do nothing
    }

    @Override
    public void deviceDetached(LocatedDevice container, LocatedDevice child) {
        //do nothing
    }

    /**
     * Callback notifying when the device want to trigger an event.
     *
     * @param device the device triggering the event.
     * @param data   the content of the event.
     */
    @Override
    public void deviceEvent(LocatedDevice device, Object data) {
        //do nothing
    }
}
