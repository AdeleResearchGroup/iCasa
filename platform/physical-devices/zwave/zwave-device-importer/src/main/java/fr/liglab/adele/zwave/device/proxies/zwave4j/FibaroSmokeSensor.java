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
package fr.liglab.adele.zwave.device.proxies.zwave4j;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.InjectedFunctionalExtension;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Thermometer;

import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;

import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;


@ContextEntity(coreServices = {Thermometer.class,Zwave4jDevice.class,})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)

public class FibaroSmokeSensor extends AbstractZwave4jDevice implements  GenericDevice, Zwave4jDevice,Thermometer {

	/**
	 * Injected Behavior
	 */
	@InjectedFunctionalExtension(id="ZwaveBehavior")
	private ZwaveDevice device;


	@Override
	protected void valueChanged(ZWaveCommandClass command, short instance, short index, float value) {
		
		switch (command) {
			
			case SENSOR_MULTILEVEL:
				
				switch (index) {
					case 1:
						temperatureValueChange(value);
						break;
				}
				
				break;

			default:
				break;
		}
	}
		

	@ContextEntity.State.Field(service = Thermometer.class,state = Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
	private Quantity<Temperature> temperature;

	@ContextEntity.State.Push(service = Thermometer.class,state =Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
	public Quantity<Temperature> temperatureValueChange(float newTemperature){
		return Quantities.getQuantity(newTemperature, Units.CELSIUS);
	}

	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
	private String serialNumber;

	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public Quantity<Temperature> getTemperature() {
		return temperature;
	}
}

/**
 <CommandClass id="156" name="COMMAND_CLASS_SENSOR_ALARM" version="1" request_flags="4">
 <Instance index="1" />
 <Value type="byte" genre="user" instance="1" index="0" label="General" units="" read_only="true" write_only="false" verify_changes="false" poll_intensity="0" min="0" max="255" value="0" />
 <Value type="byte" genre="user" instance="1" index="1" label="Smoke" units="" read_only="true" write_only="false" verify_changes="false" poll_intensity="0" min="0" max="255" value="0" />
 <Value type="byte" genre="user" instance="1" index="4" label="Heat" units="" read_only="true" write_only="false" verify_changes="false" poll_intensity="0" min="0" max="255" value="0" />
 </CommandClass>


 <CommandClass id="49" name="COMMAND_CLASS_SENSOR_MULTILEVEL" version="8">
 <Instance index="1" />
 <Value type="decimal" genre="user" instance="1" index="1" label="Temperature" units="" read_only="true" write_only="false" verify_changes="false" poll_intensity="0" min="0" max="0" value="0.0" />
 </CommandClass>
 **/