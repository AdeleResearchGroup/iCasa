/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.freshness.scenario.application2;

import fr.liglab.adele.freshness.facilities.ipojo.annotation.Freshness;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component(name = "FreshnessApplication2")

@Provides(properties = {
        @StaticServiceProperty(name = "icasa.application", type = "String", value = "Freshness.Scenario.Application2", immutable = true)
}, specifications = {PeriodicRunnable.class})
@Instantiate
public class FreshnessApplication2 implements PeriodicRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(FreshnessApplication2.class);

    private final double temperatureTarget;

    private final Object m_lock;

    private final Object m_Energylock;

    private double maximumEnergyAllowed = 1;

    public FreshnessApplication2() {
        m_lock = new Object();
        m_Energylock = new Object();

        temperatureTarget = 288.15;
    }

    /**
     * Component Lifecycle Method
     */
    @Invalidate
    public void stop() {

    }

    /**
     * Component Lifecycle Method
     */
    @Validate
    public void start() {

    }

    @Freshness(time = 600)
    @Requires(id = "thermometers", optional = true, specification = Thermometer.class, filter = "(locatedobject.object.zone=wineHouse)", proxy = false)
    private List<Thermometer> thermometers;

    @Requires(id = "heaters", optional = true, specification = Heater.class, filter = "(locatedobject.object.zone=wineHouse)", proxy = false)
    private List<Heater> heaters;

    @Requires(id = "coolers", optional = true, specification = Cooler.class, filter = "(locatedobject.object.zone=wineHouse)", proxy = false)
    private List<Cooler> coolers;

    @Bind(id = "thermometers")
    public void bindThermometer(Thermometer thermometer) {
    }

    @Unbind(id = "thermometers")
    public void unbindThermometer(Thermometer thermometer) {
    }

    @Bind(id = "heaters")
    public void bindHeater(Heater heater) {
    }

    @Unbind(id = "heaters")
    public void unbindHeater(Heater heater) {
    }

    @Bind(id = "coolers")
    public void bindCooler(Cooler cooler) {
    }

    @Unbind(id = "coolers")
    public void unbindCooler(Cooler cooler) {
    }

    @Override
    public long getPeriod() {
        return 10;
    }

    @Override
    public TimeUnit getUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    public void run() {
        synchronized (m_lock) {
            double tempInZone = getAverageTemperature();
            synchronized (m_Energylock) {
                if (tempInZone > temperatureTarget + 1) {
                    for (Cooler cooler : coolers) {
                        cooler.setPowerLevel(maximumEnergyAllowed);
                    }
                    for (Heater heater : heaters) {
                        heater.setPowerLevel(0);
                    }
                } else if (tempInZone < temperatureTarget - 1) {
                    for (Heater heater : heaters) {
                        heater.setPowerLevel(maximumEnergyAllowed);
                    }
                    for (Cooler cooler : coolers) {
                        cooler.setPowerLevel(0);
                    }
                } else if (tempInZone < temperatureTarget - 0.5) {
                    for (Heater heater : heaters) {
                        heater.setPowerLevel(0.03);
                    }
                    for (Cooler cooler : coolers) {
                        cooler.setPowerLevel(0);
                    }
                } else if (tempInZone > temperatureTarget + 0.5) {
                    for (Cooler cooler : coolers) {
                        cooler.setPowerLevel(0.03);
                    }
                    for (Heater heater : heaters) {
                        heater.setPowerLevel(0);
                    }
                } else {
                    for (Heater heater : heaters) {
                        heater.setPowerLevel(0);
                    }
                    for (Cooler cooler : coolers) {
                        cooler.setPowerLevel(0);
                    }
                }
            }
        }
    }

    public double getAverageTemperature() {
        double tempSum = 0;
        for (Thermometer thermometer : thermometers) {
            Quantity<Temperature> tempValue = thermometer.getTemperature();
            tempSum += tempValue.getValue().doubleValue();
        }
        return tempSum / thermometers.size();
    }
}
