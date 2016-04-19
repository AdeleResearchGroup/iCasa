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
package fr.liglab.adele.zwave.device.proxyes;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ZWaveAlarmValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass.ZWaveAlarmSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass.ZWaveBinarySensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.icasa.device.DeviceDataEvent;
import fr.liglab.adele.icasa.device.DeviceEventType;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.zwave.device.importer.ZWaveImporter;

/**
 *
 */
@Component(name="FibaroMotionSensor")

@Provides(
		
		specifications	= {	
			MotionSensor.class, GenericDevice.class 
		},
		
		properties 		= { 
			@StaticServiceProperty(immutable = true, name = ZWaveImporter.FACTORY_PROPERTY_MANUFACTURER,	type="java.lang.String", value = "010F"),
			@StaticServiceProperty(immutable = true, name = ZWaveImporter.FACTORY_PROPERTY_DEVICE_ID, 		type="java.lang.String", value = "1001"),
			@StaticServiceProperty(immutable = true, name = ZWaveImporter.FACTORY_PROPERTY_DEFAULT_PROXY,	type="java.lang.String", value = "true")
		}
)

public class FibaroMotionSensor extends AbstractDevice implements MotionSensor,  ZWaveEventListener  {

	private static final Logger LOG = LoggerFactory.getLogger(FibaroMotionSensor.class);


    @Property(mandatory = true, name = ZWaveImporter.PROXY_PROPERTY_CONTROLLER)
    private ZWaveController  controller;

    @Property(mandatory = true, name = ZWaveImporter.PROXY_PROPERTY_NODE)
    private int  node;

    @Property(mandatory = true, name = ZWaveImporter.PROXY_PROPERTY_ENDPOINT)
    private int endpoint;

    @ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String serialNumber;

    public FibaroMotionSensor(){
        super();
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, GenericDevice.LOCATION_UNKNOWN);
		setPropertyValue("zwave.batteryLevel", "unknown");
    }

    @Validate
    private void start() {
    	controller.addEventListener(this);
    }
    
    @Invalidate
    private void stop() {
    	controller.removeEventListener(this);
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }


	@Override
	public void ZWaveIncomingEvent(ZWaveEvent event) {
		
		if (event.getNodeId() == node && event.getEndpoint() == endpoint) {
			
			if (event instanceof ZWaveAlarmSensorValueEvent) {

				ZWaveAlarmSensorValueEvent alarm = (ZWaveAlarmSensorValueEvent) event;
				LOG.debug("Alarm received for Fibaro motion sensor ["+alarm+"] "+alarm.getValue());
			
				switch (alarm.getAlarmType()) {
					default:
						break;
				}
			}
			
			if (event instanceof ZWaveAlarmValueEvent) {

				ZWaveAlarmValueEvent alarm = (ZWaveAlarmValueEvent) event;
				LOG.debug("Alarm received for Fibaro motion sensor ["+alarm+"] "+alarm.getValue());
			
				switch (alarm.getAlarmType()) {
					default:
						break;
				}
			}

			if (event instanceof ZWaveBinarySensorValueEvent) {

				ZWaveBinarySensorValueEvent sensor = (ZWaveBinarySensorValueEvent) event;
				LOG.debug("Sensed value received for Fibaro motion sensor ["+sensor+"] "+sensor.getValue());

				switch (sensor.getSensorType()) {
					case UNKNOWN:
						int sensed = ((Integer) sensor.getValue()).intValue();
						if (sensed == 255) {
							LOG.info("Motion sensed "+sensor+"] "+sensor.getValue());
			                this.notifyListeners(new DeviceDataEvent<Boolean>(this, DeviceEventType.DEVICE_EVENT, Boolean.TRUE));
						}
						break;
					default:
						break;
				}
			}			
			
			if (event instanceof ZWaveMultiLevelSensorValueEvent) {

				ZWaveMultiLevelSensorValueEvent sensor = (ZWaveMultiLevelSensorValueEvent) event;
				LOG.debug("Sensed value received for Fibaro motion sensor ["+sensor+"] "+sensor.getValue());
				switch (sensor.getSensorType()) {
					default:
						break;
				}
			}
			
			if (event instanceof ZWaveCommandClassValueEvent) {

				ZWaveCommandClassValueEvent changedValue = (ZWaveCommandClassValueEvent) event;
				if (changedValue.getCommandClass().equals(CommandClass.BATTERY)) {
					Integer batteryLevel = (Integer) changedValue.getValue();
					setPropertyValue("zwave.batteryLevel", batteryLevel);
				}
			}

		}

	}

}
