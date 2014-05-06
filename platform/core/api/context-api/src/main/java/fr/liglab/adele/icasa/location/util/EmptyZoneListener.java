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

import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.ZoneListener;

/**
 *
 */
public class EmptyZoneListener implements ZoneListener{
    /**
     * Called callback when a new zone has been added. This method will not be called for added zones previous the zone
     * listener registration.
     *
     * @param zone The new zone.
     */
    @Override
    public void zoneAdded(Zone zone) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called callback when a zone is removed.
     *
     * @param zone
     */
    @Override
    public void zoneRemoved(Zone zone) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called callback when the zone has move.
     *
     * @param zone        The zone that has move.
     * @param oldPosition The old top-left relative position.
     * @param newPosition The new top-left relative position.
     */
    @Override
    public void zoneMoved(Zone zone, Position oldPosition, Position newPosition) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called callback when a zone has been resized.
     *
     * @param zone The resized zone.
     */
    @Override
    public void zoneResized(Zone zone) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called callback when adding a parent to an existent zone.
     *
     * @param zone          The zone with new parent.
     * @param oldParentZone The old parent of the zone. null for the first time.
     * @param newParentZone The new parent zone.
     */
    @Override
    public void zoneParentModified(Zone zone, Zone oldParentZone, Zone newParentZone) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Invoked when a device has been attached a zone
     *
     * @param container
     * @param child
     */
    @Override
    public void deviceAttached(Zone container, LocatedDevice child) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * * Invoked when a device has been detached from a zone
     *
     * @param container
     * @param child
     */
    @Override
    public void deviceDetached(Zone container, LocatedDevice child) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called callback when a variable is added in a zone.
     *
     * @param zone         the zone where the variable was added.
     * @param variableName the name of variable added.
     */
    @Override
    public void zoneVariableAdded(Zone zone, String variableName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called callback when a variable is removed from a zone.
     *
     * @param zone         The zone where the variable was removed.
     * @param variableName The name of variable removed.
     */
    @Override
    public void zoneVariableRemoved(Zone zone, String variableName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called callback when a variable has been modified.
     *
     * @param zone         The zone where the variable was modified.
     * @param variableName The name of variable modified.
     * @param oldValue     The previous value of the variable.
     */
    @Override
    public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
