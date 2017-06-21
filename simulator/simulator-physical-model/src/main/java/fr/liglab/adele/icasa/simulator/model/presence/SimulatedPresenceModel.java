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
package fr.liglab.adele.icasa.simulator.model.presence;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.Relation;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulator.model.api.PresenceModel;
import fr.liglab.adele.icasa.simulator.person.Person;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.List;

@ContextEntity(coreServices = PresenceModel.class)
public class SimulatedPresenceModel implements PresenceModel {

    @State.Field(service = PresenceModel.class, state = CURRENT_PRESENCE, value = "false")
    private boolean presence;

    @State.Field(service = PresenceModel.class, state = ZONE_ATTACHED)
    private String zoneName;

    @Override
    public boolean getCurrentPresence() {
        return presence;
    }

    @Override
    public String getAttachedZone() {
        return zoneName;
    }

    /**
     * Zone management
     */

    public static final String RELATION_IS_ATTACHED="presence.model.of";

    @Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone", specification=Zone.class, optional=false)
    Zone zone;

    @Bind(id = "zone")
    public void bindZone(Zone zone){
        pushZone(zone.getZoneName());
    }

    @State.Push(service = PresenceModel.class,state = PresenceModel.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    /**
     * Presence management
     * Computes and updates the presence property value of specified zone.
     * Computation is done with presence sensors.
     */
    @State.Push(service = PresenceModel.class,state = PresenceModel.CURRENT_PRESENCE)
    public boolean pushPresence(boolean presence) {
        return presence;
    }

    @Requires(id="persons", specification = Person.class, optional = true, filter = "(locatedobject.object.zone=${presencemodel.zone.attached})")
    List<Person> persons;

    @Bind(id = "persons")
    public void bindPerson(Person person){
        updatePresence();
    }

    @Unbind(id = "persons")
    public void unbindPerson(Person person){
        updatePresence();
    }

    private void updatePresence() {
        if (persons.isEmpty()){
            pushPresence(false);
        } else {
            pushPresence(true);
        }
    }

}
