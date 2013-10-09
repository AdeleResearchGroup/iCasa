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
package fr.liglab.adele.icasa.remote;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public interface SimulatedDeviceManager {

	/**
	 * @param deviceType
	 * @param deviceId
	 * @param properties
	 */
	void createDevice(String deviceType, String deviceId,
			Map<String, Object> properties);

	/**
	 * @param deviceId
	 */
	void removeDevice(String deviceId);
	
	/**
	 * Retrieve the list of simulated devices
	 * @return the list of types.
	 */
	Set<String> getDeviceTypes();

}
