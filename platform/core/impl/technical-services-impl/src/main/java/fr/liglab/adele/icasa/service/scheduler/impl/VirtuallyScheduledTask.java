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

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.service.scheduler.ICasaRunnable;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.service.scheduler.ScheduledRunnable;

import java.util.concurrent.TimeUnit;

import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;


/**
 * A task that is scheduled using the virtual clock of the platform.
 * 
 * Unlike the real clock, the virtual clock can be suspended, resumed, accelerated or reset during the lifetime of
 * the task. Virtually scheduled task are executed by a real time counterpart, that is created/destroyed in response
 * to changes of the virtual clock.  
 */
public abstract class VirtuallyScheduledTask<R extends ICasaRunnable> {

	/**
	 * The runnable that will be executed by this virtual task 
	 */
	protected final R runnable;
	
	/**
	 * The real-time task associated to this virtual task
	 */
    protected ManagedScheduledFutureTask<?> task;

    
    protected VirtuallyScheduledTask(R runnable) {
    	this.runnable = runnable;
    }
    
    /**
     * Creates a new real task to execute the associated runnable using the specified scheduler and virtual clock
     */
    protected abstract ManagedScheduledFutureTask<?> schedule(ManagedScheduledExecutorService scheduler, Clock clock);

    /**
     * Associates a new real task with this execution
     */
    protected void bind(ManagedScheduledExecutorService scheduler, Clock clock) {
    	
		if (! clock.isPaused()) {
	    	task = schedule(scheduler,clock);
		}
    }

    /**
     * Destroys the real task when it is no longer valid 
     */
    protected void unbind() {
    	
    	ManagedScheduledFutureTask<?> terminated = task;
    	task = null;
    	
    	if (terminated != null) {
    		terminated.cancel(false);
    	}
    }

    /**
     * Reconfigure the real task when the clock is modified
     */
    protected void rebind(ManagedScheduledExecutorService scheduler, Clock clock) {
    	unbind();
    	bind(scheduler,clock);
    }
    
    
    /*
     * The task life-cycle methods associated to the clock  
     */
    
    public void activate(ManagedScheduledExecutorService scheduler, Clock clock) {
    	bind(scheduler,clock);
    }
    
    public void deactivate() {
		unbind();
    }

    public void pause(ManagedScheduledExecutorService scheduler, Clock clock) {
    	unbind();
    }

    public void resume(ManagedScheduledExecutorService scheduler, Clock clock) {
    	bind(scheduler,clock);
    }

    public void reset(ManagedScheduledExecutorService scheduler, Clock clock) {
    	rebind(scheduler,clock);
    }

    public void reset(long oldStartDate, ManagedScheduledExecutorService scheduler, Clock clock) {
    	rebind(scheduler,clock);
    }
    
    public void accelerate(int factor, ManagedScheduledExecutorService scheduler, Clock clock) {
    	rebind(scheduler,clock);
    }


    /**
     * The scheduled task associated with a periodic runnable
     */
    public static class Periodic extends VirtuallyScheduledTask<PeriodicRunnable> {

		public Periodic(PeriodicRunnable runnable) {
			super(runnable);
		}

		@Override
		protected ManagedScheduledFutureTask<?> schedule(ManagedScheduledExecutorService scheduler, Clock clock) {
			
			long period = getPeriod(clock);
			return scheduler.scheduleAtFixedRate(runnable, period, period, getUnit(clock));
		}

		/**
		 * The real time period that corresponds to the specified virtual period
		 */
		private long getPeriod(Clock clock) {
			
			/*
			 * convert to the finer granularity to avoid truncation when applying the factor
			 */
	        return TimeUnit.NANOSECONDS.convert(runnable.getPeriod(), runnable.getUnit()) / clock.getFactor();

		}

		/**
		 * The real time units of the period
		 */
		private TimeUnit getUnit(Clock clock) {
			return TimeUnit.NANOSECONDS;
		}
	}
    
    /**
     * The scheduled task associated with a fixed date runnable
     */
    public static class FixedDate extends VirtuallyScheduledTask<ScheduledRunnable> {

		public FixedDate(ScheduledRunnable runnable) {
			super(runnable);
		}

		@Override
		protected ManagedScheduledFutureTask<?> schedule(ManagedScheduledExecutorService scheduler, Clock clock) {
			
			long delay = getDelay(clock);
			
			if (delay < 0) {
            	return null;
            }
            
            return scheduler.schedule(runnable, delay, getUnit(clock));
		}
		
		/**
		 * The real time delay until the specified virtual date happens
		 */
	    public long getDelay(Clock clock) {
	        return (runnable.getExecutionDate() - clock.currentTimeMillis()) / clock.getFactor() ;
	    }

		/**
		 * The real time units of the execution delay
		 */
		private TimeUnit getUnit(Clock clock) {
			return TimeUnit.MILLISECONDS;
		}
	}
    
}
