package fr.liglab.adele.icasa.application.actimetrics.manager;


import fr.liglab.adele.icasa.actimetrics.event.webservice.api.ProcessEventException;
import fr.liglab.adele.icasa.actimetrics.event.webservice.api.ProcessEventService;
import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.dependency.handler.annotations.RequiresDevice;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import org.apache.felix.ipojo.annotations.*;

import java.util.Date;
import java.util.Map;



/**
 *
 */
@Component(name="ActimetricManager")
@Instantiate(name="ActimetricManagerImpl-0")
public class ActimetricManagerImpl implements DeviceListener {

    protected static final String APPLICATION_ID = "actimetrics";
    /**
     * The name of the LOCATION property
     */
    public static final String LOCATION_PROPERTY_NAME = "Location";

    /**
     * The name of the location for unknown value
     */
    public static final String LOCATION_UNKNOWN = "unknown";

    /**
     * The name of the LOCATION property
     */
    public static final String USERNAME = "Aurelie";

    private int motionCounter = 1 ;

    private int pushCounter = 1 ;

    private int photometerCounter = 1 ;

    private int presenceCounter = 1 ;

    @Requires
    private Clock clock;

    @Requires
    private ProcessEventService processEventService;

    @Requires
    private Preferences preferences;


    /** Field for presenceSensors dependency */
    @RequiresDevice(id="presenceSensors", type="field", optional=true)
    private PresenceSensor[] presenceSensors;


    /** Field for presenceSensors dependency */
    @RequiresDevice(id="motionSensors", type="field", optional=true)
    private MotionSensor[] motionSensors;

    /** Field for presenceSensors dependency */
    @RequiresDevice(id="pushButtons", type="field", optional=true)
    private PushButton[] pushButtons;

    /**
     * Bind Method for PresenceSensors dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="presenceSensors", type="bind")
    public void bindPresenceSensor(PresenceSensor presenceSensor, Map<Object, Object> properties) {
        presenceSensor.addListener(this);

    }

    /**
     * Unbind Method for PresenceSensors dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="presenceSensors", type="unbind")
    public void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
        presenceSensor.removeListener(this);
    }

    /**
     * Bind Method for motionSensors dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="motionSensors", type="bind")
    public void bindMotionSensor(MotionSensor motionSensor, Map<Object, Object> properties) {
        motionSensor.addListener(this);
    }

    /**
     * Unbind Method for motionSensors dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="motionSensors", type="unbind")
    public void unbindMotionSensor(MotionSensor motionSensor, Map properties) {
        motionSensor.removeListener(this);
    }


    /**
     * Bind Method for pushButtons dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="pushButtons", type="bind")
    public void bindPushButton(PushButton pushButton, Map<Object, Object> properties) {
        pushButton.addListener(this);

    }

    /**
     * Unbind Method for pushButtons dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="pushButtons", type="unbind")
    public void unbindPushButton(PushButton pushButton, Map properties) {
        pushButton.removeListener(this);
    }





    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
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

        for (MotionSensor motionSensor : motionSensors) {
            motionSensor.removeListener(this);
        }

        for (PushButton pushButton : pushButtons) {
            pushButton.removeListener(this);
        }
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        clock.resume();
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

        if (device instanceof PushButton ){
            PushButton sensor = (PushButton)device;
            if( !( ( (String)sensor.getPropertyValue(LOCATION_PROPERTY_NAME) ).equals(LOCATION_UNKNOWN) ) ){
                if(propertyName.compareTo(PushButton.PUSH_AND_HOLD) == 0){
                    notifyPushButton(sensor);
                }
            }
        } else if (device instanceof PresenceSensor ){
            PresenceSensor sensor = (PresenceSensor)device;
            if( !( ( (String)sensor.getPropertyValue(LOCATION_PROPERTY_NAME) ).equals(LOCATION_UNKNOWN) ) ){
                if (sensor.getSensedPresence()){
                    notifyPresenceSensor(sensor);
                }
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
        if (device instanceof MotionSensor ){
            MotionSensor sensor = (MotionSensor) device;
            if( !( ( (String)sensor.getPropertyValue(LOCATION_PROPERTY_NAME) ).equals(LOCATION_UNKNOWN) ) ){
                if ((data != null) && (data instanceof Boolean)) {
                    boolean movementDetected = (Boolean) data;
                    if (movementDetected)
                        notifyMotionSensor(sensor);
                }
            }
        } else if (device instanceof PushButton ){
            PushButton sensor = (PushButton) device;

            if( !( ( (String)sensor.getPropertyValue(LOCATION_PROPERTY_NAME) ).equals(LOCATION_UNKNOWN) ) ){
                if ((data != null) && (data instanceof Boolean)) {
                    boolean movementDetected = (Boolean) data;

                    if (movementDetected)
                        notifyPushButton(sensor);
                }
            }
        }
    }


    public void notifyMotionSensor(MotionSensor sensor){
        Date eventDate= new Date(clock.currentTimeMillis());

        if (motionCounter%4 == 0){
            try{
                processEventService.processEventData("shake",getUsername(),"location",eventDate,(float)100.0,(String) sensor.getPropertyValue(LOCATION_PROPERTY_NAME));
                motionCounter =1;
            }catch (ProcessEventException e) {
                e.printStackTrace();
            }
        }else{
            try{
                processEventService.processEventData("shake",getUsername(),"location",eventDate,(float)60.0,(String) sensor.getPropertyValue(LOCATION_PROPERTY_NAME));
                motionCounter ++;
            }catch (ProcessEventException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyPushButton(PushButton sensor){
        Date eventDate= new Date(clock.currentTimeMillis());

        if (pushCounter%4 == 0){
            try{
                processEventService.processEventData("shake",getUsername(),"location",eventDate,(float)100.0,(String) sensor.getPropertyValue(LOCATION_PROPERTY_NAME));
                pushCounter =1;
            }catch (ProcessEventException e) {
                e.printStackTrace();
            }
        }else{
            try{
                processEventService.processEventData("shake",getUsername(),"location",eventDate,(float)60.0,(String) sensor.getPropertyValue(LOCATION_PROPERTY_NAME));
                pushCounter ++;
            }catch (ProcessEventException e) {
                e.printStackTrace();
            }
        }
    }


    public void notifyPresenceSensor(PresenceSensor sensor){
        Date eventDate= new Date(clock.currentTimeMillis());

        if (presenceCounter%4 == 0){
            try{
                processEventService.processEventData("shake",getUsername(),"location",eventDate,(float)100.0,(String) sensor.getPropertyValue(LOCATION_PROPERTY_NAME));
                presenceCounter =1;
            }catch (ProcessEventException e) {
                e.printStackTrace();
            }
        }else{
            try{
                processEventService.processEventData("shake",getUsername(),"location",eventDate,(float)60.0,(String) sensor.getPropertyValue(LOCATION_PROPERTY_NAME));
                presenceCounter ++;
            }catch (ProcessEventException e) {
                e.printStackTrace();
            }
        }
    }

    private String getUsername() {
        String tempValue = (String) preferences.getApplicationPropertyValue(APPLICATION_ID, "Username");
        if (tempValue != null) {
            return tempValue;
        } else {
            return USERNAME;
        }
    }

}

