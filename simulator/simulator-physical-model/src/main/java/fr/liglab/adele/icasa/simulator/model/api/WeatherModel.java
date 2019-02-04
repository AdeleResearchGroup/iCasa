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
package fr.liglab.adele.icasa.simulator.model.api;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;


/**
 * The weather conditions at the current moment of the simulation.
 * 
 * @author vega
 *
 */
public @ContextService interface WeatherModel {

    public static final @State
    String CURRENT_TEMPERATURE = "current.temperature";

    public Quantity<Temperature> getTemperature();

}
