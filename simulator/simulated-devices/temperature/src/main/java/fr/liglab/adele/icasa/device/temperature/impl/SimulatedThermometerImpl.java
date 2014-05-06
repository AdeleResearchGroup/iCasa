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
package fr.liglab.adele.icasa.device.temperature.impl;

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.ZoneListener;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.listener.util.BaseZoneListener;

/**
 * Implementation of a simulated thermometer device.
 *
 */
@Component(name = "iCasa.Thermometer")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedThermometerImpl extends AbstractDevice implements Thermometer, SimulatedDevice {

	@ServiceProperty(name = Thermometer.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;

	private volatile Zone m_zone;

	private ZoneListener listener = new ThermometerZoneListener();

	public SimulatedThermometerImpl() {
        super();
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(Thermometer.THERMOMETER_CURRENT_TEMPERATURE, 0.0d);
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public synchronized double getTemperature() {
		return (Double) getPropertyValue(Thermometer.THERMOMETER_CURRENT_TEMPERATURE);
	}

	@Validate
	public synchronized void start() {

	}

	@Invalidate
	public synchronized void stop() throws InterruptedException {

	}


	@Override
	public void enterInZones(List<Zone> zones) {
        boolean atLeaastOne = false;
		if (!zones.isEmpty()) {
			for (Zone zone : zones) {
				if (zone.getVariableValue("Temperature") != null) {
					subscribeZone(zone);
                    atLeaastOne = true;
					break;
				}
			}
            if (!atLeaastOne){ // if any zone has the Temperature variable.
                Zone fZone = zones.get(0); // get the first Zone
                subscribeZone(fZone);
            }
		}
	}

    private void subscribeZone(Zone zone){
        m_zone = zone;
        getTemperatureFromZone();
        m_zone.addListener(listener);
    }

	@Override
	public void leavingZones(List<Zone> zones) {
		setPropertyValue(Thermometer.THERMOMETER_CURRENT_TEMPERATURE, -1.0d);
		if (m_zone != null)
			m_zone.removeListener(listener);
	}

	private void getTemperatureFromZone() {
		if (m_zone != null) {
			Double currentTemperature = ((Double) m_zone.getVariableValue("Temperature"));
			if (currentTemperature != null){
				setPropertyValue(Thermometer.THERMOMETER_CURRENT_TEMPERATURE, currentTemperature);
            } else {
                setPropertyValue(Thermometer.THERMOMETER_CURRENT_TEMPERATURE, -1.0d);
            }
		}
	}

	class ThermometerZoneListener extends BaseZoneListener {

		@Override
		public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue) {

			if (m_zone == zone) {
				if (!(getFault().equalsIgnoreCase("yes")))
					if (variableName.equals("Temperature"))
						getTemperatureFromZone();
			}
		}
	}

}
