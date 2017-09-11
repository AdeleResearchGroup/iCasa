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
package fr.liglab.adele.icasa.device.doorWindow;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Service definition of a simple window shutter device.
 *
 */
public @ContextService interface WindowShutter extends GenericDevice {

	/**
	 * Service property that indicates whether a shutter is closed or open.
	 *
	 * <ul>
	 * <li>This property is <b>mandatory</b></li>
	 * <li>Type of values : <b><code>java.lang.Double</code></b>, between
	 * <code>0.0d</code> and <code>1.0d</code></li>
	 * <li>Description : value is <code>0.0d</code> when the shutter is completely
	 * closed, <code>1.0d</code> when completely opened.</li>
	 * </ul>
	 *
	 * @see #getShutterLevel()
	 * @see #setShutterLevel(double)
	 */
	@State String SHUTTER_LEVEL = "WindowShutter.shutterLevel";

	/**
	 * Return the current level of this shutter.
	 *
	 * @return the current level of this shutter.
	 * @see #setShutterLevel(double)
	 * @see #SHUTTER_LEVEL
	 */
	double getShutterLevel();

	/**
	 * Change the level of this shutter.
	 *
	 * @param level
	 *           the new power level of this dimmer light.
	 * @return the previous power level of this dimmer light.
	 * @see #getShutterLevel()
	 * @see #SHUTTER_LEVEL
	 */
	void setShutterLevel(double level);

}
