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
package fr.liglab.adele.icasa.location;

import fr.liglab.adele.icasa.listener.IcasaListener;

/**
 * Listener of zone variables
 *
 */
public interface ZonePropListener extends IcasaListener {

	/**
	 * Called callback when a variable is added in a zone.
	 * @param zone the zone where the variable was added.
	 * @param variableName the name of variable added.
	 */
	public void zoneVariableAdded(Zone zone, String variableName);

	/**
	 * Called callback when a variable is removed from a zone.
	 * @param zone The zone where the variable was removed.
	 * @param variableName The name of variable removed.
	 */
	public void zoneVariableRemoved(Zone zone, String variableName);

	/**
	 * Called callback when a variable has been modified.
	 * @param zone The zone where the variable was modified.
	 * @param variableName The name of variable modified.
	 * @param oldValue The previous value of the variable.
	 */
	public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue);
	
}
