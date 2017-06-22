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

import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.ContextDependencyRegistration;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.GoalModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.GoalModelListener;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.impl.models.api.GoalModelUpdate;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component (immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class GoalModel implements GoalModelAccess, GoalModelUpdate, ContextDependencyRegistration {

    @Context
    private BundleContext bundleContext;

    /*GOALS*/
    private static Map<String, ContextAPIConfig> contextGoalsByApp = new HashMap<>();
    private static Map<ContextAPIEnum, Boolean> contextGoalsActivability = new HashMap<>();



    private static Map<ContextAPIEnum, Boolean> contextGoalsState = new HashMap<>();


    /*EVENT LISTENERS*/
    @Requires(optional = true)
    @SuppressWarnings("all")
    private GoalModelListener[] listeners;

    private void notifyGoalSetChange(){
        Set<ContextAPIEnum> goals = getGoals();
        for (GoalModelListener goalModelListener : listeners){
            goalModelListener.notifyGoalSetChange(goals);
        }
    }

    /*ToDo no event for now use this*/
    private void notifyGoalStateChange(ContextAPIEnum goal, Boolean state){
        for (GoalModelListener goalModelListener : listeners){
            goalModelListener.notifyGoalStateChange(goal, state);
        }
    }

    @Validate
    private void start(){
        /*Goal model initialization*/
    }

    /*CONTEXT DEPENDENCIES REGISTRATION*/
    @Override
    public synchronized boolean registerContextDependencies(String id, ContextAPIConfig contextAPIConfig) {
        boolean results = true;
        try{
            /*ToDo useful for event changes*/
//            Set<ContextAPIEnum> newConfig = contextAPIConfig.getConfig();
//            Set<ContextAPIEnum> oldConfig = Collections.emptySet();
//            if(contextGoalsByApp.containsKey(id)){
//                oldConfig = contextGoalsByApp.get(id).getConfig();
//            }
//
//            Set<ContextAPIEnum> addedContextAPI = new HashSet<>(newConfig);
//            Set<ContextAPIEnum> removedContextAPI = new HashSet<>(oldConfig);
//
//            addedContextAPI.removeAll(oldConfig);
//            removedContextAPI.removeAll(newConfig);


            /*Modifying goal by app map*/
            contextGoalsByApp.put(id, contextAPIConfig);
//            if(logLevel>=2) {
//                LOG.info("GOALS " + contextAPIConfigs.getConfig().toString());
//            }
            notifyGoalSetChange();
        } catch (NullPointerException ne){
            results = false;
        }
        return results;
    }

    @Override
    public ContextAPIConfig getRegisteredContextDependencies(String id) {
        if(id == null)
            return null;
        else
            return contextGoalsByApp.get(id);
    }

    @Override
    public synchronized boolean unregisterContextDependencies(String id) {
        boolean results = true;
        try{
            Set<ContextAPIEnum> oldConfig = contextGoalsByApp.get(id).getConfig();

            /*Modifying goal by app map*/
            contextGoalsByApp.remove(id);
//            if(logLevel>=2) {
//                LOG.info("GOALS REMOVED" + contextAPIConfigs.getConfig().toString());
//            }
            notifyGoalSetChange();
        } catch (NullPointerException ne){
            results = false;
        }
        return results;
    }


    /*GOAL MODEL ACCESS*/
    @Override
    public Set<String> getManagedApps() {
        return contextGoalsByApp.keySet();
    }

    @Override
    public Set<ContextAPIEnum> getGoals() {
        Set<ContextAPIEnum> goals = new HashSet<>();
        for (ContextAPIConfig contextAPIConfigs : contextGoalsByApp.values()) {
            goals.addAll(contextAPIConfigs.getConfig());
        }
        return goals;
    }

    @Override
    public Map<String, ContextAPIConfig> getGoalsByApp() {
        return new HashMap<>(contextGoalsByApp);
    }

    @Override
    public Map<ContextAPIEnum, Boolean> getContextGoalsActivability() {
        return new HashMap<>(contextGoalsActivability);
    }

    @Override
    public void setContextGoalsActivability(Map<ContextAPIEnum, Boolean> contextGoalsActivability) {
        GoalModel.contextGoalsActivability = new HashMap<>(contextGoalsActivability);
    }

    @Override
    public Map<ContextAPIEnum, Boolean> getGoalsState() {
        goalServicesAvailabilityCheck();

        return new HashMap<>(contextGoalsState);
    }

    @Override
    public Map<ContextAPIEnum, Boolean> getGoalsStateForApp(String app) {
        goalServicesAvailabilityCheck();

        Map<ContextAPIEnum, Boolean> result = new HashMap<>();
        try {
            ContextAPIConfig config = contextGoalsByApp.get(app);
            for (ContextAPIEnum goal : config.getConfig()) {
                result.put(goal, contextGoalsState.get(goal));
            }
        } catch (NullPointerException ne){
            ne.printStackTrace();
        }
        return result;
    }

    @Override
    public Boolean getGoalState(ContextAPIEnum goal) {
        return contextGoalsState.get(goal);
    }

    /*GOAL MODEL STATE UPDATE*/
    /*ToDo Event update*/
    private void goalServicesAvailabilityCheck(){
        /*Vérification*/
        contextGoalsState = new HashMap<>();
        /*Services à activer*/
        for (ContextAPIEnum contextAPI : getGoals()) {
            contextGoalsState.put(contextAPI,
                    (bundleContext.getServiceReference(contextAPI.getInterfaceName()) != null));
        }
    }
}