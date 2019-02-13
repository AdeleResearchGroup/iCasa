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
package fr.liglab.adele.icasa.simulator.model.temperature;

import java.util.List;
import java.util.function.Supplier;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

import fr.liglab.adele.icasa.clockservice.Clock;

import fr.liglab.adele.icasa.location.Zone;

import fr.liglab.adele.icasa.device.PowerObservable;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;

import fr.liglab.adele.icasa.simulator.model.api.WeatherModel;
import fr.liglab.adele.icasa.simulator.model.measure.Integral;
import fr.liglab.adele.icasa.simulator.model.measure.Sample;
import fr.liglab.adele.icasa.simulator.model.measure.Signal;
import fr.liglab.adele.icasa.simulator.model.api.TemperatureModel;


import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Volume;

@ContextEntity(coreServices = {TemperatureModel.class	})

public class SimulatedTemperatureModel implements TemperatureModel {

    public static final String RELATION_IS_ATTACHED="model.attached.to";

    @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone", optional=false)
    private Zone zone;

    @ContextEntity.State.Field(service = TemperatureModel.class,state = TemperatureModel.ZONE_ATTACHED)
    public String zoneName;

    @ContextEntity.State.Push(service = TemperatureModel.class,state = TemperatureModel.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    @Bind(id = "zone")
    public void bindZone(Zone zone){
        pushZone(zone.getZoneName());
    }

    @Requires(optional=false)
    private Clock clock;


    private final Integral<Energy,Power> appliedPower 	= new Integral<>(this::getPowerInZone, Units.JOULE, Energy.class);
    private final Integral<Energy,Power> transferPower	= new Integral<>((this::getTransferPower), Units.JOULE, Energy.class);

    private volatile EstimatedTemperature estimation;

    @Validate
    public void validated() {
    	
    	estimation = this.new EstimatedTemperature(clock, weather.getTemperature());
    	
    	appliedPower.sample(clock,true);
    	transferPower.sample(clock,true);
    }

    @Invalidate
    public void invalidated() {
    	estimation = null;
    }

    /**
     * A signal that represents the last calculated temperature based on the heat sources of the zone, it keeps a memoized value
     * to avoid triggering the recalculation of the temperature at each sample
     */

    private class EstimatedTemperature implements Signal<Temperature> {

    	private Sample<Temperature> lastEstimation;
    	
    	public EstimatedTemperature(Clock clock, Quantity<Temperature> initial) {
    		this.lastEstimation = Sample.value(initial.to(Units.KELVIN)).atTimeOf(clock);
    	}
    	
		@Override
		public Sample<Temperature> sample(Clock clock) {
			return sample(clock,false);
		}

	    /**
	     * Calculate the temperature based on the heat sources in the room and the lost power
	     * 
	     */
		public Sample<Temperature> sample(Clock clock, boolean update) {
			
			if (!update) {
				return lastEstimation;
			}
			
	        /*
	         * Calculate the increase/decrease of temperature due the power sources affecting the zone
	         */
	        Quantity<Temperature> deltaTemperature	= appliedPower.sample(clock,true).getValue().
	        											subtract(transferPower.sample(clock,true).getValue()).
	        											divide(getThermalCapacity()).
	        											asType(Temperature.class);

	        synchronized (this) {

	        	Quantity<Temperature> newEstimation = lastEstimation.getValue().add(deltaTemperature);
		        
		        if (newEstimation.getValue().doubleValue() < 0.0d) {
		        	newEstimation =  Quantities.getQuantity(0.0d, Units.KELVIN);
		        }
		        
		        lastEstimation = Sample.value(newEstimation).atTimeOf(clock);
		        return lastEstimation;
			}
			
		}
    }
    

    @ContextEntity.State.Field(service = TemperatureModel.class,state = TemperatureModel.CURRENT_TEMPERATURE)
    public Quantity<Temperature> currentTemperature;

    @Override
    public Quantity<Temperature> getTemperature() {
        return currentTemperature;
    }

    @ContextEntity.State.Pull(service = TemperatureModel.class, state = TemperatureModel.CURRENT_TEMPERATURE)
    Supplier<Quantity<Temperature>> pullTemperature = this::estimateTemperature;


    private Quantity<Temperature> estimateTemperature() {
    	return estimation.sample(clock,true).getValue();
    }

    @ContextEntity.State.Push(service = TemperatureModel.class, state =  TemperatureModel.CURRENT_TEMPERATURE)
    private Quantity<Temperature> pushTemperature() {
    	return estimateTemperature();
    }

    private void update() {
    	if (estimation != null) {
    		pushTemperature();
    	}
    }


    /**
     * The model of the power loss due to heat transfer from the exterior of the home,  a very basic simulation with a constant W/K factor
     * 
     * IMPORTANT Notice that calculation of the transfer power requires the estimated zone temperature and vice-versa. To avoid infinite
     * recursion we request the last memoized value when sampling the estimated temperature
     */
    @Requires(id="weather", optional=false)
    private WeatherModel weather;


    @Modified(id="weather")
    public void weatherModelUpdated() {
    	update();
    }

    

    private final static Quantity<?> Q = Quantities.getQuantity(5.0d, Units.WATT.divide(Units.KELVIN));

    public Sample<Power> getTransferPower(Clock clock) {
    	
    	Quantity<Temperature> gap 	= estimation.sample(clock,false).getValue().subtract(weather.getTemperature());
    	return Sample.value(Q.multiply(gap).asType(Power.class)).atTimeOf(clock);
    }

    /**
     * The current applied power in the zone by heaters and coolers 
     */

    @Requires(id="coolers", specification = Cooler.class, filter = "(locatedobject.object.zone=${temperaturemodel.zone.attached})", optional = true)
    List<Cooler> coolersInZone;

    @Requires(id = "heaters", specification = Heater.class, filter = "(locatedobject.object.zone=${temperaturemodel.zone.attached})", optional = true)
    List<Heater> heatersInZone;

    private Sample<Power> getPowerInZone(Clock clock) {
    	
    	Quantity<Power> powerInZone = Quantities.getQuantity(0.0d, Units.WATT);
        
    	for (Heater heater : heatersInZone) {
    		powerInZone = powerInZone.add(power(heater));
        }
    	
        for (Cooler cooler : coolersInZone) {
    		powerInZone = powerInZone.subtract(power(cooler));
        }
        
        return Sample.value(powerInZone).atTimeOf(clock);
    }

    @Bind(id="coolers")
    public void coolerBound() {
    	update();
    }

    @Unbind(id="coolers")
    public void coolerUnbound() {
    	update();
    }

    @Modified(id="coolers")
    public void coolerUpdated() {
    	update();
    }
    
    @Bind(id="heaters")
    public void heaterBound() {
    	update();
    }

    @Unbind(id="heaters")
    public void heaterUnbound() {
    	update();
    }

    @Modified(id="heaters")
    public void heaterUpdated() {
    	update();
    }

    /**
     * Define constants to compute the value of the thermal capacity of the room
     */
    public static final Quantity<?> AIR_MASS_CAPACITY 	= Quantities.getQuantity(1000.0d, Units.JOULE.divide(Units.KILOGRAM.multiply(Units.KELVIN)));
    public static final Quantity<?> AIR_DENSITY 		= Quantities.getQuantity(1.2d, Units.KILOGRAM.divide(Units.CUBIC_METRE)); 

    /**
     * The thermal capacity of the zone (units J/K)
     * 
     */
    private Quantity<?> getThermalCapacity() {
        
    	Quantity<Volume> zoneVolume =  Quantities.getQuantity(zone.getYLength() * zone.getXLength() * zone.getZLength(), Units.CUBIC_METRE);

        /*
         * TODO currently the simulator DO NOT handle physical measures of zones (the calculated volume is then wrong as it is just some pixel
         * value used by the front-end), by now just use a constant volume for all the zones
         */
    	
        if (true) {
            zoneVolume =  Quantities.getQuantity(100.0d, Units.CUBIC_METRE);
        }
        
        return AIR_MASS_CAPACITY.multiply(AIR_DENSITY.multiply(zoneVolume));

    }


    /**
     * Define constants to compute heating power in the zone for devices that do not provide instant power consumption
     */
    public static final Quantity<Power> DEFAULT_MAX_POWER = Quantities.getQuantity(1000.0d, Units.WATT);

    /**
     * The measured power of a heat load in the zone
     */
    private final static Quantity<Power> power(Heater heater) {
    	
    	if (heater instanceof PowerObservable) {
    		double value = ((PowerObservable) heater).getCurrentConsumption();
            return Quantities.getQuantity(value, Units.WATT);
        }
    	 
    	return DEFAULT_MAX_POWER.multiply(heater.getPowerLevel());
    }

    private final static Quantity<Power> power(Cooler cooler) {

    	if (cooler instanceof PowerObservable) {
    		double value = ((PowerObservable) cooler).getCurrentConsumption();
            return Quantities.getQuantity(value, Units.WATT);
        }
    	 
    	return DEFAULT_MAX_POWER.multiply(cooler.getPowerLevel());
    }


}
