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
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfigs;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIAppRegistration;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;
import org.apache.felix.ipojo.annotations.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * TEMP
 * Classe principale du gestionnaire de contexte
 */
@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class ContextManager implements ContextAPIAppRegistration {

    /*Thread management*/
    private static ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture scheduledFuture = null;

    private static long delay = 10L;
    private static TimeUnit timeUnit = TimeUnit.SECONDS;

    /*Goal management*/
    private static Map<String, ContextAPIConfigs> contextGoalMap = new HashMap<>();

    /*Management sub-parts*/
    @Requires(optional = false)
    private ContextInternalManager contextInternalManager;

    /*Resolution machine : calcule et effectue l'adaptation*/
    private static Runnable resolutionMachine;

    /*Adaptation*/
    private Runnable contextCompositionAdaptation = () -> {
        /*Passage de l'etat du contexte à un instant t*/
        contextInternalManager.configureGoals(ContextManager.contextGoalMap);
        /*Adaptation*/
        resolutionMachine.run();
    };

    @Validate
    public void start(){
        /*TODO: REMOVE INITIAL CONFIG*/
        Set<ContextAPI> optimalConfig = new HashSet<>();
        optimalConfig.add(ContextAPI.IOPController);
        ContextAPIConfigs contextAPIConfigs = new ContextAPIConfigs(optimalConfig);
        registerContextGoals(this.getClass().toGenericString(), contextAPIConfigs);

        /*Start scheduling*/
        resolutionMachine = contextInternalManager.getContextResolutionMachine();
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(contextCompositionAdaptation, 0, delay, TimeUnit.SECONDS);
    }

    @Invalidate
    public void stop(){
        /*TODO bug de stop/start*/
        scheduledExecutorService.shutdown();
        singleExecutorService.shutdown();
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getDelay() {
        return delay;
    }

    public boolean setDelay(long delay, TimeUnit timeUnit){
        boolean modified = true;

        if(scheduledFuture != null){
            modified = scheduledFuture.cancel(false);
        }

        if(modified){
            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(contextCompositionAdaptation, 0, delay, timeUnit);
            ContextManager.timeUnit = timeUnit;
            ContextManager.delay = delay;
//            ContextManager.delay = scheduledFuture.getDelay(ContextManager.timeUnit);
        }

        return modified;
    }

    @Override
    public boolean registerContextGoals(String appId, ContextAPIConfigs contextAPIConfigs) {
        /*TODO CHECK GOALS*/
        contextGoalMap.put(appId, contextAPIConfigs);
//        singleExecutorService.submit(contextCompositionAdaptation);
        /*TODO test*/
//        scheduledExecutorService.execute(contextCompositionAdaptation);
        return true;
    }

    @Override
    public ContextAPIConfigs getRegisteredContextGoals(String appId) {
        return contextGoalMap.get(appId);
    }

    @Override
    public boolean unregisterContextGoals(String appId) {
        contextGoalMap.remove(appId);
        return true;
    }
}