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

/**
 * A listener of zones
 *
 * 
 */
public interface ZoneListener extends ZonePropListener {

	/**
	 * Called callback when a new zone has been added. This method will not be called for added zones previous the zone
	 * listener registration.
	 * 
	 * @param zone The new zone.
	 */
	public void zoneAdded(Zone zone);

	/**
	 * Called callback when a zone is removed.
	 * 
	 * @param zone
	 */
	public void zoneRemoved(Zone zone);

	/**
	 * Called callback when the zone has move.
	 * 
	 * @param zone The zone that has move.
	 * @param oldPosition The old top-left relative position.
	 * @param newPosition The new top-left relative position.
	 */
	public void zoneMoved(Zone zone, Position oldPosition, Position newPosition);

	/**
	 * Called callback when a zone has been resized.
	 * 
	 * @param zone The resized zone.
	 */
	public void zoneResized(Zone zone);

	/**
	 * Called callback when adding a parent to an existent zone.
	 * 
	 * @param zone The zone with new parent.
	 * @param oldParentZone The old parent of the zone. null for the first time.
	 * @param newParentZone The new parent zone.
	 */
	public void zoneParentModified(Zone zone, Zone oldParentZone, Zone newParentZone);

	/**
	 * Invoked when a device has been attached a zone
	 * 
	 * @param container
	 * @param child
	 */
	void deviceAttached(Zone container, LocatedDevice child);

	/**
	 ** Invoked when a device has been detached from a zone
	 * 
	 * @param container
	 * @param child
	 */
	void deviceDetached(Zone container, LocatedDevice child);

}
