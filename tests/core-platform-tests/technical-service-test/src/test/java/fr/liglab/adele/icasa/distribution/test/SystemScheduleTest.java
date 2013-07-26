/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research Group
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.distribution.test;

import fr.liglab.adele.commons.distribution.test.AbstractDistributionBaseTest;
import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.service.scheduler.ScheduledRunnable;
import fr.liglab.adele.icasa.service.scheduler.SpecificClockPeriodicRunnable;
import fr.liglab.adele.icasa.service.scheduler.SpecificClockScheduledRunnable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;



@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class SystemScheduleTest extends AbstractDistributionBaseTest {

    static final long ONE_SECOND=1000;

	@Inject
	public BundleContext context;

    @Inject
    public Clock clock;
	
	@Before
	public void setUp() {
		waitForStability(context);
	}

	@After
	public void tearDown() {
        // do nothing
	}

    public static Option helpBundles() {

        return new DefaultCompositeOption(
                systemProperty( "iCasa.ThreadPool.default.maxThread" ).value( "10" ),
                systemProperty( "iCasa.ThreadPool.group1.maxThread" ).value( "3" ),
                systemProperty( "iCasa.ThreadPool.group2.maxThread" ).value( "5" )
        );
    }

    @org.ops4j.pax.exam.Configuration
    public Option[] configuration() {

        List<Option> lst = super.config();
        lst.add(helpBundles());
        Option conf[] = lst.toArray(new Option[0]);
        return conf;
    }

	/**
	 * Test scheduling a periodic task.
	 */
	@Test
	public void scheduleAPeriodicTaskTest(){

        PeriodicRunnable mockPeriodic = mock(PeriodicRunnable.class);
        when(mockPeriodic.getPeriod()).thenReturn(Long.valueOf(1000));
        when(mockPeriodic.getGroup()).thenReturn("group1");
        ServiceRegistration register = context.registerService(PeriodicRunnable.class.getName(), mockPeriodic, new Hashtable());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        register.unregister();
        verify(mockPeriodic, atLeast(2)).run();
    }


    /**
     * Test scheduling one shot task.
     */
    @Test
    public void scheduleTaskTest(){

        Date now = new Date();
        Date scheduledTime = new Date(now.getTime() + ONE_SECOND*5);//Scheduled in 5 seconds from now

        ScheduledRunnable spyTask = mock(ScheduledRunnable.class);
        when(spyTask.getExecutionDate()).thenReturn(scheduledTime.getTime());
        when(spyTask.getGroup()).thenReturn("group1");
        ServiceRegistration register = context.registerService(ScheduledRunnable.class.getName(), spyTask, new Hashtable());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        register.unregister();
        verify(spyTask,times(1)).run();//at least one run.
    }

    /**
     * Test scheduling a task with null group.
     */
    @Test
    public void taskWithNullGroupTest(){
        PeriodicRunnable mockPeriodic = mock(PeriodicRunnable.class);
        when(mockPeriodic.getPeriod()).thenReturn(Long.valueOf(1000));
        when(mockPeriodic.getGroup()).thenReturn(null);
        ServiceRegistration register = context.registerService(PeriodicRunnable.class.getName(), mockPeriodic, new Hashtable());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        register.unregister();
        verify(mockPeriodic, atLeast(2)).run();
    }

    /**
     * Test .
     */
    @Test
    public void scheduleTaskWithNullGroupTest(){

        Date now = new Date();
        Date scheduledTime = new Date(now.getTime() + ONE_SECOND*5);//Scheduled in 5 seconds from now

        ScheduledRunnable spyTask = mock(ScheduledRunnable.class);
        when(spyTask.getExecutionDate()).thenReturn(scheduledTime.getTime());
        when(spyTask.getGroup()).thenReturn(null);
        ServiceRegistration register = context.registerService(ScheduledRunnable.class.getName(), spyTask, new Hashtable());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        register.unregister();
        verify(spyTask,atLeast(1)).run();//at least one run.
    }

    /**
     * Test scheduling one shot task.
     */
    @Test
    public void specificClockScheduleTaskTest(){

        Date now = new Date();
        Date scheduledTime = new Date(now.getTime() + ONE_SECOND*5);//Scheduled in 5 seconds from now
        Date secondTime = new Date(scheduledTime.getTime() + ONE_SECOND);

        ManagedClock managedClock = new ManagedClock("specificClockTest-1");
        managedClock.setStartDate(now.getTime());

        SpecificClockScheduledRunnable spyTask = mock(SpecificClockScheduledRunnable.class);
        when(spyTask.getExecutionDate()).thenReturn(scheduledTime.getTime());
        when(spyTask.getGroup()).thenReturn("group1");
        when(spyTask.getClock()).thenReturn(managedClock);
        ServiceRegistration register = context.registerService(SpecificClockScheduledRunnable.class.getName(), spyTask, new Hashtable());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(spyTask, times(0)).run();//no run.

        managedClock.setCurrentDate(secondTime);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        register.unregister();
        verify(spyTask,times(1)).run();//at least one run.
    }

    /**
     * Test scheduling one shot task.
     */
    @Test
    public void specificClockPeriodicTaskTest(){

        Date now = new Date();
        long period = ONE_SECOND * 2;
        Date firstScheduledTime = new Date(now.getTime() + period);//Scheduled in 5 seconds from now
        Date secondTime = new Date(firstScheduledTime.getTime() + ONE_SECOND);
        Date secondScheduledTime = new Date(firstScheduledTime.getTime() + period);//Scheduled in 5 seconds from now
        Date thirdTime = new Date(secondScheduledTime.getTime() + ONE_SECOND);

        ManagedClock managedClock = new ManagedClock("specificClockTest-2");
        managedClock.setStartDate(now.getTime());

        SpecificClockPeriodicRunnable spyTask = mock(SpecificClockPeriodicRunnable.class);
        when(spyTask.getPeriod()).thenReturn(Long.valueOf(period));
        when(spyTask.getGroup()).thenReturn("group3");
        when(spyTask.getClock()).thenReturn(managedClock);
        ServiceRegistration register = context.registerService(SpecificClockPeriodicRunnable.class.getName(), spyTask, new Hashtable());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(spyTask,times(0)).run();//no run.

        managedClock.setCurrentDate(secondTime);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(spyTask,times(1)).run();//at least one run.

        managedClock.setCurrentDate(thirdTime);
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(spyTask,times(2)).run();//exactly two runs.

        register.unregister();
    }

}
