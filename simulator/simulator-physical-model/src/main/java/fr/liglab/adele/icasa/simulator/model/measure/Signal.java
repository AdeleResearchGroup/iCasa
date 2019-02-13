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
package fr.liglab.adele.icasa.simulator.model.measure;

import java.util.function.Function;

import javax.measure.Quantity;

import fr.liglab.adele.icasa.clockservice.Clock;

/**
 * A representation of a varying time signal overt the logical clock 
 */
@FunctionalInterface
public interface Signal<Q extends Quantity<Q>> extends Function<Clock,Sample<Q>> {
	
	/**
	 * Perform a measurement of the underlying signal
	 */
	public Sample<Q> sample(Clock clock);
	
	@Override
	default Sample<Q> apply(Clock clock) {
		return sample(clock);
	}
	
}