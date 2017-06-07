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

import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.generic.Util;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * Programmatic context resolution machine
 * Determine the best programmatic context configuration to meet app needs, regarding the current external resources
 */
final class LinkingLogic implements Runnable {
    /*Log*/
    private int logLevel = ContextManager.logLevel;
    private static final Logger LOG = LoggerFactory.getLogger(LinkingLogic.class);
    private static int executionNumber = 0;

    /*ContextInternalManagerImpl*/
    private static ContextInternalManagerImpl contextInternalManager;

    /*Models from context internal manager*/
    private Map<String, Set<String>> eCreatorsByServices = new HashMap<>();
    private Map<String, Set<String>> eCreatorsRequirements = new HashMap<>();
    private Map<String, EntityProvider> eProviderByCreatorName = new HashMap<>();
    private Set<EntityProvider> entityProviders = new HashSet<>();

    /*Models to context internal manager*/
    private Set<String> lookupFilter = new HashSet<>();



    /*Linking Logic models*/
    private Set<ContextAPIEnum> goals = new HashSet<>();
    private static Map<String, DefaultMutableTreeNode> mediationTrees = new HashMap<>();
    private Set<String> creatorsToActivate = new HashSet<>();
    private Set<String> nonActivableServices = new HashSet<>();
    private Set<String> nonActivableGoals = new HashSet<>();
    private boolean mediationTreesOk = false;



    /*Initialization*/
    protected LinkingLogic(ContextInternalManagerImpl contextInternalManager) {
        LinkingLogic.contextInternalManager = contextInternalManager;
    }

    @Override
    public void run() {
        /*TODO Check multiple accesses and coordination*/
        if(logLevel>=1) {
            LOG.info("CONTEXT RESOLUTION MACHINE - Execution " + executionNumber++);
        }
        /*Update LogLevel*/
        logLevel = ContextManager.logLevel;

        resolutionAlgorithm();
        contextInternalManager.setLookupFilter(lookupFilter);
    }

    private synchronized void resolutionAlgorithm() {
        /*Update goal model*/
        updateGoals();

        /*TO CHANGE*/
        enableAllRelations();

        /*Initialization*/
        mediationTreeInitializationWithGoals();

        /*Update models accessible from components*/
        updateExternalProviderModels();

        /*Tree calculation*/
        buildMediationTree();

        /*Activation calculation by goal*/
        mediationCalculation(nonActivableServices);

        /*Activation by creator*/
        enablingCreators(creatorsToActivate);

        /*Verification*/
        goalServicesAvailabilityCheck();
    }



    private void updateGoals(){
        /*App, set de services en config optimale*/
        /*TODO SELECTION DES CONFIGS D'APP ? */
        /*TODO (pour l'instant toutes les app traitées en même temps)*/
        Set<ContextAPIConfig> contextAPIConfigsSet = new HashSet<>(contextInternalManager.getContextGoalMap().values());
        goals = new HashSet<>();
        for (ContextAPIConfig contextAPIConfigs : contextAPIConfigsSet) {
            goals.addAll(contextAPIConfigs.getConfig());
            if(logLevel>=2) {
                LOG.info("GOALS " + contextAPIConfigs.getConfig().toString());
            }
        }
    }

    private void enableAllRelations(){
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
    }

    private void mediationTreeInitializationWithGoals(){
        /*Services to activate*/
        mediationTrees = new HashMap<>();
        for (ContextAPIEnum contextAPI : goals) {
            String goalName = contextAPI.getInterfaceName();
            if(logLevel>=2) {
                LOG.info("CONTEXT API TO ACTIVATE: " + contextAPI + " named: " + goalName);
            }

            DefaultMutableTreeNode goalNode = new DefaultMutableTreeNode(goalName);
            mediationTrees.put(goalName, goalNode);
        }
    }

    private void updateExternalProviderModels(){
        entityProviders = contextInternalManager.getEntityProviders();
        eCreatorsByServices = contextInternalManager.geteCreatorsByServices();
        eCreatorsRequirements = contextInternalManager.geteCreatorsRequirements();
        eProviderByCreatorName = contextInternalManager.geteProviderByCreatorName();
    }



    private void buildMediationTree(){
        nonActivableServices = new HashSet<>();

        /*Mediation tree build*/
        mediationTreesOk = recursivelyBuildMediationTree(new HashSet<>(mediationTrees.values()), nonActivableServices);

        if(!mediationTreesOk){
            if(logLevel>=2) {
                /*Provided services are always linked to a creator to be available*/
                /*TODO Erase that branch instead of stopping everything?*/
                LOG.info("ERROR: MISSING PROVIDER");

            }
        }
    }

    private boolean recursivelyBuildMediationTree(Set<DefaultMutableTreeNode> requiredNodes, Set<String> servicesWithoutCreator){
        /*Update LogLevel*/
        logLevel = ContextManager.logLevel;

        Set<DefaultMutableTreeNode> stepRequiredNodes = new HashSet<>();

        for(DefaultMutableTreeNode requiredNode : requiredNodes){
            String requiredService = (String) requiredNode.getUserObject();
            if(logLevel>=3) {
                LOG.info("REQUIRED SERVICE: "+requiredService);
            }

            /*TODO LOOKUP FILTER*/
            /*Add all services of the tree*/
            lookupFilter = new HashSet<>();
            lookupFilter.add(requiredService);

            /*Find corresponding creators*/
            Set<String> creators = eCreatorsByServices.get(requiredService);
            if(creators == null){
                if(logLevel>=3) {
                    /*Services without creators at this point <=> non providable service*/
                    LOG.info("NO CREATORS FOUND");
                }
                servicesWithoutCreator.add(requiredService);
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

        return stepRequiredNodes.isEmpty() || recursivelyBuildMediationTree(stepRequiredNodes, servicesWithoutCreator);
    }

    private void mediationCalculation(Set<String> nonActivableServices){
        /*Si activation possible --> activation*/
        /*Dans tous les cas query*/

        /*To activate by goal*/
        creatorsToActivate = new HashSet<>();

        if(mediationTreesOk){
            nonActivableGoals = new HashSet<>();

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

            /*To activate for this goal*/
                Set<String> creatorsToActivateSubSet = new HashSet<>();

                if(mediationCalculationByGoal(goalNode, creatorsToActivateSubSet, nonActivableServices)){
                    if(logLevel>=2) {
                        LOG.info("ACTIVATION POSSIBLE FOR GOAL: " + goalName);
                    }
                    creatorsToActivate.addAll(creatorsToActivateSubSet);
                } else {
                    if(logLevel>=2) {
                        LOG.info("ACTIVATION NOT POSSIBLE FOR GOAL: " + goalName);
                    }
                    nonActivableGoals.add(goalName);
                /*TODO : TRAITER LE CHANGEMENT DE CONFIG ET LES CAS OU LA CONFIG COMPLETE N'EST PAS RESOLUE*/
                }
            }
        }
//        return creatorsToActivate;
    }

    /*TODO PBM*/
    private boolean mediationCalculationByGoal(DefaultMutableTreeNode goal, Set<String> creatorsToActivate, Set<String> nonActivableServices){
        /*Update LogLevel*/
        logLevel = ContextManager.logLevel;

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
                            String entityName = Util.getProvidedItemFromCreatorName(creator);
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
                        /*TODO LOOKUP FILTER*/
                        nonActivableServices.add(service.toString());
                        providableGoal = false;
                        if(logLevel>=2) {
                            LOG.info("MISSING INSTANCES FOR SERVICE: " +service.toString());
                        }
                    }
                }
                nextLeaf = nextLeaf.getNextLeaf();
            }

            /*If providable goal*/
            if(!providableGoal) {
                creatorsToActivate = Collections.emptySet();
            } else {
                for(DefaultMutableTreeNode service : serviceSet){
                    /*Until service .isRoot()*/
                    DefaultMutableTreeNode nextParent = ((DefaultMutableTreeNode)service.getParent());
                    while(nextParent != null){
                        String creator = nextParent.toString();
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

    private void enablingCreators(Set<String> creatorsToActivate){
        /*Activation de la création des entities*/
        /*Désactivation de la création des entities dans les providers non traités*/
        for(EntityProvider entityProvider : entityProviders){
            for(String providedEntity : entityProvider.getProvidedEntities()){
                String creatorName = Util.eCreatorName(entityProvider, providedEntity);
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
    }

    private void goalServicesAvailabilityCheck(){
        /*Vérification*/
        /*Services à activer*/
        BundleContext bundleContext = contextInternalManager.getBundleContext();
        for (ContextAPIEnum contextAPI : goals) {
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
}