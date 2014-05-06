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

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.service.scheduler.TaskExecutionReport;
import fr.liglab.adele.icasa.service.scheduler.TaskReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class TaskReferenceImpl implements TaskReference, Runnable {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG);


    // The access lock
    private final Object m_lock;

    private Clock m_service;


    // The task handler to execute
    private final Runnable m_task;

    // The registration time of this task
    private final long m_registrationTime;


    // // The flag indicating whether or not two concurrent executions of this
    // task
    // // can occur
    // private final boolean m_mayOverlap;

    // The execution reports of this task
    private final List/* <TaskExecutionReport> */m_reports;

    // The cancellation time of this task, or -1 if the task is not cancelled
    private volatile long m_cancellationTime;

    // The number of times this task has been executed
    private volatile long m_executionCount;

    // TODO
    public TaskReferenceImpl(Clock service,
                             Runnable task) {
        m_lock = new Object();
        // Initialize task with provided parameters
        m_service = service;
        m_task = task;
        // Set the task as registered now and not cancelled
        m_registrationTime = m_service.currentTimeMillis();
        m_cancellationTime = -1;
        m_executionCount = 0;
        m_reports = new ArrayList(1);
    }


    public long getRegistrationTime() {
        return m_registrationTime;
    }

    // Getters for mutable fields, needing

    public long getCancellationTime() {
        synchronized (m_lock) {
            return m_cancellationTime;
        }
    }

    public boolean isCancelled() {
        synchronized (m_lock) {
            return m_cancellationTime != -1;
        }
    }

    public TaskExecutionReport[] getExecutionReports() {
        TaskExecutionReport[] result = new TaskExecutionReport[0];
        synchronized (m_lock) {
            return (TaskExecutionReport[]) m_reports.toArray(result);
        }
    }

    public Object getLastResult() {
        TaskExecutionReport lastReport;
        synchronized (m_lock) {
            int lastIndex = m_reports.size() - 1;
            if (lastIndex < 0) {
                return null;
            }
            lastReport = (TaskExecutionReport) m_reports.get(lastIndex);
        }
        return lastReport.getResult();
    }

    //

    // Mark this task as cancelled. If the interrupt flag is set, also notify
    // the service that running executions must be interrupted
    public void cancel(boolean interrupt) {
        synchronized (m_lock) {
            // Task already cancelled, return immediately
            if (m_cancellationTime != -1) {
                return;
            }
            // Set the task as cancelled now
            m_cancellationTime = m_service.currentTimeMillis();
            if (interrupt) {
                // TODO notify the scheduler that running executions of this
                // task must be interrupted
            }
        }
    }

    // TODO
    public void run() {
        long executionCount;
        synchronized (m_lock) {
            // Task already cancelled, log and return immediately
            if (m_cancellationTime != -1) {
                logger.warn("Ignoring execution : task is cancelled");
                return;
            }
            executionCount = m_executionCount;
            m_executionCount++;
        }
        // Initialize a report and send a pre-execution event
        TaskExecutionReportImpl report = new TaskExecutionReportImpl();
        report.executionCount = executionCount;
        report.startupTime = m_service.currentTimeMillis();
        // Do execute the task, filling out the report
        try {
            m_task.run();
        } catch (Throwable e) {
            report.exception = e;
        } finally {
            report.terminationTime = m_service.currentTimeMillis();
        }
        // Insert the report at the end of the list
        synchronized (m_lock) {
            m_reports.add(report);
        }
    }

    /**
     * Return the time wanted for the next execution of this task.
     *
     * @param fromTime
     *            the time base used to compute the next execution time.
     * @return the time wanted for the next execution of this task.
     */
    public abstract Long computeNextExecutionTime(long fromTime);

    /**
     * Return the time wanted for the next execution of this task.
     *
     * @return the time wanted for the next execution of this task.
     */
    public abstract Long getNextExecutionTime();
}
