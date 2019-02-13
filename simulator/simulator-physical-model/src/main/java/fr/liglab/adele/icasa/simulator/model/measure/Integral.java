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
import javax.measure.Unit;
import javax.measure.quantity.Time;

import fr.liglab.adele.icasa.clockservice.Clock;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

/**
 * A representation of the integral of a signal
 *
 */
public class Integral<I extends Quantity<I>, Q extends Quantity<Q>> implements Signal<I> {
    
	private final Signal<Q>		signal;
	private final Unit<I> 		unit;
	private final Class<I>		type;

	private Quantity<I>			integral;
	private Sample<Q> 			lastMeasure;

	public Integral(Signal<Q> signal, Unit<I> unit, Class<I> type) {
		
		this.signal			= signal;
		this.unit			= unit;
		this.type			= type;
		
		this.integral 		= Quantities.getQuantity(0.0d,this.unit);
   		this.lastMeasure	= null;
	}
	
	/**
	 * samples the integrated signal
	 */

   	public Sample<I> sample(Clock clock) {
   		return sample(clock,false);
   	}
   	
	/**
	 * samples the base signal and integrates its current value, optionally resetting the cumulated value
	 */
	public Sample<I> sample(Clock clock, boolean reset) {

		Sample<Q> measure = signal.sample(clock);


		/*
		 * Integrate the previous measure into the accumulated value 
		 */
		synchronized (this) {

			if (lastMeasure != null) {
				
	    		assert integral.getUnit().isCompatible(lastMeasure.getValue().getUnit().multiply(Units.SECOND));

	    		
		        long elapsedTime			= measure.getEllapsedTime(lastMeasure);
		        Quantity<Time> deltaTime	= Quantities.getQuantity(elapsedTime <= 0 ? 0.0d : (elapsedTime / 1000.0d), Units.SECOND);
		        
		        integral = integral.add(lastMeasure.getValue().multiply(deltaTime).asType(type).to(unit));

			}

			Sample<I> current	= Sample.value(integral).atTimeOf(measure);

			this.integral 		= reset ? Quantities.getQuantity(0.0d,this.unit) : current.getValue();
	   		this.lastMeasure	= measure;

			return current;

		}
		
	}
	

}