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
    public void validate(){
        lastUpdate = clock.currentTimeMillis();
    }

    @Invalidate
    public void invalidate(){

    }

    @Requires
    private Clock clock;

    @Requires(specification = Cooler.class,filter = "(locatedobject.object.zone=${temperaturemodel.zone.attached})",optional = true)
    List<Cooler> coolersInZone;

    @Requires(specification = Heater.class,filter = "(locatedobject.object.zone=${temperaturemodel.zone.attached})",optional = true)
    List<Heater> heatersInZone;

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

    private long lastUpdate;

    private double lastTemperature = DEFAULT_TEMP_VALUE;

    @ContextEntity.State.Pull(service = TemperatureModel.class,state = TemperatureModel.CURRENT_TEMPERATURE, period = 1)
    Supplier<Double> pullCurrentTemp = () -> {

        double computeTemperature;

        long timeDiff = clock.currentTimeMillis() - lastUpdate;
        if (timeDiff < 0){
            timeDiff = 0;
        }
        double newTemperature = DEFAULT_TEMP_VALUE; // 20 degrees by default

        double powerLevelTotal = getPowerInZone();
        double timeDiffInSeconds = timeDiff / 1000.0d;

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
            powerLevelTotal += getPowerInZone();
        } else if( (powerLevelTotal) < 0 && (lastTemperature > DEFAULT_TEMP_VALUE) ) {
            powerLevelTotal += getPowerInZone();
        }

        double delta = (powerLevelTotal  * timeDiffInSeconds) / getThermalCapacity();

        newTemperature = lastTemperature  + delta;

        /**
         * Clipping function to saturate the temperature at a certain level
         */
        if (newTemperature > HIGHEST_TEMP)
            newTemperature = HIGHEST_TEMP;
        else if (newTemperature < LOWER_TEMP)
            newTemperature = LOWER_TEMP;

        lastTemperature = newTemperature;

        return newTemperature ;
    };

    private double getPowerInZone(){
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

    private double getThermalCapacity() {
        double newVolume = 2.5d; // use this value as default to avoid divide by zero
        double zoneVolume =  (zone.getYLength()*zone.getXLength()*zone.getZLength());
        if (zoneVolume > 0.0d){
            newVolume =  zoneVolume;
        }
        return AIR_MASS * AIR_MASS_CAPACITY * newVolume;

    }
}
