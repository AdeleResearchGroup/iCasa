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
package fr.liglab.adele.icasa.device.motion.impl;

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.DeviceDataEvent;
import fr.liglab.adele.icasa.device.DeviceEventType;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
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
@Component(name = "iCasa.MotionSensor")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedMotionSensorImpl extends AbstractDevice implements MotionSensor, SimulatedDevice,
      PersonListener {

	@ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;

	@Requires
	private SimulationManager manager;

	/**
	 * Influence zone corresponding to the zone with highest level where the
	 * device is located
	 */
	private volatile Zone m_zone;

	public SimulatedMotionSensorImpl() {
		super();
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public void enterInZones(List<Zone> zones) {
		if (!zones.isEmpty()) {
			m_zone = zones.get(0);
		}
	}

	@Override
	public void leavingZones(List<Zone> zones) {
		m_zone = null;
	}

	@Override
	public void personAdded(Person person) {
		updateState(person, null);
	}

	@Override
	public void personRemoved(Person person) {
		updateState(person, null);
	}

	@Override
	public void personMoved(Person person, Position oldPosition) {
		updateState(person, oldPosition);
	}


    /**
     * Calculates if a person is found in the detection zone of this device. When there is a change of previous
     * detection a event is sent to listeners
     */
    private void updateState(Person person, Position oldPosition) {
        if (m_zone != null) {
            if (m_zone.contains(person)) {
                // when a person arrives or moves into the  zone.
                this.notifyListeners(new DeviceDataEvent<Boolean>(this, DeviceEventType.DEVICE_EVENT, Boolean.TRUE));
            } else if (oldPosition != null) {
                // when a person leaves the device influence zone
                if (m_zone.contains(oldPosition)) {
                    this.notifyListeners(new DeviceDataEvent<Boolean>(this, DeviceEventType.DEVICE_EVENT, Boolean.TRUE));
                }
            }
        }
    }

	@Validate
	protected void start() {
		manager.addListener(this);
	}

	@Invalidate
	protected void stop() {
		manager.removeListener(this);
	}


    @Override
    public void personDeviceAttached(Person person, LocatedDevice device) {
        // Nothing to do
    }

    @Override
    public void personDeviceDetached(Person person, LocatedDevice device) {
        // Nothing to do
    }
}
