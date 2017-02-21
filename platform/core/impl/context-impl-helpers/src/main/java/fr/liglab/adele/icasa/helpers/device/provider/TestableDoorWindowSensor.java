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
package fr.liglab.adele.icasa.helpers.device.provider;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;
import fr.liglab.adele.icasa.device.doorWindow.DoorWindowSensor;
import fr.liglab.adele.icasa.device.testable.TestResult;
import fr.liglab.adele.icasa.device.testable.Testable;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.concurrent.ManagedFutureTask;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.util.concurrent.TimeUnit;

@FunctionalExtender(contextServices = Testable.class)
public class TestableDoorWindowSensor extends AbstractTestableService implements Testable{

    @Requires(filter = "(name=" + ManagedScheduledExecutorService.SYSTEM + ")", proxy = false)
    ManagedScheduledExecutorService scheduler;

    private ManagedFutureTask futureTask ;


    @FunctionalExtender.ChangeOn(contextService = DoorWindowSensor.class, id = DoorWindowSensor.DOOR_WINDOW_SENSOR_OPENING_DETECTCION)
    public void changeOn(boolean status){
        if (isTestRunning()){
            futureTask.cancel(true);
            finishTest(TestResult.SUCCESS,"Event detected on DoorWindow sensor");
        }
    }

    @Invalidate
    public void invalidateMethod(){
        super.stop();
    }

    @Override
    protected void testLaunch() {
        futureTask = scheduler.schedule(()-> finishTest(TestResult.FAILED,"No event detected during 20 seconds"),20, TimeUnit.SECONDS);
    }
}
