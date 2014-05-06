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
package fr.liglab.adele.icasa.clock.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.clock.ClockListener;

/**
 * 
 */
@Component(name="SimulatedClock")
@Provides
@Instantiate(name="simulated-clock")
public class SimulatedClockImpl implements Clock {

	/**
	 * Initial date
	 */
	private volatile long initDate;

	/**
	 * Time elapsed from initial date
	 */
	private volatile long elapsedTime;

	/**
	 * Factor of virtual time
	 */
	private volatile int factor;

	/**
	 * Indicates if the clock has been paused
	 */
	private volatile boolean pause = true;
	
	private static final int TIME_THREAD_STEEP = 20;
	
	private final List<ClockListener> listeners = new ArrayList<ClockListener>();

	/**
	 * Thread used to increment the clock
	 */
	private Thread timeThread;

    @Override
    public String getId() {
        return "simulatedClock";
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#currentTimeMillis()
	 */
	public long currentTimeMillis() {
		return initDate + elapsedTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#getFactor()
	 */
	public int getFactor() {
		return factor;
	}

    @Override
    public long getStartDate() {
   	 return initDate;
    }

    /*
      * (non-Javadoc)
      *
      * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#setFactor(int)
      */
	public void setFactor(int factor) {
		if (factor == this.factor) 
			return;
		
		int oldFactor = this.factor;
				
		this.factor = factor;
		
		// Call all listeners sequentially
		for (ClockListener listener : getListenersCopy()) {
            try {
                listener.factorModified(oldFactor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#setStartDate(long)
	 */
	public void setStartDate(long startDate) {
		long oldDate = initDate;
		initDate = startDate;
		

		// Call all listeners sequentially
		for (ClockListener listener : getListenersCopy()) {
            try {
                listener.startDateModified(oldDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        if(notify){
            // Call all listeners sequentially
            for (ClockListener listener : getListenersCopy()) {
                listener.clockPaused();
            }
        }
    }

    @Override
    public void resume(){
        resume(true);
    }

	@Override
	public void resume(boolean notify) {
		if (!pause)
			return;
		
		pause = false;		
		if(notify){
            // Call all listeners sequentially
            for (ClockListener listener : getListenersCopy()) {
                try {
                    listener.clockResumed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
		
	}
		

	@Override
	public void reset(){		
		pause(false);
        initDate = System.currentTimeMillis();
		elapsedTime = 0;
		
		// Call all listeners sequentially
		for (ClockListener listener : getListenersCopy()) {
            try {
                listener.clockReset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}
	
	@Override
	public long getElapsedTime() {
	   return elapsedTime;
	}
	
	@Override
   public boolean isPaused() {
	   return pause;
   }
	
	@Validate
	public void start() {
		initDate = System.currentTimeMillis();
		factor = 1;
		pause = true;
		timeThread = new Thread(new ClockTimeMover(), "Clock-Thread");
		timeThread.start();
	}
	
	@Invalidate
	public void stop() {
		try {
			timeThread.interrupt();
	      timeThread.join();
      } catch (InterruptedException e) {
	      e.printStackTrace();
      }
	}


	@Override
   public void addListener(final ClockListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}
		synchronized (listeners) {
			listeners.add(listener);
		}	   
   }

	
	@Override
	public void removeListener(ClockListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}
		synchronized (listeners) {
			listeners.remove(listener);
		}	 	   
	}
	
	
	private List<ClockListener> getListenersCopy() {
		List<ClockListener> listenersCopy;
		synchronized (listeners) {
			listenersCopy = Collections.unmodifiableList(new ArrayList<ClockListener>(listeners));
		}
		return listenersCopy;
	}
	
	/**
	 * Clock Time mover Thread (Runnable) class
	 *
	 *
	 */
	private final class ClockTimeMover implements Runnable {
		
		@Override
		public void run() {
			boolean execute = true;
			while (execute) {					
				try {						
					long enterTime = System.currentTimeMillis();						
					Thread.sleep(TIME_THREAD_STEEP);						
					if (!pause) {
						long realElapsedTime = System.currentTimeMillis() - enterTime;
						elapsedTime += realElapsedTime * factor;
					}							
				} catch (InterruptedException e) {
					execute = false;
				}
			}
		}
	}


}
