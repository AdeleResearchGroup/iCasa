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
package fr.liglab.adele.icasa.clockservice.system.impl;

import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.clockservice.ClockListener;

/**
 *
 */

@Component(name="SystemClock")
@Instantiate(name="System-clock")
@Provides
public class SystemClockImpl implements Clock {

   
	
	private long initDate;

    @Override
    public String getId() {
        return "systemClock";
    }

    /* (non-Javadoc)
    * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#currentTimeMillis()
    */
   public long currentTimeMillis() {     
      return System.currentTimeMillis(); 
   }

	@Override
	public long currentTime(TimeUnit unit) {
		return unit.convert(currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

   /* (non-Javadoc)
    * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#getFactor()
    */
   public int getFactor() {
      return 1;
   }

   /* (non-Javadoc)
    * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#setFactor(int)
    */
   public void setFactor(int factor) {
      // do nothing, cannot change factor or system clock
   }

   /* (non-Javadoc)
    * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#setStartDate(long)
    */
   public void setStartDate(long startDate) {
   	initDate = startDate;
   }

	@Override
   public void pause() {
	   // TODO Auto-generated method stub
	   
   }

	@Override
   public void resume() {
	   // TODO Auto-generated method stub
	   
   }

    /**
     * Pauses the (virtual) time flowing.
     *
     * @param notify True to notify listeners, false if not.
     */
    @Override
    public void pause(boolean notify) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Resumes the (virtual) time flowing.
     *
     * @param notify True to notify listeners, false if not.
     */
    @Override
    public void resume(boolean notify) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
   public void reset() {
	   // TODO Auto-generated method stub
	   
   }

	@Override
   public long getElapsedTime() {
	   return currentTimeMillis() - initDate;
   }

	@Override
   public long getElapsedTime(TimeUnit unit) {
	   return unit.convert(getElapsedTime(),TimeUnit.MILLISECONDS);
   }

    public long getStartDate() {
   	 return initDate;
    }

	@Override
   public boolean isPaused() {
	   return false;
   }

	@Override
   public void addListener(ClockListener listener) {

	   
   }
	
	@Override
	public void removeListener(ClockListener listener) {
	   
	}


}
