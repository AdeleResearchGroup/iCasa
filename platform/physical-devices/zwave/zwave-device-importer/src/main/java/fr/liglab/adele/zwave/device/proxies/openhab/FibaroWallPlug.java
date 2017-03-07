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
import fr.liglab.adele.icasa.device.power.SmartPlug;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

import java.math.BigDecimal;

@ContextEntity(services = {SmartPlug.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)
public class FibaroWallPlug implements  ZWaveEventListener, SmartPlug {


    @Requires(optional = false, proxy=false)
    private OpenhabController controller;

    /**
     * STATES
     */
    @ContextEntity.State.Field(service = SmartPlug.class,state = SmartPlug.SMART_PLUG_STATUS,value = "false")
    private boolean status;

    @ContextEntity.State.Field(service = SmartPlug.class,state = SmartPlug.SMART_PLUG_CONSUMPTION,value = "0.0")
    private float consumption;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    /**
     * Injected Behavior
     */
    @InjectedFunctionalExtension(id="ZwaveBehavior")
    ZwaveDevice device;

    @Override
    public boolean isOn() {
        return status;
    }

    @Override
    public float currentConsumption() {
        return consumption;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * LifeCycle
     */
    @Validate
    public void start(){
        controller.addEventListener(this);
    }

    @Invalidate
    public void stop(){
        controller.removeEventListener(this);
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        if (event.getNodeId() == device.getNodeId()) {
            if (event instanceof ZWaveCommandClassValueEvent) {
                ZWaveCommandClassValueEvent commandClass = (ZWaveCommandClassValueEvent) event;
                if (commandClass.getCommandClass().getLabel().equals(ZWaveCommandClass.CommandClass.SWITCH_BINARY.getLabel())) {
                    pushStatus(commandClass.getValue().equals(255));
                }
            }

            if (event instanceof ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent) {
                ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent castEvent = (ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent) event;

                ZWaveMultiLevelSensorCommandClass.SensorType sensorType = castEvent.getSensorType();
                if (sensorType.getLabel().equals(ZWaveMultiLevelSensorCommandClass.SensorType.POWER.getLabel())){
                    Object value = castEvent.getValue();
                    if (value instanceof BigDecimal){
                        pushConsumption(((BigDecimal) value).floatValue());
                    }
                }
            }
        }
    }

    @ContextEntity.State.Push(service = SmartPlug.class,state =SmartPlug.SMART_PLUG_STATUS )
    public boolean pushStatus(boolean newStatus){
        return newStatus;
    }

    @ContextEntity.State.Push(service = SmartPlug.class,state =SmartPlug.SMART_PLUG_CONSUMPTION )
    public float pushConsumption(float newConso){
        return newConso;
    }
}
