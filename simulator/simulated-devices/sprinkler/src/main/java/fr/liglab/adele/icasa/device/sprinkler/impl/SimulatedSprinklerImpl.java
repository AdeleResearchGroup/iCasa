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
package fr.liglab.adele.icasa.device.sprinkler.impl;

import fr.liglab.adele.icasa.device.sprinkler.Sprinkler;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;

/**
 * Implementation of a simulated binary light device.
 *
 */
@Component(name = "iCasa.Sprinkler")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedSprinklerImpl extends AbstractDevice implements Sprinkler, SimulatedDevice {

	@ServiceProperty(name = Sprinkler.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;

	public SimulatedSprinklerImpl() {
		super();
		super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
		super.setPropertyValue(Sprinkler.SPRINKLER_POWER_STATUS, false);
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public synchronized boolean getPowerStatus() {
		Boolean powerStatus = (Boolean) getPropertyValue(Sprinkler.SPRINKLER_POWER_STATUS);
		if (powerStatus == null)
			return false;

		return powerStatus;
	}

	@Override
	public synchronized boolean setPowerStatus(boolean status) {
		setPropertyValue(Sprinkler.SPRINKLER_POWER_STATUS, (Boolean) status);
		return status;
	}

	@Override
	public void turnOn() {
		setPowerStatus(true);
	}

	@Override
	public void turnOff() {
		setPowerStatus(false);
	}

}
