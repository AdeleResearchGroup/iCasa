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
package fr.liglab.adele.icasa.service.scheduler.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import org.apache.felix.ipojo.annotations.*;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.clockservice.ClockListener;

import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.service.scheduler.ScheduledRunnable;

import org.wisdom.api.concurrent.ManagedScheduledExecutorService;


@Component(immediate = true)
@Instantiate
public class SchedulerTechnicalService implements ClockListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerTechnicalService.class);

    @Requires(optional=false, proxy=false)
    Clock clock;

    @Requires(filter = "(name=" + ManagedScheduledExecutorService.SYSTEM + ")", optional=false, proxy=false)
    ManagedScheduledExecutorService scheduler;

    private final Map<Long,VirtuallyScheduledTask<?>> tasks = new ConcurrentHashMap<>();

    @Bind(id = "PeriodicRunnable", aggregate = true, optional = true)
    public synchronized void bindPeriodicRunnable(PeriodicRunnable runnable,ServiceReference<?> serviceReference) {
        Long serviceRefId = (Long) serviceReference.getProperty(Constants.SERVICE_ID);
        LOGGER.info(" Scheduled Runnable " + serviceRefId);
        if (runnable.getPeriod() <= 0){
            LOGGER.error("Periodic Runnable with service Ref" + serviceRefId + " cannot be scheduled because period is equal to 0 or negative");
            return;
        }
        if (runnable.getUnit() == null){
            LOGGER.error("Periodic Runnable with service Ref" + serviceRefId + " cannot be scheduled because unit is null");
            return;
        }
        
        VirtuallyScheduledTask<PeriodicRunnable> task = new VirtuallyScheduledTask.Periodic(runnable);
        tasks.put(serviceRefId,task);
        
        if (clock != null && scheduler != null) {
        	task.activate(scheduler, clock);
        }
    }

    @Unbind(id = "PeriodicRunnable")
    public synchronized void unbindPeriodicRunnable(PeriodicRunnable runnable,ServiceReference<?> serviceReference) {
        Long serviceRefId = (Long) serviceReference.getProperty(Constants.SERVICE_ID);
        LOGGER.info(" UnScheduled Runnable " + serviceRefId);
        
        VirtuallyScheduledTask<?> task = tasks.remove(serviceRefId);
        if (task != null) {
        	task.deactivate();
        }
    }


    @Bind(id = "ScheduledRunnable",aggregate = true,optional = true)
    public synchronized void bindScheduledRunnable(ScheduledRunnable runnable,ServiceReference<?> serviceReference) {
        Long serviceRefId = (Long) serviceReference.getProperty(Constants.SERVICE_ID);
        LOGGER.info(" Scheduled Runnable " + serviceRefId);
        if (runnable.getExecutionDate() < 0){
            LOGGER.error("Scheduled Runnable with service Ref" + serviceRefId + " cannot be scheduled because execution date is negative");
            return;
        }
        
        VirtuallyScheduledTask<ScheduledRunnable> task = new VirtuallyScheduledTask.FixedDate(runnable);
        tasks.put(serviceRefId,task);
        
        if (clock != null && scheduler != null) {
        	task.activate(scheduler, clock);
        }
    }

    @Unbind(id = "ScheduledRunnable")
    public synchronized void unbindScheduledRunnable(ScheduledRunnable runnable,ServiceReference<?> serviceReference) {
        Long serviceRefId = (Long) serviceReference.getProperty(Constants.SERVICE_ID);
        LOGGER.info(" UnScheduled Runnable " + serviceRefId);
        
        VirtuallyScheduledTask<?> task = tasks.remove(serviceRefId);
        if (task != null) {
        	task.deactivate();
        }
    }

    @Validate
    public synchronized void validate() {
        
    	clock.addListener(this);
        
    	for (VirtuallyScheduledTask<?> task : tasks.values()) {
			task.activate(scheduler, clock);
		}
    }

    @Invalidate
    public synchronized void invalidate() {
        clock.removeListener(this);
        
    	for (VirtuallyScheduledTask<?> task : tasks.values()) {
			task.deactivate();
		}
    }

    @Override
    public synchronized void clockPaused() {
    	for (VirtuallyScheduledTask<?> task : tasks.values()) {
			task.pause(scheduler, clock);
		}
    }

    @Override
    public synchronized void clockResumed() {
    	for (VirtuallyScheduledTask<?> task : tasks.values()) {
			task.resume(scheduler, clock);
		}
    }

    @Override
    public synchronized void clockReset() {
    	for (VirtuallyScheduledTask<?> task : tasks.values()) {
			task.reset(scheduler, clock);
		}
    }

    @Override
    public synchronized void startDateModified(long oldStartDate) {
    	for (VirtuallyScheduledTask<?> task : tasks.values()) {
			task.reset(oldStartDate, scheduler, clock);
		}
    }

    @Override
    public synchronized void factorModified(int oldFactor) {
    	for (VirtuallyScheduledTask<?> task : tasks.values()) {
			task.accelerate(oldFactor, scheduler, clock);
		}
    }


}
