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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.ValueId;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Temperature;


@ContextEntity(services = {PresenceSensor.class,Thermometer.class,Photometer.class,Zwave4jDevice.class,HumiditySensor.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)

public class AeonMultiSensor extends AbstractZwave4jDevice implements  GenericDevice, Zwave4jDevice, PresenceSensor,Thermometer,Photometer,HumiditySensor {

	private static final Logger LOG = LoggerFactory.getLogger(AeonMultiSensor.class);

	/**
	 * Injected Behavior
	 */
	@InjectedFunctionalExtension(id="ZwaveBehavior")
	private ZwaveDevice device;

	@Override
	public void initialize(Manager manager) {

	}


	@Override
	public void notification(Manager manager, Notification notification) {
		super.notification(manager, notification);
	}

	@Override
	protected void nodeStatusChanged(Manager manager, short status) {

	}

	@Override
	protected void valueChanged(Manager manager, ValueId valueId) {

		ZWaveCommandClass command = ZWaveCommandClass.valueOf(valueId.getCommandClassId());
		LOG.debug("Value changed = "+command+" instance "+valueId.getInstance()+" index "+valueId.getIndex()+" type "+valueId.getType());

		switch (command) {

			case SENSOR_BINARY:
				presenceValueChange((Boolean)getValue(manager,valueId));
				break;
			case SENSOR_MULTILEVEL:
				if (valueId.getIndex() == 1){
					temperatureValueChange((Float)getValue(manager,valueId));
				}else if (valueId.getIndex() == 3){
					luminosityValueChange((Float)getValue(manager,valueId));
				}else if (valueId.getIndex() == 5){
					humidityValueChange((Float)getValue(manager,valueId));
				}
				break;
			case BATTERY:
				break;
			default:
				break;
		}
	}

	@ContextEntity.State.Field(service = PresenceSensor.class,state = PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE,value = "false")
	private boolean status;

	@ContextEntity.State.Field(service = Thermometer.class,state = Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
	private Quantity<Temperature> temperature;

	@ContextEntity.State.Field(service = Photometer.class,state = Photometer.PHOTOMETER_CURRENT_ILLUMINANCE)
	private Quantity<Illuminance> luminosity;

	@ContextEntity.State.Field(service = HumiditySensor.class,state = HumiditySensor.HUMIDITY,value = "-1")
	private double humidity;

	@ContextEntity.State.Push(service = PresenceSensor.class,state =PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)
	public boolean presenceValueChange(boolean newStatus){
		return newStatus;
	}

	@ContextEntity.State.Push(service = Photometer.class,state =Photometer.PHOTOMETER_CURRENT_ILLUMINANCE)
	public Quantity<Illuminance> luminosityValueChange(float newLuminosity){
		return Quantities.getQuantity(newLuminosity,Units.LUX);
	}

	@ContextEntity.State.Push(service = Thermometer.class,state =Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
	public Quantity<Temperature> temperatureValueChange(float newTemperature){
		return Quantities.getQuantity(newTemperature, Units.CELSIUS);
	}

	@ContextEntity.State.Push(service = HumiditySensor.class,state =HumiditySensor.HUMIDITY)
	public double humidityValueChange(float newHumidity){
		return newHumidity;
	}


	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
	private String serialNumber;

	@Override
	public String getSerialNumber() {
		return serialNumber;
	}


	@Override
	public boolean getSensedPresence() {
		return status;
	}

	@Override
	public Quantity<Illuminance> getIlluminance() {
		return luminosity;
	}

	@Override
	public Quantity<Temperature> getTemperature() {
		return temperature;
	}

	@Override
	public double getHumidityPercentage() {
		return humidity;
	}
}
