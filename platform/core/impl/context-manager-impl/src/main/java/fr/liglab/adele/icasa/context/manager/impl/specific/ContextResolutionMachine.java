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
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;
import fr.liglab.adele.icasa.context.manager.impl.temp.generic.ContextMediationConfig;
import fr.liglab.adele.icasa.context.manager.impl.temp.generic.ContextMediationSlice;
import fr.liglab.adele.icasa.context.manager.impl.temp.specific.AbstractContextEntities;
import fr.liglab.adele.icasa.context.manager.impl.temp.specific.ContextMediationConfigMap;
import fr.liglab.adele.icasa.context.manager.impl.temp.specific.EnvProxy;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * Programmatic context resolution machine
 * Determine the best programmatic context configuration to meet app needs, regarding the current external resources
 */
final class ContextResolutionMachine implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ContextResolutionMachine.class);

    private static int i = 0;

    /**/
    private static ContextInternalManagerImpl contextInternalManager;

    /*Trees*/
    private static Map<String, DefaultMutableTreeNode> mediationTrees = new HashMap<>();


        private Map<String, Set<String>> eCreatorsByServices = new HashMap<>();
    private Map<String, Set<String>> eCreatorsRequirements = new HashMap<>();
    private Map<String, EntityProvider> eProviderByCreatorName = new HashMap<>();
    private Set<String> lookupFilter = new HashSet<>();

    protected ContextResolutionMachine(ContextInternalManagerImpl contextInternalManager) {
        ContextResolutionMachine.contextInternalManager = contextInternalManager;
    }

    @Override
    public void run() {
        /*Attention aux multiples acces*/

        /*LogLevel*/
        int logLevel = ContextManager.logLevel;
        if(logLevel>=1) {
            LOG.info("CONTEXT RESOLUTION MACHINE - Execution " + i++);
        }

        resolutionAlgorithm();
        contextInternalManager.setLookupFilter(lookupFilter);
    }


    /*TODO Algorithme naze, à repenser*/
    private synchronized void resolutionAlgorithm() {

        /*LogLevel*/
        int logLevel = ContextManager.logLevel;


        /*App, set de services en config optimale*/
        /*TODO SELECTION DES CONFIGS D'APP ? */
        /*TODO (pour l'instant toutes les app traitées en même temps, avec la meilleure config)*/
        Set<ContextAPIConfig> contextAPIConfigsSet = new HashSet<>(contextInternalManager.getContextGoalMap().values());
        Set<ContextAPI> goals = new HashSet<>();
        for (ContextAPIConfig contextAPIConfigs : contextAPIConfigsSet) {
            goals.addAll(contextAPIConfigs.getConfig());
            if(logLevel>=2) {
                LOG.info("GOALS " + contextAPIConfigs.getConfig().toString());
            }
        }

        Set<RelationProvider> relationProviders = contextInternalManager.getRelationProviders();
        /*TODO Modifier (difficile de déduire les relations depuis le service -> requises par les implementations)*/
        /*TODO Dépend d'un service et d'une implem*/
        /*TODO Activer si les 2 sont présents*/
        /*Activation de toutes les relations*/
        for(RelationProvider relationProvider : relationProviders){
            for(String relation : relationProvider.getProvidedRelations()){
                relationProvider.enable(relation);
                if(logLevel>=3) {
                    LOG.info("PROVIDER "+relationProvider.getName()+" ENABLE RELATION "+relation);
                }
            }
        }

        /*Services à activer*/
        /*Initialisation des arbres*/
        mediationTrees = new HashMap<>();
        for (ContextAPI contextAPI : goals) {
            String goalName = contextAPI.getInterfaceName();
            if(logLevel>=2) {
                LOG.info("CONTEXT API TO ACTIVATE: " + contextAPI + " named: " + goalName);
            }

            DefaultMutableTreeNode goalNode = new DefaultMutableTreeNode(goalName);
            mediationTrees.put(goalName, goalNode);
        }

        /*Si activation possible --> activation, sinon autre config/query*/
        Set<String> creatorsToActivate = new HashSet<>();
        Set<String> nonActivableServices = new HashSet<>();
        Set<String> nonActivableGoals = new HashSet<>();

        Set<EntityProvider> entityProviders = contextInternalManager.getEntityProviders();
        eCreatorsByServices = contextInternalManager.geteCreatorsByServices();
        eCreatorsRequirements = contextInternalManager.geteCreatorsRequirements();
        eProviderByCreatorName = contextInternalManager.geteProviderByCreatorName();

        /*TODO*/
        lookupFilter = new HashSet<>();
        if (recursiveCreatorRegistrationTree(new HashSet<>(mediationTrees.values()), nonActivableServices)) {
            for (Map.Entry<String, DefaultMutableTreeNode> mediationTree : mediationTrees.entrySet()) {
                String goalName = mediationTree.getKey();
                DefaultMutableTreeNode goalNode = mediationTree.getValue();
                if(logLevel>=3) {
                    if (goalNode != null) {
                        LOG.info("TREE OK FOR GOAL: " + goalName);
                    } else {
                        LOG.info("TREE KO FOR GOAL: " + goalName);
                    }
                }


                Set<String> creatorsToActivateSubSet = new HashSet<>();
                if(creatorTreeActivation(goalNode, creatorsToActivateSubSet, nonActivableGoals)){
                    if(logLevel>=2) {
                        LOG.info("ACTIVATION POSSIBLE FOR GOAL: " + goalName);
                    }
                    creatorsToActivate.addAll(creatorsToActivateSubSet);
                } else {
                    if(logLevel>=2) {
                        LOG.info("ACTIVATION NOT POSSIBLE FOR GOAL: " + goalName);
                    }
                    /*TODO : TRAITER LE CHANGEMENT DE CONFIG ET LES CAS OU LA CONFIG COMPLETE N'EST PAS RESOLUE*/
                }
            }
        } else {
            if(logLevel>=2) {
                LOG.info("ERROR: MISSING PROVIDER");
                /*TODO : TRAITER les provider manquants*/
            }
        }

        /*Désactivation de la création des entities dans les providers non traités*/
        for(EntityProvider entityProvider : entityProviders){
            for(String providedEntity : entityProvider.getProvidedEntities()){
                String creatorName = eCreatorName(entityProvider, providedEntity);
                if(creatorsToActivate.contains(creatorName)) {
                    entityProvider.enable(providedEntity);
                    if(logLevel>=3) {
                        LOG.info("PROVIDER " + entityProvider.getName() + " ENABLE ENTITY " + providedEntity);
                    }
                } else {
                    entityProvider.disable(providedEntity);
                    if(logLevel>=3) {
                        LOG.info("PROVIDER " + entityProvider.getName() + " DISABLE ENTITY " + providedEntity);
                    }
                }
            }
        }

        /*Vérification*/
        /*Services à activer*/
        BundleContext bundleContext = contextInternalManager.getBundleContext();
        for (ContextAPI contextAPI : goals) {
            String contextAPIName = contextAPI.getInterfaceName();
            if (bundleContext.getServiceReference(contextAPIName) == null) {
                if(logLevel>=1) {
                    LOG.info("MISSING CONTEXT API: " + contextAPIName);
                }
            } else {
                if(logLevel>=1) {
                    LOG.info("PROVIDING CONTEXT API: " + contextAPIName);
                }
            }
        }
    }

    String eCreatorName(EntityProvider entityProvider, String providedEntity) {
        return entityProvider.getName() + ":" + providedEntity;
    }

    private String getProvidedEntityFromCreatorName(String eCreatorName) {
        return eCreatorName.split(":")[1];
    }

    private String getEntityProviderFromCreatorName(String eCreatorName) {
        return eCreatorName.split(":")[0];
    }

    private boolean recursiveCreatorRegistrationTree(Set<DefaultMutableTreeNode> requiredNodes, Set<String> nonActivableServices){
        /*LogLevel*/
        int logLevel = ContextManager.logLevel;

        Set<DefaultMutableTreeNode> stepRequiredNodes = new HashSet<>();

        for(DefaultMutableTreeNode requiredNode : requiredNodes){
            String requiredService = (String) requiredNode.getUserObject();
            if(logLevel>=3) {
                LOG.info("REQUIRED SERVICE: "+requiredService);
            }

            /*TODO ADD TO LOOKUP FILTER*/
            /*All services*/
            lookupFilter.add(requiredService);

            /*Find corresponding creators*/
            Set<String> creators = eCreatorsByServices.get(requiredService);
            if(creators == null){
                if(logLevel>=3) {
                    LOG.info("NO CREATORS FOUND");
                }

                nonActivableServices.add(requiredService);
                /*TODO REMOVE ?*/
                return false;
            } else {
                for(String creator : creators){
                    DefaultMutableTreeNode creatorNode = new DefaultMutableTreeNode(creator);
                    requiredNode.add(creatorNode);
                    Set<String> reqSet = eCreatorsRequirements.get(creator);
                    for(String req : reqSet){
                        DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode(req);
                        creatorNode.add(serviceNode);
                        stepRequiredNodes.add(serviceNode);
                    }

                    if(logLevel>=3) {
                        LOG.info("CREATOR FOUND: "+creator);
                        LOG.info("ASSOCIATED REQUIRED SERVICES: "+reqSet);
                    }
                }
            }
        }

        return stepRequiredNodes.isEmpty() || recursiveCreatorRegistrationTree(stepRequiredNodes, nonActivableServices);
    }

    private boolean creatorTreeActivation(DefaultMutableTreeNode goal, Set<String> creatorsToActivate, Set<String> nonActivableServices){
        /*LogLevel*/
        int logLevel = ContextManager.logLevel;

        if(goal.getDepth()>0){

            boolean providableGoal = true;
            Set<DefaultMutableTreeNode> serviceSet = new HashSet<>();

            DefaultMutableTreeNode nextLeaf = goal.getFirstLeaf();
            while((nextLeaf != null) && providableGoal) {
                DefaultMutableTreeNode service = ((DefaultMutableTreeNode)nextLeaf.getParent());
                if(!serviceSet.contains(service)){
                    serviceSet.add(service);

                    DefaultMutableTreeNode nextChild = (DefaultMutableTreeNode)service.getFirstChild();
                    boolean providableService = false;
                    while(nextChild != null){

                        try{
                            /*Pour l'instant, activation que des creators où il y a des instances disponibles*/
                            String creator = nextChild.toString();
                            String entityName = getProvidedEntityFromCreatorName(creator);
                            EntityProvider entityProvider = eProviderByCreatorName.get(creator);
                            creatorsToActivate.add(creator);

                            if(logLevel>=3) {
                                LOG.info("CREATOR TO ACTIVATE: "+creator);
                            }

                            if(entityProvider.getInstances(entityName, true).isEmpty()){
                                if(logLevel>=3) {
                                    LOG.info("KO CREATOR: "+creator);
                                }
                            } else {
                                providableService = true;
                                if(logLevel>=3) {
                                    LOG.info("OK CREATOR: "+creator);
                                }
                            }
                        } catch (NullPointerException ne){
                            if(logLevel>=3) {
                                LOG.info("ERROR IN PROCESSING CREATOR: " +nextChild.toString());
                            }
                        }

                        nextChild = nextChild.getNextSibling();
                    }

                    if(!providableService){
                        nonActivableServices.add(service.toString());
                        providableGoal = false;
                        if(logLevel>=2) {
                            LOG.info("MISSING INSTANCES FOR SERVICE: " +service.toString());
                        }
                    }
                }

                nextLeaf = nextLeaf.getNextLeaf();
            }

            /*TODO : activation à la fin*/
            /*TODO: enable le reste de la mediation si toutes les leafs sont disponibles ?*/
            if(!providableGoal) {
                creatorsToActivate = Collections.emptySet();
            } else {
                for(DefaultMutableTreeNode service : serviceSet){
                    /*TODO recursif jusqu'à ce que service .isRoot()*/
                    DefaultMutableTreeNode nextParent = ((DefaultMutableTreeNode)service.getParent());
                    while(nextParent != null){
                        String creator = nextParent.toString();
                        /*TODO VERIFY*/
                        creatorsToActivate.add(creator);

                        if(logLevel>=3) {
                            LOG.info("CREATOR TO ACTIVATE: "+creator);
                        }

                        nextParent = ((DefaultMutableTreeNode)nextParent.getParent());
                        if(nextParent != null){
                            nextParent = ((DefaultMutableTreeNode)nextParent.getParent());
                        }
                    }
                }
            }

            return providableGoal;
        }

        return false;
    }



    /*TODO OLD VERSION !!!*/
    /*TODO REMOVE !!!*/
//    private synchronized void resolutionAlgorithm_creator_activation_no_environment_check(){
//
//        /*Pas de détermination de faisabilité, verir API a la fin*/
//        /*Pas de traitement de la non-activation*/
//        /*Pas de distinction dans les branches :*/
//        /*Pas de traitement particulier dans le cas où une branche n'a pas de require, mais que ce n'est pas grave*/
//        /*parce qu'une autre branche est complète*/
//
//        /*App, set de services en config optimale*/
//        /*Toutes les app traitées en même temps, avec la meilleure config*/
//        Set<ContextAPIConfig> contextAPIConfigsSet = new HashSet<>(contextInternalManager.getContextGoalMap().values());
//        Set<ContextAPI> goals = new HashSet<>();
//        for(ContextAPIConfig contextAPIConfigs : contextAPIConfigsSet){
//            goals.addAll(contextAPIConfigs.getConfig());
//            LOG.info("GOALS "+ contextAPIConfigs.getConfig().toString());
//        }
//
//        /*Services à activer*/
//        Set<String> goalsName = new HashSet<>();
//        for (ContextAPI contextAPI : goals){
//            String goalName = contextAPI.getInterfaceName();
//            LOG.info("CONTEXT API TO ACTIVATE: "+contextAPI+" named: "+goalName);
//            goalsName.add(goalName);
//        }
//
//        /*Si activation possible --> activation, sinon autre config/query*/
//        Set<String> creatorsToActivate = new HashSet<>();
//        Set<String> nonActivableServices = new HashSet<>();
//        Map<String,Set<String>> creatorsToActivateByProviderNames = new HashMap<>();
//
//        Set<EntityProvider> entityProviders = contextInternalManager.getEntityProviders();
//        eCreatorsByServices = contextInternalManager.geteCreatorsByServices();
//        eCreatorsRequirements = contextInternalManager.geteCreatorsRequirements();
//
//
//        if(recursiveCreatorActivation(goalsName, creatorsToActivate, nonActivableServices)){
//            for(String creator: creatorsToActivate){
//                String providerName = getEntityProviderFromCreatorName(creator);
//                String entityName = getProvidedEntityFromCreatorName(creator);
//
//                if (!creatorsToActivateByProviderNames.containsKey(providerName)) {
//                    Set<String> providedEntitySubSet = new HashSet<>();
//                    providedEntitySubSet.add(entityName);
//                    creatorsToActivateByProviderNames.put(providerName, providedEntitySubSet);
//                } else {
//                    creatorsToActivateByProviderNames.get(providerName).add(entityName);
//                }
//            }
//        } else {
//            LOG.info("ERROR: MISSING PROVIDER");
//        }
//
//        /*Activation de la création des entities dans chaque provider*/
//        for(EntityProvider entityProvider : entityProviders){
//            if (creatorsToActivateByProviderNames.containsKey(entityProvider.getName())){
//                Set<String> providedEntitySubSet = creatorsToActivateByProviderNames.get(entityProvider.getName());
//                for(String providedEntity : entityProvider.getProvidedEntities()){
//                    if(providedEntitySubSet.contains(providedEntity)){
//                        entityProvider.enable(providedEntity);
//                        LOG.info("PROVIDER "+entityProvider.getName()+" ENABLE ENTITY "+providedEntity);
//                    } else {
//                        entityProvider.disable(providedEntity);
//                        LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE ENTITY "+providedEntity);
//                    }
//                }
//            } else {
//                for(String providedEntity : entityProvider.getProvidedEntities()){
//                    entityProvider.disable(providedEntity);
//                    LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE ENTITY "+providedEntity);
//                }
//            }
//        }
//
//        Set<RelationProvider> relationProviders = contextInternalManager.getRelationProviders();
//        /*Activation de toutes les relations*/
//        for(RelationProvider relationProvider : relationProviders){
//            for(String relation : relationProvider.getProvidedRelations()){
//                relationProvider.enable(relation);
//                LOG.info("PROVIDER "+relationProvider.getName()+" ENABLE RELATION "+relation);
//            }
//        }
//
//        /*Vérification des services à activer*/
//        BundleContext bundleContext = contextInternalManager.getBundleContext();
//        for (ContextAPI contextAPI : goals){
//            String contextAPIName = contextAPI.getInterfaceName();
//            if(bundleContext.getServiceReference(contextAPIName) == null){
//                LOG.info("MISSING CONTEXT API: " +contextAPIName);
//            } else {
//                LOG.info("PROVIDING CONTEXT API: " +contextAPIName);
//            }
//        }
//    }
//
//    private boolean recursiveCreatorActivation(Set<String> requiredServices, Set<String> creatorsToActivate, Set<String> nonActivableServices){
//        Set<String> stepRequiredServices = new HashSet<>();
//
//        for(String requiredService : requiredServices){
//            LOG.info("REQUIRED SERVICE: "+requiredService);
//            /*Find corresponding creators*/
//            Set<String> creators = eCreatorsByServices.get(requiredService);
//            if(creators == null){
//                LOG.info("NO CREATORS FOUND");
//                nonActivableServices.add(requiredService);
//            } else {
//                LOG.info("CREATORS FOUND : "+creators);
//                for(String creator : creators){
//                    creatorsToActivate.add(creator);
//                    Set<String> reqSet = eCreatorsRequirements.get(creator);
//                    if (reqSet != null){
//                        stepRequiredServices.addAll(reqSet);
//                    }
//                }
//                LOG.info("ASSOCIATED REQUIRED SERVICES: "+stepRequiredServices);
//            }
//        }
//
//        return stepRequiredServices.isEmpty() || recursiveCreatorActivation(stepRequiredServices, creatorsToActivate, nonActivableServices);
//    }
//
//
//    private Map<ContextAPI, ContextMediationConfig> contextMediationConfigMap = new ContextMediationConfigMap().getContextMediationConfigMap();
//    private synchronized void resolutionAlgorithm_config_creators(){
//        /*Version 0, config en dur, pas de reflexion particulière*/
//
//        /*App, set de services en config optimale*/
//        Set<ContextAPIConfig> contextAPIConfigsSet = new HashSet<>(contextInternalManager.getContextGoalMap().values());
//        Set<ContextAPI> goals = new HashSet<>();
//        for(ContextAPIConfig contextAPIConfigs : contextAPIConfigsSet){
//            goals.addAll(contextAPIConfigs.getConfig());
//            LOG.info("GOALS "+ contextAPIConfigs.getConfig().toString());
//        }
//
//        /*Entities à activer*/
//        Set<AbstractContextEntities> entityActivationSet = new HashSet<>();
//        Set<EnvProxy> proxyActivationSet = new HashSet<>();
//
//        for (ContextAPI contextAPI : goals){
//            LOG.info("CONTEXT API TO ACTIVATE: "+contextAPI);
//            ContextMediationConfig contextMediationConfig = contextMediationConfigMap.get(contextAPI);
//            if(contextMediationConfig !=null){
//                ContextMediationSlice contextMediationSlice = contextMediationConfig.getOptimalContextMediationSlices();
//                if(contextMediationSlice != null){
//                    entityActivationSet.addAll(contextMediationSlice.getAbstractContextEntities());
//                    proxyActivationSet.addAll(contextMediationSlice.getEnvProxies());
//                    LOG.info("--> IN ACTIVATION LIST - ENTITY: "+ entityActivationSet +" - PROXY: "+ proxyActivationSet);
//                }
//            }
//        }
//
//        Set<EntityProvider> entityProviders = contextInternalManager.getEntityProviders();
//        /*Activation de la création des entities dans chaque provider*/
//        for(EntityProvider entityProvider : entityProviders){
//            for(String implementation : entityProvider.getProvidedEntities()){
//                LOG.info("PROVIDER "+implementation+" entities "+entityProvider.getInstances(implementation, true));
//
//                AbstractContextEntities abstractContextEntities = AbstractContextEntities.containsImplem(implementation);
//                if(abstractContextEntities != null){
//                    if(entityActivationSet.contains(abstractContextEntities)){
//                        entityProvider.enable(implementation);
//                        LOG.info("PROVIDER "+entityProvider.getName()+" ENABLE ENTITY "+implementation);
//                    } else {
//                        entityProvider.disable(implementation);
//                        LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE ENTITY "+implementation);
//                    }
//                }
//
//                EnvProxy envProxy = EnvProxy.containsImplem(implementation);
//                if(envProxy != null){
//                    if(proxyActivationSet.contains(envProxy)){
//                        entityProvider.enable(implementation);
//                        LOG.info("PROVIDER "+entityProvider.getName()+" ENABLE PROXY "+implementation);
//                    } else {
//                        entityProvider.disable(implementation);
//                        LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE PROXY "+implementation);
//                    }
//                }
//            }
//        }
//    }
//
//    private synchronized void resolutionAlgorithm_tests(){
//        /*Test metadata providers*/
//        Set<EntityProvider> entityProviders = contextInternalManager.getEntityProviders();
//        for(EntityProvider entityProvider: entityProviders){
//            LOG.info("ENTITY " + entityProvider.toString());
//            try{
//                Set<String> services = entityProvider.getPotentiallyProvidedEntityServices();
//                LOG.info("SERVICES " + services.toString());
//                Set<String> requiredServices = entityProvider.getPotentiallyRequiredServices();
//                LOG.info("REQUIRES " + requiredServices.toString());
//            } catch(Exception e){
//                LOG.error("MEH. "+ e);
//            } catch (Error e){
//                LOG.error("ERR. "+ e);
//            }
//        }
//
//        /*Verif config optimale*/
//        Set<String> activatedEntityMap = new HashSet<>();
//
//        Set<ContextEntity> contextEntities = contextInternalManager.getContextEntities();
//        for(ContextEntity contextEntity : contextEntities){
//            for(String s : contextEntity.getServices()){
////                LOG.debug("CONTEXT ENTITY "+contextEntity.toString()+" SERVICE "+s);
////                if(goals.contains(s)){
//////                    entityActivationSet.add(contextEntity.getClass().toString());
//////                    LOG.debug("CONTEXT ENTITY TO ACTIVATE "+contextEntity.getClass().toString());
////                    entityActivationSet.add(contextEntity.toString());
////                    LOG.debug("--> TO ACTIVATE");
////                }
//            }
//        }
//    }
}