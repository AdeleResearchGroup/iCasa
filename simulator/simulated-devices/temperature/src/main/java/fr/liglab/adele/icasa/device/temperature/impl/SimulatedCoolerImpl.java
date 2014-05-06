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

import fr.liglab.adele.icasa.device.PowerObservable;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;

/**
 * Implementation of a simulated cooler device.
 *
 */
@Component(name = "iCasa.Cooler")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedCoolerImpl extends AbstractDevice implements Cooler, SimulatedDevice,PowerObservable {

    @ServiceProperty(name = Cooler.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String m_serialNumber;

    public SimulatedCoolerImpl() {
        super();
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(Cooler.COOLER_POWER_LEVEL, 0.0d);
        super.setPropertyValue(Cooler.COOLER_MAX_POWER_LEVEL, 1000.0d);
        super.setPropertyValue(PowerObservable.POWER_OBSERVABLE_CURRENT_POWER_LEVEL, 0.0d);
    }

    @Override
    public String getSerialNumber() {
        return m_serialNumber;
    }

    @Validate
    public synchronized void start() {
        // do nothing
    }

    @Invalidate
    public synchronized void stop() throws InterruptedException {
        // do nothing
    }

    @Override
    public synchronized double getPowerLevel() {
        Double powerLevel = (Double) getPropertyValue(Cooler.COOLER_POWER_LEVEL);
        if (powerLevel == null)
            return 0.0d;
        return powerLevel;
    }

    @Override
    public synchronized double setPowerLevel(double level) {
        if (level < 0.0d || level > 1.0d || Double.isNaN(level)) {
            throw new IllegalArgumentException("Invalid power level : " + level);
        }
        setPropertyValue(Cooler.COOLER_POWER_LEVEL, level);
        return level;
    }

    @Override
    public void setPropertyValue(String propertyName, Object value) {
        if (propertyName.equals(Cooler.COOLER_POWER_LEVEL)) {

            double previousLevel = getPowerLevel();
            double level = (value instanceof String) ? Double.parseDouble((String)value) : (Double) value;

            if (previousLevel!=level) {
                super.setPropertyValue(Cooler.COOLER_POWER_LEVEL, level);
                getCurrentConsumption();
                //m_logger.debug("Power level set to " + level);
            }
        } else
            super.setPropertyValue(propertyName, value);
    }

    @Override
    public double getMaxPowerLevel() {
        Double maxLevel = (Double) getPropertyValue(Cooler.COOLER_MAX_POWER_LEVEL);
        if (maxLevel==null)
            return 0;
        return maxLevel;
    }


    @Override
    public synchronized double getCurrentConsumption() {
        Double maxLevel = (Double) getPropertyValue(Cooler.COOLER_MAX_POWER_LEVEL);
        Double powerLevel = (Double) getPropertyValue(Cooler.COOLER_POWER_LEVEL);
        setPropertyValue(PowerObservable.POWER_OBSERVABLE_CURRENT_POWER_LEVEL,(double)(powerLevel*maxLevel));
        return powerLevel*maxLevel;
    }

}
