package fr.liglab.adele.icasa.self.star.follow.me.exercice.two.dimmer.light.follow.me;

import fr.liglab.adele.icasa.dependency.handler.annotations.RequiresDevice;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */

@Component(name="DimmerLightFollowMe")
@Instantiate
public class DimmerLightFollowMeImpl implements DeviceListener {
    /**
     * The maximum number of lights to turn on when a user enters the room :
     **/
    private int maxLightsToTurnOnPerRoom = 1;

    /**
     * The name of the LOCATION property
     */
    public static final String LOCATION_PROPERTY_NAME = "Location";

    /**
     * The name of the location for unknown value
     */
    public static final String LOCATION_UNKNOWN = "unknown";

    /** Field for binaryLights dependency */
    @RequiresDevice(id="binaryLights", type="field", optional=true)
    private BinaryLight[] binaryLights;

    /** Field for presenceSensors dependency */
    @RequiresDevice(id="presenceSensors", type="field", optional=true)
    private PresenceSensor[] presenceSensors;

    /** Field for presenceSensors dependency */
    @RequiresDevice(id="dimmerLights", type="field", optional=true)
    private DimmerLight[] dimmerLights;

    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="binaryLights", type="bind")
    public void bindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
        System.out.println("bind binary light " + binaryLight.getSerialNumber());
        binaryLight.addListener(this);
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="binaryLights", type="unbind")
    public void unbindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
        System.out.println("unbind binary light " + binaryLight.getSerialNumber());
        binaryLight.removeListener(this);
    }

    /**
     * Bind Method for PresenceSensors dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="presenceSensors", type="bind")
    public void bindPresenceSensor(PresenceSensor presenceSensor, Map<Object, Object> properties) {
        System.out.println("bind presence sensor " + presenceSensor.getSerialNumber());
        presenceSensor.addListener(this);

    }

    /**
     * Unbind Method for PresenceSensors dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="presenceSensors", type="unbind")
    public void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
        System.out.println("Unbind presence sensor "+ presenceSensor.getSerialNumber());
        presenceSensor.removeListener(this);
    }

    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="dimmerLights", type="bind")
    public void bindDimmerLight(DimmerLight dimmerLight, Map<Object, Object> properties) {
        System.out.println("bind dimmer light " + dimmerLight.getSerialNumber());
        dimmerLight.addListener(this);
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="dimmerLights", type="unbind")
    public void unbindDimmerLight(DimmerLight dimmerLight, Map<Object, Object> properties) {
        System.out.println("unbind dimmer light " + dimmerLight.getSerialNumber());
        dimmerLight.removeListener(this);
    }


    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component is stopping...");
                /*
		 * It is extremely important to unregister the device listener.
		 * Otherwise, iCasa will continue to send notifications to the
		 * unpredictable and invalid component instance.
		 * This will also causes problem when the bundle is stopped as iCasa
		 * will still hold a reference on the device listener object.
		 * Consequently, it (and its bundle) won't be garbage collected
		 * causing a memory issue known as stale reference.
		 */
        for (PresenceSensor presenceSensor : presenceSensors) {
            presenceSensor.removeListener(this);
        }
        for(BinaryLight binaryLight  : binaryLights) {
            binaryLight.removeListener(this);
        }

        for(DimmerLight dimmerLight  : dimmerLights) {
            dimmerLight.removeListener(this);
        }
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component is starting...");
    }

    @Override
    public void deviceAdded(GenericDevice device) {
        System.out.println("Device Added");
    }

    @Override
    public void deviceRemoved(GenericDevice device) {
        System.out.println("Device Removed");
    }


    /**
     * This method is part of the DeviceListener interface and is called when a
     * subscribed device property is modified.
     *
     * @param device
     *            is the device whose property has been modified.
     * @param propertyName
     *            is the name of the modified property.
     */
    @Override
    public void devicePropertyModified(GenericDevice device,String propertyName, Object oldValue, Object newValue) {

        System.out.println("Device Property Modified");
        //we assume that we listen only to presence sensor events (otherwise there is a bug)
        if (device instanceof PresenceSensor ){
            System.out.println("PRESENCE SENSOR ");
            PresenceSensor changingSensor = (PresenceSensor) device;
            presenceModified( changingSensor, propertyName,  oldValue,  newValue);
        }

        if (device instanceof BinaryLight ){
            System.out.println("Binary Light ");
            BinaryLight changingBinaryLight = (BinaryLight) device;
            binaryLightModified( changingBinaryLight, propertyName, oldValue,newValue);
        }

        if (device instanceof DimmerLight ){
            System.out.println("Dimmer Light ");
            DimmerLight changingDimmerLight = (DimmerLight) device;
            dimmerLightModified( changingDimmerLight, propertyName, oldValue,newValue);
        }
    }



    @Override
    public void devicePropertyAdded(GenericDevice device, String propertyName) {
        System.out.println("Device Property Added");
    }

    @Override
    public void devicePropertyRemoved(GenericDevice device, String propertyName) {
        System.out.println("Device Property Removed");
    }

    @Override
    public void deviceEvent(GenericDevice device, Object data) {
        System.out.println("Device Event");
    }

    /**
     * Return all BinaryLight from the given location
     *
     * @param location
     *            : the given location
     * @return the list of matching BinaryLights
     */
    private synchronized List<BinaryLight> getBinaryLightFromLocation(String location) {
        List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();
        for (BinaryLight binLight : binaryLights) {
            if (binLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                binaryLightsLocation.add(binLight);
            }
        }
        return binaryLightsLocation;
    }

    /**
     * Return all BinaryLight from the given location
     *
     * @param location
     *            : the given location
     * @return the list of matching BinaryLights
     */
    private synchronized List<DimmerLight> getDimmerLightFromLocation(String location) {
        List<DimmerLight> dimmerLightsLocation = new ArrayList<DimmerLight>();
        for (DimmerLight dimmerLight : dimmerLights) {
            if (dimmerLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                dimmerLightsLocation.add(dimmerLight);
            }
        }
        return dimmerLightsLocation;
    }

    private synchronized int getLightTurnOn(String location) {
        int countTurnOn= 0;
        //count binary light on
        countTurnOn += getBinaryLightTurnOn(getBinaryLightFromLocation(location));
        //count dimmer light on
        countTurnOn += getDimmerLightTurnOn(getDimmerLightFromLocation(location));
        return countTurnOn;
    }

    private synchronized int getBinaryLightTurnOn(List<BinaryLight> listBinaryLights) {
        int countTurnOn= 0;
        for (BinaryLight binLight : listBinaryLights) {
            if (binLight.getPowerStatus()) {
                countTurnOn +=1;
            }
        }
        return countTurnOn;
    }

    private synchronized int getDimmerLightTurnOn(List<DimmerLight> listDimmerLights) {
        int countTurnOn= 0;
        for (DimmerLight binLight : listDimmerLights) {
            if (binLight.getPowerLevel() > 0) {
                countTurnOn +=1;
            }
        }
        return countTurnOn;
    }

    private synchronized List<PresenceSensor> getPresenceSensorFromLocation(String location) {
        List<PresenceSensor> presenceSensorLocation = new ArrayList<PresenceSensor>();
        for (PresenceSensor presenceSensor : presenceSensors) {
            if (presenceSensor.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                presenceSensorLocation.add(presenceSensor);
            }
        }
        return presenceSensorLocation;
    }

    private synchronized int setBinaryLightFromLocation(String location,int lightOn) {
        // get the related binary lights
        List<BinaryLight> oldSameLocationLigths = getBinaryLightFromLocation(location);
        for (BinaryLight binaryLight : oldSameLocationLigths) {
            if (lightOn < maxLightsToTurnOnPerRoom ){
                binaryLight.turnOn();
                lightOn ++;
            }else{
                break;
            }
        }
        return lightOn;
    }

    private synchronized int setDimmerLightFromLocation(String location,int lightOn) {
        // get the related binary lights
        List<DimmerLight> oldSameLocationLigths = getDimmerLightFromLocation(location);

        for (DimmerLight dimmerLight : oldSameLocationLigths) {
            if (lightOn < maxLightsToTurnOnPerRoom ){
                dimmerLight.setPowerLevel(1);
                lightOn ++;
            }else{
                break;
            }
        }
        return lightOn;
    }
    private synchronized boolean presenceFromLocation(String location) {
        int switchOn = 0;
        int switchOff = 0;
        List<PresenceSensor> presenceSensorLocation = new ArrayList<PresenceSensor>();
        presenceSensorLocation = getPresenceSensorFromLocation(location);
        for (PresenceSensor presenceSensor : presenceSensorLocation) {
            if (presenceSensor.getSensedPresence()) {
                switchOn +=1;
            }
            else{
                switchOff +=1;
            }
        }
        if (switchOn > switchOff){
            return true;
        }
        else{
            return false;
        }
    }
    private synchronized void dimmerLightModified(DimmerLight changingDimmerLight,String propertyName, Object oldValue, Object newValue) {
        if (propertyName.equals(DimmerLight.LOCATION_PROPERTY_NAME)){
            String dimmerLightLocation = (String) changingDimmerLight.getPropertyValue(LOCATION_PROPERTY_NAME);

            if (!dimmerLightLocation.equals(LOCATION_UNKNOWN)) {
                // get the related presence sensor
                List<PresenceSensor> sameLocationPresenceSensor = getPresenceSensorFromLocation(dimmerLightLocation);
                int countLightOn = getLightTurnOn(dimmerLightLocation);
                if (changingDimmerLight.getPowerLevel()>0) countLightOn --;
                if (countLightOn<maxLightsToTurnOnPerRoom){
                    List<DimmerLight> sameLocationDimmerLigths = getDimmerLightFromLocation(dimmerLightLocation);
                    boolean presenceLocation = presenceFromLocation(dimmerLightLocation);
                    // decide to change or not the binary light state
                    if (presenceLocation){
                        changingDimmerLight.setPowerLevel(1);
                    }
                    else{
                        changingDimmerLight.setPowerLevel(0);
                    }
                }
                else{
                    changingDimmerLight.setPowerLevel(0);
                }

                String oldLocation = (String) oldValue;
                if (!oldLocation.equals(LOCATION_UNKNOWN)) {
                    //check if in the old location have always a person
                    if (presenceFromLocation(oldLocation)){
                        int countLightOnOldLocation = getLightTurnOn(oldLocation);
                        countLightOnOldLocation = setBinaryLightFromLocation(oldLocation,countLightOnOldLocation);
                        setDimmerLightFromLocation(oldLocation,countLightOnOldLocation);

                    }
                }
            }
            else{
                changingDimmerLight.setPowerLevel(0);
            }
        }
    }

    private synchronized void presenceModified(PresenceSensor changingSensor,String propertyName, Object oldValue, Object newValue) {
        // check the change is related to presence sensing
        if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {
            // get the location where the sensor is:
            String detectorLocation = (String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME);
            // if the location is known :
            if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
                // get the related binary lights
                List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(detectorLocation);
                //get the number off light already turn on
                int countBinaryLightOn = getBinaryLightTurnOn(sameLocationLigths);
                for (BinaryLight binaryLight : sameLocationLigths) {
                    // and switch them on/off depending on the sensed presence
                    if((Boolean) oldValue){

                        binaryLight.setPowerStatus(!(Boolean) oldValue);
                    }
                    else {
                        //check if we can turn off more lights
                        if (countBinaryLightOn < maxLightsToTurnOnPerRoom ){
                            binaryLight.turnOn();
                            countBinaryLightOn ++;
                        }else{
                            break;
                        }
                    }
                }
                List<DimmerLight> sameLocationDimmerLigths = getDimmerLightFromLocation(detectorLocation);
                for (DimmerLight dimmerLight : sameLocationDimmerLigths) {
                    // and switch them on/off depending on the sensed presence
                    if((Boolean) oldValue){
                        dimmerLight.setPowerLevel(0);
                    }
                    else {
                        //check if we can turn off more lights
                        if (countBinaryLightOn < maxLightsToTurnOnPerRoom ){
                            dimmerLight.setPowerLevel(1);
                            countBinaryLightOn ++;
                        }else{
                            break;
                        }
                    }
                }
            }
        }
    }

    private synchronized void binaryLightModified(BinaryLight changingBinaryLight,String propertyName, Object oldValue, Object newValue) {
        if (propertyName.equals(BinaryLight.LOCATION_PROPERTY_NAME)){
            String binaryLightLocation = (String) changingBinaryLight.getPropertyValue(LOCATION_PROPERTY_NAME);

            if (!binaryLightLocation.equals(LOCATION_UNKNOWN)) {
                // get the related presence sensor
                List<PresenceSensor> sameLocationPresenceSensor = getPresenceSensorFromLocation(binaryLightLocation);

                List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(binaryLightLocation);
                int countLightOn = getLightTurnOn(binaryLightLocation);
                if (changingBinaryLight.getPowerStatus()) countLightOn --;
                if (countLightOn < maxLightsToTurnOnPerRoom){
                    if (presenceFromLocation(binaryLightLocation)){
                        changingBinaryLight.turnOn();
                    }
                    else{
                        changingBinaryLight.turnOff();
                    }
                }
                else{
                    changingBinaryLight.turnOff();
                }
                String oldLocation = (String) oldValue;
                if (!oldLocation.equals(LOCATION_UNKNOWN)) {
                    //check if in the old location have always a person
                    if (presenceFromLocation((String) oldValue)){
                        int countLightOnOldLocation = getLightTurnOn(oldLocation);
                        countLightOnOldLocation = setBinaryLightFromLocation(oldLocation,countLightOnOldLocation);
                        setDimmerLightFromLocation(oldLocation,countLightOnOldLocation);
                    }
                }
            }
            else{
                changingBinaryLight.turnOff();
            }
        }
    }
}
