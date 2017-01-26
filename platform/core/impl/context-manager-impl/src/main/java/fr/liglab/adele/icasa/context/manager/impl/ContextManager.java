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

package fr.liglab.adele.icasa.context.manager.impl;

import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.ContextGoal;
import fr.liglab.adele.icasa.context.manager.api.ContextGoalRegistration;
import org.apache.felix.ipojo.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * TEMP
 * Classe principale du gestionnaire de contexte
 */
@Component(immediate = true, publicFactory = false)
@Provides
@Instantiate
public class ContextManager implements ContextGoalRegistration {

    /*Thread management*/
    private static ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture scheduledFuture = null;

    private long delay = 10L;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /*Goal management*/
    private Map<String, ContextGoal> contextGoalMap = new HashMap<>();

    /*Managed elements*/
    @Requires(optional = true)
    private ContextEntity[] contextEntities;

    @Requires(optional = true)
    private EntityProvider[] entityProviders;

    @Requires(optional = true)
    private RelationProvider[] relationProviders;

    /*Adaptation*/
    private Runnable contextCompositionAdaptation = new Runnable() {
        /*Resolution machine : calcule et effectue l'adaptation*/
        private ContextResolutionMachine resolutionMachine = new ContextResolutionMachine();

        @Override
        public void run() {
            /*Passage de l'etat du contexte à un instant t*/
            resolutionMachine.configureState(
                    ContextManager.this.contextGoalMap,
                    ContextManager.this.contextEntities,
                    ContextManager.this.entityProviders,
                    ContextManager.this.relationProviders);
            /*Adaptation*/
            resolutionMachine.run();
        }
    };

    @Validate
    public void start(){
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(contextCompositionAdaptation, 0, delay, TimeUnit.SECONDS);
    }

    @Invalidate
    public void stop(){
        scheduledExecutorService.shutdown();
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
            this.timeUnit = timeUnit;
            this.delay = scheduledFuture.getDelay(this.timeUnit);
        }

        return modified;
    }

    @Override
    public boolean registerContextGoals(String appId, ContextGoal contextGoal) {
        contextGoalMap.put(appId,contextGoal);
        singleExecutorService.submit(contextCompositionAdaptation);
        /*TODO test*/
//        scheduledExecutorService.execute(contextCompositionAdaptation);
        return true;
    }

    @Override
    public ContextGoal getRegisteredContextGoals(String appId) {
        return contextGoalMap.get(appId);
    }

    @Override
    public boolean unregisterContextGoals(String appId) {
        contextGoalMap.remove(appId);
        return true;
    }
}