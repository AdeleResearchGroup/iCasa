/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa-Simulator/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.simulation.test.temperature;

import fr.liglab.adele.icasa.location.Zone;

/**
 * Condition is true only if specified zone has specified temperature value.
 *
 * @author Thomas Leveque
 */
public class TemperatureGreaterThanCondition extends TemperatureVarExistsCondition {

    private double _temp;

    public TemperatureGreaterThanCondition(Zone zone, double temp) {
        super(zone);
        _temp = temp;
    }

    public boolean isChecked() {
        if (!super.isChecked())
            return false;

        Object tempObj = _zone.getVariableValue("Temperature");
        if ((tempObj == null) || !(tempObj instanceof Double))
            return false;
        return ((Double) tempObj) > _temp;
    }

    public String getDescription() {
        return "Temperature variable must be greater than " + _temp + " on zone " + _zone.getId() + ".";
    }
}
