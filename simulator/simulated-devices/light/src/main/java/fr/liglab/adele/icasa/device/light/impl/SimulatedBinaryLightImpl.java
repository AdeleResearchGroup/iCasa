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

import fr.liglab.adele.icasa.device.PowerObservable;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;

/**
 * Implementation of a simulated binary light device.
 *
 */
@Component(name = "iCasa.BinaryLight")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedBinaryLightImpl extends AbstractDevice implements BinaryLight, SimulatedDevice,PowerObservable {

    @ServiceProperty(name = BinaryLight.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String m_serialNumber;

    public SimulatedBinaryLightImpl() {
        super();
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(BinaryLight.BINARY_LIGHT_POWER_STATUS, false);
        super.setPropertyValue(BinaryLight.BINARY_LIGHT_MAX_POWER_LEVEL, 100.0d);
        super.setPropertyValue(PowerObservable.POWER_OBSERVABLE_CURRENT_POWER_LEVEL, 0.0d);
    }

    @Override
    public String getSerialNumber() {
        return m_serialNumber;
    }

    @Override
    public boolean getPowerStatus() {
        Boolean powerStatus = (Boolean) getPropertyValue(BinaryLight.BINARY_LIGHT_POWER_STATUS);
        if (powerStatus == null)
            return false;

        return powerStatus;
    }

    @Override
    public synchronized boolean setPowerStatus(boolean status) {
        setPropertyValue(BinaryLight.BINARY_LIGHT_POWER_STATUS, (Boolean) status);
        getCurrentConsumption();
        return status;
    }

    @Override
    public double getMaxPowerLevel() {
        Double maxLevel = (Double) getPropertyValue(BinaryLight.BINARY_LIGHT_MAX_POWER_LEVEL);
        if (maxLevel == null)
            return 0;

        return maxLevel;
    }

    @Override
    public void turnOn() {
        setPowerStatus(true);
    }

    @Override
    public void turnOff() {
        setPowerStatus(false);
    }

    @Override
    public synchronized double getCurrentConsumption() {
        Double maxLevel = (Double) getPropertyValue(BinaryLight.BINARY_LIGHT_MAX_POWER_LEVEL);
        boolean status = (Boolean) getPropertyValue(BinaryLight.BINARY_LIGHT_POWER_STATUS);
        if (status){
            setPropertyValue(PowerObservable.POWER_OBSERVABLE_CURRENT_POWER_LEVEL,maxLevel);
            return maxLevel;
        }else{
            setPropertyValue(PowerObservable.POWER_OBSERVABLE_CURRENT_POWER_LEVEL,0.0d);
            return 0.0d;
        }
    }

}
