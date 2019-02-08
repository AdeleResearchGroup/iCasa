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
package fr.liglab.adele.icasa.clockservice.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.clockservice.ClockListener;

/**
 *
 */
@Component(name="SimulatedClock")
@Provides
@Instantiate(name="simulated-clock")
public class VirtualClock implements Clock {

	/**
	 * Every time that the start date of a virtual clock is set a new era starts 
	 */
	private final AtomicInteger era = new AtomicInteger();

	/**
	 * The last measured value of the clock for the current era
	 */
	private volatile Instant lastMeasure;

    /**
     * Factor of virtual time
     */
    private volatile int factor;

    /**
     * Indicates if the clock has been paused
     */
    private volatile boolean pause;


    /**
     * A thread safe list of listeners, notice that  copy-on-write semantics is efficient only when traversal
     * operations vastly outnumber mutations
     */
    private final List<ClockListener> listeners = new CopyOnWriteArrayList<ClockListener>();


	/**
	 * This class represents a moment of time measured by the virtual clock
	 */
	public static class Instant {

		/**
		 * The clock that measured the instant
		 */
		
		private final VirtualClock clock;
		
		/**
		 * The era at the time of the measure
		 */
		private final int era;

		/**
		 * The starting date of the era
		 */
		private final long epoch;
		
		/**
		 * The elapsed time from the beginning of the era in milliseconds
		 */
		private final long elapsed;
		
		/**
		 * the real time when the instant was measured (in milliseconds since the Unix epoch). Notice that it is meaningless
		 * to compare measure times of two instants as the virtual clock may have been paused/resumed 
		 */
		private final long measureTime;
		
		
		private Instant(VirtualClock clock, int era, long epoch, long elapsed, long measureTime) {
			
			this.clock 			= clock;
			this.era			= era;
			this.epoch			= epoch;
			this.elapsed		= elapsed;
			
			this.measureTime	= measureTime;
		}

		/**
		 * An instant that represents the beginning of an epoch for the specified clock 
		 */
		public static Instant epoch(VirtualClock clock, long epoch) {
			long now = System.currentTimeMillis();
			return new Instant(clock, clock.era.get(), epoch, 0, now);
		}

		
		/**
		 * Creates a new measure that represents the beginning of the era associated to this instant
		 */
		protected Instant rewind() {
			long now = System.currentTimeMillis();
			return new Instant(this.clock, this.era, this.epoch, 0, now);
		}

		/**
		 * Creates a new measure that represents the same virtual time but with an updated real time, this
		 * is useful to make a measure after the resume of the clock
		 */
		protected Instant refresh() {
			long now = System.currentTimeMillis();
			return new Instant(this.clock, this.era, this.epoch, this.elapsed, now);
		}

		/**
		 * Creates a new measure of time by forwarding this instant by the amount of real time elapsed since
		 * its creation until now
		 */
		protected Instant forward() {
			long now = System.currentTimeMillis();
			return new Instant(this.clock, this.era, this.epoch, this.elapsed + ((now - this.measureTime)  * this.clock.factor), now);
		}

		/**
		 * The epoch when this instant was measured
		 */
		public long getEpoch() {
			return epoch;
		}
		
		/**
		 * The elapsed time since the epoch
		 */
		public long getElapsed(TimeUnit unit) {
			return unit.convert(elapsed,TimeUnit.MILLISECONDS);
		}
		
		/**
		 * The absolute date and time represented by this instant
		 */
		public Date toDate() {
			return new Date(epoch + elapsed);
		}
		
		/**
		 * Verifies in this instant is in the present era of the clock
		 */
		public boolean isContemporary(VirtualClock clock) {
			return this.clock == clock && this.era == clock.era.get();
		}

	}
	
    @Override
    public String getId() {
        return "simulatedClock";
    }


    private void changeEra(long epoch, boolean notify) {

    	long previuosEpoch = this.lastMeasure != null ? this.lastMeasure.getEpoch() : 0L;
    	
        if (epoch == previuosEpoch)
            return;

    	/*
    	 * Pause the clock without notifying listeners, this is implied by the startDateModified event
    	 */
        pause(false);

        /*
         * Consider possible concurrent threads trying to change era, only ONE should succeed ! 
         */
        int present = this.era.get();
        if (this.era.compareAndSet(present,present+1)) {
            this.lastMeasure = Instant.epoch(this, epoch);
        }
        
    	/*
    	 * Resume the clock without notifying listeners, this is implied by the startDateModified event
    	 */
        resume(false);
        
        if (notify) {
            notify(ClockListener::startDateModified, previuosEpoch);
        }
    	
    } 

    @Validate
    private void activated() {
        
        factor	= 1;
        pause	= true;

        changeEra(System.currentTimeMillis(), false);
    }

    @Invalidate
    private void deactivated() {
    }


    @Override
    public long getStartDate() {
        return lastMeasure.getEpoch();
    }

    @Override
	public void setStartDate(long epoch) {
    	changeEra(epoch,true);
    }


    /**
     * Updates the last measured time considering eventual problems if the era changed while the new measure
     * was calculated
     */
    public Instant tick(Instant newMeasure) {

    	if (newMeasure.isContemporary(this)) {
    		this.lastMeasure = newMeasure;
    	}
       
    	return newMeasure;
    }
    
    /**
     * The current instant of the virtual clock, this may update the last measure.
     */
    public Instant now() {
    	
    	/*
    	 * Recalculate virtual elapsed time since the last measure.
    	 * 
    	 * Note that if several threads concurrently measure the time, some of the measures may get lost
    	 * and this may cause some glitches in the clock, that will not appear as monotonic. By now
    	 * we ignore these errors to have a simple (non synchronized) simulation.
    	 * 
     	 */
    	return pause ? lastMeasure : tick(lastMeasure.forward());
    }
    
    @Override
    public long getElapsedTime() {
        return now().getElapsed(TimeUnit.MILLISECONDS);
    }

    @Override
    public long currentTimeMillis() {
    	return now().toDate().getTime();
    }

    @Override
    public boolean isPaused() {
        return pause;
    }

    @Override
    public void pause() {
        pause(true);
    }

    @Override
    public void pause(boolean notify) {

    	if (pause)
    		return;

        pause = true;
        
        if(notify) {
        	notify(ClockListener::clockPaused);
        }
    }

    @Override
    public void resume() {
        resume(true);
    }

    @Override
    public void resume(boolean notify) {
        
    	if (!pause)
            return;

    	tick(lastMeasure.refresh());
    	
        pause 	= false;
        if(notify) {
        	notify(ClockListener::clockResumed);
        }

    }

    @Override
    public void reset() {
    	reset(true);
     }

    private void reset(boolean notify) {

    	/*
    	 * The pause event is coalesced with the reset event
    	 */
    	pause(false);
        
    	tick(lastMeasure.rewind());

        if(notify) {
        	notify(ClockListener::clockReset);
        }

    }


    @Override
    public int getFactor() {
        return factor;
    }


    @Override
    public void setFactor(int factor) {

    	if (factor == this.factor)
            return;

        int oldFactor = this.factor;
        this.factor = factor;

        notify(ClockListener::factorModified, oldFactor);
    }



    @Override
    public void addListener(final ClockListener listener) {
    	listeners.add(listener);
    }


    @Override
    public void removeListener(ClockListener listener) {
    	listeners.remove(listener);
    }

    
    private void notify(Consumer<ClockListener> event) {
    	for (ClockListener listener : listeners) {
            try {
                event.accept(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
			
		}
    }
    
    private <P> void notify(BiConsumer<ClockListener,P> event, P parameter ) {
    	for (ClockListener listener : listeners) {
            try {
                event.accept(listener,parameter);
            } catch (Exception e) {
                e.printStackTrace();
            }
			
		}
    }

}