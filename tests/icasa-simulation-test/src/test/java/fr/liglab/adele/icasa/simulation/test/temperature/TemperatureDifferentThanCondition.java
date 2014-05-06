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
public class TemperatureDifferentThanCondition extends TemperatureVarExistsCondition {
    
    private double _goalValue;
        
    private SizeCondition _sizeCondition;
    
    
    public TemperatureDifferentThanCondition(Zone zone, double originalValue, double delta, SizeCondition sizeCondition) {
        super(zone);
        
        _sizeCondition = sizeCondition;        
        
        if (sizeCondition == SizeCondition.BIGGER) {
            _goalValue = originalValue + delta;
        } else {
            _goalValue = originalValue - delta;
        }
    }

    public boolean isChecked() {
        if (!super.isChecked())
            return false;
            
        Object tempObj = _zone.getVariableValue("Temperature");
          
            
        if ((tempObj == null) || !(tempObj instanceof Double))
            return false;
        
        double newValue = (Double) tempObj;
        
        if (_sizeCondition == SizeCondition.BIGGER) {
            return newValue >_goalValue;
        } else {
            return newValue < _goalValue;
        }
    }

    public String getDescription() {
        String temp = "bigger";
        
        if (_sizeCondition == SizeCondition.SMALLER)
            temp = "smaller";
        
        return "Temperature variable must be" + temp +" than " + _goalValue + " on zone " + _zone.getId() + ".";
    }
    
    enum SizeCondition {
        BIGGER, SMALLER
    }
}
