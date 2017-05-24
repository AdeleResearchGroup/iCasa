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
package fr.liglab.adele.icasa.context.manager.impl.temp.generic;

import fr.liglab.adele.icasa.context.manager.api.temp.generic.EntityResource;
import fr.liglab.adele.icasa.context.manager.api.temp.generic.RelationResource;
import fr.liglab.adele.icasa.context.manager.api.temp.generic.Resource;
import fr.liglab.adele.icasa.context.manager.api.temp.generic.ResourceRegistration;

import org.apache.felix.ipojo.annotations.*;

import java.util.*;

//@Component (immediate = true, publicFactory = false)
//@Instantiate
//@Provides
//@SuppressWarnings("unused")
public class FlatResourceModel implements ResourceRegistration, FlatResourceModelAccess {

    /*RESOURCES*/
    private Map<UUID, EntityResource> entityModel = Collections.emptyMap();
    private Map<UUID, RelationResource> relationModel = Collections.emptyMap();

    /*EVENT LISTENERS*/
    @Requires(optional = true)
    @SuppressWarnings("all")
    private ResourceListener[] listeners;

    private void notifyAddingResources(Set<Resource> resources){
        if(!resources.isEmpty()){
            for(ResourceListener resourceListener : listeners){
                resourceListener.notifyAddingResources(resources);
            }
        }
    }

    private void notifyRemovingResources(Set<Resource> resources){
        if(!resources.isEmpty()) {
            for (ResourceListener resourceListener : listeners) {
                resourceListener.notifyRemovingResources(resources);
            }
        }
    }

    /*RESOURCE REGISTRATION*/
    @Override
    public Set<Resource> getImportedResources(String importerId) {
        Set<Resource> resultSet = Collections.emptySet();
        for (EntityResource resource : entityModel.values()){
            if(importerId.equals(resource.getImporter()))
                resultSet.add(resource);
        }
        for (RelationResource resource : relationModel.values()){
            if(importerId.equals(resource.getImporter()))
                resultSet.add(resource);
        }
        return resultSet;
    }

    @Override
    public synchronized boolean importResource(EntityResource resource) {
        boolean result = true;
        try{
            entityModel.put(resource.getId(), resource);
            notifyAddingResources(Collections.singleton(resource));
        } catch (NullPointerException ne){
            result = false;
        }
        return result;
    }

    @Override
    public boolean importResource(RelationResource relation) {
        boolean result = true;
        try{
            if(!entityModel.containsKey(relation.getSource())
                    || !entityModel.containsKey(relation.getDestination())){
                throw new NullPointerException();
            }
            relationModel.put(relation.getId(), relation);
            notifyAddingResources(Collections.singleton(relation));
        } catch (NullPointerException ne){
            result = false;
        }
        return result;
    }

    private Set<Resource> removeRelationLinkedToEntity(UUID entityId){
        Set<Resource> removedResources = Collections.emptySet();
        Iterator<RelationResource> it = relationModel.values().iterator();
        for(;it.hasNext();){
            RelationResource resource = it.next();
            if(entityId.equals(resource.getSource())
                    ||entityId.equals(resource.getDestination()))
                removedResources.add(resource);
            relationModel.remove(resource.getId());
        }
        return removedResources;
    }

    private Set<Resource> safelyRemoveResource(UUID resourceId) throws NullPointerException{
        Set<Resource> result = Collections.emptySet();
        Resource resource;
        try{
            resource = relationModel.get(resourceId);
            if(resource != null){
                result.add(resource);
                relationModel.remove(resourceId);
            } else {
                resource = entityModel.get(resourceId);
                if(resource != null){
                    result.add(resource);
                    result.addAll(removeRelationLinkedToEntity(resourceId));
                    entityModel.remove(resourceId);
                }
            }
        } catch (NullPointerException ne){
            throw new NullPointerException();
        }
        return result;
    }

    @Override
    public synchronized boolean removeResource(UUID resourceId) {
        boolean result = true;
        try{
            Set<Resource> removedResources = safelyRemoveResource(resourceId);
            notifyRemovingResources(removedResources);
        } catch (NullPointerException ne){
            result = false;
        }
        return result;
    }

    @Override
    public synchronized boolean removeAllResourcesFromImporter(String importerId) {
        boolean result = true;
        try{
            //Remove all sources from that importer - Entities
            Set<Resource> removedResources = Collections.emptySet();
            Iterator<EntityResource> itE = entityModel.values().iterator();
            for(;itE.hasNext();){
                EntityResource resource = itE.next();
                if(importerId.equals(resource.getImporter()))
                    removedResources.addAll(safelyRemoveResource(resource.getId()));
            }
            //Remove all sources from that importer - Relations
            Iterator<RelationResource> itR = relationModel.values().iterator();
            for(;itR.hasNext();){
                RelationResource resource = itR.next();
                if(importerId.equals(resource.getImporter()))
                    removedResources.addAll(safelyRemoveResource(resource.getId()));
            }
            //Notifies
            notifyRemovingResources(removedResources);
        } catch (NullPointerException ne){
            result = false;
        }
        return result;
    }

    /*RESOURCE MODEL ACCESS*/
    @Override
    public Set<Resource> getResources() {
        Set<Resource> result = Collections.emptySet();
        result.addAll(entityModel.values());
        result.addAll(relationModel.values());
        return result;
    }

    @Override
    public Set<EntityResource> getEntitiesOfType(String type) {
        Set<EntityResource> result = Collections.emptySet();
        try{
            for (EntityResource entityResource : entityModel.values()){
                if(type.equals(entityResource.getType())){
                    result.add(entityResource);
                }
            }
        } catch (NullPointerException ne){
            result = null;
        }
        return result;
    }

    @Override
    public Set<RelationResource> getRelationsOfType(String type) {
        Set<RelationResource> result = Collections.emptySet();
        try{
            for (RelationResource relationResource : relationModel.values()){
                if(type.equals(relationResource.getType())){
                    result.add(relationResource);
                }
            }
        } catch (NullPointerException ne){
            result = null;
        }
        return result;
    }

    @Override
    public Set<EntityResource> getEntities() {
        return new HashSet<>(entityModel.values());
    }

    @Override
    public Set<RelationResource> getRelations() {
        return new HashSet<>(relationModel.values());
    }
}