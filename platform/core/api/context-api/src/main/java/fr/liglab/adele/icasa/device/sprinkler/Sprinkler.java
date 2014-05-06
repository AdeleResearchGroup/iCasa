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
package fr.liglab.adele.icasa.device.sprinkler;

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Service definition of a sprinkler device.
 *
 */
public interface Sprinkler extends GenericDevice{

    /**
     * Service property indicating whether the sprinkler is is turned on or
     * off.
     *
     * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.Boolean</code></b></li>
     * <li>Description : value is <code>true</code> when the sprinkler is turned on,
     * <code>false</code> otherwise.</li>
     * </ul>
     *
     * @see #getPowerStatus()
     * @see #setPowerStatus(boolean)
     * @see #turnOn()
     * @see #turnOff()
     */
    String SPRINKLER_POWER_STATUS = "sprinkler.powerStatus";

    /**
     * Return the current power state of this sprinkler.
     *
     * @return the current power state of this sprinkler.
     * @see #setPowerStatus(boolean)
     * @see #SPRINKLER_POWER_STATUS
     */
    boolean getPowerStatus();

    /**
     * Change the power status of this sprinkler.
     *
     * @param state the new power state of this sprinkler.
     * @return the previous power state of this sprinkler.
     * @see #getPowerStatus()
     * @see #SPRINKLER_POWER_STATUS
     */
    boolean setPowerStatus(boolean state);

    /**
     * Change the power status to true
     */
    void turnOn();

    /**
     * Change the power status to false
     */
    void turnOff();
}
