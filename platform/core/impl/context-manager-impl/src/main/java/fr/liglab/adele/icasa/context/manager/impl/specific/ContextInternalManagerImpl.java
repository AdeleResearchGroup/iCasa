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
    private static ContextResolutionMachine resolutionMachine;

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
    private Map<String, Set<String>> eCreatorsByServices = new HashMap<>();
    private Map<String, Set<String>> eCreatorsRequirements = new HashMap<>();
    private Map<String, EntityProvider> eProviderByCreatorName = new HashMap<>();
    private Map<String, ContextAPIConfig> contextGoalMap = new HashMap<>();
    private Set<String> lookupFilter = new HashSet<>();

    /*Initialization*/
    public ContextInternalManagerImpl(){
        resolutionMachine = new ContextResolutionMachine(this);
    }

    @Override
    public void configureGoals(Map<String, ContextAPIConfig> contextGoalMap) {
        this.contextGoalMap = new HashMap<>(contextGoalMap);
    }

    @Override
    public ContextResolutionMachine getContextResolutionMachine() {
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
        return eCreatorsByServices;
    }

    Map<String, Set<String>> geteCreatorsRequirements() {
        return eCreatorsRequirements;
    }

    Map<String, EntityProvider> geteProviderByCreatorName() {
        return eProviderByCreatorName;
    }

    Map<String, ContextAPIConfig> getContextGoalMap() {
        return contextGoalMap;
    }

    void setLookupFilter(Set<String> filter){
        lookupFilter = new HashSet<>(filter);
    }

    @Bind(id = "entityProviders")
    private void bindEntityProvider(EntityProvider entityProvider){
        /*LogLevel*/
        int logLevel = ContextManager.logLevel;
        if(logLevel>=1){
            LOG.info("PROVIDER ADDED: "+ entityProvider.getName());
        }

        for(String providedEntity : entityProvider.getProvidedEntities()) {
            String creatorName = resolutionMachine.eCreatorName(entityProvider, providedEntity);

            if(logLevel>=3){
                LOG.info("ENTITY: "+ providedEntity);
                LOG.info("PROVIDING: "+ entityProvider.getPotentiallyProvidedEntityServices(providedEntity));
                LOG.info("WITH REQUIREMENTS: "+ entityProvider.getPotentiallyRequiredServices(providedEntity));
            }

            eCreatorsRequirements.put(creatorName, entityProvider.getPotentiallyRequiredServices(providedEntity));
            eProviderByCreatorName.put(creatorName, entityProvider);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (!eCreatorsByServices.containsKey(service)) {
                    Set<String> entityProviderSubSet = new HashSet<>();
                    entityProviderSubSet.add(creatorName);
                    eCreatorsByServices.put(service, entityProviderSubSet);
                } else {
                    eCreatorsByServices.get(service).add(creatorName);
                }
            }
        }
    }

    @Modified(id = "entityProviders")
    private void modifyEntityProvider(EntityProvider entityProvider){
        /*LogLevel*/
        int logLevel = ContextManager.logLevel;
        if(logLevel>=1) {
            LOG.info("PROVIDER MODIFIED: "+ entityProvider.getName());
        }

        for(String providedEntity : entityProvider.getProvidedEntities()) {
            String creatorName = resolutionMachine.eCreatorName(entityProvider, providedEntity);

            if(!eProviderByCreatorName.containsKey(creatorName)){

                if(logLevel>=3) {
                    LOG.info("ENTITY: " + providedEntity);
                    LOG.info("PROVIDING: " + entityProvider.getPotentiallyProvidedEntityServices(providedEntity));
                    LOG.info("WITH REQUIREMENTS: " + entityProvider.getPotentiallyRequiredServices(providedEntity));
                }

                eCreatorsRequirements.put(creatorName, entityProvider.getPotentiallyRequiredServices(providedEntity));
                eProviderByCreatorName.put(creatorName, entityProvider);

                for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                    if (!eCreatorsByServices.containsKey(service)) {
                        Set<String> entityProviderSubSet = new HashSet<>();
                        entityProviderSubSet.add(creatorName);
                        eCreatorsByServices.put(service, entityProviderSubSet);
                    } else {
                        eCreatorsByServices.get(service).add(creatorName);
                    }
                }
            }
            /*TODO: does not manage creator deletion*/
        }
    }

    @Unbind
    private void unbindEntityProvider(EntityProvider entityProvider){
        /*TODO Might have been not tested*/
        for(String providedEntity : entityProvider.getProvidedEntities()) {
            String creatorName = resolutionMachine.eCreatorName(entityProvider, providedEntity);

            eCreatorsRequirements.remove(creatorName);
            eProviderByCreatorName.remove(creatorName);

            for (String service : entityProvider.getPotentiallyProvidedEntityServices(providedEntity)) {
                if (eCreatorsByServices.containsKey(service)) {
                    eCreatorsByServices.get(service).remove(creatorName);
                }
            }
        }

        if(ContextManager.logLevel>=1) {
            LOG.info("PROVIDER REMOVED: "+ entityProvider.getName());
        }
    }
}
