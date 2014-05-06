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
package fr.liglab.adele.icasa.device.gasSensor;

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Service definition of a CO sensor device.
 *
 */
public interface CarbonMonoxydeSensor extends GenericDevice {

    /**
     * Service property indicating the current value of CO measured by the gas sensor, expressed in µg/m^3.
     * 
     * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.Double</code></b></li>
     * <li>Description : value is a CO concentration expressed in µg/m^3, so it
     * is <code>always positive</code>.</li>
     * </ul>
     * 
     * @see #getCOConcentration()
     */
    String CARBON_MONOXYDE_SENSOR_CURRENT_CONCENTRATION = "carbonMonoxydeSensor.currentConcentration";

    /**
     * Return the current CO concentration sensed by this gas sensor, expressed in
     * µg/m^3.
     * 
     * @return the current CO concentration sensed by this gas sensor, expressed in
     *         µg/m^3.
     */
    double getCOConcentration();

}
