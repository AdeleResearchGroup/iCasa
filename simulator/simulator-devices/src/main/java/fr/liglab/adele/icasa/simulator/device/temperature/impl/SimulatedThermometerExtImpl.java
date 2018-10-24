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
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.battery.BatteryObservable;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.model.api.TemperatureModel;
import org.apache.felix.ipojo.annotations.*;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;


/**
 * Implementation of a simulated thermometer device.
 *
 */
@ContextEntity(coreServices = {ThermometerExt.class, SimulatedDevice.class,BatteryObservable.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
public class SimulatedThermometerExtImpl implements ThermometerExt, SimulatedDevice,GenericDevice,BatteryObservable {

    public final static String SIMULATED_THERMOMETEREXT = "iCasa.ThermometerExt";

    @ContextEntity.State.Field(service = ThermometerExt.class,state=ThermometerExt.THERMOMETER_CURRENT_TEMPERATURE)
    private Quantity<Temperature> currentSensedTemperature;

    @ContextEntity.State.Field(service = SimulatedDevice.class,state = SIMULATED_DEVICE_TYPE,value = SIMULATED_THERMOMETEREXT)
    private String deviceType;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @ContextEntity.State.Field(service = BatteryObservable.class,state = BatteryObservable.BATTERY_LEVEL,directAccess = true,value = "48")
    private double batteryLevel;

    @Requires(id="MoD",specification = MomentOfTheDay.class,optional = false)
    private MomentOfTheDay MoD;

    @Validate
    public void validate(){
        currentSensedTemperature=Quantities.getQuantity(291,Units.KELVIN);
    }

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public Quantity<Temperature> getTemperature() {
        return currentSensedTemperature;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Bind(id ="TemperatureModelDependency" ,filter = "(& (locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+") (!(objectClass=fr.liglab.adele.iop.device.api.IOPService)) )",optional = true,aggregate = true)
    public void bindTemperature(TemperatureModel model){
        pushTemperature();
    }



    @Modified(id="MoD")
    public void modMoD(){
        pushTemperature();
    }

    @Modified(id = "TemperatureModelDependency")
    public void modifiedTemperature(TemperatureModel model){
        pushTemperature();
    }

    @Unbind(id = "TemperatureModelDependency")
    public void unbindTemperature(TemperatureModel model){
        pushTemperature();
    }

    @ContextEntity.State.Push(service = ThermometerExt.class,state = ThermometerExt.THERMOMETER_CURRENT_TEMPERATURE)
    public Quantity<Temperature> pushTemperature(){
        double temp =-2;
        if(MoD.getCurrentPartOfTheDay().toString().equals("MORNING")){
            temp=270d;
        }else if(MoD.getCurrentPartOfTheDay().toString().equals("AFTERNOON")){
            temp=291d;
        }else if(MoD.getCurrentPartOfTheDay().toString().equals("EVENING")){
            temp=280d;
        }else if(MoD.getCurrentPartOfTheDay().toString().equals("NIGHT")){
            temp=275d;
        }else{
            temp=291d;
        }
        return Quantities.getQuantity(temp, Units.KELVIN);
    }

    @Override
    public double getBatteryPercentage() {
        return batteryLevel;
    }
}
