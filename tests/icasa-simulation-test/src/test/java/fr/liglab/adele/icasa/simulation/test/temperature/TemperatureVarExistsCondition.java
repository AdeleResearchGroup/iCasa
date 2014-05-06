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
package fr.liglab.adele.icasa.simulation.test.temperature;

import fr.liglab.adele.icasa.location.Zone;
import org.ow2.chameleon.runner.test.utils.Condition;

/**
 * Condition is true only if specified zone has temperature variable.
 *
 */
public class TemperatureVarExistsCondition implements Condition {

    protected Zone _zone;

    public TemperatureVarExistsCondition(Zone zone) {
        _zone = zone;
    }

    public boolean isChecked() {
        return _zone.getVariableNames().contains("Temperature");
    }

    public String getDescription() {
        return "Temperature variable must exist on zone " + _zone.getId() + ".";
    }
}
