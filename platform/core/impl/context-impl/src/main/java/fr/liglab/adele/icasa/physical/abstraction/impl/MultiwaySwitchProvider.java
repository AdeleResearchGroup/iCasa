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

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

@Component(immediate = true,publicFactory = false)
@Instantiate
public class MultiwaySwitchProvider {

    @Creator.Field(requirements = {Zone.class, PushButton.class}) Creator.Entity<MultiwaySwitchImpl> creator;

    @Creator.Field(value = MultiwaySwitchImpl.RELATION_IS_ATTACHED, requirements = Zone.class) Creator.Relation<MultiwaySwitchImpl,Zone> attachedMultiSwitchModelCreator;

    @Bind(id = "zones",specification = Zone.class,aggregate = true,optional = true)
    public void bindZone(Zone zone){
        String name = generateEntityName(zone);
        creator.create(name);
        attachedMultiSwitchModelCreator.create(name,zone);
    }

    @Unbind(id = "zones")
    public void unbindZone(Zone zone){
        String name = generateEntityName(zone);
        creator.delete(name);
        attachedMultiSwitchModelCreator.delete(name,zone);
    }

    private String generateEntityName(Zone zone){
        return zone.getZoneName()+".multiway.switch";
    }
}