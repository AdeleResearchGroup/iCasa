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

import fr.liglab.adele.icasa.device.GenericDevice;

import java.util.List;
import java.util.Set;

/**
 * 
 * This device interface is used to add properties not specified in the device implementation. The location is also
 * added by this interface. Implementations have to maintain a reference to the Device implementation.
 *
 * 
 */
public interface LocatedDevice extends LocatedObject {

	/**
	 * Gets the device serial number.
	 * 
	 * @return device serial number
	 */
	public String getSerialNumber();

	/**
	 * Gets the device type.
	 * 
	 * @return device type.
	 */
	public String getType();

	/**
	 * Gets the set of properties names.
	 * 
	 * @return set of properties names.
	 */
	public Set<String> getProperties();

	/**
	 * Get the value of a property.
	 * 
	 * @param propertyName Property name
	 * @return Property value
	 */
	public Object getPropertyValue(String propertyName);

	/**
	 * Sets a property value
	 * 
	 * @param propertyName the name of property
	 * @param value the new value
	 */
	public void setPropertyValue(String propertyName, Object value);

	/**
	 * Determines if the device contains a property definition with this name
	 * 
	 * @param propertyName the property name
	 * @return true if contains the property, false otherwise.
	 */
	public boolean constainsProperty(String propertyName);

	/**
	 * Determines if a property has been set.
	 * 
	 * @param propertyName the property name
	 * @return true if property has a value. false if property has not been set (value null) or if property does not
	 *         exist.
	 */
	public boolean hasPropertyValue(String propertyName);

	/**
	 * Removes a property on this device.
	 * 
	 * @param propertyName the property name
	 * @return true if property was removed. false if property does not exist.
	 */
	public boolean removeProperty(String propertyName);

	/**
	 * Adds a listener to the Located device.
	 * 
	 * @param listener the listener to be added.
	 */
	public void addListener(final LocatedDeviceListener listener);

	/**
	 * Removes a listener from the Located device.
	 * 
	 * @param listener the listener to be removed.
	 */
	public void removeListener(final LocatedDeviceListener listener);

	/**
	 * Callback notifying this device enters in one or more zones
	 * 
	 * @param zones The list of zones where the device has entered
	 */
	public void enterInZones(List<Zone> zones);

	/**
	 * Callback notifying this device leaves one or more zones
	 * 
	 * @param zones The list of zones where that device has leaved
	 */
	public void leavingZones(List<Zone> zones);

	/**
	 * Gets a reference to the device implementation.
	 * 
	 * @return Reference to the device implementation.
	 */
	public GenericDevice getDeviceObject();

}
