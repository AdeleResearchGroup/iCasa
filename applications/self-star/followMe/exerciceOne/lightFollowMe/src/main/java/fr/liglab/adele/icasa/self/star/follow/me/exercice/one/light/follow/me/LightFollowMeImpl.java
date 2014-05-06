package fr.liglab.adele.icasa.self.star.follow.me.exercice.one.light.follow.me;

import fr.liglab.adele.icasa.dependency.handler.annotations.RequiresDevice;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
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

@Component(name="LightFollowMe")
@Instantiate
public class LightFollowMeImpl implements DeviceListener {

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

        //we assume that we listen only to presence sensor events (otherwise there is a bug)
        if (device instanceof PresenceSensor ){

            PresenceSensor changingSensor = (PresenceSensor) device;
            // check the change is related to presence sensing
            PresencePropertyModified( changingSensor, propertyName,  oldValue,  newValue);
        }

        if (device instanceof BinaryLight ){
            BinaryLight changingBinaryLight = (BinaryLight) device;
            BinaryPropertyModified( changingBinaryLight, propertyName,  oldValue,  newValue);
        }
    }

    public synchronized void PresencePropertyModified(PresenceSensor changingSensor,String propertyName, Object oldValue, Object newValue) {
        if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {
            // get the location where the sensor is:
            String detectorLocation = (String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME);
            // if the location is known :
            if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
                // get the related binary lights
                List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(detectorLocation);
                for (BinaryLight binaryLight : sameLocationLigths) {
                    // and switch them on/off depending on the sensed presence
                    binaryLight.setPowerStatus(!(Boolean) oldValue);
                }
            }
        }
    }

    public synchronized void BinaryPropertyModified(BinaryLight changingBinaryLight,String propertyName, Object oldValue, Object newValue) {
        if (propertyName.equals(BinaryLight.LOCATION_PROPERTY_NAME)){
            String binaryLightLocation = (String) changingBinaryLight.getPropertyValue(LOCATION_PROPERTY_NAME);
            if (!binaryLightLocation.equals(LOCATION_UNKNOWN)) {
                if (presenceFromLocation((String)newValue)){
                    changingBinaryLight.turnOn();
                }else{
                    changingBinaryLight.turnOff();
                }
            }
            else{
                changingBinaryLight.turnOff();
            }
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

    private synchronized List<PresenceSensor> getPresenceSensorFromLocation(String location) {
        List<PresenceSensor> presenceSensorLocation = new ArrayList<PresenceSensor>();
        for (PresenceSensor presenceSensor : presenceSensors) {
            if (presenceSensor.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                presenceSensorLocation.add(presenceSensor);
            }
        }
        return  presenceSensorLocation;
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

}
