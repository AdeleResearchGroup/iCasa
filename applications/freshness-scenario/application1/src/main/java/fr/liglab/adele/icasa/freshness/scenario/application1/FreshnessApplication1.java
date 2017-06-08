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
package fr.liglab.adele.icasa.freshness.scenario.application1;

import fr.liglab.adele.freshness.facilities.ipojo.annotation.Freshness;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component(name = "FreshnessApplication1")

@Provides(properties = {
        @StaticServiceProperty(name = "icasa.application", type = "String", value = "Freshness.Scenario.Application1", immutable = true)
}, specifications = {PeriodicRunnable.class})
@Instantiate
public class FreshnessApplication1 implements PeriodicRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(FreshnessApplication1.class);

    /**
     * The name of the LOCATION property
     */
    public static final String LOCATION_PROPERTY_NAME = "Location";

    /**
     * The name of the location for unknown value
     */
    public static final String LOCATION_UNKNOWN = "unknown";


    private Map<String, Double> mapTemperatureTarget;

    private Map<String, Boolean> mapManagementAuto;

    private final Object m_lock;

    private final Object m_Energylock;

    private double maximumEnergyAllowed = 1;

    public FreshnessApplication1() {
        m_lock = new Object();
        m_Energylock = new Object();
        mapTemperatureTarget = new HashMap<String, Double>();
        mapManagementAuto = new HashMap<String, Boolean>();

        mapTemperatureTarget.put("kitchen", 288.15);
        mapTemperatureTarget.put("livingroom", 291.15);
        mapTemperatureTarget.put("bedroom", 293.15);
        mapTemperatureTarget.put("bathroom", 296.15);

        mapManagementAuto.put("kitchen", true);
        mapManagementAuto.put("livingroom", true);
        mapManagementAuto.put("bedroom", true);
        mapManagementAuto.put("bathroom", true);
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

    @Freshness(time = 40)
    @Requires(id = "thermometers", optional = true, specification = Thermometer.class, filter = "(!(locatedobject.object.zone=" + LocatedObject.LOCATION_UNKNOWN + "))", proxy = false)
    private List<Thermometer> thermometers;

    @Freshness(time = 35)
    @Requires(id = "heaters", optional = true, specification = Heater.class, filter = "(!(locatedobject.object.zone=" + LocatedObject.LOCATION_UNKNOWN + "))", proxy = false)
    private List<Heater> heaters;

    @Freshness(time = 35)
    @Requires(id = "coolers", optional = true, specification = Cooler.class, filter = "(!(locatedobject.object.zone=" + LocatedObject.LOCATION_UNKNOWN + "))", proxy = false)
    private List<Cooler> coolers;

    @Override
    public long getPeriod() {
        return 1;
    }

    @Override
    public TimeUnit getUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    public void run() {
        synchronized (m_lock) {
            Map<String, Double> temperatureMap = new HashMap<String, Double>();
            temperatureMap = temperatureAverageInAllZone();
            for (String zoneId : temperatureMap.keySet()) {
                if (mapManagementAuto.get(zoneId)) {
                    double tempInZone = temperatureMap.get(zoneId);
                    synchronized (m_Energylock) {
                        if (tempInZone > mapTemperatureTarget.get(zoneId) + 1) {
                            Set<Cooler> coolerSet = coolerInZone(zoneId);
                            for (Cooler cooler : coolerSet) {
                                cooler.setPowerLevel(maximumEnergyAllowed);
                            }
                            Set<Heater> heaterSet = heaterInZone(zoneId);
                            for (Heater heater : heaterSet) {
                                heater.setPowerLevel(0);
                            }
                        } else if (tempInZone < mapTemperatureTarget.get(zoneId) - 1) {
                            Set<Heater> heaterSet = heaterInZone(zoneId);
                            for (Heater heater : heaterSet) {
                                heater.setPowerLevel(maximumEnergyAllowed);
                            }
                            Set<Cooler> coolerSet = coolerInZone(zoneId);
                            for (Cooler cooler : coolerSet) {
                                cooler.setPowerLevel(0);
                            }
                        } else if (tempInZone < mapTemperatureTarget.get(zoneId) - 0.5) {
                            Set<Heater> heaterSet = heaterInZone(zoneId);
                            for (Heater heater : heaterSet) {
                                heater.setPowerLevel(0.03);
                            }
                            Set<Cooler> coolerSet = coolerInZone(zoneId);
                            for (Cooler cooler : coolerSet) {
                                cooler.setPowerLevel(0);
                            }
                        } else if (tempInZone > mapTemperatureTarget.get(zoneId) + 0.5) {
                            Set<Cooler> coolerSet = coolerInZone(zoneId);
                            for (Cooler cooler : coolerSet) {
                                cooler.setPowerLevel(0.03);
                            }
                            Set<Heater> heaterSet = heaterInZone(zoneId);
                            for (Heater heater : heaterSet) {
                                heater.setPowerLevel(0);
                            }
                        } else {
                            Set<Heater> heaterSet = heaterInZone(zoneId);
                            Set<Cooler> coolerSet = coolerInZone(zoneId);
                            for (Heater heater : heaterSet) {
                                heater.setPowerLevel(0);
                            }
                            for (Cooler cooler : coolerSet) {
                                cooler.setPowerLevel(0);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, Double> temperatureAverageInAllZone() {
        Map<String, Double> returnMap = new HashMap<String, Double>();
        Map<String, Integer> countMap = new HashMap<String, Integer>();
        for (Thermometer thermometer : thermometers) {
            Quantity<Temperature> tempValue = thermometer.getTemperature();
            String thermometerLocation = ((LocatedObject) thermometer).getZone();
            if (!thermometerLocation.equals(LOCATION_UNKNOWN)) {
                if (returnMap.containsKey(thermometerLocation)) {
                    double tempSum = returnMap.get(thermometerLocation) + tempValue.getValue().doubleValue();
                    returnMap.put(thermometerLocation, tempSum);
                    int count = countMap.get(thermometerLocation);
                    count += 1;
                    countMap.put(thermometerLocation, count);
                } else {
                    returnMap.put(thermometerLocation, tempValue.getValue().doubleValue());
                    countMap.put(thermometerLocation, 1);
                }
            }
        }

        for (String location : returnMap.keySet()) {
            double tempSum = returnMap.get(location);
            int count = countMap.get(location);
            returnMap.put(location, (tempSum / count));
        }
        return returnMap;
    }

    private Set<Cooler> coolerInZone(String zoneId) {
        Set<Cooler> coolerSet = new HashSet<Cooler>();
        for (Cooler cooler : coolers) {
            String coolerLocation = ((LocatedObject) cooler).getZone();
            if (coolerLocation.equals(zoneId)) {
                coolerSet.add(cooler);
            }
        }
        return coolerSet;
    }

    private Set<Heater> heaterInZone(String zoneId) {
        Set<Heater> heaterSet = new HashSet<Heater>();
        for (Heater heater : heaters) {
            String heaterLocation = ((LocatedObject) heater).getZone();
            if (heaterLocation.equals(zoneId)) {
                heaterSet.add(heater);
            }
        }
        return heaterSet;
    }

}
