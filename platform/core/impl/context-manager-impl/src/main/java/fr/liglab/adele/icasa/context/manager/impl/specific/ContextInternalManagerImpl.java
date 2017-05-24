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
package fr.liglab.adele.icasa.context.manager.impl.specific;

import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.generic.Util;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * TEMP
 */

@Component (immediate = true, publicFactory = false)
@Instantiate
@Provides
public class ContextInternalManagerImpl implements ContextInternalManager {

    private static final Logger LOG = LoggerFactory.getLogger(ContextInternalManagerImpl.class);

    /*Resolution machine : calcule et effectue l'adaptation*/
    private static LinkingLogic resolutionMachine;

    @Context
    private BundleContext bundleContext;

    /**/
    @Requires(optional = true)
    private ContextManager contextManager;

    /*Managed elements*/
    @Requires(optional = true)
    private ContextEntity[] contextEntities;

    @Requires(id = "entityProviders", optional = true)
    private EntityProvider[] entityProviders;

    @Requires(optional = true)
    private RelationProvider[] relationProviders;

    /*Maintained set and map for context resolution machine*/
    private Map<String, Set<String>> mEntityCreatorsByService = new HashMap<>();
    private Map<String, Set<String>> mEntityCreatorsRequirements = new HashMap<>();
    private Map<String, EntityProvider> mEntityProviderByCreatorName = new HashMap<>();
    private Map<EntityProvider, Set<String>> mEntitiesByProvider = new HashMap<>();

    private Map<String, Set<String>> mRelationCreatorsByService = new HashMap<>();
    private Map<String, Set<String>> mRelationCreatorsRequirements = new HashMap<>();
    private Map<String, RelationProvider> mRelationProviderByCreatorName = new HashMap<>();
    private Map<RelationProvider, Set<String>> mRelationsByProvider = new HashMap<>();

    private Map<String, ContextAPIConfig> contextGoalMap = new HashMap<>();
    private Set<String> lookupFilter = new HashSet<>();

    /*Initialization*/
    public ContextInternalManagerImpl(){
        resolutionMachine = new LinkingLogic(this);
    }

    @Override
    public void configureGoals(Map<String, ContextAPIConfig> contextGoalMap) {
        this.contextGoalMap = new HashMap<>(contextGoalMap);
    }

    @Override
    public LinkingLogic getContextResolutionMachine() {
        return resolutionMachine;
    }

    @Override
    public Set<String> getCurrentLookupFilter() {
        return lookupFilter;
    }

    BundleContext getBundleContext() {
        return bundleContext;
    }

    Set<ContextEntity> getContextEntities() {
        Set<ContextEntity> contextEntities;
        try{
            contextEntities  = new HashSet<>(Arrays.asList(this.contextEntities));
        } catch (NullPointerException ne){
            contextEntities = new HashSet<>();
        }
        return contextEntities;
    }

    Set<EntityProvider> getEntityProviders() {
        Set<EntityProvider> entityProviders;
        try{
            entityProviders  = new HashSet<>(Arrays.asList(this.entityProviders));
        } catch (NullPointerException ne){
            entityProviders = new HashSet<>();
        }
        return entityProviders;
    }

    Set<RelationProvider> getRelationProviders() {
        Set<RelationProvider> relationProviders;
        try{
            relationProviders  = new HashSet<>(Arrays.asList(this.relationProviders));
        } catch (NullPointerException ne){
            relationProviders = new HashSet<>();
        }
        return relationProviders;
    }

    Map<String, Set<String>> geteCreatorsByServices() {
        return mEntityCreatorsByService;
    }

    Map<String, Set<String>> geteCreatorsRequirements() {
        return mEntityCreatorsRequirements;
    }

    Map<String, EntityProvider> geteProviderByCreatorName() {
        return mEntityProviderByCreatorName;
    }

    Map<String, ContextAPIConfig> getContextGoalMap() {
        return contextGoalMap;
    }

    void setLookupFilter(Set<String> filter){
        lookupFilter = new HashSet<>(filter);
    }

    @Bind(id = "entityProviders")
    @SuppressWarnings("unused")
    private void bindEntityProvider(EntityProvider entityProvider){
        /*LogLevel*/
        int logLevel = ContextManager.logLevel;
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

    @Modified(id = "entityProviders")
    @SuppressWarnings("unused")
    private void modifyEntityProvider(EntityProvider entityProvider){
        /*LogLevel*/
        int logLevel = ContextManager.logLevel;
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

    @Unbind(id = "entityProviders")
    @SuppressWarnings("unused")
    private void unbindEntityProvider(EntityProvider entityProvider){
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

        if(ContextManager.logLevel>=1) {
            LOG.info("PROVIDER REMOVED: "+ entityProvider.getName());
        }
    }

    @Bind(id = "relationProviders")
    @SuppressWarnings("unused")
    private void bindRelationProvider(RelationProvider relationProvider){
        /*LogLevel*/
        int logLevel = ContextManager.logLevel;
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

    @Modified(id = "relationProviders")
    @SuppressWarnings("unused")
    private void modifyRelationProvider(RelationProvider relationProvider){
        /*LogLevel*/
        int logLevel = ContextManager.logLevel;
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

    @Unbind(id = "relationProviders")
    @SuppressWarnings("unused")
    private void unbindRelationProvider(RelationProvider relationProvider){
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

        if(ContextManager.logLevel>=1) {
            LOG.info("PROVIDER REMOVED: "+ relationProvider.getName());
        }
    }

    /*TODO MODIFY*/
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
