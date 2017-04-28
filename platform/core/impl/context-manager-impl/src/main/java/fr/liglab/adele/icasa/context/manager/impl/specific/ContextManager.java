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
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Programmatic context manager main class
 */
@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@CommandProvider(namespace = "AM-ctxt")
public class ContextManager implements ContextAPIAppRegistration{

    private static final Logger LOG = LoggerFactory.getLogger(ContextManager.class);

    /*DEBUG*/
    /*Log level - 0: nothing, 3: all*/
    private static final int logLevelMax = 3;
    static int logLevel = 2;

    /*CONFIGURATION (TEMPORARY)*/
    /*Starting configuration of the context model - mandatory goals*/
    /*(includes context components that are useful for RoSe*/
    private static final ContextAPIConfig startingGoalsConfig = new ContextAPIConfig(
            new HashSet<>(Collections.singletonList(ContextAPI.IOPController)));

    /*Environment lookup mode*/
    private static boolean autoLookup = false;

    /*Internal configuration mode*/
    private static final int MODE_SCHEDULED = 0;
    private static final int MODE_EVENT_DRIVEN = 1;
    private static final int internal_configuration_mode = MODE_SCHEDULED;

    /*Only scheduled mode - Thread management*/
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture scheduledFuture = null;
    private static long delay = 10L;
    private static TimeUnit timeUnit = TimeUnit.SECONDS;

    /*Event-driven mode*/
    private static ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();


    /*CONTEXT AM EXTERNAL INTERFACES*/
    /*Goal management*/
    private static Map<String, ContextAPIConfig> contextGoalMap = new HashMap<>();

    /*Environment filter - Lookup IOP Controller*/
    @Requires(optional = true)
    private IOPLookupService iopLookupService;

    /*Environment lookup filter*/
    private static Set<String> lookupFilter = new HashSet<>();


    /*CONTEXT AM INTERNAL INTERFACES*/
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
        registerContextGoals(ContextManager.class.toGenericString(), startingGoalsConfig);

        /*Start scheduling*/
        switch (internal_configuration_mode){
            case MODE_SCHEDULED:
                resolutionMachine = contextInternalManager.getContextResolutionMachine();
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
                        contextCompositionAdaptation, 0, delay, TimeUnit.SECONDS);
                break;
            case MODE_EVENT_DRIVEN:
                break;
        }


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
    public boolean registerContextGoals(String appId, ContextAPIConfig contextAPIConfig) {
        /*TODO CHECK GOALS*/
        contextGoalMap.put(appId, contextAPIConfig);
//        singleExecutorService.submit(contextCompositionAdaptation);
        /*TODO test*/
//        scheduledExecutorService.execute(contextCompositionAdaptation);
        return true;
    }

    @Override
    public ContextAPIConfig getRegisteredContextGoals(String appId) {
        return contextGoalMap.get(appId);
    }

    @Override
    public boolean unregisterContextGoals(String appId) {
        contextGoalMap.remove(appId);
        return true;
    }

    /*Environment interface*/
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

    /*Context manager debug interface*/
    @Command
    @SuppressWarnings("unused")
    public void ctxtAmLogLevel(int level){
        if(level<0) level = 0;
        if(level>logLevelMax) level = logLevelMax;
        logLevel = level;
    }

    @Command
    @SuppressWarnings("unused")
    public void ctxtAmAutoLookup(){
        autoLookup = true;
    }

    @Command
    @SuppressWarnings("unused")
    public void ctxtAmManualLookup(){
        autoLookup = false;
    }
}