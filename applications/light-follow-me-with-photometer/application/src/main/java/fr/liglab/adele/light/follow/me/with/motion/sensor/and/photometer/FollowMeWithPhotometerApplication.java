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
package fr.liglab.adele.light.follow.me.with.motion.sensor.and.photometer;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.clock.ClockListener;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.dependency.handler.annotations.RequiresDevice;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.ZoneListener;
import fr.liglab.adele.icasa.service.scheduler.ScheduledRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(name = "FollowMeWithPhotometerApplication")
@Instantiate(name = "FollowMeWithPhotometerApplication-0")
@CommandProvider(namespace = "LightFollowMeWithPhotometer")
public class FollowMeWithPhotometerApplication implements DeviceListener,ZoneListener,ClockListener {


    protected static final String APPLICATION_ID = "light.follow.me.with.motion.sensor.and.photometer";

    protected static double MIN_LUX = 50.0;

    private final BundleContext bundleContext;

    private final Object m_lock ;

    private final Object m_lockLux ;
    /**
     * The name of the location for unknown value
     */
    public static final String LOCATION_UNKNOWN = "unknown";

    private ServiceRegistration _computeTempTaskSRef;

    @Requires
    private ContextManager _contextMgr;

    @Requires
    private Clock _clock;

    private static long DEFAULT_TIMEOUT = 60000;

    private Map<String,TurnOffLightTask> turnOffLightTaskMap = new HashMap<String, TurnOffLightTask>();

    private Map<String,ServiceRegistration> serviceRegistrationMap = new HashMap<String, ServiceRegistration>();

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + "."+APPLICATION_ID);

    @Command
    public void setLux(Double lux){
        if (lux >  0.0) {
            synchronized (m_lockLux){
                MIN_LUX = lux;
            }
        }else{
            System.out.println(" Value is < 0 ");
        }

    }
    /** Field for binaryLights dependency */
    @RequiresDevice(id = "binaryLights", type = "field", optional = true)
    private BinaryLight[] binaryLights;

    /** Field for motionSensors dependency */
    @RequiresDevice(id = "motionSensors", type = "field", optional = true)
    private MotionSensor[] motionSensors;

    /** Field for motionSensors dependency */
    @RequiresDevice(id = "photometerSensors", type = "field", optional = true)
    private Photometer[] photometerSensors;

    /** Field for binaryLights dependency */
    @RequiresDevice(id = "dimmerLigths", type = "field", optional = true)
    private DimmerLight[] dimmerLigths;

    /** Bind Method for null dependency */
    @RequiresDevice(id = "motionSensors", type = "bind")
    public void bindMotionSensor(MotionSensor motionSensor, Map properties) {
        logger.trace("Register Listener to MotionSensor" + motionSensor.getSerialNumber());
        motionSensor.addListener(this);
    }

    /** Unbind Method for null dependency */
    @RequiresDevice(id = "motionSensors", type = "unbind")
    public void unbindMotionSensor(MotionSensor motionSensor, Map properties) {
        logger.trace("Remove Listener to MotionSensor" + motionSensor.getSerialNumber());
        motionSensor.removeListener(this);
    }

    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="binaryLights", type="bind")
    public void bindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
        binaryLight.addListener(this);
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="binaryLights", type="unbind")
    public void unbindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
        binaryLight.removeListener(this);
        String serialNumber = (String)properties.get(DimmerLight.DEVICE_SERIAL_NUMBER);
        for (LocatedDevice locatedDevice : _contextMgr.getDevices()){
            if ( locatedDevice.getSerialNumber().equals(serialNumber)){
                GenericDevice genericDevice = locatedDevice.getDeviceObject();
                if (genericDevice instanceof BinaryLight){
                    BinaryLight light = (BinaryLight) genericDevice;
                    light.turnOff();
                }
            }
        }

    }

    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="dimmerLigths", type="bind")
    public void bindDimmerLight(DimmerLight dimmerLight, Map<Object, Object> properties) {
        dimmerLight.addListener(this);
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="dimmerLigths", type="unbind")
    public void unbindDimmerLight(DimmerLight dimmerLight, Map<Object, Object> properties) {
        dimmerLight.removeListener(this);
        String serialNumber = (String)properties.get(DimmerLight.DEVICE_SERIAL_NUMBER);
        for (LocatedDevice locatedDevice : _contextMgr.getDevices()){
            if ( locatedDevice.getSerialNumber().equals(serialNumber)){
                GenericDevice genericDevice = locatedDevice.getDeviceObject();
                if (genericDevice instanceof DimmerLight){
                    DimmerLight light = (DimmerLight) genericDevice;
                    light.setPowerLevel(0.0);
                }
            }
        }
    }

    @Requires
    private Clock clock;

    public FollowMeWithPhotometerApplication(BundleContext context) {
        this.bundleContext = context;
        m_lock = new Object();
        m_lockLux = new Object();
    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        /*
         * It is extremely important to unregister the device listener. Otherwise, iCasa will continue to send
         * notifications to the unpredictable and invalid component instance. This will also causes problem when the
         * bundle is stopped as iCasa will still hold a reference on the device listener object. Consequently, it (and
         * its bundle) won't be garbage collected causing a memory issue known as stale reference.
         */
        for (MotionSensor motionSensorSensor : motionSensors) {
            motionSensorSensor.removeListener(this);
        }

        for (BinaryLight binaryLight : binaryLights) {
            binaryLight.removeListener(this);
        }

        for (DimmerLight dimmerLight : dimmerLigths) {
            dimmerLight.removeListener(this);
        }

        _contextMgr.removeListener(this);

    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        _contextMgr.addListener(this);
        _clock.resume();
    }


    @Override
    public void factorModified(int oldFactor) {

    }

    @Override
    public void startDateModified(long oldStartDate) {

    }

    @Override
    public void clockPaused() {

    }

    @Override
    public void clockResumed() {

    }

    @Override
    public void clockReset() {

    }

    @Override
    public void deviceAdded(GenericDevice device) {

    }

    @Override
    public void deviceRemoved(GenericDevice device) {

    }

    @Override
    public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {

        if (device instanceof BinaryLight){
            synchronized (m_lock){
                BinaryLight changingBinaryLight = (BinaryLight) device;
                if (propertyName.equals(BinaryLight.LOCATION_PROPERTY_NAME)){
                    changingBinaryLight.turnOff();
                }
            }
        }
        if (device instanceof DimmerLight){
            synchronized (m_lock){
                DimmerLight changingDimmerLight = (DimmerLight) device;
                if (propertyName.equals(DimmerLight.LOCATION_PROPERTY_NAME)){
                    changingDimmerLight.setPowerLevel(0);
                }
            }
        }
    }

    @Override
    public void devicePropertyAdded(GenericDevice device, String propertyName) {

    }

    @Override
    public void devicePropertyRemoved(GenericDevice device, String propertyName) {

    }

    @Override
    public void deviceEvent(GenericDevice device, Object data) {
        String location = String.valueOf(device.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME));
        if (!location.equals(LOCATION_UNKNOWN)){
            synchronized (m_lockLux){
                if(getMediaIlluminance(location) < MIN_LUX ){
                    synchronized (m_lock){
                        setOnAllLightsInLocation(location);
                    }
                    if (turnOffLightTaskMap.containsKey(location)){
                        serviceRegistrationMap.get(location).unregister();
                        serviceRegistrationMap.remove(location);
                        turnOffLightTaskMap.remove(location);
                    }
                    TurnOffLightTask task = new TurnOffLightTask() ;
                    task.setExecutionDate(clock.currentTimeMillis() + DEFAULT_TIMEOUT);
                    task.setLocation(location);
                    turnOffLightTaskMap.put(location, task);
                    _computeTempTaskSRef = bundleContext.registerService(ScheduledRunnable.class.getName(), task,new Hashtable());
                    serviceRegistrationMap.put(location,_computeTempTaskSRef);
                }
            }
        }
    }

    @Override
    public void zoneAdded(Zone zone) {

    }

    @Override
    public void zoneRemoved(Zone zone) {

    }

    @Override
    public void zoneMoved(Zone zone, Position oldPosition, Position newPosition) {

    }

    @Override
    public void zoneResized(Zone zone) {

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

    }

    /**
     * Return all BinaryLight from the given location
     *
     * @param location
     *            : the given location
     * @return the list of matching BinaryLights
     */
    private  List<BinaryLight> getBinaryLightFromLocation(String location) {
        List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();
        for (BinaryLight binLight : binaryLights) {
            if (binLight.getPropertyValue(BinaryLight.LOCATION_PROPERTY_NAME).equals(location)) {
                binaryLightsLocation.add(binLight);
            }
        }
        return binaryLightsLocation;
    }

    private  List<DimmerLight> getDimmerLightFromLocation(String location) {
        List<DimmerLight> dimmerLightsLocation = new ArrayList<DimmerLight>();
        for (DimmerLight dimmerLight : dimmerLigths) {
            if (dimmerLight.getPropertyValue(BinaryLight.LOCATION_PROPERTY_NAME).equals(location)) {
                dimmerLightsLocation.add(dimmerLight);
            }
        }
        return dimmerLightsLocation;
    }


    private  void setOffAllLightsInLocation(String location) {

        List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(location);
        for(BinaryLight binaryLight : sameLocationLigths){
            binaryLight.turnOff();
        }
        List<DimmerLight> sameLocationDimmerLigths = getDimmerLightFromLocation(location);
        for(DimmerLight dimmerLight : sameLocationDimmerLigths){
            dimmerLight.setPowerLevel(0);
        }

    }

    private  void setOnAllLightsInLocation(String location) {

        List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(location);
        for(BinaryLight binaryLight : sameLocationLigths){
            binaryLight.turnOn();
        }

        List<DimmerLight> sameLocationDimmerLigths = getDimmerLightFromLocation(location);
        for(DimmerLight dimmerLight : sameLocationDimmerLigths){
            dimmerLight.setPowerLevel(1);
        }

    }


    /**
     * Return all Photometers from the given location
     *
     * @param location : the given location
     * @return the list of matching Photometers
     */
    private synchronized Set<Photometer> getPhotometerFromLocation(String location) {
        Set<Photometer> photometersLocation = new HashSet<Photometer>();

        //if zone does nor exist, return an empty list.
        if(_contextMgr.getZone(location) == null) {
            return photometersLocation;
        }
        //if zone exists, we get the BinaryLight objects by its location
        for (Photometer photometers : photometerSensors) {
            if (photometers.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME).equals(location)) {
                photometersLocation.add(photometers);
                logger.trace("Photometer match with location " + photometers.getSerialNumber());
            } else {
                logger.trace("Photometer not matching with location " + photometers.getSerialNumber());
            }
        }
        return photometersLocation;
    }


    private double getMediaIlluminance(String location) {
        Set<Photometer> photometers = getPhotometerFromLocation(location);
        double illuminance = 0;
        if(photometers.size()<1){
            return 0;//we don't have information.
        }
        for(Photometer photometer: photometers){
            illuminance += photometer.getIlluminance();
        }
        return illuminance/photometers.size();
    }


    /**
     * This task is charged of turn off the light.
     */
    public class TurnOffLightTask implements ScheduledRunnable {



        private long executionDate ;

        private String location;

        private String groupName = "Light-Follow-Me-With-Photometer";

        public void setExecutionDate(long executionDate) {
            this.executionDate = executionDate;
        }

        public void setLocation(String location) {
            this.location = location;
            this.groupName =  "Light-Follow-Me-With-Photometer-"+location;
        }

        @Override
        public long getExecutionDate() {
            return executionDate;
        }

        @Override
        public String getGroup() {
            return groupName;
        }

        @Override
        public void run() {
            synchronized (m_lock){
                setOffAllLightsInLocation(location);
            }
        }

    }



}
