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
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.physical.abstraction.PresenceService;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = PresenceService.class)
public class PresenceServiceImpl implements PresenceService {

    @ContextEntity.State.Field(service = PresenceService.class,state = PresenceService.PRESENCE_SENSED)
    public PresenceSensing presenceSensing;

    @ContextEntity.State.Field(service = PresenceService.class,state = PresenceService.ZONE_ATTACHED)
    public String zoneName;

    public static final String RELATION_IS_ATTACHED = "presence.of";
    @Override
    public PresenceSensing havePresenceInZone() {
        return presenceSensing;
    }

    @Override
    public String sensePresenceIn() {
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

    @ContextEntity.State.Push(service = PresenceService.class,state = PresenceService.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    /**
     * Presence Synchro
     */
    @Requires(id = "presenceSensors",specification = PresenceSensor.class,filter = "(locatedobject.object.zone=${presenceservice.zone.attached})",optional = true)
    List<PresenceSensor> presenceSensors;

    @Bind(id = "presenceSensors")
    public void bindPresenceSensor(){
        presencePush();
    }

    @Modified(id = "presenceSensors")
    public void modifiedPresenceSensor(){
        presencePush();
    }

    @Unbind(id = "presenceSensors")
    public void unbindPresenceSensor(){
        presencePush();
    }

    @ContextEntity.State.Push(service = PresenceService.class,state = PresenceService.PRESENCE_SENSED)
    public PresenceSensing presencePush(){
        if (presenceSensors.isEmpty()){
            return PresenceSensing.NOT_MEASURED;
        }
        for (PresenceSensor sensor : presenceSensors){
            if (sensor.getSensedPresence()){
                return PresenceSensing.YES;
            }
        }
        return PresenceSensing.NO;
    };

    @ContextEntity.State.Pull(service = PresenceService.class,state = PresenceService.PRESENCE_SENSED)
    Supplier<PresenceSensing> presencePull=()->{
        return presencePush();
    };
}
