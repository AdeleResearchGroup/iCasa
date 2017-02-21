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
import fr.liglab.adele.icasa.device.doorWindow.DoorWindowSensor;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

@ContextEntity(services = {DoorWindowSensor.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)

public class FibaroDoorWindowSensor implements  GenericDevice, DoorWindowSensor, ZWaveEventListener {

    /**
     * iPOJO Require
     */
    @Requires(optional = false, proxy=false)
    private OpenhabController controller;

    /**
     * STATES
     */
    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @ContextEntity.State.Field(service = DoorWindowSensor.class,state = DoorWindowSensor.DOOR_WINDOW_SENSOR_OPENING_DETECTCION,value = "false")
    private boolean status;

    /**
     * Injected Behavior
     */
    @InjectedFunctionalExtension(id="ZwaveBehavior")
    ZwaveDevice device;

    /**
     * SERVICE
     */

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public boolean isOpened() {
        return status;
    }

    /**
     * LIFECYCLE
     */

    @Validate
    private void start() {
        controller.addEventListener(this);
    }

    @Invalidate
    private void stop() {
        controller.removeEventListener(this);
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        if (event.getNodeId() == device.getNodeId()) {
            if (event instanceof ZWaveBinarySensorCommandClass.ZWaveBinarySensorValueEvent){
                ZWaveBinarySensorCommandClass.ZWaveBinarySensorValueEvent castEvent = (ZWaveBinarySensorCommandClass.ZWaveBinarySensorValueEvent) event;
                ZWaveCommandClass.CommandClass commandClass = castEvent.getCommandClass();
                if (commandClass.getLabel().equals(ZWaveCommandClass.CommandClass.SENSOR_BINARY.getLabel())){
                    Object value = castEvent.getValue();
                    pushStatus(value.equals(255));
                }
            }
        }
    }

    @ContextEntity.State.Push(service = DoorWindowSensor.class,state =DoorWindowSensor.DOOR_WINDOW_SENSOR_OPENING_DETECTCION)
    public boolean pushStatus(boolean newStatus){
        return newStatus;
    }
}
