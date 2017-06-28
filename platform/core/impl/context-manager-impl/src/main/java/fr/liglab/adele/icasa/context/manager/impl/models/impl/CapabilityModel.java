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
package fr.liglab.adele.icasa.context.manager.impl.models.impl;

import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.models.CapabilityModelAccess;
import fr.liglab.adele.icasa.context.manager.api.config.ContextManagerAdmin;
import fr.liglab.adele.icasa.context.manager.api.Util;
import fr.liglab.adele.icasa.context.manager.impl.models.api.CapabilityModelUpdate;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*ToDo*/
@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class CapabilityModel implements CapabilityModelAccess, CapabilityModelUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(CapabilityModel.class);

    /*Entity creator model*/
    private Map<String, Set<String>> eCreatorsByService = new HashMap<>();
    private Map<String, Set<String>> eCreatorsRequirements = new HashMap<>();
    private Map<String, EntityProvider> eProviderByCreatorName = new HashMap<>();
    private Map<EntityProvider, Set<String>> entitiesByProvider = new HashMap<>();

    /*Relation creator model*/
    private Map<String, Set<String>> rCreatorsByService = new HashMap<>();
    private Map<String, Set<String>> rCreatorsRequirements = new HashMap<>();
    private Map<String, RelationProvider> rProviderByCreatorName = new HashMap<>();
    private Map<RelationProvider, Set<String>> relationsByProvider = new HashMap<>();

    /*MODEL ACCESS*/
    @Override
    public Set<EntityProvider> getEntityProviders() {
        return entitiesByProvider.keySet();
    }

    @Override
    public Set<RelationProvider> getRelationProviders() {
        return relationsByProvider.keySet();
    }

    @Override
    public Map<String, Set<String>> getEntityCreatorsByService() {
        return eCreatorsByService;
    }

    @Override
    public Map<String, Set<String>> getEntityCreatorsRequirements() {
        return eCreatorsRequirements;
    }

    @Override
    public Map<String, EntityProvider> getEntityProviderByCreatorName() {
        return eProviderByCreatorName;
    }

    @Override
    public Map<EntityProvider, Set<String>> getEntitiesByProvider() {
        return entitiesByProvider;
    }


    /*ToDo CHECK*/
    @Override
    public Map<EntityProvider, Set<String>> getEntitiesByProvider(OriginEnum originEnum){
        Map<EntityProvider, Set<String>> result = new HashMap<>();
        for(EntityProvider entityProvider : getEntityProviders()){
            Set<String> creators = new HashSet<>();
            for(String providedEntity : entityProvider.getProvidedEntities()){
                if(originEnum.equals(entityProvider.getOrigin(providedEntity))){
                    creators.add(providedEntity);
                }
            }
            if(!creators.isEmpty())
                result.put(entityProvider, creators);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, Set<String>> getRelationCreatorsByService() {
        return rCreatorsByService;
    }

    @Override
    public Map<String, Set<String>> getRelationCreatorsRequirements() {
        return rCreatorsRequirements;
    }

    @Override
    public Map<String, RelationProvider> getRelationProviderByCreatorName() {
        return rProviderByCreatorName;
    }

    @Override
    public Map<RelationProvider, Set<String>> getRelationsByProvider() {
        return relationsByProvider;
    }

    @Override
    public Set<String> getInstancesByCreator(String creator) {
        Set<String> result = new HashSet<>();

        if(eProviderByCreatorName.containsKey(creator)){
            result.addAll(eProviderByCreatorName.get(creator).getInstances(Util.getProvidedItemFromCreatorName(creator),true));
        }

        if(rProviderByCreatorName.containsKey(creator)){
            result.addAll(rProviderByCreatorName.get(creator).getInstances(Util.getProvidedItemFromCreatorName(creator),true));
        }

        return result;
    }


    /*ToDo CHECK*/
    @Override
    public Map<RelationProvider, Set<String>> getRelationsByProvider(OriginEnum originEnum){
        Map<RelationProvider, Set<String>> result = new HashMap<>();
        for(RelationProvider relationProvider : getRelationProviders()){
            Set<String> creators = new HashSet<>();
            for(String providedRelation : relationProvider.getProvidedRelations()){
                if(originEnum.equals(relationProvider.getOrigin(providedRelation))){
                    creators.add(providedRelation);
                }
            }
            if(!creators.isEmpty())
                result.put(relationProvider, creators);
        }
        return Collections.unmodifiableMap(result);
    }


    /*MODEL UPDATE*/
    @Override
    public void addEntityProvider (EntityProvider entityProvider){
        /*LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();
        if(logLevel>=1){
            LOG.info("PROVIDER ADDED: "+ entityProvider.getName());
        }

        Set<String> providedEntities = entityProvider.getProvidedEntities();
        entitiesByProvider.put(entityProvider, providedEntities);

        for(String providedEntity : providedEntities) {
            String creatorName = Util.creatorName(entityProvider, providedEntity);

            if(logLevel>=3){
                LOG.info("ENTITY: "+ providedEntity);
                LOG.info("PROVIDING: "+ entityProvider.getPotentiallyProvidedEntityServices(providedEntity));
                LOG.info("WITH REQUIREMENTS: "+ entityProvider.getPotentiallyRequiredServices(providedEntity));
            }

            eCreatorsRequirements.put(creatorName, entityProvider.getPotentiallyRequiredServices(providedEntity));
            eProviderByCreatorName.put(creatorName, entityProvider);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (!eCreatorsByService.containsKey(service)) {
                    Set<String> entityProviderSubSet = new HashSet<>();
                    entityProviderSubSet.add(creatorName);
                    eCreatorsByService.put(service, entityProviderSubSet);
                } else {
                    eCreatorsByService.get(service).add(creatorName);
                }
            }
        }
    }

    @Override
    public void modifyEntityProvider (EntityProvider entityProvider){
        /*LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();
        if(logLevel>=1) {
            LOG.info("PROVIDER MODIFIED: "+ entityProvider.getName());
        }

        Set<String> newConfig = entityProvider.getProvidedEntities();
        Set<String> oldConfig = entitiesByProvider.get(entityProvider);

        Set<String> added   = new HashSet<>(newConfig);
        Set<String> removed = new HashSet<>(oldConfig);
        added.removeAll(oldConfig);
        removed.removeAll(newConfig);

        entitiesByProvider.put(entityProvider, newConfig);

        for(String providedEntity : added){
            String creatorName = Util.creatorName(entityProvider, providedEntity);

            if(logLevel>=3) {
                LOG.info("ADDED:");
                LOG.info("ENTITY: " + providedEntity);
                LOG.info("PROVIDING: " + entityProvider.getPotentiallyProvidedEntityServices(providedEntity));
                LOG.info("WITH REQUIREMENTS: " + entityProvider.getPotentiallyRequiredServices(providedEntity));
            }

            eCreatorsRequirements.put(creatorName, entityProvider.getPotentiallyRequiredServices(providedEntity));
            eProviderByCreatorName.put(creatorName, entityProvider);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (!eCreatorsByService.containsKey(service)) {
                    Set<String> entityProviderSubSet = new HashSet<>();
                    entityProviderSubSet.add(creatorName);
                    eCreatorsByService.put(service, entityProviderSubSet);
                } else {
                    eCreatorsByService.get(service).add(creatorName);
                }
            }
        }

        for(String providedEntity : removed){
            String creatorName = Util.creatorName(entityProvider, providedEntity);

            if(logLevel>=3) {
                LOG.info("REMOVED:");
                LOG.info("ENTITY: " + providedEntity);
            }

            eCreatorsRequirements.remove(creatorName);
            eProviderByCreatorName.remove(creatorName);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (eCreatorsByService.containsKey(service)) {
                    eCreatorsByService.get(service).remove(creatorName);
                }
            }
        }
    }

    @Override
    public void removeEntityProvider(EntityProvider entityProvider){
        entitiesByProvider.remove(entityProvider);

        for(String providedEntity : entityProvider.getProvidedEntities()) {
            String creatorName = Util.creatorName(entityProvider, providedEntity);

            eCreatorsRequirements.remove(creatorName);
            eProviderByCreatorName.remove(creatorName);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (eCreatorsByService.containsKey(service)) {
                    eCreatorsByService.get(service).remove(creatorName);
                }
            }
        }

        if(ContextManagerAdmin.getLogLevel()>=1) {
            LOG.info("PROVIDER REMOVED: "+ entityProvider.getName());
        }
    }

    @Override
    public void addRelationProvider(RelationProvider relationProvider){
        /*LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();
        if(logLevel>=1){
            LOG.info("PROVIDER ADDED: "+ relationProvider.getName());
        }

        Set<String> providedRelations = relationProvider.getProvidedRelations();
        relationsByProvider.put(relationProvider, providedRelations);

        for(String providedRelation : providedRelations) {
            String creatorName = Util.creatorName(relationProvider, providedRelation);

            if(logLevel>=3){
                LOG.info("RELATION: "+ providedRelation);
                LOG.info("PROVIDING: "+ relationProvider.getPotentiallyProvidedRelationServices(providedRelation));
                LOG.info("WITH REQUIREMENTS: "+ relationProvider.getPotentiallyRequiredServices(providedRelation));
            }

            rCreatorsRequirements.put(creatorName, relationProvider.getPotentiallyRequiredServices(providedRelation));
            rProviderByCreatorName.put(creatorName, relationProvider);

            for (String service : relationProvider.getPotentiallyProvidedRelationServices(providedRelation)) {
                if (!rCreatorsByService.containsKey(service)) {
                    Set<String> relationProviderSubSet = new HashSet<>();
                    relationProviderSubSet.add(creatorName);
                    rCreatorsByService.put(service, relationProviderSubSet);
                } else {
                    rCreatorsByService.get(service).add(creatorName);
                }
            }
        }
    }

    @Override
    public void modifyRelationProvider(RelationProvider relationProvider){
        /*LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();
        if(logLevel>=1) {
            LOG.info("PROVIDER MODIFIED: "+ relationProvider.getName());
        }

        Set<String> newConfig = relationProvider.getProvidedRelations();
        Set<String> oldConfig = Collections.emptySet();
        if(relationsByProvider.containsKey(relationProvider)){
            oldConfig = relationsByProvider.get(relationProvider);
        }

        Set<String> added   = new HashSet<>(newConfig);
        Set<String> removed = new HashSet<>(oldConfig);
        added.removeAll(oldConfig);
        removed.removeAll(newConfig);

        relationsByProvider.put(relationProvider, newConfig);

        for(String providedRelation : added){
            String creatorName = Util.creatorName(relationProvider, providedRelation);

            if(logLevel>=3) {
                LOG.info("ADDED:");
                LOG.info("RELATION: " + providedRelation);
                LOG.info("PROVIDING: " + relationProvider.getPotentiallyProvidedRelationServices(providedRelation));
                LOG.info("WITH REQUIREMENTS: " + relationProvider.getPotentiallyRequiredServices(providedRelation));
            }

            rCreatorsRequirements.put(creatorName, relationProvider.getPotentiallyRequiredServices(providedRelation));
            rProviderByCreatorName.put(creatorName, relationProvider);

            for (String service : relationProvider.getPotentiallyProvidedRelationServices(providedRelation)) {
                if (!rCreatorsByService.containsKey(service)) {
                    Set<String> relationProviderSubSet = new HashSet<>();
                    relationProviderSubSet.add(creatorName);
                    rCreatorsByService.put(service, relationProviderSubSet);
                } else {
                    rCreatorsByService.get(service).add(creatorName);
                }
            }
        }

        for(String providedRelation : removed){
            String creatorName = Util.creatorName(relationProvider, providedRelation);

            if(logLevel>=3) {
                LOG.info("REMOVED:");
                LOG.info("RELATION: " + providedRelation);
            }

            rCreatorsRequirements.remove(creatorName);
            rProviderByCreatorName.remove(creatorName);

            for (String service : relationProvider.getPotentiallyProvidedRelationServices(providedRelation)) {
                if (rCreatorsByService.containsKey(service)) {
                    rCreatorsByService.get(service).remove(creatorName);
                }
            }
        }
    }

    @Override
    public void removeRelationProvider(RelationProvider relationProvider){
        relationsByProvider.remove(relationProvider);

        for(String providedRelation : relationProvider.getProvidedRelations()) {
            String creatorName = Util.creatorName(relationProvider, providedRelation);

            rCreatorsRequirements.remove(creatorName);
            rProviderByCreatorName.remove(creatorName);

            for (String service : relationProvider.getPotentiallyProvidedRelationServices(providedRelation)) {
                if (rCreatorsByService.containsKey(service)) {
                    rCreatorsByService.get(service).remove(creatorName);
                }
            }
        }

        if(ContextManagerAdmin.getLogLevel()>=1) {
            LOG.info("PROVIDER REMOVED: "+ relationProvider.getName());
        }
    }


}
