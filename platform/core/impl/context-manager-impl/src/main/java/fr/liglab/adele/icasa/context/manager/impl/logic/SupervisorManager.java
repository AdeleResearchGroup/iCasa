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

import fr.liglab.adele.icasa.context.manager.api.config.ContextManagerAdmin;
import fr.liglab.adele.icasa.context.manager.api.models.CapabilityModelAccess;
import fr.liglab.adele.icasa.context.manager.api.models.ExternalModelAccess;
import fr.liglab.adele.icasa.context.manager.api.models.TargetLinkModelAccess;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextDependencyRegistration;
import fr.liglab.adele.icasa.context.manager.api.models.goals.GoalModelAccess;
import fr.liglab.adele.icasa.context.manager.api.models.goals.GoalModelListener;
import fr.liglab.adele.icasa.context.manager.api.config.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.impl.models.api.ExternalModelUpdate;
import fr.liglab.adele.icasa.context.manager.impl.models.api.GoalModelUpdate;
import fr.liglab.adele.icasa.context.manager.impl.models.api.TargetLinkModelUpdate;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.*;

/**
 * Programmatic context manager main class
 */
@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class SupervisorManager implements GoalModelListener {
    /*LOG*/
    private static final Logger LOG = LoggerFactory.getLogger(SupervisorManager.class);

    /*SCHEDULING*/
    /*Only scheduled mode - Thread management*/
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture scheduledFuture = null;

    /*Event-driven mode*/
    private static ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();


    /*EXTERNAL MODELS*/
    /*Goal model - access*/
    @Requires
    @SuppressWarnings("unused")
    private GoalModelAccess goalModelAccess;

    /*Goal model - update*/
    @Requires
    @SuppressWarnings("unused")
    private GoalModelUpdate goalModelUpdate;

    /*Lookup filter model - access*/
    @Requires
    @SuppressWarnings("unused")
    private ExternalModelAccess externalModelAccess;

    /*Lookup filter model - update*/
    @Requires
    @SuppressWarnings("unused")
    private ExternalModelUpdate externalModelUpdate;


    /*INTERNAL MODELS*/
    /*Capability model - access*/
    @Requires
    @SuppressWarnings("unused")
    private CapabilityModelAccess capabilityModelAccess;

    /*Link model - access*/
    @Requires
    @SuppressWarnings("unused")
    private TargetLinkModelAccess targetLinkModelAccess;

    /*Link model - update*/
    @Requires
    @SuppressWarnings("unused")
    private TargetLinkModelUpdate targetLinkModelUpdate;


    /*EXTERNAL INTERFACES*/
    /*Applications - registration of context dependencies*/
    @Requires
    @SuppressWarnings("unused")
    private ContextDependencyRegistration contextDependencyRegistration;

    /*RoSe/X-Ware - Interaction with remote systems*/
    @Requires
    @SuppressWarnings("unused")
    private ExternalInteractionsManager externalInteractionsManager;


    /*INTERNAL INTERFACES*/
    /*Resolution machine : calcule et effectue l'adaptation*/
    private Runnable resolutionMachine;

    /*Adaptation*/
    private Runnable contextCompositionAdaptation = () -> {
        /*Adaptation*/
        resolutionMachine.run();
        /*Lookup filter*/
        externalInteractionsManager.updateLookupFilter();
    };


    @Validate
    public void start(){

        /*Initialization of algorithms sub-parts*/
        resolutionMachine = new LinkingLogic(goalModelAccess, goalModelUpdate,
                capabilityModelAccess,
                targetLinkModelAccess, targetLinkModelUpdate,
                externalModelAccess, externalModelUpdate);
        /*Start scheduling*/
        switch (ContextManagerAdmin.INTERNAL_CONFIGURATION_MODE){
            case ContextManagerAdmin.MODE_SCHEDULED:
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(contextCompositionAdaptation,
                        0, ContextManagerAdmin.getDelay(), ContextManagerAdmin.getTimeUnit());
                break;
            case ContextManagerAdmin.MODE_EVENT_DRIVEN:
                /*ToDo*/
                break;
        }
    }

    @Invalidate
    public void stop(){
        /*ToDo bug de stop/start*/
        scheduledExecutorService.shutdown();
        singleExecutorService.shutdown();
    }


    /*SCHEDULING CONFIGURATION*/
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

    /*EVENT HANDLER*/
    @Override
    public void notifyGoalSetChange(Set<ContextAPIEnum> goals) {
          /*ToDo does not work on event: interlock problem*/
          /*ToDo manage with schedule - try to avoid interlock*/
//        resolutionMachine.run();
    }

    @Override
    public void notifyGoalStateChange(ContextAPIEnum goal, Boolean state) {

    }

    /*ToDo CHECK GOALS AVAILABILITY REGULARLY*/
}