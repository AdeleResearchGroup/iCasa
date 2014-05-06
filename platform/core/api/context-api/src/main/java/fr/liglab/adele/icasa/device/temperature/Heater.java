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
package fr.liglab.adele.icasa.device.temperature;

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Service definition of a simple heater device.
 *
 */
public interface Heater extends GenericDevice {

    /**
     * Service property indicating the current power level of the heater.
     * 
     * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.Double</code></b>, between
     * <code>0.0d</code> and <code>1.0d</code></li>
     * <li>Description : value is <code>0.0d</code> when the heater is
     * completely turned off, <code>1.0d</code> when completely turned on.</li>
     * </ul>
     * 
     * @see #getPowerLevel()
     * @see #setPowerLevel(double)
     */
    String HEATER_POWER_LEVEL = "heater.powerLevel";
    
    /**
     * Service property indicating the max power level of the heater in Watts.
     * 
     * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of value : <b><code>java.lang.Double</code></b> fixed to 1000.0 Watts.
     * </ul>
     * 
     * @see #getMaxPowerLevel()
     * 
     */
    String HEATER_MAX_POWER_LEVEL ="heater.maxPowerLevel";
    
    String HEATER_UPDATE_PERIOD = "heater.updaterThread.period";

    /**
     * Return the current power level of this heater.
     * 
     * @return the current power level of this heater.
     * @see #setPowerLevel(double)
     * @see #HEATER_POWER_LEVEL
     */
    double getPowerLevel();

    /**
     * Change the power level of this heater.
     * 
     * @param level
     *            the new power level of this heater.
     * @return the previous power level of this heater.
     * @see #getPowerLevel()
     * @see #HEATER_POWER_LEVEL
     */
    double setPowerLevel(double level);
    
    /**
     * Return the max power level of this heater
     * 
     * @return the max power level of this heater in Watts.
     * @see #HEATER_MAX_POWER_LEVEL
     */
    double getMaxPowerLevel();

}
