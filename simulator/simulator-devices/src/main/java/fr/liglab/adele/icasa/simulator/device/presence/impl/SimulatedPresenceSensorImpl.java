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
package fr.liglab.adele.icasa.simulator.device.presence.impl;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.model.api.PresenceModel;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

/**
 * Implementation of a simulated binary light device.
 *
 */
@ContextEntity(coreServices = {PresenceSensor.class,SimulatedDevice.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
public class SimulatedPresenceSensorImpl implements PresenceSensor,SimulatedDevice,GenericDevice{

    public final static String SIMULATED_PRESENCE_SENSOR = "iCasa.PresenceSensor";

    @State.Field(service = PresenceSensor.class,state = PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE,value = "false")
    private boolean currentSensedPresence;

    @State.Field(service = SimulatedDevice.class,state = SIMULATED_DEVICE_TYPE,value = SIMULATED_PRESENCE_SENSOR)
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
    public boolean getSensedPresence() {
        return currentSensedPresence;
    }

    /**
     * Presence
     */
    @Requires(id="presence.model",specification=PresenceModel.class,optional=true,filter = "(presencemodel.zone.attached=${locatedobject.object.zone})")
    private PresenceModel presenceModel;

    @Bind(id ="presence.model")
    public void bindPresenceModel(PresenceModel model){
        pushPresence(model.getCurrentPresence());
    }

    @Modified(id = "presence.model")
    public void modifiedPresenceModel(PresenceModel model){
        pushPresence(model.getCurrentPresence());
    }

    @Unbind(id = "presence.model")
    public void unbindPresenceModel(PresenceModel model){
        pushPresence(false);
    }

    @State.Push(service = PresenceSensor.class,state = PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)
    public boolean pushPresence(boolean presence){
        return presence;
    }
}
