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
package fr.liglab.adele.icasa.device.presence.impl;

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulator.Person;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import fr.liglab.adele.icasa.simulator.listener.PersonListener;

/**
 * Implementation of a simulated presence sensor device.
 *
 */
@Component(name = "iCasa.PresenceSensor")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedPresenceSensorImpl extends AbstractDevice implements PresenceSensor, SimulatedDevice,
      PersonListener {

	@ServiceProperty(name = PresenceSensor.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;

	@Requires
	private SimulationManager manager;

	/**
	 * Influence zone corresponding to the zone with highest level where the
	 * device is located
	 */
	private volatile Zone m_zone;

	public SimulatedPresenceSensorImpl() {
		super();
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE, false);
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public synchronized boolean getSensedPresence() {
		Boolean presence = (Boolean) getPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE);
		if (presence != null)
			return presence;
		return false;
	}

	@Override
	public void enterInZones(List<Zone> zones) {
		if (!zones.isEmpty()) {
			m_zone = zones.get(0);
			// setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME,
			// m_zone.getId());
			updateState();
		}
	}

	@Override
	public void leavingZones(List<Zone> zones) {
		m_zone = null;
		// setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME,
		// SimulatedDevice.LOCATION_UNKNOWN);
	}

	@Override
	public void personAdded(Person person) {
		updateState();
	}

	@Override
	public void personRemoved(Person person) {
		updateState();
	}

	@Override
	public void personMoved(Person person, Position oldPosition) {
		updateState();
	}

	@Override
	public void personDeviceAttached(Person person, LocatedDevice device) {
		// Nothing to do
	}

	@Override
	public void personDeviceDetached(Person person, LocatedDevice device) {
		// Nothing to do
	}

	/**
	 * Calculates if a person is found in the detection zone of this device. When
	 * there is a change of previous detection a event is sent to listeners
	 */
	private void updateState() {
		if (m_zone != null) {

			boolean personFound = personInZone();
			boolean previousDetection = (Boolean) getPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE);

			if (!previousDetection) { // New person in Zone
				if (personFound) {
					setPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE, true);
				}
			} else {
				if (!personFound) { // The person has leave the detection zone
					setPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE, false);
				}
			}
		}
	}

	private boolean personInZone() {
		for (Person person : manager.getPersons()) {
			if (m_zone.contains(person))
				return true;
		}
		return false;
	}

	@Validate
	protected void start() {
		manager.addListener(this);
	}

	@Invalidate
	protected void stop() {
		manager.removeListener(this);
	}
}
