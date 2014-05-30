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
package fr.liglab.adele.icasa.simulator.illuminance.impl;

import java.util.*;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.service.zone.size.calculator.ZoneSizeCalculator;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.Variable;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.LocatedDeviceListener;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.ZoneListener;
import fr.liglab.adele.icasa.simulator.PhysicalModel;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Component(name = "illuminance-model")
@Instantiate(name = "illuminance-model-0")
@Provides(specifications = PhysicalModel.class)
public class IlluminancePMImpl implements PhysicalModel, ZoneListener, LocatedDeviceListener {


    /**
     * Rought Constant to establish the correspondance between power & illuminance
     */
    public static final double LUMENS_CONSTANT_VALUE = 683.0d;


    public static final String ILLUMINANCE_PROP_NAME = "Illuminance";

    public static final String SURFACE_PROP_NAME = "Surface";

    private Set<Variable> _computedVariables;

    private Set<Variable> _requiredZoneVariables;

    private final Object _deviceLock ;

    // There is no need of full illuminance in the morning
    public static final double  MORNING_EXTERNAL_SOURCE_POWER = 800 ;
    // In the afternoon the illuminance can be largely limited
    public static final double  AFTERNOON_EXTERNAL_SOURCE_POWER = 1600;
    // In the evening, the illuminance should be the best
    public static final double  EVENING_EXTERNAL_SOURCE_POWER = 700;
    // In the night, there is no need to use the full illuminance
    public static final double  NIGHT_EXTERNAL_SOURCE_POWER = 200;

    private double currentExternalSource = MORNING_EXTERNAL_SOURCE_POWER;
    /*
     * @gardedBy(_deviceLock)
     */
    private Set<LocatedDevice> _lights = new HashSet<LocatedDevice>();

    private final Object _clockLock ;

    private ServiceRegistration _computeTempTaskSRef;


    private BundleContext _context;

    @Requires
    private ContextManager _contextMgr;

    @Requires
    private Clock _clock;

    @Requires
    ZoneSizeCalculator _zoneSizeCalc;

    /**
     * The current moment of the day :
     **/
    PartOfTheDay currentMomentOfTheDay  ;

    public IlluminancePMImpl(BundleContext context) {
        // workaround of ipojo bug of object member initialization
        _clockLock = new Object();
        _deviceLock = new Object();
        _context = context;

        _computedVariables = new HashSet<Variable>();

        _computedVariables.add(new Variable(ILLUMINANCE_PROP_NAME, Double.class, "in lux"));
        _requiredZoneVariables = new HashSet<Variable>();
        _requiredZoneVariables.add(new Variable(SURFACE_PROP_NAME, Double.class, "surface in square meters"));
    }

    @Override
    public void deviceAdded(LocatedDevice locatedDevice) {
        GenericDevice device = locatedDevice.getDeviceObject();
        if (!((device != null) &&
                isLight(device)))
            return; // ignore it

        synchronized (_deviceLock) {
            _lights.add(locatedDevice);
        }
        updateZones(locatedDevice);
    }

    private boolean isLight(GenericDevice device) {
        return ((device instanceof BinaryLight) || (device instanceof DimmerLight));
    }

    @Override
    public void deviceRemoved(LocatedDevice locatedDevice) {
        GenericDevice device = locatedDevice.getDeviceObject();
        if (!((device != null) &&
                isLight(device)))
            return; // ignore it

        synchronized (_deviceLock) {
            _lights.remove(locatedDevice);
        }
        updateZones(locatedDevice);
    }

    @Override
    public void devicePropertyModified(LocatedDevice locatedDevice, String propName, Object oldValue, Object newValue) {
        updateZonesIfPropChanged(locatedDevice, propName);
    }

    private void updateZonesIfPropChanged(LocatedDevice locatedDevice, String propName) {
        if (BinaryLight.BINARY_LIGHT_MAX_POWER_LEVEL.equals(propName) ||
                BinaryLight.BINARY_LIGHT_POWER_STATUS.equals(propName) ||
                DimmerLight.DIMMER_LIGHT_MAX_POWER_LEVEL.equals(propName) ||
                DimmerLight.DIMMER_LIGHT_POWER_LEVEL.equals(propName)) {

            updateZones(locatedDevice);
        }
    }

    @Override
    public void devicePropertyAdded(LocatedDevice locatedDevice, String propName) {
        updateZonesIfPropChanged(locatedDevice, propName);
    }

    @Override
    public void devicePropertyRemoved(LocatedDevice locatedDevice, String propName) {
        //do nothing
    }

    @Override
    public void deviceMoved(LocatedDevice locatedDevice, Position oldPosition, Position newPosition) {
        updateZones(oldPosition);
        updateZones(locatedDevice);
    }

    @Override
    public void deviceAttached(LocatedDevice locatedDevice, LocatedDevice locatedDevice1) {
        //do nothing
    }

    @Override
    public void deviceDetached(LocatedDevice locatedDevice, LocatedDevice locatedDevice1) {
        //do nothing
    }

    @Override
    public void deviceEvent(LocatedDevice locatedDevice, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void updateZones(LocatedDevice locatedDevice) {
        updateZones(locatedDevice.getCenterAbsolutePosition());
    }

    private void updateZones() {
        List<Zone> zones = _contextMgr.getZones();
        for(Zone zone : zones){
            updateIlluminance(zone);
        }
    }

    private void updateZones(Position devicePosition) {
        Set<Zone> zonesToUpdate = getZones(devicePosition);
        for (Zone zone : zonesToUpdate) {
            updateIlluminance(zone);
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
        return Collections.unmodifiableSet(new HashSet<LocatedDevice>(_lights));
    }

    @Validate
    private void start() {
        _contextMgr.addListener(this);

        DateTime dateTimeEli =new DateTime(_clock.currentTimeMillis());
        int hour = dateTimeEli.getHourOfDay();
        currentMomentOfTheDay = PartOfTheDay.getCorrespondingMoment(hour) ;

        PeriodicRunnable computeTempTask = new PeriodicRunnable() {
            @Override
            public long getPeriod() {
                return 1800*1000 ; // call every 30 minutes
            }


            @Override
            public String getGroup() {
                return "IlluminancePM-group";
            }

            @Override
            public void run() {
                DateTime dateTimeEli =new DateTime(_clock.currentTimeMillis());
                int hour = dateTimeEli.getHourOfDay();
                PartOfTheDay temp = PartOfTheDay.getCorrespondingMoment(hour) ;;
                if (currentMomentOfTheDay != temp ){
                    synchronized (_clockLock){
                        currentMomentOfTheDay = temp;
                    }
                    momentOfTheDayHasChanged(currentMomentOfTheDay);
                }

            }

        };


        _computeTempTaskSRef = _context.registerService(PeriodicRunnable.class.getName(), computeTempTask,
                new Hashtable());
    }

    @Invalidate
    private void stop() {
        _contextMgr.removeListener(this);
    }

    /**
     * Returns a new mutable set of all light devices for each call.
     *
     * @return a new mutable set of all light devices for each call.
     */
    private Set<GenericDevice> getLightDevices() {
        Set<GenericDevice> devices = new HashSet<GenericDevice>();
        synchronized (_deviceLock) {
            for (LocatedDevice locatedDevice : _lights) {
                GenericDevice deviceObject = locatedDevice.getDeviceObject();
                if (deviceObject != null)
                    devices.add(deviceObject);
            }
        }
        return devices;
    }

    private Set<GenericDevice> getLightDevicesFromZone(Zone zone) {
        Set<GenericDevice> devices = getLightDevices();
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
     * Computes and updates the illuminance property value of specified zone .
     * The formula used to compute the illuminance is :
     * Illuminance [cd/m² or lux]=(power[W]*680.0[lumens])/surface[m²]
     *
     * @param zone a zone
     */
    private void updateIlluminance(Zone zone) {
        double returnedIlluminance = 0.0;
        synchronized (_clockLock){
            returnedIlluminance = currentExternalSource;
        }

        int activeLightSize = 0;
        double surface = _zoneSizeCalc.getSurfaceInMeterSquare(zone.getId());;
        double powerLevelTotal = 0.0;

        Set<GenericDevice> devices = getLightDevicesFromZone(zone);
        for (GenericDevice device : devices) {
            if (device instanceof BinaryLight) {
                BinaryLight binaryLight = (BinaryLight) device;

                if (binaryLight.getPowerStatus()) {
                    activeLightSize += 1;
                    powerLevelTotal += binaryLight.getMaxPowerLevel();
                } else powerLevelTotal += 0.0d;
            } else if (device instanceof DimmerLight) {
                DimmerLight dimmerLight = (DimmerLight) device;

                if (dimmerLight.getPowerLevel() != 0.0d) {
                    activeLightSize += 1;
                    powerLevelTotal += dimmerLight.getPowerLevel() * dimmerLight.getMaxPowerLevel();
                }
            }
        }

        if (activeLightSize != 0){
            returnedIlluminance += ( (powerLevelTotal  * LUMENS_CONSTANT_VALUE) / surface) ;
        }

        zone.setVariableValue(ILLUMINANCE_PROP_NAME, returnedIlluminance);
    }

    @Override
    public void zoneAdded(Zone zone) {
        updateIlluminance(zone);
    }

    @Override
    public void zoneRemoved(Zone zone) {
        //do nothing
    }

    @Override
    public void zoneMoved(Zone zone, Position oldPosition, Position newPosition) {
        updateIlluminance(zone);
    }

    @Override
    public void zoneResized(Zone zone) {
        updateIlluminance(zone);
    }

    @Override
    public void zoneParentModified(Zone zone, Zone oldZone, Zone newZone) {
        //do nothing
    }

    @Override
    public void deviceAttached(Zone zone, LocatedDevice locatedDevice) {
        //do nothing
    }

    @Override
    public void deviceDetached(Zone zone, LocatedDevice locatedDevice) {
        //do nothing
    }

    @Override
    public void zoneVariableAdded(Zone zone, String s) {
        //do nothing  //TODO if manage external illuminance, may have impact
    }

    @Override
    public void zoneVariableRemoved(Zone zone, String s) {
        //do nothing //TODO if manage external illuminance, may have impact
    }

    @Override
    public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue) {
        //do nothing //TODO if manage external illuminance, may have impact
    }


    public void momentOfTheDayHasChanged(PartOfTheDay newMomentOfTheDay) {
        synchronized (_clockLock){
            if (newMomentOfTheDay == PartOfTheDay.AFTERNOON){
                currentExternalSource = AFTERNOON_EXTERNAL_SOURCE_POWER;
            }else if (newMomentOfTheDay == PartOfTheDay.NIGHT){
                currentExternalSource = NIGHT_EXTERNAL_SOURCE_POWER;
            }else if(newMomentOfTheDay == PartOfTheDay.EVENING){
                currentExternalSource = EVENING_EXTERNAL_SOURCE_POWER;
            }else if(newMomentOfTheDay == PartOfTheDay.MORNING){
                currentExternalSource = MORNING_EXTERNAL_SOURCE_POWER;
            }
        }
        updateZones();
    }

}
