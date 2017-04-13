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
package fr.liglab.adele.icasa.simulator.device.gasSensor.impl;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.gasSensor.CarbonDioxydeSensor;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;

/**
 * Implementation of a simulated carbon dioxyde Sensor.
 *
 */
@ContextEntity(services = {CarbonDioxydeSensor.class,SimulatedDevice.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
public class SimulatedCarbonDioxydeSensorImpl implements CarbonDioxydeSensor,SimulatedDevice,GenericDevice{

    public final static String SIMULATED_CARBON_DIOXYDE_SENSOR = "iCasa.CO2GasSensor";

    @State.Field(service = CarbonDioxydeSensor.class,state = CarbonDioxydeSensor.CARBON_DIOXYDE_SENSOR_CURRENT_CONCENTRATION,value ="4.0" )
    private double currentConcentration;

    @State.Field(service = SimulatedDevice.class,state = SIMULATED_DEVICE_TYPE,value = SIMULATED_CARBON_DIOXYDE_SENSOR)
    private String deviceType;

    @State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public double getCO2Concentration() {
        return currentConcentration;
    }
}