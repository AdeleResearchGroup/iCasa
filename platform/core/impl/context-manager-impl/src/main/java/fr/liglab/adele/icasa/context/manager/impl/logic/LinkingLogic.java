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
package fr.liglab.adele.icasa.context.manager.impl.logic;

import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextManagerAdmin;
import fr.liglab.adele.icasa.context.manager.api.generic.Util;
import fr.liglab.adele.icasa.context.manager.api.generic.models.CapabilityModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.ExternalFilterModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.LinkModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.GoalModelAccess;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.impl.models.api.ExternalFilterModelUpdate;
import fr.liglab.adele.icasa.context.manager.impl.models.api.GoalModelUpdate;
import fr.liglab.adele.icasa.context.manager.impl.models.api.LinkModelUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * Programmatic context resolution machine
 * Determine the best programmatic context configuration to meet app needs, regarding the current external resources
 */
final class LinkingLogic implements Runnable {
    /*LOG*/
    private static final Logger LOG = LoggerFactory.getLogger(LinkingLogic.class);
    private static final String LOG_PREFIX = "LINKING LOGIC - ";
    private static int executionNumber = 0;


    /*MODELS*/
    private static GoalModelAccess goalModelAccess;
    private static GoalModelUpdate goalModelUpdate;
    private static CapabilityModelAccess capabilityModelAccess;
    private static LinkModelAccess linkModelAccess;
    private static LinkModelUpdate linkModelUpdate;
    private static ExternalFilterModelAccess externalFilterModelAccess;
    private static ExternalFilterModelUpdate externalFilterModelUpdate;


    /*Internal models*/ /*Check information consistency*/
    private Map<String, Set<String>> eCreatorsByServices = new HashMap<>();
    private Map<String, Set<String>> eCreatorsRequirements = new HashMap<>();
    private Map<String, EntityProvider> eProviderByCreatorName = new HashMap<>();
    private Set<EntityProvider> entityProviders = new HashSet<>();


    /*Constructor: binds to models*/
    protected LinkingLogic(GoalModelAccess goalModelAccess, GoalModelUpdate goalModelUpdate,
                           CapabilityModelAccess capabilityModelAccess,
                           LinkModelAccess linkModelAccess, LinkModelUpdate linkModelUpdate,
                           ExternalFilterModelAccess externalFilterModelAccess, ExternalFilterModelUpdate externalFilterModelUpdate){

        LinkingLogic.goalModelAccess = goalModelAccess;
        LinkingLogic.goalModelUpdate = goalModelUpdate;
        LinkingLogic.capabilityModelAccess = capabilityModelAccess;
        LinkingLogic.linkModelAccess = linkModelAccess;
        LinkingLogic.linkModelUpdate = linkModelUpdate;
        LinkingLogic.externalFilterModelAccess = externalFilterModelAccess;
        LinkingLogic.externalFilterModelUpdate = externalFilterModelUpdate;
    }

    @Override
    public void run() {
        /*TODO Check multiple accesses and coordination*/
        if(ContextManagerAdmin.getLogLevel()>=1) {
            LOG.info(LOG_PREFIX + "Execution " + executionNumber++);
        }

        /*Context adaptation*/
        resolutionAlgorithm();
    }

    /*ToDo trigger on events (for now it doesn't work because of interlocked callbacks)*/
    private synchronized void resolutionAlgorithm() {
        /*ToDo CHANGE THAT!!!*/
        enableAllRelations();

        /*Initialization*/
        mediationTreeInitializationWithGoals();

        /*Update models accessible from components*/
        updateExternalProviderModels();

        /*Tree calculation*/
        buildMediationTree();

        /*Activation calculation by goal*/
        mediationCalculation();

        /*Activation by creator*/
        enablingCreators();

        /*Verification*/
        goalServicesAvailabilityCheck();
    }

    private void enableAllRelations(){
        /*Update LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();

        Set<RelationProvider> relationProviders = capabilityModelAccess.getRelationProviders();

        /*ToDO CHANGE THAT*/
        /*Depends on 1 service + 1 implem - activate only if this 2 are present?*/
        /*Difficult to know from service*/

        /*Activate all relations*/
        for(RelationProvider relationProvider : relationProviders){
            for(String relation : relationProvider.getProvidedRelations()){
                relationProvider.enable(relation);
                if(logLevel>=3) {
                    LOG.info(LOG_PREFIX + "PROVIDER "+relationProvider.getName()+" ENABLE RELATION "+relation);
                }
            }
        }
    }

    private void mediationTreeInitializationWithGoals(){
        /*Update LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();

        /*Services to activate*/
        Map<String, DefaultMutableTreeNode> mediationTrees = new HashMap<>();
        for (ContextAPIEnum goal : goalModelAccess.getGoals()) {
            String goalName = goal.getInterfaceName();
            if(logLevel>=2) {
                LOG.info(LOG_PREFIX + "GOAL : " + goal.getInterfaceName());
            }

            DefaultMutableTreeNode goalNode = new DefaultMutableTreeNode(goalName);
            mediationTrees.put(goalName, goalNode);
        }
        linkModelUpdate.setMediationTrees(mediationTrees);
    }

    private void updateExternalProviderModels(){

        entityProviders = capabilityModelAccess.getEntityProviders();
        eCreatorsByServices = capabilityModelAccess.getEntityCreatorsByService();
        eCreatorsRequirements = capabilityModelAccess.getEntityCreatorsRequirements();
        eProviderByCreatorName = capabilityModelAccess.getEntityProviderByCreatorName();
    }

    private void buildMediationTree(){
        Set<String> nonActivableServices = new HashSet<>();

        /*Mediation tree build*/
        Set<String> lookupFilter = new HashSet<>();
        boolean mediationTreesOk = recursivelyBuildMediationTree(
                new HashSet<>(linkModelAccess.getMediationTrees().values()), nonActivableServices, lookupFilter);
        linkModelUpdate.setMediationTreesOk(mediationTreesOk);
        linkModelUpdate.setNonActivableServices(nonActivableServices);

        /*External request adaptation*/
        externalFilterModelUpdate.setLookupFilter(lookupFilter);

        if(!mediationTreesOk){
            if(ContextManagerAdmin.getLogLevel()>=2) {
                /*Provided services are always linked to a creator to be available*/
                /*ToDo Erase that branch instead of stopping everything?*/
                LOG.info(LOG_PREFIX + "ERROR: MISSING PROVIDER");

            }
        }
    }

    private boolean recursivelyBuildMediationTree(Set<DefaultMutableTreeNode> requiredNodes, Set<String> servicesWithoutCreator, Set<String> lookupFilter){
        /*Update LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();

        Set<DefaultMutableTreeNode> stepRequiredNodes = new HashSet<>();

        for(DefaultMutableTreeNode requiredNode : requiredNodes){
            String requiredService = (String) requiredNode.getUserObject();
            if(logLevel>=3) {
                LOG.info(LOG_PREFIX + "REQUIRED SERVICE: "+requiredService);
            }

            /*ToDo Change strategy for lookup filter?*/
            /*Add all services of the tree*/
            lookupFilter.add(requiredService);

            /*Find corresponding creators*/
            Set<String> creators = eCreatorsByServices.get(requiredService);
            if(creators == null){
                if(logLevel>=3) {
                    /*Services without creators at this point <=> non providable service*/
                    LOG.info(LOG_PREFIX + "NO CREATORS FOUND");
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
                        LOG.info(LOG_PREFIX + "CREATOR FOUND: "+creator);
                        LOG.info(LOG_PREFIX + "ASSOCIATED REQUIRED SERVICES: "+reqSet);
                    }
                }
            }
        }

        return stepRequiredNodes.isEmpty() || recursivelyBuildMediationTree(stepRequiredNodes, servicesWithoutCreator, lookupFilter);
    }

    private void mediationCalculation(){
        /*Update LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();
        /*Si activation possible --> activation*/
        /*Dans tous les cas query*/

        /*To activate by goal*/
        Set<String> creatorsToActivate = new HashSet<>();
        Set<String> nonActivableServices = linkModelAccess.getNonActivableServices();

        if(linkModelAccess.isMediationTreesOk()){
            Map<ContextAPIEnum, Boolean> goalsActivability = new HashMap<>();

            for (Map.Entry<String, DefaultMutableTreeNode> mediationTree : linkModelAccess.getMediationTrees().entrySet()) {
                String goalName = mediationTree.getKey();
                DefaultMutableTreeNode goalNode = mediationTree.getValue();
                if(logLevel>=3) {
                    if (goalNode != null) {
                        LOG.info(LOG_PREFIX + "TREE OK FOR GOAL: " + goalName);
                    } else {
                        LOG.info(LOG_PREFIX + "TREE KO FOR GOAL: " + goalName);
                    }
                }

                /*To activate for this goal*/
                Set<String> creatorsToActivateSubSet = new HashSet<>();

                boolean activable = mediationCalculationByGoal(goalNode, creatorsToActivateSubSet, nonActivableServices);
                if(activable){
                    if(logLevel>=2) {
                        LOG.info(LOG_PREFIX + "ACTIVATION POSSIBLE FOR GOAL: " + goalName);
                    }
                    creatorsToActivate.addAll(creatorsToActivateSubSet);
                } else {
                    if(logLevel>=2) {
                        LOG.info(LOG_PREFIX + "ACTIVATION NOT POSSIBLE FOR GOAL: " + goalName);
                    }
                    /*ToDo Modification of configuration? How to handle it?*/
                }
                /*ToDo*/
                try{
                    goalsActivability.put(ContextAPIEnum.containsInterface(goalName), activable);
                } catch(NullPointerException ne){
                    ne.printStackTrace();
                }
            }
            goalModelUpdate.setContextGoalsActivability(goalsActivability);
        } else {
            /*ToDo MODIFY*/
            Map<ContextAPIEnum, Boolean> goalsActivability = new HashMap<>();
            for(ContextAPIEnum contextAPIEnum : goalModelAccess.getGoals()){
                goalsActivability.put(contextAPIEnum, false);
            }
            goalModelUpdate.setContextGoalsActivability(goalsActivability);
        }

        linkModelUpdate.setCreatorsToActivate(creatorsToActivate);
    }

    private boolean mediationCalculationByGoal(DefaultMutableTreeNode goal, Set<String> creatorsToActivate, Set<String> nonActivableServices){
        /*Update LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();

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
                                LOG.info(LOG_PREFIX + "CREATOR TO ACTIVATE: "+creator);
                            }

                            if(entityProvider.getInstances(entityName, true).isEmpty()){
                                if(logLevel>=3) {
                                    LOG.info(LOG_PREFIX + "KO CREATOR: "+creator);
                                }
                            } else {
                                providableService = true;
                                if(logLevel>=3) {
                                    LOG.info(LOG_PREFIX + "OK CREATOR: "+creator);
                                }
                            }
                        } catch (NullPointerException ne){
                            if(logLevel>=3) {
                                LOG.info(LOG_PREFIX + "ERROR IN PROCESSING CREATOR: " +nextChild.toString());
                            }
                        }
                        nextChild = nextChild.getNextSibling();
                    }

                    if(!providableService){
                        /*ToDo Change startegy for lookup filter?*/
                        nonActivableServices.add(service.toString());
                        providableGoal = false;
                        if(logLevel>=2) {
                            LOG.info(LOG_PREFIX + "MISSING INSTANCES FOR SERVICE: " +service.toString());
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
                            LOG.info(LOG_PREFIX + "CREATOR TO ACTIVATE: "+creator);
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

    private void enablingCreators(){
        /*Update LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();

        Set<String> creatorsToActivate = linkModelAccess.getCreatorsToActivate();
        /*Activate creation for needed entities*/
        /*Deactivate others*/
        for(EntityProvider entityProvider : entityProviders){
            for(String providedEntity : entityProvider.getProvidedEntities()){
                String creatorName = Util.creatorName(entityProvider, providedEntity);
                if(creatorsToActivate.contains(creatorName)) {
                    entityProvider.enable(providedEntity);
                    if(logLevel>=3) {
                        LOG.info(LOG_PREFIX + "PROVIDER " + entityProvider.getName() + " ENABLE ENTITY " + providedEntity);
                    }
                } else {
                    entityProvider.disable(providedEntity);
                    if(logLevel>=3) {
                        LOG.info(LOG_PREFIX + "PROVIDER " + entityProvider.getName() + " DISABLE ENTITY " + providedEntity);
                    }
                }
            }
        }
    }

    private void goalServicesAvailabilityCheck(){
        /*Update LogLevel*/
        int logLevel = ContextManagerAdmin.getLogLevel();

        /*Verification*/
        /*Check availability of requested goals*/
        for (Map.Entry<ContextAPIEnum, Boolean> goalState :goalModelAccess.getGoalsState().entrySet()){
            String goal = goalState.getKey().getInterfaceName();
            String state = goalState.getValue()?"PROVIDING":"MISSING";

            if(logLevel>=1) {
                LOG.info(LOG_PREFIX + state + " CONTEXT API: " + goal);
            }
        }
    }
}