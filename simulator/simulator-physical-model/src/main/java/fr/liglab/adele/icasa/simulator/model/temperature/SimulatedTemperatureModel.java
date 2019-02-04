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

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.device.PowerObservable;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.simulator.model.api.TemperatureModel;
import fr.liglab.adele.icasa.simulator.model.api.WeatherModel;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import org.apache.felix.ipojo.annotations.Validate;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Modified;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.measure.Quantity;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;
import javax.measure.quantity.Volume;

@ContextEntity(coreServices = {TemperatureModel.class, PeriodicRunnable.class})

public class SimulatedTemperatureModel implements TemperatureModel, PeriodicRunnable {

    public static final String RELATION_IS_ATTACHED="model.attached.to";

    @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone",specification=Zone.class,optional=false)
    Zone zone;

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

    @ContextEntity.State.Field(service = TemperatureModel.class,state = TemperatureModel.CURRENT_TEMPERATURE)
    public Quantity<Temperature> currentTemperature;

    @ContextEntity.State.Push(service = TemperatureModel.class, state =  TemperatureModel.CURRENT_TEMPERATURE)
    private Quantity<Temperature> pushTemperature() {
    	return estimateTemperature();
    }

    @Requires(optional=false)
    private Clock clock;

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

    @Override
    public Quantity<Temperature> getCurrentTemperature() {
        return currentTemperature;
    }

    @Validate
    public void validate() {
    	lastUpdateTime = clock.currentTimeMillis();
    }

    /**
     * Calculate the power lost from heat transfer from the exterior, a very basic simulation with a constant W/K factor
     * 
     */
    private final static Quantity<?> Q = Quantities.getQuantity(5.0d, Units.WATT.divide(Units.KELVIN));

    @Requires(id="weather", optional=false)
    private WeatherModel weather;

    @Modified(id="weather"	)
    public void estimateWeatherInfluence() {
    	
    	Quantity<Temperature> gap 	= lastTemperature.subtract(weather.getTemperature());
    	Quantity<Power> power 		= Q.multiply(gap).asType(Power.class);
    	
    	synchronized (this) {
    		weatherPower = power;
		}
    }

    /**
     * Calculate the temperature based on the loads in the room and the lost power
     * 
     */

    private long					lastUpdateTime;

    private Quantity<Temperature> 	lastTemperature = Quantities.getQuantity(20, Units.CELSIUS).to(Units.KELVIN);
    private Quantity<Power> 		powerToApply	= Quantities.getQuantity(0.0d, Units.WATT);
    private Quantity<Power> 		weatherPower	= Quantities.getQuantity(0.0d, Units.WATT);

    private synchronized Quantity<Temperature> estimateTemperature() {
    	
    	long currentTime		= clock.currentTimeMillis();
        long elapsedTime		= currentTime - lastUpdateTime;
        
  
        Quantity<Time> deltaTime = Quantities.getQuantity(elapsedTime <= 0 ? 0.0d : (elapsedTime / 1000.0d), Units.SECOND);
        
        /*
         * Calculate the increase/decrease of temperature due the applied power
         */
        Quantity<Temperature> deltaTemperature	= powerToApply.subtract(weatherPower).multiply(deltaTime).divide(getThermalCapacity()).asType(Temperature.class);

        lastTemperature = lastTemperature.add(deltaTemperature);
        
        if (lastTemperature.getValue().doubleValue() < 0.0d) {
        	lastTemperature =  Quantities.getQuantity(0.0d, Units.KELVIN);
        }
        
        lastUpdateTime	= currentTime;
        
        return lastTemperature;
    };

    /**
     * The current applied power in the zone (units: J/s = Watts) 
     */

    @Requires(id="coolers", specification = Cooler.class, filter = "(locatedobject.object.zone=${temperaturemodel.zone.attached})", optional = true)
    List<Cooler> coolersInZone;

    @Requires(id = "heaters", specification = Heater.class, filter = "(locatedobject.object.zone=${temperaturemodel.zone.attached})", optional = true)
    List<Heater> heatersInZone;

    
    @Modified(id="coolers")
    private void coolerModified() {
    	pushPower();
    }

    @Modified(id="heaters")
    private void haeterModified() {
    	pushPower();
    }

    private void pushPower() {
    	
    	Quantity<Power> updatedPowerToApply = getPowerInZone();

    	synchronized (this) {
			if (this.powerToApply.equals(updatedPowerToApply)) {
				return;
			}
		}
    	
    	pushTemperature();
    	
    	synchronized (this) {
        	powerToApply = updatedPowerToApply;
		}
    	
    }
    
    private Quantity<Power> getPowerInZone() {
    	
    	Quantity<Power> powerInZone = Quantities.getQuantity(0.0d, Units.WATT);
        
    	for (Heater heater : heatersInZone) {
    		powerInZone = powerInZone.add(power(heater));
        }
    	
        for (Cooler cooler : coolersInZone) {
    		powerInZone = powerInZone.subtract(power(cooler));
        }
        
        return powerInZone;
    }

    /**
     * Define constants to compute heating power in the zone for devices that do not get instant consumption
     */
    public static final Quantity<Power> DEFAULT_MAX_POWER = Quantities.getQuantity(1000.0d, Units.WATT);

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

    

    /**
     * Define constants to compute the value of the thermal capacity of the room
     */
    public static final Quantity<?> AIR_MASS_CAPACITY 				= Quantities.getQuantity(1000.0d, Units.JOULE.divide(Units.KILOGRAM.multiply(Units.KELVIN)));
    public static final Quantity<?> AIR_DENSITY 					= Quantities.getQuantity(1.2d, Units.KILOGRAM.divide(Units.CUBIC_METRE)); 

    /**
     * The thermal capacity of the zone (units J/°K)
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

}
