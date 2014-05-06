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

/**
 * Condition is true only if specified zone has specified temperature value.
 *
 */
public class TemperatureStableCondition extends TemperatureVarExistsCondition {
    
    private double _previuosValue;
    
    private int _counter = 0;
    
    
    public TemperatureStableCondition(Zone zone, double originalValue) {
        super(zone);        
        _previuosValue = originalValue;        
    }

    public boolean isChecked() {
        if (!super.isChecked())
            return false;
            
        Object tempObj = _zone.getVariableValue("Temperature");
          
            
        if ((tempObj == null) || !(tempObj instanceof Double))
            return false;
        
        double newValue = (Double) tempObj;
                
        if (_previuosValue == newValue) {
            _counter++;
        } else {
            _counter = 0;
        }
        
        if (_counter > 3) {
            return true;
        }

        return false;
    }

    public String getDescription() {
        return "Temperature variable must be stable in zone"  + _zone.getId() + ".";
    }
}
