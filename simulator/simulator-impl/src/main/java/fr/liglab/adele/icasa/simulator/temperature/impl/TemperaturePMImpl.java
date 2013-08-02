/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa-Simulator/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.simulator.temperature.impl;

/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa-Simulator/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.Variable;
import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.util.LocatedDeviceTracker;
import fr.liglab.adele.icasa.device.util.LocatedDeviceTrackerCustomizer;
import fr.liglab.adele.icasa.service.scheduler.SpecificClockPeriodicRunnable;
import fr.liglab.adele.icasa.simulator.PhysicalModel;

import fr.liglab.adele.icasa.location.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.*;

@Component(name = "temperature-model")
@Instantiate(name = "temperature-model-1")
@Provides(specifications = PhysicalModel.class)
public class TemperaturePMImpl implements PhysicalModel, LocatedDeviceTrackerCustomizer {

    public static final String TEMPERATURE_PROP_NAME = "Temperature";

    /**
     * Define constants to compute the value of the thermal capacity
     */
    public static final double AIR_MASS_CAPACITY = 1000; //mass capacity of the air in J/(Kg.K)
    public static final double AIR_MASS = 1.2; //mass of the air in Kg/m^3
    public static final double K = 0.724; // 0.661 < k < 0.787
    public static final String VOLUME_PROP_NAME = "Volume";

    private volatile long m_lastUpdateTime;

    private Set<Variable> _computedVariables;
    private Set<Variable> _requiredZoneVariables;

    private Object _deviceLock = new Object();

    /*
     * @GardedBy(_deviceLock)
     */
    private Set<LocatedDevice> _temperatureDevices = new HashSet<LocatedDevice>();

    private Object _zoneLock = new Object();

    @Requires
    private ContextManager _contextMgr;

    @Requires
    private Clock _clock;

    private LocatedDeviceTracker _heaterTracker;
    private LocatedDeviceTracker _coolerTracker;
//    private ZoneTracker _zoneTracker;

    private static final Clock _systemClock = SystemClockImpl.SINGLETON;

    private BundleContext _context;

    private ServiceRegistration _computeTempTaskSRef;
    private boolean _started;

    public TemperaturePMImpl(BundleContext context) {
        _context = context;
        _started = false;

        // workaround of ipojo bug of object member initialization
        _zoneLock = new Object();
        _deviceLock = new Object();
        _heaterTracker = new LocatedDeviceTracker(context, Heater.class, this);
        _coolerTracker = new LocatedDeviceTracker(context, Cooler.class, this);
//        _zoneTracker = new ZoneTracker(VOLUME_PROP_NAME, this);
    }

    @Validate
    private void start() {
        _started = true;
        _heaterTracker.open();
        _coolerTracker.open();
//        _zoneTracker.open();

        SpecificClockPeriodicRunnable computeTempTask = new SpecificClockPeriodicRunnable() {
            @Override
            public long getPeriod() {
                return 300;
            }

            @Override
            public Clock getClock() {
                return _systemClock;
            }

            @Override
            public String getGroup() {
                return "TempPM-group";
            }

            @Override
            public void run() {
                updateTemperatures();
            }
        };
        _computeTempTaskSRef = _context.registerService(SpecificClockPeriodicRunnable.class.getName(), computeTempTask, new Hashtable());
    }

    private void updateTemperatures() {
        //TODO
    }

    @Invalidate
    private void stop() {
        _started = false;
       if (_computeTempTaskSRef != null) {
           _computeTempTaskSRef.unregister();
           _computeTempTaskSRef = null;
       }

//       _zoneTracker.close();
       _heaterTracker.close();
       _coolerTracker.close();
    }

    private void updateZonesIfPropChanged(LocatedDevice locatedDevice, String propName) {
        if (Heater.HEATER_POWER_LEVEL.equals(propName) ||
                Heater.HEATER_MAX_POWER_LEVEL.equals(propName) ||
                Cooler.COOLER_POWER_LEVEL.equals(propName) ||
                Cooler.COOLER_MAX_POWER_LEVEL.equals(propName)) {

            //TODO
        }
    }

    private Set<Zone> getZones(Position devicePosition) {
        List<Zone> zones = _contextMgr.getZones();
        Set<Zone> zonesToUpdate = new HashSet<Zone>();
        for (Zone zone : zones) {
            if (zone.contains(devicePosition))
                zonesToUpdate.add(zone);
        }
        return zonesToUpdate;
    }

    private Set<Zone> getZones(LocatedDevice locatedDevice) {
        Position devicePosition = locatedDevice.getCenterAbsolutePosition();
        return getZones(devicePosition);
    }

    public TemperaturePMImpl(Set<Variable> _computedVariables) {
        this._computedVariables = new HashSet<Variable>();
        _computedVariables.add(new Variable(TEMPERATURE_PROP_NAME, Double.class, "in Kelvin"));

        _requiredZoneVariables = new HashSet<Variable>();
        _requiredZoneVariables.add(new Variable(VOLUME_PROP_NAME, Double.class, "volume in cubic meters"));
    }

    @Override
    public Set<Variable> getComputedZoneVariables() {
        return _computedVariables;
    }

    @Override
    public Set<Variable> getRequiredZoneVariables() {
        return _requiredZoneVariables;
    }

    @Override
    public Set<LocatedDevice> getUsedDevices() {
        return Collections.unmodifiableSet(new HashSet<LocatedDevice>(_temperatureDevices));
    }

//    @Validate
//    private void start() {
//        _contextMgr.addListener(this);
//        m_lastUpdateTime = System.currentTimeMillis();
//    }
//
//    @Invalidate
//    private void stop() {
//        _contextMgr.removeListener(this);
//    }

    /**
     * Returns a new mutable set of all temperature devices for each call.
     *
     * @return a new mutable set of all temperature devices for each call.
     */
    private Set<GenericDevice> getTemperatureDevices() {
        Set<GenericDevice> devices = new HashSet<GenericDevice>();
        synchronized (_deviceLock) {
            for (LocatedDevice locatedDevice : _temperatureDevices) {
                GenericDevice deviceObject = locatedDevice.getDeviceObject();
                if (deviceObject != null)
                    devices.add(deviceObject);
            }
        }
        return devices;
    }

    private Set<GenericDevice> getTemperatureDevicesFromZone(Zone zone) {
        Set<GenericDevice> devices = getTemperatureDevices();
        Set<GenericDevice> filteredDevices = new HashSet<GenericDevice>();
        for (GenericDevice device : devices) {
            Position devicePosition = _contextMgr.getDevicePosition(device.getSerialNumber());
            if ((devicePosition != null) && (zone.contains(devicePosition))) {
                filteredDevices.add(device);
            }
        }

        return filteredDevices;
    }

    /**
     * Computes and updates the temperature property value of specified zone .
     * The formula used to compute the temperature is :
     * CurrentTemperature [K]=(P[W]/C[J/K])*t[s]+T0[K]
     *
     * @param zone a zone
     * @return the temperature currently produced by this heater
     */
    private void updateTemperature(Zone zone) {
        synchronized (_zoneLock) {
            Object zoneTemperature = zone.getVariableValue(TEMPERATURE_PROP_NAME);
            double returnedTemperature = 20.0;
            if (zoneTemperature != null)
                try {
                    returnedTemperature = (Double) zoneTemperature; //TODO manage external temperature
                } catch (Exception e) {
                    e.printStackTrace();
                }
            int activeTemperatureDeviceSize = 0;
            int height = zone.getZLength();
            int length = zone.getYLength();
            int width = zone.getXLength();

            long time = System.currentTimeMillis();
            double timeDiff = ((long) (time - m_lastUpdateTime)) / 1000.0d;
            m_lastUpdateTime = time;

            double thermalCapacity = 0.0; //Thermal capacity used to compute the temperature. Expressed in J/K.
            Double roomVolume = (Double) zone.getVariableValue(VOLUME_PROP_NAME); //TODO should be robust to casting errors
            if (roomVolume == null)
                return; // computation cannot be performed ; there is a missing information
            double powerLevelTotal = 0.0d;
            double currentTemperature = 0.0;

            Set<GenericDevice> devices = getTemperatureDevicesFromZone(zone);
            for (GenericDevice device : devices) {
                if (device instanceof Heater) {
                    Heater heater = (Heater) device;

                    if (heater.getPowerLevel() != 0.0d) {
                        activeTemperatureDeviceSize += 1;
                        powerLevelTotal += heater.getPowerLevel() * heater.getMaxPowerLevel();
                    }
                } else if (device instanceof Cooler) {
                    Cooler cooler = (Cooler) device;

                    if (cooler.getPowerLevel() != 0.0d) {
                        activeTemperatureDeviceSize += 1;
                        powerLevelTotal -= cooler.getPowerLevel() * cooler.getMaxPowerLevel();
                    }
                }
            }

            if ((activeTemperatureDeviceSize != 0) && (roomVolume > 0)) {
                currentTemperature = (Double) zone.getVariableValue(TEMPERATURE_PROP_NAME);
                thermalCapacity = AIR_MASS * AIR_MASS_CAPACITY * roomVolume;
                returnedTemperature += ((powerLevelTotal * timeDiff) / thermalCapacity) + currentTemperature;

                /**
                 * Clipping fonction to saturate the temperature at a certain level
                 */
                if (powerLevelTotal > 0) {
                    if (returnedTemperature > 303.16) returnedTemperature = 303.16;
                } else if (powerLevelTotal < 0) {
                    if (returnedTemperature > 283.16) returnedTemperature = 283.16;
                }
            }

            zone.setVariableValue(TEMPERATURE_PROP_NAME, returnedTemperature);
        }
    }

    private void updateZoneModel(Zone zone) {
        //TODO implement it
    }



    /*
     * LocateDeviceTracker
     */

    @Override
    public boolean addingDevice(LocatedDevice locatedDevice) {
        return true;
    }

    @Override
    public void addedDevice(LocatedDevice locatedDevice) {

    }

    @Override
    public void modifiedDevice(LocatedDevice locatedDevice, String s, Object o, Object o1) {

    }

    @Override
    public void movedDevice(LocatedDevice locatedDevice, Position position, Position position1) {

    }

    @Override
    public void removedDevice(LocatedDevice locatedDevice) {

    }
}
