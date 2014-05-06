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
package fr.liglab.adele.icasa.service.scheduler;

/**
 * The reference to a task scheduled for one or more executions on the
 * {@code SchedulerService}.
 *
 * {@link TaskReference} allows getting a report after each execution of the
 * attached {@link Task}.
 */
public interface TaskReference {

    /**
     * Gets the time of registration of this task. This time is <b>not</b>
     * the time of the first execution.
     *
     * @return the time of registration of this task.
     */
    long getRegistrationTime();

    /**
     * Cancels all non-running (and optionally interrupt currently-running)
     * executions of this task and returns immediately.
     *
     * <p>
     * Currently running executions of this task are interrupted (see
     * {@link Thread#interrupt()}) if the {@code interrupt} parameter is set to
     * {@code true}, otherwise they can continue until they terminate.
     * </p>
     *
     * <p>
     * Calling this method has no effect if this task is already canceled (see
     * {@link #isCancelled()}.
     * </p>
     *
     * @param interrupt
     *            {@code true} if the currently running executions of this task
     *            must be immediately interrupted, {@code false} otherwise.
     * @see #isCancelled()
     */
    void cancel(boolean interrupt);

    /**
     * Gets {@code true} if this task has been canceled (using the
     * {@link #cancel(boolean)} method), {@code false} otherwise.
     *
     * @return {@code true} if this task was canceled, {@code false} otherwise.
     * @see #cancel(boolean)
     */
    boolean isCancelled();

    /**
     * Gets the cancellation time of this task if it has been canceled,
     * {@code -1} otherwise.
     *
     * @return the cancellation time of this task if it has been canceled,
     *         {@code -1} otherwise.
     * @see #isCancelled()
     */
    long getCancellationTime();

    /**
     * Gets the reports of all the completed executions of this task.
     * @return the list of all the task executions of this task, or empty
     *         if no execution of this task has completed yet.
     */
    TaskExecutionReport[] getExecutionReports();


}
