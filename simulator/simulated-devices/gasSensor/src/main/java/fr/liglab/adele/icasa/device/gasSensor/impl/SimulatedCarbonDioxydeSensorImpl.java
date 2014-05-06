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
package fr.liglab.adele.icasa.device.gasSensor.impl;

import fr.liglab.adele.icasa.device.gasSensor.CarbonDioxydeSensor;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.ZoneListener;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.listener.util.BaseZoneListener;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.framework.Constants;

import java.util.List;

/**
 * Implementation of a CO2 sensor device.
 *
 */
@Component(name = "iCasa.CO2GasSensor")
@Provides(properties = {@StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION)})
public class SimulatedCarbonDioxydeSensorImpl extends AbstractDevice implements CarbonDioxydeSensor, SimulatedDevice {

    @ServiceProperty(name = CarbonDioxydeSensor.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String m_serialNumber;

    private volatile Zone m_zone;

    private ZoneListener listener = new GasSensorZoneListener();

    public SimulatedCarbonDioxydeSensorImpl() {
        super();
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(CarbonDioxydeSensor.CARBON_DIOXYDE_SENSOR_CURRENT_CONCENTRATION, 0.0d);
    }

    @Override
    public String getSerialNumber() {
        return m_serialNumber;
    }

    @Override
    public double getCO2Concentration() {
        return (Double) getPropertyValue(CarbonDioxydeSensor.CARBON_DIOXYDE_SENSOR_CURRENT_CONCENTRATION);
    }

    @Override
    public void enterInZones(List<Zone> zones) {
        if (!zones.isEmpty()) {
            for (Zone zone : zones) {
                if (zone.getVariableValue("CO2Concentration") != null) {
                    m_zone = zone;
                    getCO2ConcentrationFromZone();
                    m_zone.addListener(listener);
                    break;
                }
            }
        }
    }

    @Override
    public void leavingZones(List<Zone> zones) {
        setPropertyValue(CarbonDioxydeSensor.CARBON_DIOXYDE_SENSOR_CURRENT_CONCENTRATION, 0.0d);
        if (m_zone != null)
            m_zone.removeListener(listener);
    }

    private void getCO2ConcentrationFromZone() {
        if (m_zone != null) {
            Double currentCO2Concentration = ((Double) m_zone.getVariableValue("CO2Concentration"));
            if (currentCO2Concentration != null)
                setPropertyValue(CarbonDioxydeSensor.CARBON_DIOXYDE_SENSOR_CURRENT_CONCENTRATION, currentCO2Concentration);
        }
    }

    class GasSensorZoneListener extends BaseZoneListener {

        @Override
        public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue) {

            if (m_zone == zone) {
                if (!(getFault().equalsIgnoreCase("yes")))
                    if (variableName.equals("CO2Concentration"))
                        getCO2ConcentrationFromZone();
            }
        }
    }
}
