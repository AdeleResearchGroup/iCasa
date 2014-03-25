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
package fr.liglab.adele.icasa.device;

/**
 * Created by aygalinc on 25/03/14.
 */

public interface PowerObservable {

    /**
     * Device property indicating the  instant power consumption  of the device.
     *
     * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.Double</code></b>, is
     * <code>XXX</code> Watts</li>
     * <li>Description : value is the wattage of the device.</li>
     * </ul>
     *
     *
     */
    String POWER_OBSERVABLE_CURRENT_POWER_LEVEL = "powerObservable.currentConsumption";

    public double getCurrentConsumption();

}
