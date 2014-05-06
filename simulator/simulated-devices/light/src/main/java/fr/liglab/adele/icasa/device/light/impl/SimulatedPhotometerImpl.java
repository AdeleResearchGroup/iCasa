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
package fr.liglab.adele.icasa.device.light.impl;

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.listener.util.BaseZoneListener;

/**
 * Implementation of a simulated photometer device.
 *
 */
@Component(name = "iCasa.Photometer")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedPhotometerImpl extends AbstractDevice implements Photometer, SimulatedDevice {

	@ServiceProperty(name = Photometer.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;

	private Zone m_zone;

	private PhotometerZoneListener listener = new PhotometerZoneListener();

	public SimulatedPhotometerImpl() {
        super();
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(Photometer.PHOTOMETER_CURRENT_ILLUMINANCE, -1.0d);
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public synchronized double getIlluminance() {
		return (Double) getPropertyValue(Photometer.PHOTOMETER_CURRENT_ILLUMINANCE);
	}


	@Override
	public void enterInZones(List<Zone> zones) {
		if (!zones.isEmpty()) {
			for (Zone zone : zones) {
				if (zone.getVariableValue("Illuminance") != null) {
					m_zone = zone;
					getIlluminanceFromZone();
					m_zone.addListener(listener);
					break;
				}
			}
		}
	}

	@Override
	public void leavingZones(List<Zone> zones) {
		setPropertyValue(Photometer.PHOTOMETER_CURRENT_ILLUMINANCE, -1.0d);
		if (m_zone != null)
			m_zone.removeListener(listener);
	}

	private void getIlluminanceFromZone() {
		if (m_zone != null) {
			Double currentIlluminance = ((Double) m_zone.getVariableValue("Illuminance"));
			if (currentIlluminance != null) {
				setPropertyValue(Photometer.PHOTOMETER_CURRENT_ILLUMINANCE, currentIlluminance);
			}
		}
	}

	class PhotometerZoneListener extends BaseZoneListener {

		@Override
		public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue) {
			if (m_zone == zone)
				if (!(getFault().equalsIgnoreCase("yes")))
					if (variableName.equals("Illuminance"))
						getIlluminanceFromZone();
		}
	}

}
