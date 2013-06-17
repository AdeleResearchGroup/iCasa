/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
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
package fr.liglab.adele.icasa.service.scheduler.impl;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.service.scheduler.ICasaRunnable;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.service.scheduler.ScheduledRunnable;
import fr.liglab.adele.icasa.service.scheduler.impl.task.OneShotTaskImpl;
import fr.liglab.adele.icasa.service.scheduler.impl.task.PeriodicTaskImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 *
 */
public class Group  {

    private final String name;
    Map<ICasaRunnable, TaskReferenceImpl> jobs = new HashMap<ICasaRunnable, TaskReferenceImpl>();
    //private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(DefensiveThreadFactory.get());
    private SchedulerThreadPoolImpl executor;
    private Logger logger;
    private Clock clock;

    private Thread additionThread = new Thread();
    private volatile boolean isRunning;

    public volatile boolean clockIsRunning = false;

    public int DEFAULT_POOL_SIZE = 5;

    public Group(String groupName, Clock clock) {
        this.name = groupName;
        this.clock = clock;
        this.logger = LoggerFactory.getLogger(Group.class.getName() + "-" + this.name);
        isRunning = true;
        executor = new SchedulerThreadPoolImpl(this.name, clock, DEFAULT_POOL_SIZE);
        clockIsRunning = !clock.isPaused();
        new Thread(executor).start();
    }

    public String getName() {
        return name;
    }

    /**
     * Add a runnable  to the group set.
     * @param runnable
     * @return
     */
    public synchronized boolean submit(ScheduledRunnable runnable) {
        TaskReferenceImpl taskRef = new OneShotTaskImpl(clock, runnable, new Long(runnable.getExecutionDate()));
        executor.addTask(taskRef);
        System.out.println("Expected time: " + new Date(runnable.getExecutionDate()));
        System.out.println("Registration at: " + new Date(taskRef.getRegistrationTime()));
        jobs.put(runnable, taskRef);
        return true;
    }


    /**
     * Add a runnable  to the group set.
     * @param runnable
     * @return
     */
    public synchronized boolean submit(PeriodicRunnable runnable) {
        TaskReferenceImpl taskRef = new PeriodicTaskImpl(clock, runnable, new Long(runnable.getPeriod()));
        System.out.println("Expected time at: " + new Date(taskRef.getNextExecutionTime()));
        System.out.println("Registration at: " + new Date(taskRef.getRegistrationTime()));
        executor.addTask(taskRef);
        jobs.put(runnable, taskRef);
        return true;
    }


    public synchronized boolean withdraw(ICasaRunnable runnable) {
        TaskReferenceImpl handle = jobs.remove(runnable);
        if (handle != null) {
            logger.info("Withdrawing job {}", runnable);
            handle.cancel(true);
            return true;
        }
        return false;
    }

    public synchronized boolean isEmpty() {
        return jobs.isEmpty();
    }

    public synchronized void close() {
        for (TaskReferenceImpl future : jobs.values()) {
            future.cancel(true);
        }
        jobs.clear();
        isRunning = false;

    }

}
