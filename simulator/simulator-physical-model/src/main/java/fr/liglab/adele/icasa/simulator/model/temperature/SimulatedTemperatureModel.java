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
import fr.liglab.adele.icasa.simulator.model.api.TemperatureModel;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = TemperatureModel.class)
public class SimulatedTemperatureModel implements TemperatureModel {

    public static final String RELATION_IS_ATTACHED="model.attached.to";

    @ContextEntity.State.Field(service = TemperatureModel.class,state = TemperatureModel.CURRENT_TEMPERATURE,value = "293.15")
    public double currentTemperature;

    @ContextEntity.State.Field(service = TemperatureModel.class,state = TemperatureModel.ZONE_ATTACHED)
    public String zoneName;

    @Override
    public double getCurrentTemperature() {
        return currentTemperature;
    }

    /**
     * Define constants to compute the value of the thermal capacity
     */
    public static final double AIR_MASS_CAPACITY = 1000; // mass capacity of the air in J/(Kg.K)
    public static final double AIR_MASS = 1.2; // mass of the air in Kg/m^3
    public static final double HIGHEST_TEMP = 303.16;
    public static final double LOWER_TEMP = 283.16;
    public static final double DEFAULT_TEMP_VALUE = 293.15; // 20 celsius degrees in kelvin

    public static final double DEFAULT_MAX_POWER = 1000;

    @Validate
    public void validate() {
    	lastUpdateTime = clock.currentTimeMillis();
    }

    @Invalidate
    public void invalidate() {

    }

    @Requires(optional=false)
    private Clock clock;

    @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone",specification=Zone.class,optional=false)
    Zone zone;

    @Bind(id = "zone")
    public void bindZone(Zone zone){
        pushZone(zone.getZoneName());
    }

    @ContextEntity.State.Push(service = TemperatureModel.class,state = TemperatureModel.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    private double getThermalCapacity() {
        
    	double zoneVolume =  zone.getYLength()*zone.getXLength()*zone.getZLength();

        /* use a default volume for zones without dimensions to avoid divide by zero */
        if (zoneVolume == 0.0d) {
            zoneVolume =  2.5d;
        }
        return AIR_MASS * AIR_MASS_CAPACITY * zoneVolume;

    }

    @Requires(id="coolers", specification = Cooler.class, filter = "(locatedobject.object.zone=${temperaturemodel.zone.attached})", optional = true)
    List<Cooler> coolersInZone;

    @Requires(id = "heaters", specification = Heater.class, filter = "(locatedobject.object.zone=${temperaturemodel.zone.attached})", optional = true)
    List<Heater> heatersInZone;

    /**
     * recalculates the temperature on-demand or when the heater/chillers are acted on
     * 
     *  TODO: push value as time passes
     *
     */
    @ContextEntity.State.Pull(service = TemperatureModel.class, state = TemperatureModel.CURRENT_TEMPERATURE)
    Supplier<Double> pullCurrentTemp = this::estimateTemperature;
    
    @ContextEntity.State.Push(service = TemperatureModel.class, state =  TemperatureModel.CURRENT_TEMPERATURE)
    private double pushTemperature() {
    	return estimateTemperature();
    }
    
    @Modified(id="coolers")
    private void coolerModified() {
    	pushTemperature();
    }

    @Modified(id="heaters")
    private void haeterModified() {
    	pushTemperature();
    }

    private double getPowerInZone() {
        double powerInZone = 0;
        for (Heater heater : heatersInZone){
            if (heater instanceof PowerObservable){
                powerInZone +=((PowerObservable) heater).getCurrentConsumption();
            }else {
                powerInZone += heater.getPowerLevel() * DEFAULT_MAX_POWER;
            }
        }
        for (Cooler cooler : coolersInZone){
            if (cooler instanceof PowerObservable){
                powerInZone -= ((PowerObservable) cooler).getCurrentConsumption();
            }else {
                powerInZone -= cooler.getPowerLevel() * DEFAULT_MAX_POWER;
            }
        }
        return powerInZone;
    }

    private long lastUpdateTime;

    private double lastTemperature = DEFAULT_TEMP_VALUE;

    private double estimateTemperature() {
    	
    	long currentTime		= clock.currentTimeMillis();
        long elapsedTime		= currentTime - lastUpdateTime;
        
        double elapsedSeconds 	= elapsedTime <= 0 ? 0.0d : (elapsedTime / 1000.0d);

        double powerLevelTotal = getPowerInZone();

        /*
         * When there are no heat/chill sources, oscillate around the default temperature
         */
        if (powerLevelTotal == 0){
            if ( lastTemperature > (DEFAULT_TEMP_VALUE + 0.5) ) {
                powerLevelTotal = -50.0;
            } else if ( lastTemperature < (DEFAULT_TEMP_VALUE - 0.5) ){
                powerLevelTotal = 50.0;
            }else {
                return lastTemperature;
            }
        }
        
        if ( (powerLevelTotal > 0) && (lastTemperature < DEFAULT_TEMP_VALUE) ) {
            powerLevelTotal += 50.0d;
        } else if( (powerLevelTotal) < 0 && (lastTemperature > DEFAULT_TEMP_VALUE) ) {
            powerLevelTotal -= 50.0d;
        }


        double estimatedTemperature = lastTemperature  + ((powerLevelTotal  * elapsedSeconds) / getThermalCapacity());

        /**
         * Clipping function to saturate the temperature at a certain level
         */
        if (estimatedTemperature > HIGHEST_TEMP)
        	estimatedTemperature = HIGHEST_TEMP;
        else if (estimatedTemperature < LOWER_TEMP)
        	estimatedTemperature = LOWER_TEMP;

        lastTemperature = estimatedTemperature;
        lastUpdateTime	= currentTime;
        
        return lastTemperature ;
    };


}
