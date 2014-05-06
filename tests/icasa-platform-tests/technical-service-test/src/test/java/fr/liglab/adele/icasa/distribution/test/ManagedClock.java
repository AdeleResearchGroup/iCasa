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
package fr.liglab.adele.icasa.distribution.test;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.clock.ClockListener;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;


/**
 *
 */
public class ManagedClock implements Clock {

    private volatile long date;

    private long initDate;
    private String _clockId;

    public ManagedClock(String clockId) {
        _clockId = clockId;
    }

    public String getId() {
        return _clockId;
    }

    /* (non-Javadoc)
    * @see fr.liglab.adele.icasa.clock.api.SimulatedClock#currentTimeMillis()
    */
    public long currentTimeMillis() {
        return date;
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
        date = initDate;
    }


    public void pause() {
        // do nothing
    }


    public void resume() {
        // do nothing
    }

    /**
     * Pauses the (virtual) time flowing.
     *
     * @param notify True to notify listeners, false if not.
     */
    public void pause(boolean notify) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Resumes the (virtual) time flowing.
     *
     * @param notify True to notify listeners, false if not.
     */
    public void resume(boolean notify) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public void reset() {
        // do nothing
    }


    public long getElapsedTime() {
        return currentTimeMillis() - initDate;
    }

    public long getStartDate() {
        return initDate;
    }


    public boolean isPaused() {
        return false;
    }


    public void addListener(ClockListener listener) {
        // do nothing
    }


    public void removeListener(ClockListener listener) {
        // do nothing
    }

    public void setCurrentDate(Date newDate) {
        date = newDate.getTime();
    }
}