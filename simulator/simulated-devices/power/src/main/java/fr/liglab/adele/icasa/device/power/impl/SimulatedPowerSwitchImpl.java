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

import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Constants;

import java.util.List;

/**
 *
 */
@Component(name = "iCasa.ToggleSwitch")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedPowerSwitchImpl extends AbstractDevice implements PowerSwitch, SimulatedDevice {

	@ServiceProperty(name = PowerSwitch.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;

	public SimulatedPowerSwitchImpl() {
		super();
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(POWER_SWITCH_CURRENT_STATUS, false);
	}

	@Override
    public boolean getStatus() {
   	 Boolean status = (Boolean) getPropertyValue(POWER_SWITCH_CURRENT_STATUS);
   	 if (status==null)
   		 return false;
        return status;
    }

	@Override
	public boolean switchOn() {
		setPropertyValue(POWER_SWITCH_CURRENT_STATUS, true);
		return getStatus();
	}

	@Override
	public boolean switchOff() {
		setPropertyValue(POWER_SWITCH_CURRENT_STATUS, false);
		return getStatus();
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

}
