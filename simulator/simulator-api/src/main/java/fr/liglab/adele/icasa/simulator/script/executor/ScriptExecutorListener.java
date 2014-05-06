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

/**
 * Listener interface for the scrip executor service
 *
 * 
 */
public interface ScriptExecutorListener {

	/**
	 * Notification when a new script has been deployed in the platform
	 * 
	 * @param scriptName the Script Name
	 */
	void scriptAdded(String scriptName);

	/**
	 * Notification when a existing script has been removed from the platform
	 * 
	 * @param scriptName the Script Name
	 */
	void scriptRemoved(String scriptName);

	/**
	 * Notification when a existing script has been modified
	 * 
	 * @param scriptName the Script Name
	 */
	void scriptUpdated(String scriptName);

	/**
	 * Notification when a script being executed is paused
	 * 
	 * @param scriptName the Script Name
	 */
	void scriptPaused(String scriptName);

	/**
	 * Notification when a paused script is resumed
	 * 
	 * @param scriptName the Script Name
	 */
	void scriptResumed(String scriptName);

	/**
	 * Notification when a script being executed is stopped
	 * 
	 * @param scriptName the Script Name
	 */
	void scriptStopped(String scriptName);

	/**
	 * Notification when a script is started (execution launched)
	 * 
	 * @param scriptName
	 */
	void scriptStarted(String scriptName);

}
