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

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.behavior.InjectedBehavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.ValueId;


@ContextEntity(services = {Thermometer.class,Zwave4jDevice.class,})

@Behavior(id="LocatedBehavior",spec = LocatedObject.class,implem = LocatedObjectBehaviorProvider.class)
@Behavior(id="ZwaveBehavior",spec = ZwaveDevice.class,implem = ZwaveDeviceBehaviorProvider.class)

public class FibaroSmokeSensor extends AbstractZwave4jDevice implements  GenericDevice, Zwave4jDevice,Thermometer {

	private static final Logger LOG = LoggerFactory.getLogger(FibaroSmokeSensor.class);

	/**
	 * Injected Behavior
	 */
	@InjectedBehavior(id="ZwaveBehavior")
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

			case SENSOR_MULTILEVEL:
				if (valueId.getIndex() == 1){
					temperatureValueChange((Float)getValue(manager,valueId));
				}
				break;
			case BATTERY:
			case SENSOR_ALARM:
				break;
			default:
				break;
		}
	}

	@ContextEntity.State.Field(service = Thermometer.class,state = Thermometer.THERMOMETER_CURRENT_TEMPERATURE,value = "-1")
	private double temperature;

	@ContextEntity.State.Push(service = Thermometer.class,state =Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
	public double temperatureValueChange(float newTemperature){
		return newTemperature;
	}

	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
	private String serialNumber;

	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public double getTemperature() {
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