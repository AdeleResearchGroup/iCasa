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
package fr.liglab.adele.icasa.simulator.listener;

import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.LocatedDeviceListener;
import fr.liglab.adele.icasa.simulator.Person;

/**
 * Interface extending {@link fr.liglab.adele.icasa.location.LocatedDeviceListener} to add simulated device related
 * methods
 *
 * 
 */
public interface SimulatedLocatedDeviceListener extends LocatedDeviceListener {

	/**
	 * Invoked when a device has been attached to a person
	 * 
	 * @param person the person
	 * @param device the attached device
	 */
	public void personDeviceAttached(Person person, LocatedDevice device);

	/**
	 * Invoked when a device has been detached from a person
	 * 
	 * @param person the person
	 * @param device the detached device
	 */
	public void personDeviceDetached(Person person, LocatedDevice device);
}
