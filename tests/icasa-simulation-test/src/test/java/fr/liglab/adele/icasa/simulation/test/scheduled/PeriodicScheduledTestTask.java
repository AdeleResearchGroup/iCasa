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
package fr.liglab.adele.icasa.simulation.test.scheduled;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

import java.util.Date;

/**
 *
 */
public class PeriodicScheduledTestTask implements PeriodicRunnable {


    private Clock clock;

    private long period;

    public PeriodicScheduledTestTask(Clock clockService, long period){
        clock = clockService;
        this.period = period;
    }

    /**
     * Gets the scheduled period.
     *
     * @return the period in second.
     */
    public long getPeriod() {
        return period;
    }

    /**
     * If false, the job is scheduled at fixed interval. If true, at fixed rate.
     */
    public boolean isScheduledAtFixedRate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the job's group.
     * Jobs sharing a group use the same thread pool.
     *
     * @return the job's group
     */
    public String getGroup() {
        return "group2";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void run() {
        System.out.println("Executing service Simulation date: " + new Date(clock.currentTimeMillis()));
        System.out.println("Executing service Real Date: " + new Date());

    }
}
