package fr.liglab.adele.habits.monitoring.autonomic.manager;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Kettani Mehdi
 * Date: 26/04/13
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class GenericDeviceListener implements DeviceListener {

    private static final Logger logger = LoggerFactory.getLogger(GenericDeviceListener.class);

    private Map<String, GenericDevice> devices;

    // DP installer
    // voir classe dans store/dp-rest-installer

    @Override
    public void deviceAdded(GenericDevice genericDevice) {

        if (devices == null){
            devices = new HashMap<String, GenericDevice>();
        }
        devices.put(genericDevice.getSerialNumber(), genericDevice);

        if (genericDevice instanceof PresenceSensor){

        }
    }

    @Override
    public void deviceRemoved(GenericDevice genericDevice) {


    }

    @Override
    public void devicePropertyModified(GenericDevice genericDevice, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void devicePropertyAdded(GenericDevice genericDevice, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void devicePropertyRemoved(GenericDevice genericDevice, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
