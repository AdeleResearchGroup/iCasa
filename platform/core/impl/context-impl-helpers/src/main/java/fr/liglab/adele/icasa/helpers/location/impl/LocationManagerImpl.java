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
package fr.liglab.adele.icasa.helpers.location.impl;


import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.model.Relation;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.impl.ZoneImpl;
import org.apache.felix.ipojo.annotations.*;


import java.util.List;

@Component(immediate = true,publicFactory=false)
@Instantiate(name = "LocationManagerImpl-0")
public class LocationManagerImpl{

    @Requires(id = "zones",specification = Zone.class,optional = true)
    List<Zone> zones;

    @Requires(id = "locatedObjects",specification = LocatedObject.class,optional = true,proxy = false)
    List<LocatedObject> locatedObjects;

    @Creator.Field(value = ZoneImpl.RELATION_CONTAINS, remote = true) 	Creator.Relation<Zone,LocatedObject> containsCreator;

    @Creator.Field(value = LocatedObjectBehaviorProvider.IS_IN_RELATION, remote = true) 	Creator.Relation<LocatedObject,Zone> isContainsCreator;

    @Bind(id = "zones")
    public synchronized void bindZone(Zone zone){
        for (LocatedObject object:locatedObjects){
            if (! zone.canContains(object.getPosition())) {
                continue;
            }
            containsCreator.create(zone,object);
            isContainsCreator.create(object,zone);

        }
    }

    @Modified(id = "zones")
    public synchronized void modifiedZone(Zone zone){
        for (LocatedObject object:locatedObjects){
            if (zone.canContains(object.getPosition())) {
                try{
                    containsCreator.create(zone,object);
                    isContainsCreator.create(object,zone);
                }catch (IllegalArgumentException e){

                }
            }else {
                containsCreator.delete(zone,object);
                isContainsCreator.delete(object,zone);
            }
        }
    }

    @Unbind(id = "zones")
    public synchronized void unbindZone(Zone zone){
        List<Relation> relations = containsCreator.getInstancesRelatedTo(zone);
        for (Relation relation:relations){
            String source = relation.getSource();
            String end = relation.getTarget();
            containsCreator.delete(source,end);
            isContainsCreator.delete(end,zone);
        }

    }


    @Bind(id = "locatedObjects")
    public synchronized void bindLocatedObject(LocatedObject object){
        for (Zone zone : zones) {
            if (! zone.canContains(object.getPosition())) {
                continue;
            }
            containsCreator.create(zone,object);
            isContainsCreator.create(object,zone);
        }
    }

    @Modified(id = "locatedObjects")
    public synchronized void modifiedLocatedObject(LocatedObject object){
        for (Zone zone : zones) {
            if (zone.canContains(object.getPosition())) {
                try{
                    containsCreator.create(zone,object);
                    isContainsCreator.create(object,zone);
                }catch (IllegalArgumentException e){

                }
            }else {
                containsCreator.delete(zone,object);
                isContainsCreator.delete(object,zone);
            }
        }
    }

    @Unbind(id = "locatedObjects")
    public synchronized void unbindLocatedObject(LocatedObject object){
        containsCreator.delete(object.getZone(),object);
        isContainsCreator.delete(object,object.getZone());
    }
}
