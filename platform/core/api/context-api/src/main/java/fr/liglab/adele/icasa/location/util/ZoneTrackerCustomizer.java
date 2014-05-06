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
package fr.liglab.adele.icasa.location.util;

import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;

/**
 *
 */
public interface ZoneTrackerCustomizer {
    /**
     * A zone is being added to the Tracker object.
     * This method is called before a zone which matched the search parameters of the Tracker object is added to it.
     * This method must return true to be tracked for this zone object.
     * @param zone the zone being added to the Tracker object.
     * @return true if the zone will be tracked, false if not.
     */
    boolean addingZone(Zone zone);

    /**
     * A device tracked by the Tracker object has been added in the list.
     * This method is called when a device has been added in the managed list (after addingDevice) and if the device has not disappeared before during the callback.
     * @param zone the added device
     */
    void addedZone(Zone zone);

    /**
     * Called when a zone tracked by the Tracker object has been modified.
     * A tracked zone is considered modified according to tracker configuration.
     *
     * @param zone the changed zone
     * @param variableName name of the property that has changed
     * @param oldValue previous value of the property
     * @param newValue new value of the property
     */
    void modifiedZone(Zone zone, String variableName, Object oldValue, Object newValue);

    /**
     *
     * @param zone
     * @param oldPosition
     * @param newPosition
     */
    void movedZone(Zone zone, Position oldPosition, Position newPosition);

    /**
     * Called when a zone tracked by the Tracker object has been resized.
     *
     * @param zone the resized zone
     */
    void resizedZone(Zone zone);

    /**
     * A zone tracked by the Tracker object has been removed.
     * This method is called after a zone is no longer being tracked by the Tracker object.
     * @param zone the removed zone.
     */
    void removedZone(Zone zone);
}
