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

import fr.liglab.adele.icasa.context.manager.api.config.ContextManagerAdmin;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextDependencyRegistration;
import fr.liglab.adele.icasa.context.manager.api.models.goals.GoalModelAccess;
import fr.liglab.adele.icasa.context.manager.api.models.goals.GoalModelListener;
import fr.liglab.adele.icasa.context.manager.api.config.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.impl.models.api.GoalModelUpdate;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component (immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class GoalModel implements GoalModelAccess, GoalModelUpdate, ContextDependencyRegistration {
    /*LOG*/
    private static final Logger LOG = LoggerFactory.getLogger(GoalModel.class);
    private static final String LOG_PREFIX = "GOAL MODEL - ";

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

    /*ToDo no event for now use this*/
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

    /*CONTEXT DEPENDENCIES REGISTRATION*/
    @Override
    public synchronized boolean registerContextDependencies(String id, ContextAPIConfig contextAPIConfig) {
        boolean results = true;
        try{
            /*Modifying goal by app map*/
            contextGoalsByApp.put(id, contextAPIConfig);
            if(ContextManagerAdmin.getLogLevel()>=2) {
                LOG.info(LOG_PREFIX + "APP REGISTERING : " + id);
                LOG.info(LOG_PREFIX + "GOALS : " + contextAPIConfig.getConfig().toString());
            }

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
            /*Modifying goal by app map*/
            contextGoalsByApp.remove(id);
            if(ContextManagerAdmin.getLogLevel()>=2) {
                LOG.info(LOG_PREFIX + "APP UNREGISTERING : " + id);
            }

            notifyGoalSetChange();
        } catch (NullPointerException ne){
            results = false;
        }
        return results;
    }

    /*GOAL MODEL UPDATE*/
    @Override
    public synchronized void setContextGoalsActivability(Map<ContextAPIEnum, Boolean> contextGoalsActivability) {
        GoalModel.contextGoalsActivability.clear();
        for (ContextAPIEnum goal : getGoals()) {
            GoalModel.contextGoalsActivability.put(goal, Boolean.TRUE.equals(contextGoalsActivability.get(goal)));
        }
    }

    @Override
    public synchronized void setContextGoalActivability(ContextAPIEnum goal, Boolean activable) {
        if(getGoals().contains(goal)) {
            contextGoalsActivability.put(goal, Boolean.TRUE.equals(activable));
        } else {
            contextGoalsActivability.remove(goal);
        }
    }


    /*GOAL MODEL ACCESS*/
    @Override
    public Set<String> getManagedApps() {
        return Collections.unmodifiableSet(contextGoalsByApp.keySet());
    }

    @Override
    public Set<ContextAPIEnum> getGoals() {
        Set<ContextAPIEnum> goals = new HashSet<>();
        for (ContextAPIConfig contextAPIConfigs : contextGoalsByApp.values()) {
            goals.addAll(contextAPIConfigs.getConfig());
        }
        return Collections.unmodifiableSet(goals);
    }

    @Override
    public Map<String, ContextAPIConfig> getGoalsByApp() {
        return Collections.unmodifiableMap(contextGoalsByApp);
    }

    @Override
    public Set<ContextAPIEnum> getGoalsForApp(String app) {
        if(getManagedApps().contains(app)){
            return Collections.unmodifiableSet(contextGoalsByApp.get(app).getConfig());
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    /*May be outdated*/
    public Map<ContextAPIEnum, Boolean> getGoalsActivability() {
        return Collections.unmodifiableMap(contextGoalsActivability);
    }

    @Override
    /*May be outdated*/
    public boolean getGoalActivability(ContextAPIEnum goal) {
        return contextGoalsActivability.getOrDefault(goal, false);
    }

    @Override
    public Map<ContextAPIEnum, Boolean> getGoalsState() {

        updateGoalStateSet();
        return Collections.unmodifiableMap(contextGoalsState);
    }

    @Override
    public Map<ContextAPIEnum, Boolean> getGoalsStateForApp(String app) {

        updateGoalStateSet();

        Map<ContextAPIEnum, Boolean> result = new HashMap<>();
        try {
            for (ContextAPIEnum goal : getGoalsForApp(app)) {
                result.put(goal, contextGoalsState.getOrDefault(goal, false));
            }
        } catch (NullPointerException ne){
            ne.printStackTrace();
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean getGoalState(ContextAPIEnum goal) {

        updateGoalState(goal);
        return contextGoalsState.getOrDefault(goal, false);
    }


    /*PRIVATE METHODS*/
    /*Update goals state*/
    private synchronized void updateGoalStateSet(){
        contextGoalsState.clear();
        for (ContextAPIEnum goal : getGoals()) {
            contextGoalsState.put(goal, (bundleContext.getServiceReference(goal.getInterfaceName()) != null));
        }
    }

    private synchronized void updateGoalState(ContextAPIEnum goal){
        if(getGoals().contains(goal)) {
            contextGoalsState.put(goal, (bundleContext.getServiceReference(goal.getInterfaceName()) != null));
        } else {
            contextGoalsState.remove(goal);
        }
    }
}