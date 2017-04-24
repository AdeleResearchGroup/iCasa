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
package fr.liglab.adele.icasa.physical.abstraction.impl;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.physical.abstraction.MultiwaySwitch;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(services = MultiwaySwitch.class)
public class MultiwaySwitchImpl implements MultiwaySwitch {

    @ContextEntity.State.Field(service = MultiwaySwitch.class,state = MultiwaySwitch.GLOBAL_SWITCH_STATE, value = "NOT_MEASURED")
    public SwitchState switchState;

    @ContextEntity.State.Field(service = MultiwaySwitch.class,state = MultiwaySwitch.ZONE_ATTACHED)
    public String zoneName;

    public static final String RELATION_IS_ATTACHED = "switchstate.of";

    @Override
    public SwitchState stateInZone() {
        return switchState;
    }

    @Override
    public String switchStateOf() {
        return zoneName;
    }


    /**
     * Zone synchro
     */
    @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone",specification=Zone.class,optional=false)
    Zone zone;

    @Bind(id = "zone")
    public void bindZone(Zone zone){
        pushZone(zone.getZoneName());
    }

    @ContextEntity.State.Push(service = MultiwaySwitch.class,state = MultiwaySwitch.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    /**
     * Switch Synchro
     */
    @Requires(id = "pushButtons",specification = PushButton.class,filter = "(locatedobject.object.zone=${multiwayswitch.zone.attached})",optional = true)
    List<PushButton> pushButtons;

    @Bind(id = "pushButtons")
    public void bindPushButton(){
        switchState(false);
    }

    @Modified(id = "pushButtons")
    public void modifiedPushButton(){
        switchState(true);
    }

    @Unbind(id = "pushButtons")
    public void unbindPushButton(){
        switchState(false);
    }

    @ContextEntity.State.Push(service = MultiwaySwitch.class,state = MultiwaySwitch.GLOBAL_SWITCH_STATE)
    public SwitchState switchState(boolean switched){
        SwitchState result = switchState;
        if (pushButtons.isEmpty()){
            return SwitchState.NOT_MEASURED;
        } else {
            switch (switchState){
                case NOT_MEASURED:
                    result =  SwitchState.OFF;
                    break;
                case ON:
                    if(switched){
                        result = SwitchState.OFF;
                    } else {
                        result = SwitchState.ON;
                    }
                    break;
                case OFF:
                    if(switched){
                        result = SwitchState.ON;
                    } else {
                        result = SwitchState.OFF;
                    }
                    break;
            }
        }
        return result;
    }

    //FAIT UNE BOUCLE INFINIE
//    @ContextEntity.State.Pull(service = MultiwaySwitch.class,state = MultiwaySwitch.GLOBAL_SWITCH_STATE)
//    Supplier<SwitchState> switchPull=()->{
//        return switchState;
//    };
}
