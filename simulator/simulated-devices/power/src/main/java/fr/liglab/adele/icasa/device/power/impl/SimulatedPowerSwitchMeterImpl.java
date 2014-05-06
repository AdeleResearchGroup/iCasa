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
package fr.liglab.adele.icasa.device.power.impl;

import java.util.List;

import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.device.power.PowerSwitchmeter;
import fr.liglab.adele.icasa.device.power.Powermeter;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;

/**
 * Implementation of a simulated Power Switch + Meter
 *
 * 
 */

/*
 * @Component(name="iCasa.PowerSwitchMeter")
 * 
 * @Provides(properties = {
 * 
 * @StaticServiceProperty(type = "java.lang.String", name =
 * Constants.SERVICE_DESCRIPTION) })
 */
public class SimulatedPowerSwitchMeterImpl extends AbstractDevice implements PowerSwitchmeter, SimulatedDevice {

	@ServiceProperty(name = AbstractDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;

	@ServiceProperty(name = PowerSwitch.POWER_SWITCH_CURRENT_STATUS, value = "false")
	private boolean m_currentStatus;

	@ServiceProperty(name = Powermeter.POWERMETER_CURRENT_RATING, value = "NaN")
	private double m_currentRating;

	@Property(name = "power.attachedDevice.name", mandatory = true)
	private String m_attachedDeviceName;

	@Property(name = "power.attachedDevice.watt", mandatory = true)
	private double m_attachedDeviceWatt;

	// private volatile SimulatedEnvironment m_env;

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public synchronized boolean getStatus() {
		return m_currentStatus;
	}

	@Override
	public synchronized boolean switchOn() {
		if (m_currentStatus) {
			return false;
		} else {
			m_currentRating = m_attachedDeviceWatt;
			m_currentStatus = true;
			return true;
		}
	}

	@Override
	public synchronized boolean switchOff() {
		if (!m_currentStatus) {
			return false;
		} else {
			m_currentRating = 0.0d;
			m_currentStatus = false;
			return true;
		}
	}

	@Override
	public double getCurrentPowerRating() {
		return m_currentRating;
	}

	@Override
	public void enterInZones(List<Zone> zones) {
		// TODO Auto-generated method stub

	}

	@Override
	public void leavingZones(List<Zone> zones) {
		// TODO Auto-generated method stub

	}
}
