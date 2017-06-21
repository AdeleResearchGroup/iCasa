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
package fr.liglab.adele.icasa.context.manager.impl.generic;

import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.CapabilityModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextManagerAdmin;
import fr.liglab.adele.icasa.context.manager.api.generic.Util;
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

    /*ToDo remove*/
    /*Maintained set and map for context resolution machine*/

    /*Entity creator model*/
    private Map<String, Set<String>> mEntityCreatorsByService = new HashMap<>();
    private Map<String, Set<String>> mEntityCreatorsRequirements = new HashMap<>();
    private Map<String, EntityProvider> mEntityProviderByCreatorName = new HashMap<>();
    private Map<EntityProvider, Set<String>> mEntitiesByProvider = new HashMap<>();

    /*Relation creator model*/
    private Map<String, Set<String>> mRelationCreatorsByService = new HashMap<>();
    private Map<String, Set<String>> mRelationCreatorsRequirements = new HashMap<>();
    private Map<String, RelationProvider> mRelationProviderByCreatorName = new HashMap<>();
    private Map<RelationProvider, Set<String>> mRelationsByProvider = new HashMap<>();

    @Override
    public Map<String, Set<String>> getmEntityCreatorsByService() {
        return mEntityCreatorsByService;
    }

    @Override
    public Map<String, Set<String>> getmEntityCreatorsRequirements() {
        return mEntityCreatorsRequirements;
    }

    @Override
    public Map<String, EntityProvider> getmEntityProviderByCreatorName() {
        return mEntityProviderByCreatorName;
    }

    @Override
    public Map<EntityProvider, Set<String>> getmEntitiesByProvider() {
        return mEntitiesByProvider;
    }

    @Override
    public Map<String, Set<String>> getmRelationCreatorsByService() {
        return mRelationCreatorsByService;
    }

    @Override
    public Map<String, Set<String>> getmRelationCreatorsRequirements() {
        return mRelationCreatorsRequirements;
    }

    @Override
    public Map<String, RelationProvider> getmRelationProviderByCreatorName() {
        return mRelationProviderByCreatorName;
    }

    @Override
    public Map<RelationProvider, Set<String>> getmRelationsByProvider() {
        return mRelationsByProvider;
    }

    @Override
    public void addEntityProvider (EntityProvider entityProvider){
        /*LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();
        if(logLevel>=1){
            LOG.info("PROVIDER ADDED: "+ entityProvider.getName());
        }

        Set<String> providedEntities = entityProvider.getProvidedEntities();
        mEntitiesByProvider.put(entityProvider, providedEntities);

        for(String providedEntity : providedEntities) {
            String creatorName = Util.eCreatorName(entityProvider, providedEntity);

            if(logLevel>=3){
                LOG.info("ENTITY: "+ providedEntity);
                LOG.info("PROVIDING: "+ entityProvider.getPotentiallyProvidedEntityServices(providedEntity));
                LOG.info("WITH REQUIREMENTS: "+ entityProvider.getPotentiallyRequiredServices(providedEntity));
            }

            mEntityCreatorsRequirements.put(creatorName, entityProvider.getPotentiallyRequiredServices(providedEntity));
            mEntityProviderByCreatorName.put(creatorName, entityProvider);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (!mEntityCreatorsByService.containsKey(service)) {
                    Set<String> entityProviderSubSet = new HashSet<>();
                    entityProviderSubSet.add(creatorName);
                    mEntityCreatorsByService.put(service, entityProviderSubSet);
                } else {
                    mEntityCreatorsByService.get(service).add(creatorName);
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
        Set<String> oldConfig = mEntitiesByProvider.get(entityProvider);

        Set<String> added   = new HashSet<>(newConfig);
        Set<String> removed = new HashSet<>(oldConfig);
        added.removeAll(oldConfig);
        removed.removeAll(newConfig);

        mEntitiesByProvider.put(entityProvider, newConfig);

        for(String providedEntity : added){
            String creatorName = Util.eCreatorName(entityProvider, providedEntity);

            if(logLevel>=3) {
                LOG.info("ADDED:");
                LOG.info("ENTITY: " + providedEntity);
                LOG.info("PROVIDING: " + entityProvider.getPotentiallyProvidedEntityServices(providedEntity));
                LOG.info("WITH REQUIREMENTS: " + entityProvider.getPotentiallyRequiredServices(providedEntity));
            }

            mEntityCreatorsRequirements.put(creatorName, entityProvider.getPotentiallyRequiredServices(providedEntity));
            mEntityProviderByCreatorName.put(creatorName, entityProvider);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (!mEntityCreatorsByService.containsKey(service)) {
                    Set<String> entityProviderSubSet = new HashSet<>();
                    entityProviderSubSet.add(creatorName);
                    mEntityCreatorsByService.put(service, entityProviderSubSet);
                } else {
                    mEntityCreatorsByService.get(service).add(creatorName);
                }
            }
        }

        for(String providedEntity : removed){
            String creatorName = Util.eCreatorName(entityProvider, providedEntity);

            if(logLevel>=3) {
                LOG.info("REMOVED:");
                LOG.info("ENTITY: " + providedEntity);
            }

            mEntityCreatorsRequirements.remove(creatorName);
            mEntityProviderByCreatorName.remove(creatorName);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (mEntityCreatorsByService.containsKey(service)) {
                    mEntityCreatorsByService.get(service).remove(creatorName);
                }
            }
        }
    }

    @Override
    public void removeEntityProvider(EntityProvider entityProvider){
        mEntitiesByProvider.remove(entityProvider);

        for(String providedEntity : entityProvider.getProvidedEntities()) {
            String creatorName = Util.eCreatorName(entityProvider, providedEntity);

            mEntityCreatorsRequirements.remove(creatorName);
            mEntityProviderByCreatorName.remove(creatorName);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (mEntityCreatorsByService.containsKey(service)) {
                    mEntityCreatorsByService.get(service).remove(creatorName);
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
        mRelationsByProvider.put(relationProvider, providedRelations);

        for(String providedRelation : providedRelations) {
            String creatorName = Util.eCreatorName(relationProvider, providedRelation);

            if(logLevel>=3){
                LOG.info("RELATION: "+ providedRelation);
                LOG.info("PROVIDING: "+ relationProvider.getPotentiallyProvidedRelationServices(providedRelation));
                LOG.info("WITH REQUIREMENTS: "+ relationProvider.getPotentiallyRequiredServices(providedRelation));
            }

            mRelationCreatorsRequirements.put(creatorName, relationProvider.getPotentiallyRequiredServices(providedRelation));
            mRelationProviderByCreatorName.put(creatorName, relationProvider);

            for (String service : relationProvider.getPotentiallyProvidedRelationServices(providedRelation)) {
                if (!mRelationCreatorsByService.containsKey(service)) {
                    Set<String> relationProviderSubSet = new HashSet<>();
                    relationProviderSubSet.add(creatorName);
                    mRelationCreatorsByService.put(service, relationProviderSubSet);
                } else {
                    mRelationCreatorsByService.get(service).add(creatorName);
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
        if(mRelationsByProvider.containsKey(relationProvider)){
            oldConfig = mRelationsByProvider.get(relationProvider);
        }

        Set<String> added   = new HashSet<>(newConfig);
        Set<String> removed = new HashSet<>(oldConfig);
        added.removeAll(oldConfig);
        removed.removeAll(newConfig);

        mRelationsByProvider.put(relationProvider, newConfig);

        for(String providedRelation : added){
            String creatorName = Util.eCreatorName(relationProvider, providedRelation);

            if(logLevel>=3) {
                LOG.info("ADDED:");
                LOG.info("RELATION: " + providedRelation);
                LOG.info("PROVIDING: " + relationProvider.getPotentiallyProvidedRelationServices(providedRelation));
                LOG.info("WITH REQUIREMENTS: " + relationProvider.getPotentiallyRequiredServices(providedRelation));
            }

            mRelationCreatorsRequirements.put(creatorName, relationProvider.getPotentiallyRequiredServices(providedRelation));
            mRelationProviderByCreatorName.put(creatorName, relationProvider);

            for (String service : relationProvider.getPotentiallyProvidedRelationServices(providedRelation)) {
                if (!mRelationCreatorsByService.containsKey(service)) {
                    Set<String> relationProviderSubSet = new HashSet<>();
                    relationProviderSubSet.add(creatorName);
                    mRelationCreatorsByService.put(service, relationProviderSubSet);
                } else {
                    mRelationCreatorsByService.get(service).add(creatorName);
                }
            }
        }

        for(String providedRelation : removed){
            String creatorName = Util.eCreatorName(relationProvider, providedRelation);

            if(logLevel>=3) {
                LOG.info("REMOVED:");
                LOG.info("RELATION: " + providedRelation);
            }

            mRelationCreatorsRequirements.remove(creatorName);
            mRelationProviderByCreatorName.remove(creatorName);

            for (String service : relationProvider.getPotentiallyProvidedRelationServices(providedRelation)) {
                if (mRelationCreatorsByService.containsKey(service)) {
                    mRelationCreatorsByService.get(service).remove(creatorName);
                }
            }
        }
    }

    @Override
    public void removeRelationProvider(RelationProvider relationProvider){
        mRelationsByProvider.remove(relationProvider);

        for(String providedRelation : relationProvider.getProvidedRelations()) {
            String creatorName = Util.eCreatorName(relationProvider, providedRelation);

            mRelationCreatorsRequirements.remove(creatorName);
            mRelationProviderByCreatorName.remove(creatorName);

            for (String service : relationProvider.getPotentiallyProvidedRelationServices(providedRelation)) {
                if (mRelationCreatorsByService.containsKey(service)) {
                    mRelationCreatorsByService.get(service).remove(creatorName);
                }
            }
        }

        if(ContextManagerAdmin.getLogLevel()>=1) {
            LOG.info("PROVIDER REMOVED: "+ relationProvider.getName());
        }
    }

    @Override
    public Set<String> getInstancesByCreator(String creator) {
        Set<String> result = new HashSet<>();

        if(mEntityProviderByCreatorName.containsKey(creator)){
            result.addAll(mEntityProviderByCreatorName.get(creator).getInstances(Util.getProvidedItemFromCreatorName(creator),true));
        }

        if(mRelationProviderByCreatorName.containsKey(creator)){
            result.addAll(mRelationProviderByCreatorName.get(creator).getInstances(Util.getProvidedItemFromCreatorName(creator),true));
        }

        return result;
    }
}
