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
package fr.liglab.adele.icasa.simulator.device.button.impl;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;

/**
 * Implementation of a simulated binary light device.
 *
 */
@ContextEntity(services = {PushButton.class,SimulatedDevice.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
public class SimulatedPushButtonImpl implements PushButton,SimulatedDevice,GenericDevice{

    public final static String SIMULATED_PUSH_BUTTON = "iCasa.PushButton";

    @ContextEntity.State.Field(service = PushButton.class,state = PUSH_AND_HOLD,value = "false")
    private boolean isPushed;

    @State.Field(service = SimulatedDevice.class,state = SIMULATED_DEVICE_TYPE,value = SIMULATED_PUSH_BUTTON)
    private String deviceType;

    @State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public boolean isPushed() {
        return isPushed;
    }

    /*TODO REMOVE (TEMP)*/
    @Override
    public void push() {
        pushButtonState(!isPushed);
    }

    /**
     * Push button events
     */
//    @Requires(id="presence.model",specification=PresenceModel.class,optional=true,filter = "(presencemodel.zone.attached=${locatedobject.object.zone})")
//    private PresenceModel presenceModel;
//
//    @Bind(id ="presence.model")
//    public void bindPresenceModel(PresenceModel model){
//        pushPresence(model.getCurrentPresence());
//    }
//
//    @Modified(id = "presence.model")
//    public void modifiedPresenceModel(PresenceModel model){
//        pushPresence(model.getCurrentPresence());
//    }
//
//    @Unbind(id = "presence.model")
//    public void unbindPresenceModel(PresenceModel model){
//        pushPresence(false);
//    }
//
//    @State.Push(service = PresenceSensor.class,state = PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)
//    public boolean pushPresence(boolean presence){
//        return presence;
//    }
    @State.Push(service = PushButton.class,state = PushButton.PUSH_AND_HOLD)
    public boolean pushButtonState(boolean isPushed){
        return isPushed;
    }
}
