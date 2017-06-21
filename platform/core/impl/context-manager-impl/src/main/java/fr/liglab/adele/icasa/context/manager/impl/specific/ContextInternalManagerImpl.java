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
import fr.liglab.adele.icasa.context.manager.api.generic.models.CapabilityModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.ExternalFilterModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.GoalModelListener;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.impl.generic.models.api.ExternalFilterModelUpdate;
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
public class ContextInternalManagerImpl implements ContextInternalManager, GoalModelListener {

    private static final Logger LOG = LoggerFactory.getLogger(ContextInternalManagerImpl.class);

    /*Resolution machine : calcule et effectue l'adaptation*/
    private static LinkingLogic resolutionMachine;

    @Context
    @SuppressWarnings("unused")
    private BundleContext bundleContext;


    /*MODELS*/
    /*Capability model(providers and creators)*/
    @Requires
    @SuppressWarnings("unused")
    private CapabilityModelAccess capabilityModelAccess;

//    @Requires
//    @SuppressWarnings("unused")
//    private CapabilityModelUpdate capabilityModelUpdate;

    /*Lookup filter*/
    @Requires
    @SuppressWarnings("unused")
    private ExternalFilterModelAccess externalFilterModelAccess;

    @Requires
    @SuppressWarnings("unused")
    private ExternalFilterModelUpdate externalFilterModelUpdate;


    /*Initialization*/
    public ContextInternalManagerImpl(){
        resolutionMachine = new LinkingLogic(this);
    }

    @Override
    public LinkingLogic getContextResolutionMachine() {
        return resolutionMachine;
    }

    BundleContext getBundleContext() {
        return bundleContext;
    }



    /*Goal model*/
    private Map<String, ContextAPIConfig> contextGoalMap = new HashMap<>();

    Map<String, ContextAPIConfig> getContextGoalMap() {
        return contextGoalMap;
    }

    /*ToDo react to event*/
    @Override
    public void notifyGoalSetChange(Set<ContextAPIEnum> goals) {
        /*TODO does not work on event: interlock problem*/
//        configureGoals(goals);
//        resolutionMachine.run();
    }

    @Override
    public void notifyGoalStateChange(ContextAPIEnum goal, Boolean state) {
        //DO NOTHING
    }

    /*ToDo REMOVE?*/
    @Override
    public void configureGoals(Map<String, ContextAPIConfig> contextGoalMap) {
        this.contextGoalMap = new HashMap<>(contextGoalMap);
    }


    /*External Filter Model*/
    void setLookupFilter(Set<String> filter){
        externalFilterModelUpdate.setLookupFilter(filter);
    }

    Set<EntityProvider> getEntityProviders(){
        return capabilityModelAccess.getEntityProviders();
    }

    Set<RelationProvider> getRelationProviders(){
        return capabilityModelAccess.getRelationProviders();
    }

    Map<String, Set<String>> geteCreatorsByServices() {
        return capabilityModelAccess.getmEntityCreatorsByService();
    }

    Map<String, Set<String>> geteCreatorsRequirements() {
        return capabilityModelAccess.getmEntityCreatorsRequirements();
    }

    Map<String, EntityProvider> geteProviderByCreatorName() {
        return capabilityModelAccess.getmEntityProviderByCreatorName();
    }

}
