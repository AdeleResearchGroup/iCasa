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
package fr.liglab.adele.icasa.simulator.script.executor;

import java.util.Date;
import java.util.List;

/**
 * Interface of service responsible for script execution into the platform
 *
 * 
 */
public interface ScriptExecutor {

	/**
	 * Enumeration representing the state of scripts
	 *
	 * 
	 */
	public enum State {
		STARTED("started"), STOPPED("stopped"), PAUSED("paused");

		private String _stateStr;

		private State(String stateStr) {
			_stateStr = stateStr;
		}

		public String toString() {
			return _stateStr;
		}

		public static State fromString(String stateStr) {
			if (stateStr == null)
				return null;

			if (STARTED.toString().equals(stateStr))
				return STARTED;
			if (STOPPED.toString().equals(stateStr))
				return STOPPED;
			if (PAUSED.toString().equals(stateStr))
				return PAUSED;

			return null;
		}
	}

	/**
	 * Adds a listener to the script executor.
	 * 
	 * @param listener the listener to be added.
	 */
	public void addListener(ScriptExecutorListener listener);

	/**
	 * Removes a listener to the script executor.
	 * 
	 * @param listener the listener to be removed.
	 */
	public void removeListener(ScriptExecutorListener listener);

	/**
	 * Gets a lists of scripts in the platform
	 * 
	 * @return
	 */
	public List<String> getScriptList();

	/**
	 * Gets the current script in execution
	 * 
	 * @return
	 */
	public String getCurrentScript();

	/**
	 * Executes the script having this name
	 * 
	 * @param scriptName
	 */
	public void execute(String scriptName);

	/**
	 * Executes the script having this name
	 * 
	 * @param scriptName
	 * @param startDate
	 * @param factor
	 */
	public void execute(String scriptName, Date startDate, int factor);

	/**
	 * Stops the script execution
	 * 
	 */
	public void stop();

	/**
	 * Pauses the execution of the current script (if there is one)
	 */
	public void pause();

	/**
	 * Resumes the execution of the current script (if there is one)
	 */
	public void resume();

	/**
	 * Gets the execution estate of the current script (if there is one)
	 * 
	 * @return
	 */
	public State getCurrentScriptState();

	/**
	 * Returns percentage of completed instructions of current script.
	 * 
	 * @return percentage of completed instructions of current script.
	 */
	public float getExecutedPercentage();

	/**
	 * Saves the current state into a script having the name use as argument
	 * 
	 * @param fileName the name of the script file
	 */
	void saveSimulationScript(String fileName);

	/**
	 * Gets the factor defined in the script file
	 * 
	 * @param scriptName the script file name
	 * @return the factor
	 */
	public int getFactor(String scriptName);

	/**
	 * Gets the start date defined in the script file
	 * 
	 * @param scriptName the script file name
	 * @return the start date
	 */
	public long getStartDate(String scriptName);

	/**
	 * Get the number of actions contained into the script
	 * 
	 * @param scriptName the script file name
	 * @return the number of actions
	 */
	public int getActionsNumber(String scriptName);

	/**
	 * Gets the execution time in (virtual) minutes of the script file
	 * 
	 * @param scriptName the script file name
	 * @return the execution time in (virtual) minutes
	 */
	public int getExecutionTime(String scriptName);

	/**
	 * Gets the current state of the script file
	 * 
	 * @param scriptName the script file name
	 * @return the state
	 */
	public State getState(String scriptName);

}
