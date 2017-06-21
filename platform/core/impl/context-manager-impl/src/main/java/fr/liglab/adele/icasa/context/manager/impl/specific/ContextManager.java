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

import fr.liglab.adele.icasa.context.manager.api.generic.ContextManagerAdmin;
import fr.liglab.adele.icasa.context.manager.api.generic.models.ExternalFilterModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.ContextDependencyRegistration;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.GoalModelAccess;
import fr.liglab.adele.icasa.context.manager.impl.generic.logic.ExternalInteractionsManager;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Programmatic context manager main class
 */
@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class ContextManager {

    //TODO Reaction on resource or model event

    private static final Logger LOG = LoggerFactory.getLogger(ContextManager.class);

    /*Only scheduled mode - Thread management*/
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture scheduledFuture = null;

    /*Event-driven mode*/
    private static ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();


    /*CONTEXT AM EXTERNAL INTERFACES*/
    /*Goal Model*/
    @Requires
    @SuppressWarnings("unused")
    private GoalModelAccess goalModel;

    /*Registration of context dependencies*/
    @Requires
    @SuppressWarnings("unused")
    private ContextDependencyRegistration contextDependencyRegistration;


    /*Lookup filter*/
    @Requires
    @SuppressWarnings("unused")
    private ExternalFilterModelAccess externalFilterModelAccess;

    @Requires
    @SuppressWarnings("unused")
    private ExternalInteractionsManager externalInteractionsManager;

    /*Environment filter - Lookup IOP Controller*/
    @Requires(optional = true)
    @SuppressWarnings("unused")
    private IOPLookupService iopLookupService;

    /*Environment lookup filter*/
    private static Set<String> lookupFilter = new HashSet<>();


    /*CONTEXT AM INTERNAL INTERFACES*/
    /*Management sub-parts*/
    @Requires
    @SuppressWarnings("unused")
    private ContextInternalManager contextInternalManager;

    /*Resolution machine : calcule et effectue l'adaptation*/
    private Runnable resolutionMachine;

    /*Adaptation*/
    private Runnable contextCompositionAdaptation = () -> {
        /*Passage de l'etat du contexte à un instant t*/
        contextInternalManager.configureGoals(goalModel.getGoalsByApp());
        /*Adaptation*/
        resolutionMachine.run();
        /*Lookup filter*/
        externalInteractionsManager.updateLookupFilter();
    };

    @Validate
    public void start(){
        /*Start scheduling*/
        switch (ContextManagerAdmin.INTERNAL_CONFIGURATION_MODE){
            case ContextManagerAdmin.MODE_SCHEDULED:
                resolutionMachine = contextInternalManager.getContextResolutionMachine();
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
                        contextCompositionAdaptation, 0, ContextManagerAdmin.getDelay(), ContextManagerAdmin.getTimeUnit());
                break;
            case ContextManagerAdmin.MODE_EVENT_DRIVEN:
                break;
        }
    }

    @Invalidate
    public void stop(){
        /*ToDo bug de stop/start*/
        scheduledExecutorService.shutdown();
        singleExecutorService.shutdown();
    }

    public boolean setDelay(long delay, TimeUnit timeUnit){
        boolean modified = true;

        if(scheduledFuture != null){
            modified = scheduledFuture.cancel(false);
        }

        if(modified){
            ContextManagerAdmin.setDelay(delay);
            ContextManagerAdmin.setTimeUnit(timeUnit);
            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(contextCompositionAdaptation, 0, ContextManagerAdmin.getDelay(), ContextManagerAdmin.getTimeUnit());
        }

        return modified;
    }
}