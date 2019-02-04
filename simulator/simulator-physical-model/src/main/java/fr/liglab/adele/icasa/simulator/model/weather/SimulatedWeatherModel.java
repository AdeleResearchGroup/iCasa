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
package fr.liglab.adele.icasa.simulator.model.weather;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

import fr.liglab.adele.icasa.simulator.model.api.WeatherModel;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import fr.liglab.adele.icasa.clockservice.Clock;

import org.apache.felix.ipojo.annotations.Requires;
import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

@ContextEntity(coreServices = {WeatherModel.class, PeriodicRunnable.class})

public class SimulatedWeatherModel implements WeatherModel, PeriodicRunnable {


    @ContextEntity.State.Field(service = WeatherModel.class,state = WeatherModel.CURRENT_TEMPERATURE)
    private Quantity<Temperature> currentTemperature;

    @Override
    public Quantity<Temperature> getTemperature() {
    	return currentTemperature;
    }

    @Requires(optional=false)
    private Clock clock;

    /**
     * recalculates the temperature on-demand or as time passes
     * 
     */
    @ContextEntity.State.Pull(service = WeatherModel.class, state = WeatherModel.CURRENT_TEMPERATURE)
    Supplier<Quantity<Temperature>> pullTemperature = this::getCurrentTemperature;
    
    @ContextEntity.State.Push(service = WeatherModel.class, state = WeatherModel.CURRENT_TEMPERATURE)
    public Quantity<Temperature> pushTemperature() {
        return getCurrentTemperature();
    }

	@Override
	public long getPeriod() {
		return 1;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.HOURS;
	}

	@Override
	public void run() {
		pushTemperature();
	}

    private Quantity<Temperature> getCurrentTemperature() {
    	return getTemperature(new DateTime(clock.currentTimeMillis()));
    }
    
    private Quantity<Temperature> getTemperature(DateTime instant) {
    	return Quantities.getQuantity(interpolator.valueAt(instant), Units.CELSIUS).to(Units.KELVIN);
    };

    /**
     * Get the model used to represent the temperatures of a typical day of the year
     */
    private static Interpolator interpolator = new EstimatedLinearInterpolator();


    private interface Interpolator {

    	/**
    	 * The value of the temperature at the specified moment
    	 */
    	public double valueAt(DateTime instant);
    }

    private static class EstimatedLinearInterpolator implements Interpolator {

		@Override
		public double valueAt(DateTime now) {
			
			DateTime yesterday 		= now.minusDays(1);
			DateTime tomorrow 		= now.plusDays(1);
			
			int previuosIntervals[]	= DataSet.DAILY_INTERVALS[yesterday.getMonthOfYear()-1];
			int intervals[]			= DataSet.DAILY_INTERVALS[now.getMonthOfYear()-1];
			int nextIntervals[]		= DataSet.DAILY_INTERVALS[tomorrow.getMonthOfYear()-1];

			double times[] 			= new double[] {
										previuosIntervals[3]- 24,
										intervals[0],intervals[1],intervals[2],intervals[3],
										24 + nextIntervals[0]
									};
			
			double temperatures[] 	= new double[] {
										DataSet.DAILY_MAX_TEMPERATURE[yesterday.getDayOfYear()-1] - DataSet.PLATEAU_HEIGHT[yesterday.getMonthOfYear()-1],
										DataSet.DAILY_MIN_TEMPERATURE[now.getDayOfYear()-1],
										DataSet.DAILY_MAX_TEMPERATURE[now.getDayOfYear()-1] - DataSet.PLATEAU_HEIGHT[now.getMonthOfYear()-1],
										DataSet.DAILY_MAX_TEMPERATURE[now.getDayOfYear()-1],
										DataSet.DAILY_MAX_TEMPERATURE[now.getDayOfYear()-1] - DataSet.PLATEAU_HEIGHT[now.getMonthOfYear()-1],
										DataSet.DAILY_MIN_TEMPERATURE[tomorrow.getDayOfYear()-1]
									};
			
 			return interpolate(times,temperatures, now.getHourOfDay()+ (now.getMinuteOfHour() / 60.0d));
		}

        /**
         * The function used to linearly interpolate the temperature of a given hour in a day using a given set of
         * segments
         */

    	private static final double interpolate(double times[], double temperatures[], double hour) {

    		assert times.length >= 3;
    		assert times.length == temperatures.length;
    		

    		/*
    		 * Find the interval the hour is located and do a linear interpolation from the two ends
    		 * 
    		 */
    		for  (int index= 1; index < times.length; index++) {
    		
    			if ( (times[index-1] <= hour) && ( hour <= times[index])) {
    				
    				final double t0 = times[index-1];
    				final double t1 = times[index];
    				
    				final double temp0 = temperatures[index-1];
    				final double temp1 = temperatures[index];
    				
    				return temp0 + (((hour - t0) / (t1 -t0)) * (temp1 - temp0));
    			}
    		}
    		
    		return Double.NaN;
    	}


    }
   
    /**
     * Historical data of daily minimum/maximum temperatures (measure in celsius) in Paris over 2018 
     */
    private static class DataSet {
    	
	    public static final int DAILY_MIN_TEMPERATURE [] = {
			7,6,8,7,5,3,6,5,2,7,4,4,1,1,2,6,3,6,3,4,5,7,8,11,8,4,3,5,9,8,5,
			2,2,1,0,-2,-1,-3,-7,-5,-3,2,-1,-2,1,4,0,2,-2,-1,3,0,-2,-2,-1,-2,-5,-6,-9,
			-5,0,-2,1,3,6,4,4,5,10,9,9,7,1,8,7,1,-1,-2,-2,1,-1,5,5,4,6,5,4,2,4,4,
			6,7,8,7,5,2,8,11,11,8,5,8,8,7,7,8,5,8,11,12,12,13,13,8,12,7,6,12,9,4,
			4,3,6,6,8,12,12,11,12,11,6,11,9,10,11,9,10,6,7,8,11,13,12,13,12,15,17,16,16,15,17,
			16,16,14,17,16,14,16,15,14,16,17,15,10,10,14,13,14,17,17,14,13,8,9,12,12,13,14,16,16,18,
			19,19,19,18,18,16,18,18,16,16,13,13,17,17,18,17,18,22,16,19,17,16,18,18,19,20,20,18,19,20,20,
			15,16,19,19,16,17,21,18,16,13,14,13,18,16,16,14,16,12,13,19,18,16,16,14,12,7,15,10,16,14,13,
			9,9,11,15,18,15,12,8,9,14,12,12,15,10,8,8,9,16,17,14,14,11,11,7,5,5,5,9,7,3,
			9,9,13,11,7,9,10,7,6,10,16,15,15,14,15,14,11,11,9,7,4,9,3,8,9,7,2,4,3,3,2,
			8,2,-2,1,7,11,9,6,4,11,11,10,6,4,6,5,0,1,0,1,-3,1,2,5,3,4,5,6,10,7,
			4,11,12,7,6,11,7,7,8,4,2,-1,-2,-2,-3,1,4,5,7,7,8,9,8,3,-1,-2,-2,-1,1,7,8
		};
	    
	    public static final int DAILY_MAX_TEMPERATURE [] = {
			9,13,11,14,11,8,6,8,9,9,8,6,6,9,11,11,8,11,6,11,11,13,13,13,9,10,7,11,11,11,9,
			8,7,5,3,1,0,0,3,2,6,8,7,4,5,12,9,9,9,6,6,4,5,4,6,3,0,1,-1,
			2,9,11,8,13,12,11,9,14,17,18,13,13,14,13,14,7,1,2,8,8,8,7,13,16,13,13,14,12,11,11,
			11,16,15,14,12,19,22,24,15,16,18,14,18,17,17,17,22,26,28,28,27,27,19,21,17,16,19,16,12,7,
			14,17,17,19,23,26,26,27,22,17,22,22,15,13,19,22,19,18,19,23,24,24,19,25,26,27,28,27,23,26,25,
			21,24,26,26,22,23,26,24,23,23,21,19,18,21,24,23,21,23,24,28,20,21,22,22,24,26,28,28,30,30,
			32,30,30,30,27,29,30,30,27,22,22,26,27,30,31,26,23,28,30,26,25,28,30,31,33,35,36,26,27,29,27,
			30,32,35,33,30,34,37,27,20,23,25,31,24,25,25,31,25,25,28,24,27,30,26,21,20,23,23,27,22,21,22,
			22,24,24,21,26,21,21,23,29,25,29,28,18,22,22,25,29,24,26,26,19,19,22,17,17,21,26,19,18,18,
			16,16,19,21,24,23,14,18,23,25,26,26,27,27,27,25,21,19,18,17,18,15,17,16,16,14,10,7,6,11,11,
			13,12,11,13,20,19,13,13,15,13,13,12,16,14,9,7,8,8,6,3,6,4,6,9,8,6,9,12,14,13,
			12,16,15,12,12,13,12,12,11,10,9,4,4,2,2,8,10,9,11,11,16,14,14,13,8,4,2,2,8,9,10
		};
	    
	    /**
	     * Approximate hours for minimum, maximum, and mid-day plateau
	     */
	    public static final int DAILY_INTERVALS [][] = {
	       	{7,12,15,17},
	       	{7,12,15,17},
	       	{7,12,15,18},
	       	{6,12,16,19},
	       	{6,12,16,19},
	       	{6,12,16,20},
	       	{5,12,17,20},
	       	{6,12,16,19},
	       	{7,12,15,18},
	       	{7,12,15,17},
	       	{7,12,14,16},
	       	{7,12,14,16} 
	    };
	    
	
	    public static final int PLATEAU_HEIGHT [] = {
	    	1, 1, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1
	    };
    }
}
