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

import fr.liglab.adele.icasa.device.humidity.HumiditySensor;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Thermometer;

import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;

import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Temperature;


@ContextEntity(coreServices = {PresenceSensor.class,Thermometer.class,Photometer.class,Zwave4jDevice.class,HumiditySensor.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)

public class AeonMultiSensor extends AbstractZwave4jDevice implements  GenericDevice, Zwave4jDevice, PresenceSensor,Thermometer,Photometer,HumiditySensor {

	/**
	 * Injected Behavior
	 */
	@InjectedFunctionalExtension(id="ZwaveBehavior")
	private ZwaveDevice device;

	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
	private String serialNumber;

	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	@ContextEntity.State.Field(service = PresenceSensor.class,state = PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE,value = "false")
	private boolean status;


	@ContextEntity.State.Push(service = PresenceSensor.class,state = PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)
	public boolean presenceChanged(boolean status){
		return status;
	}

	@Override
	public boolean getSensedPresence() {
		return status;
	}

	@ContextEntity.State.Field(service = Thermometer.class,state = Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
	private Quantity<Temperature> temperature;

	@ContextEntity.State.Push(service = Thermometer.class,state =Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
	public Quantity<Temperature> temperatureChanged(float measure){
		return Quantities.getQuantity(measure, Units.CELSIUS);
	}

	@Override
	public Quantity<Temperature> getTemperature() {
		return temperature;
	}

	@ContextEntity.State.Field(service = Photometer.class,state = Photometer.PHOTOMETER_CURRENT_ILLUMINANCE)
	private Quantity<Illuminance> luminosity;

	@ContextEntity.State.Push(service = Photometer.class,state =Photometer.PHOTOMETER_CURRENT_ILLUMINANCE)
	public Quantity<Illuminance> luminosityChanged(float measure){
		return Quantities.getQuantity(measure,Units.LUX);
	}

	@Override
	public Quantity<Illuminance> getIlluminance() {
		return luminosity;
	}

	@ContextEntity.State.Field(service = HumiditySensor.class,state = HumiditySensor.HUMIDITY,value = "-1")
	private double humidity;

	@ContextEntity.State.Push(service = HumiditySensor.class,state = HumiditySensor.HUMIDITY)
	public double humidityChanged(float measure){
		return measure;
	}

	@Override
	public double getHumidityPercentage() {
		return humidity;
	}


	@Override
	protected void valueChanged(ZWaveCommandClass command, short intance, short index, boolean value) {
		switch(command) {
			case SENSOR_BINARY:
				presenceChanged(value);
				break;
			default:
				break;
		}
	}

	@Override
	protected void valueChanged(ZWaveCommandClass command, short intance, short index, float value) {
		switch(command) {
			case SENSOR_MULTILEVEL:
				switch(index) {
					case 1:
						temperatureChanged(value);
						break;
					case 3:
						luminosityChanged(value);
						break;
					case 5:
						humidityChanged(value);
						break;
				}
				break;
			default:
				break;
		}
	}

}
