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
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfigs;
import fr.liglab.adele.icasa.context.manager.impl.generic.ContextMediationConfig;
import fr.liglab.adele.icasa.context.manager.impl.generic.ContextMediationSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * TEMP
 * Classe qui détermine la meilleure configuration avec les buts et l'environnement actuel
 */
final class ContextResolutionMachine implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ContextResolutionMachine.class);

    private static int i = 0;

    /*Copied state*/
    private Set<ContextAPIConfigs> contextAPIConfigsSet = new HashSet<>();
    private Set<ContextEntity> contextEntities = new HashSet<>();
    private Set<EntityProvider> entityProviders = new HashSet<>();
    private Set<RelationProvider> relationProviders = new HashSet<>();

    private Map<ContextAPI,ContextMediationConfig> contextMediationConfigMap = new ContextMediationConfigMap().getContextMediationConfigMap();

    Map<String, Set<String>> eCreatorsByServices = new HashMap<>();
    Map<String, Set<String>> eCreatorsRequirements = new HashMap<>();
    Map<String, EntityProvider> eProviderByName = new HashMap<>();

    @Override
    public void run() {
        /*Attention aux multiples acces*/
        /*TODO Recupérer les info nécessaires du manager*/
        LOG.info("CONTEXT RESOLUTION MACHINE - Execution " + i++);
        /*TODO UNCOMMENT*/
        resolutionAlgorithm();
    }

    public synchronized void configureState(Map<String, ContextAPIConfigs> contextGoalMap, ContextEntity[] contextEntities,
                                            EntityProvider[] entityProviders, RelationProvider[] relationProviders){
        this.contextAPIConfigsSet = new HashSet<>(contextGoalMap.values());
        try{
            this.contextEntities = new HashSet<>(Arrays.asList(contextEntities));
        } catch (NullPointerException ne){
            this.contextEntities = new HashSet<>();
        }
        try{
            this.entityProviders = new HashSet<>(Arrays.asList(entityProviders));
        } catch (NullPointerException ne){
            this.entityProviders = new HashSet<>();
        }
        try{
            this.relationProviders = new HashSet<>(Arrays.asList(relationProviders));
        } catch (NullPointerException ne){
            this.relationProviders = new HashSet<>();
        }
    }

    private synchronized void resolutionAlgorithm(){
        /*TODO Algorithme naze, à repenser*/

        /*TODO Ne pas refaire à chaque fois, maintenir autrement (bind, unbind)*/
        eCreatorsByServices = new HashMap<>();
        eCreatorsRequirements = new HashMap<>();

        for(EntityProvider entityProvider : entityProviders){
            for(String providedEntity : entityProvider.getProvidedEntities()) {
                String creatorName = eCreatorName(entityProvider, providedEntity);

                LOG.info("PROVIDER: "+ entityProvider.getName());
                LOG.info("ENTITY: "+ providedEntity);
                LOG.info("PROVIDING: "+ entityProvider.getPotentiallyProvidedEntityServices(providedEntity));
                LOG.info("WITH REQUIREMENTS: "+ entityProvider.getPotentiallyRequiredServices(providedEntity));

                eCreatorsRequirements.put(creatorName, entityProvider.getPotentiallyRequiredServices(providedEntity));

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

        /*App, set de services en config optimale*/
        /*TODO Sélectionner les configs ? (pour l'instant toutes les app traitées en même temps)*/
        Set<ContextAPI> goals = new HashSet<>();
        for(ContextAPIConfigs contextAPIConfigs : contextAPIConfigsSet){
            goals.addAll(contextAPIConfigs.getOptimalConfig());
            LOG.info("GOALS "+ contextAPIConfigs.getOptimalConfig().toString());
        }

        /*Services à activer*/
        Set<String> goalsName = new HashSet<>();
        for (ContextAPI contextAPI : goals){
            LOG.info("CONTEXT API TO ACTIVATE: "+contextAPI+ " named: "+contextAPI.getInterfaceName());
            goalsName.add(contextAPI.getInterfaceName());
        }

        /*Si activation possible --> activation, sinon autre config/query*/
        Set<String> creatorsToActivate = new HashSet<>();
        /*TODO TRAITER LA NON-ACTIVATION*/
        Set<String> nonActivableServices = new HashSet<>();
        Map<String,Set<String>> creatorsToActivateByProviderNames = new HashMap<>();

        if(recursiveActivation(goalsName, creatorsToActivate, nonActivableServices)){
            for(String creator: creatorsToActivate){
                String providerName = getEntityProviderFromCreatorName(creator);
                String entityName = getProvidedEntityFromCreatorName(creator);

                if (!creatorsToActivateByProviderNames.containsKey(providerName)) {
                    Set<String> providedEntitySubSet = new HashSet<>();
                    providedEntitySubSet.add(entityName);
                    creatorsToActivateByProviderNames.put(providerName, providedEntitySubSet);
                } else {
                    creatorsToActivateByProviderNames.get(providerName).add(entityName);
                }
            }
        } else {
            LOG.info("ERROR: MISSING PROVIDER");
        }

        /*Activation de la création des entities dans chaque provider*/
        for(EntityProvider entityProvider : entityProviders){
            if (creatorsToActivateByProviderNames.containsKey(entityProvider.getName())){
                Set<String> providedEntitySubSet = creatorsToActivateByProviderNames.get(entityProvider.getName());
                for(String providedEntity : entityProvider.getProvidedEntities()){
                    if(providedEntitySubSet.contains(providedEntity)){
                        entityProvider.enable(providedEntity);
                        LOG.info("PROVIDER "+entityProvider.getName()+" ENABLE ENTITY "+providedEntity);
                    } else {
                        entityProvider.disable(providedEntity);
                        LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE ENTITY "+providedEntity);
                    }
                }
            } else {
                for(String providedEntity : entityProvider.getProvidedEntities()){
                    entityProvider.disable(providedEntity);
                    LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE ENTITY "+providedEntity);
                }
            }
        }

        /*TODO Modifier (difficile de déduire les relations depuis le service -> requises par les implementations)*/
        /*TODO Dépend d'un service et d'une implem*/
        /*TODO Activer si les 2 sont présents*/
        /*Activation de toutes les relations*/
        for(RelationProvider relationProvider : relationProviders){
            for(String relation : relationProvider.getProvidedRelations()){
                relationProvider.enable(relation);
                LOG.info("PROVIDER "+relationProvider.getName()+" ENABLE RELATION "+relation);
            }
        }
    }

    private synchronized void resolutionAlgorithm_1(){
        /*Version 0, config en dur, pas de reflexion particulière*/

        /*App, set de services en config optimale*/
        Set<ContextAPI> goals = new HashSet<>();
        for(ContextAPIConfigs contextAPIConfigs : contextAPIConfigsSet){
            goals.addAll(contextAPIConfigs.getOptimalConfig());
            LOG.info("GOALS "+ contextAPIConfigs.getOptimalConfig().toString());
        }

        /*Entities à activer*/
        Set<AbstractContextEntities> entityActivationSet = new HashSet<>();
        Set<EnvProxy> proxyActivationSet = new HashSet<>();

        for (ContextAPI contextAPI : goals){
            LOG.info("CONTEXT API TO ACTIVATE: "+contextAPI);
            ContextMediationConfig contextMediationConfig = contextMediationConfigMap.get(contextAPI);
            if(contextMediationConfig !=null){
                ContextMediationSlice contextMediationSlice = contextMediationConfig.getOptimalContextMediationSlices();
                if(contextMediationSlice != null){
                    entityActivationSet.addAll(contextMediationSlice.getAbstractContextEntities());
                    proxyActivationSet.addAll(contextMediationSlice.getEnvProxies());
                    LOG.info("--> IN ACTIVATION LIST - ENTITY: "+ entityActivationSet +" - PROXY: "+ proxyActivationSet);
                }
            }
        }

        /*Activation de la création des entities dans chaque provider*/
        for(EntityProvider entityProvider : entityProviders){
            for(String implementation : entityProvider.getProvidedEntities()){
                LOG.info("PROVIDER "+implementation+" entities "+entityProvider.getInstances(implementation, true));

                AbstractContextEntities abstractContextEntities = AbstractContextEntities.containsImplem(implementation);
                if(abstractContextEntities != null){
                    if(entityActivationSet.contains(abstractContextEntities)){
                        entityProvider.enable(implementation);
                        LOG.info("PROVIDER "+entityProvider.getName()+" ENABLE ENTITY "+implementation);
                    } else {
                        entityProvider.disable(implementation);
                        LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE ENTITY "+implementation);
                    }
                }

                EnvProxy envProxy = EnvProxy.containsImplem(implementation);
                if(envProxy != null){
                    if(proxyActivationSet.contains(envProxy)){
                        entityProvider.enable(implementation);
                        LOG.info("PROVIDER "+entityProvider.getName()+" ENABLE PROXY "+implementation);
                    } else {
                        entityProvider.disable(implementation);
                        LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE PROXY "+implementation);
                    }
                }
            }
        }
    }

    private synchronized void resolutionAlgorithm_tests(){
        /*Test metadata providers*/
        for(EntityProvider entityProvider: entityProviders){
            LOG.info("ENTITY " + entityProvider.toString());
            try{
                Set<String> services = entityProvider.getPotentiallyProvidedEntityServices();
                LOG.info("SERVICES " + services.toString());
                Set<String> requiredServices = entityProvider.getPotentiallyRequiredServices();
                LOG.info("REQUIRES " + requiredServices.toString());
            } catch(Exception e){
                LOG.error("MEH. "+ e);
            } catch (Error e){
                LOG.error("ERR. "+ e);
            }
        }

        /*Verif config optimale*/
        Set<String> activatedEntityMap = new HashSet<>();
        for(ContextEntity contextEntity : contextEntities){
            for(String s : contextEntity.getServices()){
//                LOG.debug("CONTEXT ENTITY "+contextEntity.toString()+" SERVICE "+s);
//                if(goals.contains(s)){
////                    entityActivationSet.add(contextEntity.getClass().toString());
////                    LOG.debug("CONTEXT ENTITY TO ACTIVATE "+contextEntity.getClass().toString());
//                    entityActivationSet.add(contextEntity.toString());
//                    LOG.debug("--> TO ACTIVATE");
//                }
            }
        }
    }

    private String eCreatorName (EntityProvider entityProvider, String providedEntity){
        return entityProvider.getName() + ":" + providedEntity;
    }

    private String getProvidedEntityFromCreatorName (String eCreatorName){
        return eCreatorName.split(":")[1];
    }

    private String getEntityProviderFromCreatorName (String eCreatorName){
        return eCreatorName.split(":")[0];
    }

    private boolean recursiveActivation(Set<String> requiredServices, Set<String> creatorsToActivate, Set<String> nonActivableServices){
        Set<String> stepRequiredServices = new HashSet<>();
        /*TODO Traiter le cas où une branche n'a pas de require, mais c'est pas grave, parce qu'une autre branche est complète*/

        for(String requiredService : requiredServices){
            LOG.info("REQUIRED SERVICE: "+requiredService);
            /*Find corresponding creators*/
            Set<String> creators = eCreatorsByServices.get(requiredService);
            if(creators == null){
                LOG.info("NO CREATORS FOUND");
                nonActivableServices.add(requiredService);
                return false;
            }
            creatorsToActivate.addAll(creators);
            LOG.info("CREATORS FOUND : "+creators);
            for(String creator : creators){
                Set<String> reqSet = eCreatorsRequirements.get(creator);
                if (reqSet != null){
                    stepRequiredServices.addAll(reqSet);
                }
            }
            LOG.info("ASSOCIATED REQUIRED SERVICES: "+stepRequiredServices);
        }

        return stepRequiredServices.isEmpty() || recursiveActivation(stepRequiredServices, creatorsToActivate, nonActivableServices);
    }
}