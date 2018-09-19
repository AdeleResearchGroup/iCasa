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

import java.util.List;

import org.apache.felix.ipojo.annotations.*;

import fr.liglab.adele.cream.annotations.provider.Creator;

import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;

import fr.liglab.adele.icasa.location.impl.ZoneImpl;


@Component(immediate = true,publicFactory=false)
@Instantiate(name = "LocationManagerImpl-0")
public class LocationManagerImpl{

    @Requires(id = "zones",specification = Zone.class, optional = true, proxy = false)
    List<Zone> zones;

    @Requires(id = "locatedObjects",specification = LocatedObject.class,optional = true, proxy = false)
    List<LocatedObject> locatedObjects;

    @Creator.Field(ZoneImpl.RELATION_CONTAINS) 	Creator.Relation<Zone,LocatedObject> relationContains;

    @Creator.Field(LocatedObjectBehaviorProvider.IS_IN_RELATION) 	Creator.Relation<LocatedObject,Zone> relationIsIn;

    private void attach(LocatedObject object, Zone zone) {
    	try {
    		
    		if (!relationContains.isLinked(zone,object)) {
    			relationContains.link(zone,object);
    		}
    		
    		if (!relationIsIn.isLinked(object,zone)) {
    			relationIsIn.link(object,zone);
    		}

    	}
    	catch (Exception unexpected) {
    		unexpected.printStackTrace();
    	}
    }
    
    private void detach(LocatedObject object, Zone zone) {
    	try {
    		if (relationContains.isLinked(zone,object)) {
    			relationContains.unlink(zone,object);
    		}
    		
    		if (relationIsIn.isLinked(object,zone)) {
    			relationIsIn.unlink(object,zone);
    		}
    	}
    	catch (Exception unexpected) {
    		unexpected.printStackTrace();
    	}
    	
    }
    
    @Bind(id = "zones")
    public synchronized void bindZone(Zone zone) {

    	for (LocatedObject object: locatedObjects) {
    		if (zone.canContains(object.getPosition())) {
    			attach(object,zone);
            }
    	}
    }

    @Unbind(id = "zones")
    public synchronized void unbindZone(Zone zone) {
    	
    	for (LocatedObject object: locatedObjects) {
   			detach(object,zone);
    	}
    }

    @Modified(id = "zones")
    public synchronized void modifiedZone(Zone zone) {
    	
        for (LocatedObject object: locatedObjects) {
        	
        	boolean wasInZone = relationIsIn.isLinked(object,zone);
        	boolean isInZone  = zone.canContains(object.getPosition());

        	if (wasInZone && !isInZone) {
        		detach(object,zone);
        	}
        	
        	if (!wasInZone && isInZone) {
        		attach(object,zone);
        	}
        }
    }

 

    @Bind(id = "locatedObjects")
    public synchronized void bindLocatedObject(LocatedObject object) {

    	for (Zone zone : zones) {
    		if (zone.canContains(object.getPosition())) {
    			attach(object,zone);
            }
        }
    }

    @Unbind(id = "locatedObjects")
    public synchronized void unbindLocatedObject(LocatedObject object) {
    	
    	for (Zone zone : zones) {
    		detach(object,zone);
        }
    }

    @Modified(id = "locatedObjects")
    public synchronized void modifiedLocatedObject(LocatedObject object) {
        
    	for (Zone zone : zones) {

        	boolean wasInZone = relationIsIn.isLinked(object,zone);
        	boolean isInZone  = zone.canContains(object.getPosition());

        	if (wasInZone && !isInZone) {
        		detach(object,zone);
        	}
        	
        	if (!wasInZone && isInZone) {
        		attach(object,zone);
        	}

    	}
    }

}
