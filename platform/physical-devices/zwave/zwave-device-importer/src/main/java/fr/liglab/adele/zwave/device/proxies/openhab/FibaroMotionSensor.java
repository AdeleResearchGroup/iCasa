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
package fr.liglab.adele.zwave.device.proxies.openhab;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.InjectedFunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
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

@ContextEntity(services = {MotionSensor.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)

public class FibaroMotionSensor implements MotionSensor, ZWaveEventListener, GenericDevice  {

    /**
     * iPOJO Require
     */
    @Requires(optional = false, proxy=false)
    private OpenhabController controller;

    private static final Logger LOG = LoggerFactory.getLogger(FibaroMotionSensor.class);

    /**
     * STATES
     */
    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    /**
     * Injected Behavior
     */
    @InjectedFunctionalExtension(id="ZwaveBehavior")
    ZwaveDevice device;

    /**
     * Services
     */
    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Validate
    private void start() {
        controller.addEventListener(this);
    }

    @Invalidate
    private void stop() {
        controller.removeEventListener(this);
    }


    /**
     * Synchro
     *
     */
    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {

        if (event.getNodeId() == device.getNodeId() ) {

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
                            //	                this.notifyListeners(new DeviceDataEvent<Boolean>(this, DeviceEventType.DEVICE_EVENT, Boolean.TRUE));
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
                    // Integer batteryLevel = (Integer) changedValue.getValue();
                    // setPropertyValue("zwave.batteryLevel", batteryLevel);
                }
            }

        }

    }

}
