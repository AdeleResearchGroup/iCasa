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

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIAppRegistration;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfigs;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * TEMP
 * Classe principale du gestionnaire de contexte
 */
@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@CommandProvider(namespace = "AM-ctxt")
public class ContextManager implements ContextAPIAppRegistration{

    private static final Logger LOG = LoggerFactory.getLogger(ContextManager.class);

    /*Log level (debug) 0: nothing, 3: all*/
    private static final int logLevelMax = 3;
    public static int logLevel = 2;


    /*Thread management*/
    private static ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture scheduledFuture = null;

    private static long delay = 10L;
    private static TimeUnit timeUnit = TimeUnit.SECONDS;

    /*Goal management*/
    private static Map<String, ContextAPIConfigs> contextGoalMap = new HashMap<>();

    /*TODO: les appels à RoSe doivent être faits ici, ou dans un composant specifique (pas dans contextInternalManager)*/
    /*Lookup IOP Controller*/
    @Requires(optional = true)
    public IOPLookupService iopLookupService;

    /*Lookup mode*/
    public static boolean autoLookup = false;
    public static Set<String> lookupFilter = new HashSet<>();

    /*Management sub-parts*/
    @Requires(optional = false)
    private ContextInternalManager contextInternalManager;

    /*Resolution machine : calcule et effectue l'adaptation*/
    private Runnable resolutionMachine;

    /*Adaptation*/
    private Runnable contextCompositionAdaptation = () -> {
        /*Passage de l'etat du contexte à un instant t*/
        contextInternalManager.configureGoals(ContextManager.contextGoalMap);
        /*Adaptation*/
        resolutionMachine.run();
        /*Lookup filter*/
        modifyLookupFilter(contextInternalManager.getCurrentLookupFilter());
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

    private void modifyLookupFilter(Set<String> filter) {
        if(autoLookup) {
            Set<String> toConsider = new HashSet<>();
            Set<String> toDiscard  = new HashSet<>();

            /*To consider list*/
            for (String service : filter) {
                if (!lookupFilter.contains(service)) {
                    toConsider.add(service);
                }
            }
            lookupFilter.addAll(toConsider);

            /*To discard list*/
            for (String service : lookupFilter) {
                if (!filter.contains(service)) {
                    toDiscard.add(service);
                }
            }
            lookupFilter.removeAll(toDiscard);

            /*Environment lookup*/
            if (iopLookupService != null) {
                if(!toConsider.isEmpty()){
                    String[] c = new String[toConsider.size()];
                    c = toConsider.toArray(c);
                    iopLookupService.consider(c);
                }

                if(!toDiscard.isEmpty()){
                    String[] d = new String[toDiscard.size()];
                    d = toDiscard.toArray(d);
                    iopLookupService.discard(d);
                }

                if(logLevel>=3) {
                    LOG.info("AUTO LOOKUP FILTER CONSIDER: " + toConsider);
                    LOG.info("AUTO LOOKUP FILTER DISCARD: " + toDiscard);
                }
                if(logLevel>=1) {
                    LOG.info("AUTO LOOKUP FILTER: " + lookupFilter);
                }
            }

        }
    }

    @Command
    public void ctxtAmLogLevel(int level){
        if(level<0) level = 0;
        if(level>logLevelMax) level = logLevelMax;
        logLevel = level;
    }

    @Command
    public void ctxtAmAutoLookup(){
        autoLookup = true;
    }

    @Command
    public void ctxtAmManualLookup(){
        autoLookup = false;
    }
}