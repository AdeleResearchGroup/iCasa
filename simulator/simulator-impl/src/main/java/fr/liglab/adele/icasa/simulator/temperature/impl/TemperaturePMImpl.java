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
package fr.liglab.adele.icasa.simulator.temperature.impl;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.Variable;
import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.location.*;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.service.zone.size.calculator.ZoneSizeCalculator;
import fr.liglab.adele.icasa.simulator.PhysicalModel;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.DateTime;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import fr.liglab.adele.icasa.location.LocatedDeviceListener;

import fr.liglab.adele.icasa.location.ZoneListener;

import java.util.*;

@Component(name = "temperature-model")
@Instantiate(name = "temperature-model-0")
@Provides(specifications = PhysicalModel.class)
public class TemperaturePMImpl implements PhysicalModel, ZoneListener, LocatedDeviceListener {

    public static final String TEMPERATURE_PROP_NAME = "Temperature";
    public static final String VOLUME_PROP_NAME = "Volume";

    /**
     * Define constants to compute the value of the thermal capacity
     */
    public static final double AIR_MASS_CAPACITY = 1000; // mass capacity of the air in J/(Kg.K)
    public static final double AIR_MASS = 1.2; // mass of the air in Kg/m^3
    public static final double K = 0.724; // 0.661 < k < 0.787
    public static final double HIGHEST_TEMP = 303.16;
    public static final double LOWER_TEMP = 283.16;
    public static final double DEFAULT_TEMP_VALUE = 293.15; // 20 celsius degrees in kelvin
    public static final double MAX_WALL_DIST = 0.3d; // maximum distance in meters between 2 walls to be considered as
    // the same wall

    private volatile long m_lastUpdateTime;

    private Set<Variable> _computedVariables;
    private Set<Variable> _requiredZoneVariables;


    // There is no need of full illuminance in the morning
    public static final double  MORNING_EXTERNAL_TEMPERATUR_FACTOR = -0.5;
    // In the afternoon the illuminance can be largely limited
    public static final double  AFTERNOON_EXTERNAL_TEMPERATUR_FACTOR = 2;
    // In the evening, the illuminance should be the best
    public static final double  EVENING_EXTERNAL_TEMPERATUR_FACTOR = 0.5;
    // In the night, there is no need to use the full illuminance
    public static final double  NIGHT_EXTERNAL_TEMPERATUR_FACTOR = -2;


    private double _temperatureFactorMomentOfTheDay = 2.0 ;
    /*
     * @GardedBy(_deviceLock)
     */
    private Set<LocatedDevice> _temperatureDevices = new HashSet<LocatedDevice>();

    private Object _zoneModelLock = new Object();

    private Object _timeLock = new Object();

    private Map<String /* zone id */, ZoneModel> _zoneModels = new HashMap<String, ZoneModel>();

    @Requires
    private ContextManager _contextMgr;

    @Requires
    private Clock _clock;

    @Requires
    ZoneSizeCalculator _zoneSizeCalc;



    private BundleContext _context;

    private ServiceRegistration _computeTempTaskSRef;
    private boolean _computationIsOn;

    public TemperaturePMImpl(BundleContext context) {
        this._context = context;
        this._computationIsOn = false;

        // workaround of ipojo bug of object member initialization
        this._zoneModelLock = new Object();

        // workaround of ipojo bug of object member initialization
        this._timeLock = new Object();

        this._computedVariables = new HashSet<Variable>();
        this._computedVariables.add(new Variable(TEMPERATURE_PROP_NAME, Double.class, "in Kelvin"));

        this._requiredZoneVariables = new HashSet<Variable>();
        this._requiredZoneVariables.add(new Variable(VOLUME_PROP_NAME, Double.class, "volume in cubic meters"));
    }

    @Validate
    private void start() {
        _computationIsOn = true;

        this._contextMgr.addListener(this);

        PeriodicRunnable computeTempTask = new PeriodicRunnable() {
            @Override
            public long getPeriod() {
                return 60000;//each 10sec will update the temperature.
            }


            @Override
            public String getGroup() {
                return "TempPM-group";
            }

            @Override
            public void run() {
                DateTime dateTimeEli =new DateTime(_clock.currentTimeMillis());
                int hour = dateTimeEli.getHourOfDay();
                if (22 <=hour ){
                    _temperatureFactorMomentOfTheDay = NIGHT_EXTERNAL_TEMPERATUR_FACTOR;
                }
                else if (18 <=hour ){
                    _temperatureFactorMomentOfTheDay = EVENING_EXTERNAL_TEMPERATUR_FACTOR;
                }
                else if (12 <=hour ){
                    _temperatureFactorMomentOfTheDay = AFTERNOON_EXTERNAL_TEMPERATUR_FACTOR;
                }
                else if (6 <=hour ){
                    _temperatureFactorMomentOfTheDay = MORNING_EXTERNAL_TEMPERATUR_FACTOR;
                }else{
                    _temperatureFactorMomentOfTheDay = NIGHT_EXTERNAL_TEMPERATUR_FACTOR;
                }
                updateTemperatures();
            }
        };

        m_lastUpdateTime = _clock.currentTimeMillis();
        _computeTempTaskSRef = _context.registerService(PeriodicRunnable.class.getName(), computeTempTask,
                new Hashtable());
    }

    private void updateTemperatures() {
        // called by only one thread
        if (!_computationIsOn){
            return;
        }
        long previousUpdateTS = m_lastUpdateTime;

        long newUpdateTS = _clock.currentTimeMillis();
        long timeDiff = newUpdateTS - previousUpdateTS;
        if (timeDiff <= 0){
            m_lastUpdateTime = newUpdateTS;
            return; // do not need to recompute
        }
        Map<String, Double> newTemps = new HashMap<String, Double>();
        synchronized (_zoneModelLock) {
            for (ZoneModel zoneModel : _zoneModels.values()) {
                synchronized (_zoneModelLock) {
                    double newTemp = computeTemperature(zoneModel.getZone(), timeDiff);
                    newTemps.put(zoneModel.getZoneId(), newTemp);
                }
            }
        }

        for (String zoneId : newTemps.keySet()) {
            Zone zone = _contextMgr.getZone(zoneId);
            if (zone != null)
                zone.setVariableValue(TEMPERATURE_PROP_NAME, newTemps.get(zoneId));
        }

        m_lastUpdateTime = newUpdateTS;
    }

    @Invalidate
    private void stop() {
        _computationIsOn = false;

        _contextMgr.removeListener(this);
        if (_computeTempTaskSRef != null) {
            _computeTempTaskSRef.unregister();
            _computeTempTaskSRef = null;
        }
    }

    private boolean propChangeHasImpactOnPower(String propName) {
        return (Heater.HEATER_POWER_LEVEL.equals(propName) || Heater.HEATER_MAX_POWER_LEVEL.equals(propName)
                || Cooler.COOLER_POWER_LEVEL.equals(propName) || Cooler.COOLER_MAX_POWER_LEVEL.equals(propName));
    }

    @Override
    public Set<Variable> getComputedZoneVariables() {
        return Collections.unmodifiableSet(_computedVariables);
    }

    @Override
    public Set<Variable> getRequiredZoneVariables() {
        return Collections.unmodifiableSet(_requiredZoneVariables);
    }

    @Override
    public Set<LocatedDevice> getUsedDevices() {
        synchronized (_zoneModelLock) {
            return Collections.unmodifiableSet(new HashSet<LocatedDevice>(_temperatureDevices));
        }
    }

    /**
     * Computes the temperature property value of specified zone according to time difference from the last computation.
     *
     * @param zone a zone
     * @param timeDiff time difference in ms from the last computation
     * @return the temperature computed for time t + dt
     */
    private double computeTemperature(Zone zone, long timeDiff) {

        String zoneId = zone.getId();

        double newTemperature = DEFAULT_TEMP_VALUE; // 20 degrees by default

        synchronized (_zoneModelLock) {
            ZoneModel zoneModel = _zoneModels.get(zoneId);

            double currentTemperature = DEFAULT_TEMP_VALUE ; // 20 degrees by default
            Object zoneTemperature = zone.getVariableValue(TEMPERATURE_PROP_NAME);
            if (zoneTemperature != null) {
                try {
                    currentTemperature = (Double) zoneTemperature; // TODO manage external temperature
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            double powerLevelTotal = zoneModel.getTotalPower();
            double timeDiffInSeconds = timeDiff / 1000.0d;
            if (powerLevelTotal == 0){
                if ( currentTemperature > (DEFAULT_TEMP_VALUE + 0.5) ) {
                    powerLevelTotal = -50.0;
                } else if ( currentTemperature < (DEFAULT_TEMP_VALUE - 0.5) ){
                    powerLevelTotal = 50.0;
                } else {
                    return DEFAULT_TEMP_VALUE;
                }
            }

            if ( (powerLevelTotal > 0) && (currentTemperature < DEFAULT_TEMP_VALUE) ) {
                powerLevelTotal = 50.0 + zoneModel.getTotalPower();
            } else if( (powerLevelTotal) < 0 && (currentTemperature > DEFAULT_TEMP_VALUE) ) {
                powerLevelTotal = -50.0 + zoneModel.getTotalPower();
            }

            double delta = (powerLevelTotal  * timeDiffInSeconds) / zoneModel.getThermalCapacity();

            newTemperature = currentTemperature  + delta;

            /* A ZONE DOESN4NT INFLUENCE TEMPERATURE IN OTHER ZONE */
            /*
            for (Map.Entry<String, Double> zoneWallSurfaceEntry : zoneModel.getWallSurfaces()) {
                String otherZoneId = zoneWallSurfaceEntry.getKey();
                double zoneWallSurface = (Double) zoneWallSurfaceEntry.getValue();
                Zone otherZone = _contextMgr.getZone(otherZoneId);
                if (otherZone != null) {
                    //Object otherZoneTemperature = _contextMgr.getZone(otherZoneId).getVariableValue(TEMPERATURE_PROP_NAME);
                    Object otherZoneTemperature = otherZone.getVariableValue(TEMPERATURE_PROP_NAME);
                    if (otherZoneTemperature != null) {
                        newTemperature += (K * zoneWallSurface * (((Double) otherZoneTemperature) - currentTemperature) * timeDiffInSeconds)
                                / zoneModel.getThermalCapacity();
                    }
                }
            }*/


            /**
             * Clipping function to saturate the temperature at a certain level
             */
            if (newTemperature > HIGHEST_TEMP)
                newTemperature = HIGHEST_TEMP;
            else if (newTemperature < LOWER_TEMP)
                newTemperature = LOWER_TEMP;
        }

        return newTemperature;
    }

    private void updateZoneSurface(ZoneModel zoneModel1, ZoneModel zoneModel2) {
        synchronized (_zoneModelLock) {
            double wallSurface = getContactWallSurface(zoneModel1.getZone(), zoneModel2.getZone());

            String zone1Id = zoneModel1.getZoneId();
            String zone2Id = zoneModel2.getZoneId();
            zoneModel1.setWallSurface(zone2Id, wallSurface);
            zoneModel2.setWallSurface(zone1Id, wallSurface);
        }
    }

    private double getContactWallSurface(Zone zone1, Zone zone2) {

        double xFactor = _zoneSizeCalc.getXScaleFactor();
        double yFactor = _zoneSizeCalc.getYScaleFactor();
        double zFactor = _zoneSizeCalc.getZScaleFactor();

        //int zone1XLength = zone1.getXLength();
        //int zone1YLength = zone1.getYLength();
        //int zone1ZLength = zone1.getZLength();
        int zone1X1 = zone1.getLeftTopAbsolutePosition().x;
        int zone1Y1 = zone1.getLeftTopAbsolutePosition().y;
        int zone1Z1 = zone1.getLeftTopAbsolutePosition().z;
        int zone1X2 = zone1.getRightBottomAbsolutePosition().x;
        int zone1Y2 = zone1.getRightBottomAbsolutePosition().y;
        int zone1Z2 = zone1.getRightBottomAbsolutePosition().z;
        Interval zone1X = new Interval(zone1X1, zone1X2);
        Interval zone1Y = new Interval(zone1Y1, zone1Y2);
        Interval zone1Z = new Interval(zone1Z1, zone1Z2);
        Parallelepiped p1 = new Parallelepiped(zone1X, zone1Y, zone1Z);

        //int zone2XLength = zone2.getXLength();
        //int zone2YLength = zone2.getYLength();
        //int zone2ZLength = zone2.getZLength();
        int zone2X1 = zone2.getLeftTopAbsolutePosition().x;
        int zone2Y1 = zone2.getLeftTopAbsolutePosition().y;
        int zone2Z1 = zone2.getLeftTopAbsolutePosition().z;
        int zone2X2 = zone2.getRightBottomAbsolutePosition().x;
        int zone2Y2 = zone2.getRightBottomAbsolutePosition().y;
        int zone2Z2 = zone2.getRightBottomAbsolutePosition().z;
        Interval zone2X = new Interval(zone2X1, zone2X2);
        Interval zone2Y = new Interval(zone2Y1, zone2Y2);
        Interval zone2Z = new Interval(zone2Z1, zone2Z2);
        Parallelepiped p2 = new Parallelepiped(zone2X, zone2Y, zone2Z);

        int interX = getIntersectionLength(zone1X1, zone1X2, zone2X1, zone2X2);
        int interY = getIntersectionLength(zone1Y1, zone1Y2, zone2Y1, zone2Y2);
        int interZ = getIntersectionLength(zone1Z1, zone1Z2, zone2Z1, zone2Z2);
        int distX = getDistanceBetweenIntervals(zone1X1, zone1X2, zone2X1, zone2X2);
        int distY = getDistanceBetweenIntervals(zone1Y1, zone1Y2, zone2Y1, zone2Y2);
        int distZ = getDistanceBetweenIntervals(zone1Z1, zone1Z2, zone2Z1, zone2Z2);

        // Determines if contact surface are close  between zones
        if ((distX*xFactor > MAX_WALL_DIST) || (distY*yFactor > MAX_WALL_DIST) || (distZ*zFactor > MAX_WALL_DIST)) {
            return 0.0d;
        }

        // disjoint zones
        if ((interX == 0) && (interY > 0)) {
            return interY * yFactor * interZ * zFactor;
        } else if ((interY == 0) && (interX > 0)) {
            return interX * xFactor * interZ * zFactor;
        } else if ((interY == 0) && (interX == 0)) {
            return 0.0d;
        }

        // Z2 into Z1 (in terms of x and y)
        boolean z2xInZ1x = isInSecondInterval(zone2X, zone1X);
        boolean z2yInZ1y = isInSecondInterval(zone2Y, zone1Y);
        boolean z2zInZ1z = isInSecondInterval(zone2Z, zone1Z);
        if (z2xInZ1x && z2yInZ1y) {
            double xyWallSurface = interX * xFactor * interY * yFactor;
            if ((interZ == 0) && ((distZ * zFactor) <= MAX_WALL_DIST))
                return xyWallSurface;
            else if (z2zInZ1z) {
                // Z2 is totally into Z1
                return getSurfaceOfParallelepipedInMeters(zone1X, zone1Y, zone1Z, xFactor, yFactor, zFactor);
            } else if (interZ > 0) {
                Parallelepiped interPara = getInterParallelepiped(p1, p2);

                return getSurfaceOfParallelepipedInMeters(interPara.xInterval, interPara.yInterval,
                        interPara.zInterval, xFactor, yFactor, zFactor) - xyWallSurface;
            }
        }

        // Z1 into Z2 (in terms of x and y)
        boolean z1xInZ2x = isInSecondInterval(zone1X, zone2X);
        boolean z1yInZ2y = isInSecondInterval(zone1Y, zone2Y);
        boolean z1zInZ2z = isInSecondInterval(zone1Z, zone2Z);
        if (z1xInZ2x && z1yInZ2y) {
            double xyWallSurface = interX * xFactor * interY * yFactor;
            if ((interZ == 0) && ((distZ * zFactor) <= MAX_WALL_DIST))
                return xyWallSurface;
            else if (z1zInZ2z) {
                // Z1 is totally into Z2
                return getSurfaceOfParallelepipedInMeters(zone2X, zone2Y, zone2Z, xFactor, yFactor, zFactor);
            } else if (interZ > 0) {
                Parallelepiped interPara = getInterParallelepiped(p1, p2);

                return getSurfaceOfParallelepipedInMeters(interPara.xInterval, interPara.yInterval,
                        interPara.zInterval, xFactor, yFactor, zFactor) - xyWallSurface;
            }
        }

        /*
         * X,Y plan ------- |Z1 ------ | | Z2 | | ------ -------
         */
        if (!z1xInZ2x && !z2xInZ1x && (z2yInZ1y || z1yInZ2y)) {
            double xzWallSurface = interX * xFactor * interZ * zFactor;
            Parallelepiped interPara = getInterParallelepiped(p1, p2);

            return (getSurfaceOfParallelepipedInMeters(interPara.xInterval, interPara.yInterval, interPara.zInterval,
                    xFactor, yFactor, zFactor) / 2.0d) + xzWallSurface;
        }

        /*
         * X,Y plan ------ ----| Z2 |--- |Z1 ------ | -------------
         */
        if (!z1yInZ2y && !z2yInZ1y && (z2xInZ1x || z1xInZ2x)) {
            double yzWallSurface = interY * yFactor * interZ * zFactor;
            Parallelepiped interPara = getInterParallelepiped(p1, p2);

            return (getSurfaceOfParallelepipedInMeters(interPara.xInterval, interPara.yInterval, interPara.zInterval,
                    xFactor, yFactor, zFactor) / 2.0d) + yzWallSurface;
        }

        /*
         * X,Y plan ------ ----| Z2 |--- |Z1 | | | ----| |--- | | ------
         */
        if ((z2xInZ1x && !z1xInZ2x && z1yInZ2y && !z2yInZ1y) || (!z2xInZ1x && z1xInZ2x && !z1yInZ2y && z2yInZ1y)) {
            double xyWallSurface = interX * xFactor * interY * yFactor;
            Parallelepiped interPara = getInterParallelepiped(p1, p2);

            return getSurfaceOfParallelepipedInMeters(interPara.xInterval, interPara.yInterval, interPara.zInterval,
                    xFactor, yFactor, zFactor) + xyWallSurface;
        }

        /*
         * X,Y plan ------ ----| Z2 |--- |Z1 | | | ----| |--- | | ------
         */
        if ((interX > 0) && (interY > 0) && !z2xInZ1x && !z1xInZ2x && !z1yInZ2y && !z2yInZ1y) {
            Parallelepiped interPara = getInterParallelepiped(p1, p2);

            return getSurfaceOfParallelepipedInMeters(interPara.xInterval, interPara.yInterval, interPara.zInterval,
                    xFactor, yFactor, zFactor) / 2.0d;
        }

        // should not reach this point

        throw new IllegalStateException("Should be dead code.");
    }

    /**
     * Prerequisite : Intersection parallelepiped must not be null.
     *
     * @param p1 a parallelepiped
     * @param p2 another parallelepiped
     * @return intersection of the two parallelepiped.
     */
    private Parallelepiped getInterParallelepiped(Parallelepiped p1, Parallelepiped p2) {
        Interval xInterval = getIntersectionInterval(p1.xInterval, p2.xInterval);
        Interval yInterval = getIntersectionInterval(p1.yInterval, p2.yInterval);
        Interval zInterval = getIntersectionInterval(p1.zInterval, p2.zInterval);

        return new Parallelepiped(xInterval, yInterval, zInterval);
    }

    private double getSurfaceOfParallelepipedInMeters(Interval zone1X, Interval zone1Y, Interval zone1Z,
                                                      double xFactor, double yFactor, double zFactor) {
        double xySurface = zone1X.getLength() * xFactor * zone1Y.getLength() * yFactor;
        double xzSurface = zone1X.getLength() * xFactor * zone1Z.getLength() * zFactor;
        double yzSurface = zone1Y.getLength() * yFactor * zone1Z.getLength() * zFactor;

        return (2 * xySurface) + (2 * xzSurface) + (2 * yzSurface);
    }

    private static int getMin(int n1, int n2, int n3) {
        return Math.min(n1, Math.min(n2, n3));
    }

    private static boolean isInSecondInterval(Interval interval1, Interval interval2) {
        return ((interval2.min >= interval1.min) && (interval2.max >= interval1.max));
    }

    private static Interval getIntersectionInterval(Interval interval1, Interval interval2) {
        Interval interval = new Interval(0, 0);

        if (interval1.min <= interval2.min)
            interval.min = interval2.min;
        else
            interval.min = interval1.min;

        if (interval1.max <= interval2.max)
            interval.max = interval1.max;
        else
            interval.max = interval2.max;

        return interval;
    }

    private static int getIntersectionLength(int coord1Min, int coord1Max, int coord2Min, int coord2Max) {
        if ((coord2Min > coord1Max) || (coord1Min > coord2Max))
            return 0;
        if (coord1Max >= coord2Min)
            return coord2Max - coord1Min;
        else if (coord2Max >= coord1Min)
            return coord1Max - coord2Min;

        return 0;
    }

    private static int getDistanceBetweenIntervals(int coord1Min, int coord1Max, int coord2Min, int coord2Max) {
        if (coord2Min > coord1Max)
            return coord2Min - coord1Max;
        if (coord1Min > coord2Max)
            return coord1Min - coord2Max;

        return 0;
    }
    public boolean isTemperatureDevice(GenericDevice device) {
        return ( (device instanceof Cooler) || (device instanceof Heater));
    }

    @Override
    public void deviceAdded(LocatedDevice locatedDevice) {
        if (!((locatedDevice != null) && isTemperatureDevice(locatedDevice.getDeviceObject() ) )){
            return;
        }

        synchronized (_zoneModelLock) {
            _temperatureDevices.add(locatedDevice);
            for (ZoneModel zoneModel : _zoneModels.values()) {
                if (zoneModel.isInTheZone(locatedDevice)){
                    zoneModel.updateTemperatureDevices();
                    zoneModel.updateTotalPower();
                }
            }
        }
    }

    @Override
    public void deviceRemoved(LocatedDevice locatedDevice) {
        if (!((locatedDevice != null) && isTemperatureDevice(locatedDevice.getDeviceObject() ) )){
            return;
        }

        synchronized (_zoneModelLock) {
            _temperatureDevices.remove(locatedDevice);

            for (ZoneModel zoneModel : _zoneModels.values()) {
                if (zoneModel.getZone().contains(locatedDevice)){
                    zoneModel.updateTemperatureDevices();
                    zoneModel.updateTotalPower();
                }
            }
        }

    }

    @Override
    public void deviceMoved(LocatedDevice locatedDevice, Position oldPosition, Position newPosition) {
        synchronized (_zoneModelLock) {

            for (ZoneModel zoneModel : _zoneModels.values()) {
                Zone zone = zoneModel.getZone();
                if(zone.contains(oldPosition)){
                    zoneModel.updateTemperatureDevices();
                    zoneModel.updateTotalPower();
                }

                if (zone.contains(locatedDevice)){
                    zoneModel.updateTemperatureDevices();
                    zoneModel.updateTotalPower();
                }
            }
        }
    }

    @Override
    public void devicePropertyModified(LocatedDevice locatedDevice, String propertyName, Object oldValue, Object newValue) {
        if (!propChangeHasImpactOnPower(propertyName))
            return;

        synchronized (_zoneModelLock) {

            for (ZoneModel zoneModel : _zoneModels.values()) {
                if (zoneModel.isInTheZone(locatedDevice)){
                    zoneModel.updateTotalPower();
                }
            }
        }
    }

    @Override
    public void devicePropertyAdded(LocatedDevice device, String propertyName) {

    }

    @Override
    public void devicePropertyRemoved(LocatedDevice device, String propertyName) {

    }

    @Override
    public void deviceAttached(LocatedDevice container, LocatedDevice child) {

    }

    @Override
    public void deviceDetached(LocatedDevice container, LocatedDevice child) {

    }

    @Override
    public void deviceEvent(LocatedDevice device, Object data) {

    }

    @Override
    public void zoneAdded(Zone zone) {
        String zoneId = zone.getId();
        ZoneModel zoneModel = new ZoneModel(zone,_temperatureDevices);

        synchronized (_zoneModelLock) {
            if (!_zoneModels.containsKey(zoneId)) {
                zone.setVariableValue(TEMPERATURE_PROP_NAME, DEFAULT_TEMP_VALUE);
                // init thermal capacity
                Object zoneVolume = zone.getVariableValue(VOLUME_PROP_NAME);
                if ((zoneVolume != null) && (zoneVolume instanceof Double))
                    zoneModel.updateThermalCapacity();

                // init zone surfaces
                for (ZoneModel curZoneModel : _zoneModels.values())
                    updateZoneSurface(zoneModel, curZoneModel);

                _zoneModels.put(zoneId, zoneModel);
            }
            zoneModel.updateTemperatureDevices();
            zoneModel.updateTotalPower();
        }

    }

    @Override
    public void zoneRemoved(Zone zone) {
        String zoneId = zone.getId();

        synchronized (_zoneModelLock) {
            _zoneModels.remove(zone.getId());
            for (ZoneModel zoneModel : _zoneModels.values()) {
                zoneModel.removeWallSurface(zoneId);
            }
        }
    }

    @Override
    public void zoneMoved(Zone zone, Position oldPosition, Position newPosition) {
        // do not need to update thermal capacity done when volume changes
        synchronized (_zoneModelLock) {
            ZoneModel zoneModel = _zoneModels.get(zone.getId());
            zoneModel.updateTemperatureDevices();
            zoneModel.updateTotalPower();
            for (ZoneModel curZoneModel : _zoneModels.values())
                updateZoneSurface(zoneModel, curZoneModel);
        }
    }

    @Override
    public void zoneResized(Zone zone) {
        // do not need to update thermal capacity done when volume changes
        synchronized (_zoneModelLock) {
            ZoneModel zoneModel = _zoneModels.get(zone.getId());
            zoneModel.updateTemperatureDevices();
            zoneModel.updateTotalPower();
            for (ZoneModel curZoneModel : _zoneModels.values())
                updateZoneSurface(zoneModel, curZoneModel);
        }
    }

    @Override
    public void zoneParentModified(Zone zone, Zone oldParentZone, Zone newParentZone) {

    }

    @Override
    public void deviceAttached(Zone container, LocatedDevice child) {

    }

    @Override
    public void deviceDetached(Zone container, LocatedDevice child) {

    }

    @Override
    public void zoneVariableAdded(Zone zone, String variableName) {

    }

    @Override
    public void zoneVariableRemoved(Zone zone, String variableName) {

    }

    @Override
    public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue) {
        if (!VOLUME_PROP_NAME.equals(variableName))
            return;


        synchronized (_zoneModelLock) {
            ZoneModel zoneModel = _zoneModels.get(zone.getId());
            zoneModel.updateThermalCapacity();
        }
    }

}
