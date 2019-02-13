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

import javax.measure.Quantity;

import fr.liglab.adele.icasa.clockservice.Clock;

/**
 * This class represents a measurement of a time varying signal
 *
 */
public class Sample<Q extends Quantity<Q>> {
	
	private final long 			timestamp;
	private final Quantity<Q> 	value;
	
	private Sample(long timestamp, Quantity<Q> value) {
		this.timestamp	= timestamp;
		this.value		= value;
	}

	public Quantity<Q> getValue() {
		return value;
	}

	public final long getEllapsedTime(Clock clock) {
		return clock.currentTimeMillis() - this.timestamp;
	}

	public final long getEllapsedTime(Sample<?> since) {
		return this.timestamp - since.timestamp;
	}


	/**
	 * Fluent API to create samples
	 *
	 */

	public static class Value<Q extends Quantity<Q>> {
		
		private final Quantity<Q> value;
		
		private Value(Quantity<Q> value) {
			this.value = value;
		}
		
		public Sample<Q> atTimeOf(Clock clock) {
			return new Sample<>(clock.currentTimeMillis(),value);
		}

		public Sample<Q> atTimeOf(Sample<?> time) {
			return new Sample<>(time.timestamp,value);
		}
	}
	
	public static <Q extends Quantity<Q>> Value<Q> value(Quantity<Q> value) {
		return new Value<>(value);
	}


}