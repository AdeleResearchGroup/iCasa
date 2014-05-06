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
package fr.liglab.adele.icasa.common;

/**
 * Listener interface to monitor progress of an operation.
 *
 *
 */
public interface ProgressMonitor {

	/**
	 * Returns completed percentage value of monitored operation.
	 * 
	 * @return completed percentage value of monitored operation.
	 */
	public int getCompletedPercent();
	
	/**
	 * Called when monitored operation starts.
	 */
	public void notifyBegin();

	/**
	 * Called when monitored operation makes progress.
	 */
	public void notifyProgress(int completedPercent);
	
	/**
	 * Called when monitored operation has finished.
	 */
	public void notifyEnd();
}
