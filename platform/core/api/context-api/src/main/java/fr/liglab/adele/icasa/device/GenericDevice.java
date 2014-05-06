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

import fr.liglab.adele.icasa.location.Zone;

import java.util.List;
import java.util.Set;

/**
 * Generic interface that is intended to be used as a skeleton for device service interface definitions.
 * 
 * @see AbstractDevice
 */
public interface GenericDevice {

	/**
	 * Service property indicating the hardware serial number of the device.
	 * 
	 * <ul>
	 * <li>This property is <b>mandatory</b></li>
	 * <li>Type of values : <b><code>java.lang.String</code></b></li>
	 * <li>Description : the hardware serial number of the device. Must be unique and immutable.</li>
	 * </ul>
	 * 
	 * @see #getSerialNumber()
	 */
	String DEVICE_SERIAL_NUMBER = "device.serialNumber";

	public static String STATE_PROPERTY_NAME = "state";
	public static String STATE_ACTIVATED = "activated";
	public static String STATE_DEACTIVATED = "deactivated";
	public static String STATE_UNKNOWN = "unknown";

	public static String FAULT_PROPERTY_NAME = "fault";
	public static String FAULT_YES = "yes";
	public static String FAULT_NO = "no";
	public static String FAULT_UNKNOWN = "unknown";

	public static String LOCATION_PROPERTY_NAME = "Location";
	public static String LOCATION_UNKNOWN = "unknown";

	/**
	 * Default icon width, in px.
	 */
	public static final int DEFAULT_WIDTH = 32;

	/**
	 * Default icon height, in px.
	 */
	public static final int DEFAULT_HEIGHT = 32;

	/**
	 * Return the serial number of the device.
	 * 
	 * @return the serial number of the device.
	 * @see #DEVICE_SERIAL_NUMBER
	 */
	String getSerialNumber();

	/**
	 * Add the specified listener to the list of this device's listeners. If the listener is already is the list, this
	 * method does nothing.
	 * 
	 * @param listener the listener to add
	 */
	void addListener(DeviceListener<?> listener);

	/**
	 * Remove the specified listener from the list of this device's listeners. If the listener wasn't in the list, this
	 * method does nothing.
	 * 
	 * @param listener the listener to remove
	 */
	void removeListener(DeviceListener<?> listener);

	/**
	 * Returns activation state of this device.
	 * 
	 * @return activation state of this device.
	 */
	public String getState();

	/**
	 * Sets activation state of this device.
	 * 
	 * @param state activation state to set
	 */
	public void setState(String state);

	/**
	 * Returns fault state of this device.
	 * 
	 * @return fault state of this device.
	 */
	public String getFault();

	/**
	 * Sets fault state of this device.
	 * 
	 * @param fault
	 */
	public void setFault(String fault);

	/**
	 * Returns names of all properties which define the device state.
	 * 
	 * @return names of all properties which define the device state.
	 */
	public Set<String> getProperties();

	/**
	 * Returns the specified property value. Returns null if there is no value or the property does not exist.
	 * 
	 * @param propertyName
	 * @return the specified property value.
	 */
	public Object getPropertyValue(String propertyName);

	/**
	 * Sets specified property value.
	 * 
	 * @param propertyName
	 * @param value
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
	 * @param propertyName the property name
	 * @return true if property was removed. false if property does not exist.
	 */
	public boolean removeProperty(String propertyName);

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

}
