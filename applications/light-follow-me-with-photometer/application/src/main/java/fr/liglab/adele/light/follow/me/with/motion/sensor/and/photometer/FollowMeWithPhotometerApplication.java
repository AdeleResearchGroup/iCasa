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
import fr.liglab.adele.icasa.dependency.handler.annotations.RequiresDevice;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import fr.liglab.adele.icasa.service.scheduler.ScheduledRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(name = "FollowMeWithPhotometerApplication")
@Instantiate(name = "FollowMeWithPhotometerApplication-0")
public class FollowMeWithPhotometerApplication implements DeviceListener,ClockListener {


    protected static final String APPLICATION_ID = "light.follow.me.with.motion.sensor.and.photometer";

    private final BundleContext bundleContext;

    private final Object m_devicelock ;

    private final Object m_taskLock ;

    private final Object m_taskRunningLock ;
    /**
     * The name of the location for unknown value
     */
    public static final String LOCATION_UNKNOWN = "unknown";

    @Requires
    private ContextManager _contextMgr;

    @Requires
    private Clock _clock;

    @Requires
    private Preferences preferences;

    private static final long DEFAULT_TIMEOUT = 5000;


    private  static final float MIN_LUX = (float)20.0 ;

    private final Map<String,TurnOffLightTask> turnOffLightTaskMap = new HashMap<String, TurnOffLightTask>();

    private final Map<String,ServiceRegistration> serviceRegistrationMap = new HashMap<String, ServiceRegistration>();

    protected final static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + "."+APPLICATION_ID);


    private long getTimeout() {
        Long tempValue = (Long) preferences.getApplicationPropertyValue(APPLICATION_ID, "Timeout");
        if (tempValue != null) {
            return tempValue;
        } else {
            return DEFAULT_TIMEOUT;
        }
    }

    private double getMinLux() {
        Float tempValue = (Float) preferences.getApplicationPropertyValue(APPLICATION_ID, "Minimum.lux");
        if (tempValue != null) {
            return tempValue;
        } else {
            return MIN_LUX;
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
    public synchronized void bindMotionSensor(MotionSensor motionSensor, Map properties) {
        try{
            logger.info("Add Listener to MotionSensor " + properties.get(MotionSensor.DEVICE_SERIAL_NUMBER));
            motionSensor.addListener(this);
        }catch (Exception e ){
            logger.warn(" Unbind motion ", e);
        }
    }

    /** Unbind Method for null dependency */
    @RequiresDevice(id = "motionSensors", type = "unbind")
    public synchronized void unbindMotionSensor(MotionSensor motionSensor, Map properties) {
        try{
            logger.info("Remove Listener to MotionSensor " + properties.get(MotionSensor.DEVICE_SERIAL_NUMBER));
            motionSensor.removeListener(this);
        }catch (Exception e ){
            logger.warn(" Unbind motion ", e);
        }
    }

    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="binaryLights", type="bind")
    public synchronized void bindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
        try{
            logger.info("Add Listener to BinaryLight " + properties.get(BinaryLight.DEVICE_SERIAL_NUMBER) );
            binaryLight.addListener(this);
        }catch (Exception e){
            logger.warn(" bind binary ", e);
        }
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="binaryLights", type="unbind")
    public synchronized void unbindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
        try{
            logger.info("Remove Listener to BinaryLight " + properties.get(BinaryLight.DEVICE_SERIAL_NUMBER) );
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
        }catch (Exception e){
            logger.warn(" unbind binary ", e);
        }
    }

    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="dimmerLigths", type="bind")
    public synchronized void bindDimmerLight(DimmerLight dimmerLight, Map<Object, Object> properties) {
        try{
            logger.info("Add Listener to BinaryLight " + properties.get(DimmerLight.DEVICE_SERIAL_NUMBER) );
            dimmerLight.addListener(this);
        }catch (Exception e){
            logger.warn(" bind dimmer : ", e);
        }
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="dimmerLigths", type="unbind")
    public synchronized void unbindDimmerLight(DimmerLight dimmerLight, Map<Object, Object> properties) {
        try{
            dimmerLight.removeListener(this);

            logger.info("Remove Listener to BinaryLight " + properties.get(DimmerLight.DEVICE_SERIAL_NUMBER) );
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
        }catch (Exception e){
            logger.warn(" Unbind dimmer : ", e);
        }
    }

    @Requires
    private Clock clock;

    public FollowMeWithPhotometerApplication(BundleContext context) {
            this.bundleContext = context;
            m_devicelock = new Object();
            m_taskLock= new Object();
            m_taskRunningLock = new Object();
    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        try{
            logger.info(" Light follow Me Stop ...");

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

            synchronized (m_taskLock){
                for(String key : serviceRegistrationMap.keySet()){
                    serviceRegistrationMap.get(key).unregister();
                }
                turnOffLightTaskMap.clear();
                serviceRegistrationMap.clear();
            }
        }catch (Exception e){
            logger.warn(" Stop Light follow Me :  ", e);
        }
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        logger.info(" Light follow Me Start ...");
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
            synchronized (m_devicelock){
                BinaryLight changingBinaryLight = (BinaryLight) device;
                if (propertyName.equals(BinaryLight.LOCATION_PROPERTY_NAME)){
                    changingBinaryLight.turnOff();
                }
            }
        }
        if (device instanceof DimmerLight){
            synchronized (m_devicelock){
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
        logger.info(" Detection Event ");
        String location = String.valueOf(device.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME));
        if ((location != null) && (!location.equals(LOCATION_UNKNOWN))){
            if(getMediaIlluminance(location) < getMinLux() ){
                synchronized (m_devicelock){
                    setOnAllLightsInLocation(location);
                }
                synchronized (m_taskLock){

                    if (turnOffLightTaskMap.containsKey(location)){
                        serviceRegistrationMap.get(location).unregister();
                        serviceRegistrationMap.remove(location);
                        turnOffLightTaskMap.remove(location);
                    }
                    TurnOffLightTask task = new TurnOffLightTask(location,clock.currentTimeMillis() + getTimeout()) ;
                    turnOffLightTaskMap.put(location, task);
                    ServiceRegistration computeTempTaskSRef = bundleContext.registerService(ScheduledRunnable.class.getName(), task,new Hashtable());
                    serviceRegistrationMap.put(location,computeTempTaskSRef);
                }
            }
        }
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
                logger.info(" light " + binLight.getSerialNumber() + " turnOn");
            }
        }
        return binaryLightsLocation;
    }

    private  List<DimmerLight> getDimmerLightFromLocation(String location) {
        List<DimmerLight> dimmerLightsLocation = new ArrayList<DimmerLight>();
        for (DimmerLight dimmerLight : dimmerLigths) {
            if (dimmerLight.getPropertyValue(BinaryLight.LOCATION_PROPERTY_NAME).equals(location)) {
                dimmerLightsLocation.add(dimmerLight);
                logger.info(" light " + dimmerLight.getSerialNumber() + " turnOn");
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
    private  Set<Photometer> getPhotometerFromLocation(String location) {
        Set<Photometer> photometersLocation = new HashSet<Photometer>();

        //if zone does nor exist, return an empty list.
        if(location.equals(LOCATION_UNKNOWN)) {
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


    private synchronized double getMediaIlluminance(String location) {
        Set<Photometer> photometers = getPhotometerFromLocation(location);
        double illuminance = 0;
        if(photometers.size()<1){
            logger.info(" No photometer in zone ");
            return 0;//we don't have information.
        }
        for(Photometer photometer: photometers){
            illuminance += photometer.getIlluminance();
        }
        logger.info(" Medium Illuminance is  " + illuminance/photometers.size());
        return illuminance/photometers.size();
    }


    /**
     * This task is charged of turn off the light.
     */
    private class TurnOffLightTask implements ScheduledRunnable {



        private final long executionDate ;

        private final String location;

        private final String groupName ;

        public TurnOffLightTask(String location,long executionDate){
            this.location = location;
            groupName = "Light-Follow-Me-With-Motion-Sensor-And-Photometer-"+location;
            this.executionDate = executionDate;
        }

        @Override
        public long getExecutionDate() {
            return executionDate;
        }

        @Override
        public synchronized String getGroup() {
            return groupName;
        }

        @Override
        public void run() {
            synchronized (m_devicelock){
                setOffAllLightsInLocation(location);
            }
        }
    }



}
