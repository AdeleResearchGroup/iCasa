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
package fr.liglab.adele.icasa.apps.demo.interop.comfort.management.context;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.device.PowerObservable;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import fr.liglab.adele.icasa.simulator.model.api.TemperatureModel;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = TemperatureModel.class)
public class SimulatedTemperatureModel implements TemperatureModel {
    //In Kelvin
    // Temperature in the morning (28)
    private static final double  MORNING_EXTERNAL_TEMPERATURE = 301.15;
    // Temperature in the afternoon (32)
    private static final double  AFTERNOON_EXTERNAL_TEMPERATURE = 305.15;
    // Temperature in the evening (28)
    private static final double  EVENING_EXTERNAL_TEMPERATURE = 301.15;
    // Temperature in the night (22)
    private static final double  NIGHT_EXTERNAL_TEMPERATURE = 295.15;

    /**
     * Define constants to compute the value of the thermal capacity
     */
    private static final double AIR_MASS_CAPACITY = 1000; // mass capacity of the air in J/(Kg.K)
    private static final double AIR_MASS = 1.2; // mass of the air in Kg/m^3
    private static final double HIGHEST_TEMP = 305.16;
    private static final double LOWER_TEMP = 283.16;
    private static final double DEFAULT_TEMP_VALUE = 298.15; // 25 celsius degrees in kelvin

    private static final double DEFAULT_MAX_POWER = 1000;


    public static final String RELATION_IS_ATTACHED="model.attached.to";

    @ContextEntity.State.Field(service = TemperatureModel.class,state = TemperatureModel.CURRENT_TEMPERATURE,value = "293.15")
    public double currentTemperature;

    @ContextEntity.State.Field(service = TemperatureModel.class,state = TemperatureModel.ZONE_ATTACHED)
    public String zoneName;


    @Validate
    public void validate(){
        lastUpdate = clock.currentTimeMillis();
    }

    @Invalidate
    public void invalidate(){

    }


    @Override
    public double getCurrentTemperature() {
        return currentTemperature;
    }

    @Requires
    Clock clock;

    @Requires(specification = MomentOfTheDay.class)
    MomentOfTheDay momentOfTheDay;

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

        double computeTemperature = getTemperatureFactor();

//        if (!( heatersInZone==null || heatersInZone.isEmpty() )|| !( coolersInZone==null || coolersInZone.isEmpty())){
//            double powerLevelTotal = getPowerInZone();
//
//            long timeDiff = clock.currentTimeMillis() - lastUpdate;
//            if (timeDiff < 0) {
//                timeDiff = 0;
//            }
//
//            double timeDiffInSeconds = timeDiff / 1000.0d;
//
//            if (powerLevelTotal == 0) {
//                if (lastTemperature > (DEFAULT_TEMP_VALUE + 0.5)) {
//                    powerLevelTotal = -50.0;
//                } else if (lastTemperature < (DEFAULT_TEMP_VALUE - 0.5)) {
//                    powerLevelTotal = 50.0;
//                } else {
//                    return lastTemperature;
//                }
//            }
//            if ((powerLevelTotal > 0) && (lastTemperature < DEFAULT_TEMP_VALUE)) {
//                powerLevelTotal += getPowerInZone();
//            } else if ((powerLevelTotal) < 0 && (lastTemperature > DEFAULT_TEMP_VALUE)) {
//                powerLevelTotal += getPowerInZone();
//            }
//
//            double delta = (powerLevelTotal * timeDiffInSeconds) / getThermalCapacity();
//
//            computeTemperature = lastTemperature + delta;
//
//            /**
//             * Clipping function to saturate the temperature at a certain level
//             */
//            if (computeTemperature > HIGHEST_TEMP)
//                computeTemperature = HIGHEST_TEMP;
//            else if (computeTemperature < LOWER_TEMP)
//                computeTemperature = LOWER_TEMP;
//
//            lastTemperature = computeTemperature;
//        }
        return computeTemperature ;
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

    private double getTemperatureFactor(){
        MomentOfTheDay.PartOfTheDay currentPartOfTheDay = momentOfTheDay.getCurrentPartOfTheDay();
        if (currentPartOfTheDay == null){
            return MORNING_EXTERNAL_TEMPERATURE;
        }
        switch (currentPartOfTheDay){
            case MORNING: return MORNING_EXTERNAL_TEMPERATURE;
            case EVENING: return EVENING_EXTERNAL_TEMPERATURE;
            case AFTERNOON: return AFTERNOON_EXTERNAL_TEMPERATURE;
            case NIGHT: return NIGHT_EXTERNAL_TEMPERATURE;
            default: return MORNING_EXTERNAL_TEMPERATURE;
        }
    }
}