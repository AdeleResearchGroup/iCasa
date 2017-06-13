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

import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextDependencyRegistration;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;
import org.apache.felix.ipojo.annotations.*;

import java.util.*;

@Component (immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class GoalModel implements ContextDependencyRegistration, GoalModelAccess {

    /*GOALS*/
    private static Map<String, ContextAPIConfig> contextGoalByApp = new HashMap<>();

    //TODO A VERIFIER / A COMPLETER (PARCEQUEFRANCHEMENTCESTPASASSEZREFLECHI)
    /*UNUSED*/
    private Map<UUID, Goal> goals = new HashMap<>();

    /*EVENT LISTENERS*/
    @Requires(optional = true)
    @SuppressWarnings("all")
    private GoalListener[] listeners;

    private void notifyGoalChange(){
        for (GoalListener goalListener : listeners){
            goalListener.notifyGoalChange(contextGoalByApp);
        }
    }

    @Validate
    private void start(){
        /*Goal model initialization*/
        for(ContextAPIEnum contextAPI : ContextAPIEnum.values()){
            /*TODO : MODIFY CONFIG*/
            Goal goal = new Goal(contextAPI,0);
            goals.put(goal.getId(), goal);
        }
    }

    /*CONTEXT DEPENDENCIES REGISTRATION*/
    @Override
    public synchronized boolean registerContextDependencies(String id, ContextAPIConfig contextAPIConfig) {
        boolean results = true;
        try{
            Set<ContextAPIEnum> newConfig = contextAPIConfig.getConfig();
            Set<ContextAPIEnum> oldConfig = Collections.emptySet();
            if(contextGoalByApp.containsKey(id)){
                oldConfig = contextGoalByApp.get(id).getConfig();
            }


            Set<ContextAPIEnum> addedContextAPI = new HashSet<>(newConfig);
            Set<ContextAPIEnum> removedContextAPI = new HashSet<>(oldConfig);

            addedContextAPI.removeAll(oldConfig);
            removedContextAPI.removeAll(newConfig);

            /*Modifying goal model*/
            for(Goal goal : goals.values()){
                if(removedContextAPI.contains(goal.getService())){
                    goal.removeRequiringApp(id);
                }
                if(addedContextAPI.contains(goal.getService())) {
                    goal.addRequiringApp(id);
                }
            }

            /*Modifying goal by app map*/
            contextGoalByApp.put(id, contextAPIConfig);
//            if(logLevel>=2) {
//                LOG.info("GOALS " + contextAPIConfigs.getConfig().toString());
//            }
            notifyGoalChange();
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
            return contextGoalByApp.get(id);
    }

    @Override
    public synchronized boolean unregisterContextDependencies(String id) {
        boolean results = true;
        try{
            Set<ContextAPIEnum> oldConfig = contextGoalByApp.get(id).getConfig();

            /*Modifying goal model*/
            for(Goal goal : goals.values()){
                if(oldConfig.contains(goal.getService())){
                    goal.removeRequiringApp(id);
                }
            }

            /*Modifying goal by app map*/
            contextGoalByApp.remove(id);
//            if(logLevel>=2) {
//                LOG.info("GOALS REMOVED" + contextAPIConfigs.getConfig().toString());
//            }
            notifyGoalChange();
        } catch (NullPointerException ne){
            results = false;
        }
        return results;
    }

    /*GOAL MODEL ACCESS*/
    @Override
    public Map<String, ContextAPIConfig> getGoalsByApp() {
        return new HashMap<>(contextGoalByApp);
    }

    @Override
    public Set<ContextAPIEnum> getGoals() {
        Set<ContextAPIEnum> goals = new HashSet<>();
        for (ContextAPIConfig contextAPIConfigs : contextGoalByApp.values()) {
            goals.addAll(contextAPIConfigs.getConfig());
        }
        return goals;
    }
}