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
///**
// *
// *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
// *   Group Licensed under a specific end user license agreement;
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
package fr.liglab.adele.icasa.simulator.device.temperature.impl;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;

import fr.liglab.adele.icasa.device.GenericDevice;

import fr.liglab.adele.icasa.device.temperature.ThermometerExt;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.model.api.WeatherModel;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;



/**
 * Implementation of a simulated thermometer device tracking the weather temperature.
 *
 */
@ContextEntity(coreServices = {ThermometerExt.class, SimulatedDevice.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
public class SimulatedThermometerExtImpl implements ThermometerExt, SimulatedDevice, GenericDevice {

    public final static String SIMULATED_THERMOMETEREXT = "iCasa.ThermometerExt";


    @ContextEntity.State.Field(service = SimulatedDevice.class, state = SIMULATED_DEVICE_TYPE, value = SIMULATED_THERMOMETEREXT)
    private String deviceType;

    @ContextEntity.State.Field(service = GenericDevice.class, state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @ContextEntity.State.Field(service=ThermometerExt.class, state=ThermometerExt.THERMOMETER_CURRENT_TEMPERATURE)
    private Quantity<Temperature> currentSensedTemperature;

    @ContextEntity.State.Push(service = ThermometerExt.class,state = ThermometerExt.THERMOMETER_CURRENT_TEMPERATURE)
    public Quantity<Temperature> push(Quantity<Temperature> quantity) {
    	return quantity;
    }

    @Override
    public Quantity<Temperature> getTemperature() {
        return currentSensedTemperature;
    }

    @Requires(id="Model", optional=false, proxy=false)
    private WeatherModel model;

    @Bind(id="Model")
    public void modelBound() {
        push(model.getTemperature());
    }

    @Modified(id="Model")
    public void modelUpdated() {
        push(model.getTemperature());
    }

}
