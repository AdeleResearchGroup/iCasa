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


import java.util.function.Supplier;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import fr.liglab.adele.icasa.device.GenericDevice;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;

import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;

import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.simulator.model.api.TemperatureModel;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;
import fr.liglab.adele.icasa.device.battery.BatteryObservable;




/**
 * Implementation of a simulated thermometer device.
 *
 */
@ContextEntity(coreServices = {Thermometer.class, SimulatedDevice.class, GenericDevice.class, BatteryObservable.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)

public class SimulatedThermometerImpl   implements Thermometer, SimulatedDevice, GenericDevice, BatteryObservable {

    public final static String SIMULATED_THERMOMETER = "iCasa.Thermometer";


    @ContextEntity.State.Field(service = SimulatedDevice.class,state = SIMULATED_DEVICE_TYPE,value = SIMULATED_THERMOMETER)
    private String deviceType;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @ContextEntity.State.Field(service=BatteryObservable.class, state=BatteryObservable.BATTERY_LEVEL, directAccess=true, value="48")
    private double batteryLevel;

    @Override
    public double getBatteryPercentage() {
        return batteryLevel;
    }

    @ContextEntity.State.Field(service=Thermometer.class, state=Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
    private Quantity<Temperature> currentSensedTemperature;

    @ContextEntity.State.Pull(service=Thermometer.class, state=Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
    Supplier<Quantity<Temperature> > pull = this::pull;
    
    private Quantity<Temperature> pull() {
    	return model != null ? model.getTemperature() : UNDEFINED;
    }
    
    @ContextEntity.State.Push(service=Thermometer.class, state=Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
    public Quantity<Temperature> push(Quantity<Temperature> quantity) {
    	return quantity;
    }

    @Override
    public Quantity<Temperature> getTemperature() {
        return currentSensedTemperature;
    }

    @Validate
    public void validate(){
    }


    /**
     * IMPORTANT NOTE : this requirement is marked optional as the device is not always necessarily attached to a zone. 
     * The measured value when the device is outside the zone is undefined.
     * 
     */
    @Requires(id ="model" ,filter="(temperaturemodel.zone.attached=${locatedobject.object.zone})", optional=true, proxy=false, nullable=false)
    TemperatureModel model;

    @Bind(id = "model")
    public void modelBound() {
    	push(model.getTemperature());
    }

    @Modified(id = "model")
    public void modelUpdated() {
    	push(model.getTemperature());
    }

    @Unbind(id = "model")
    public void modelUnbound() {
    	push(UNDEFINED);
    }

    private final static Quantity<Temperature> UNDEFINED = Quantities.getQuantity(Thermometer.FAULT_VALUE,Units.KELVIN);
}
