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
package fr.liglab.adele.habits.monitoring.button;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.annotations.Collector;
import fr.liglab.adele.cilia.framework.AbstractCollector;
import fr.liglab.adele.habits.monitoring.measure.generator.Measure;
import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.dependency.handler.annotations.RequiresDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Measure Collector for presence sensor devices.
 *
 * 
 */
@Collector(name="PushButtonCollector")
public class ButtonCollector extends AbstractCollector implements
		DeviceListener<PushButton> {

	private Map<String, PushButton> detectors = new HashMap<String, PushButton>();

	private int counter = 1;

	private static final Logger logger = LoggerFactory
			.getLogger(Constants.ICASA_LOG + ".apps.habits-monitoring");

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.device.DeviceListener#notifyDeviceEvent()
	 */

	/**
	 * It is called when a new service of type PresenceDetector is registered
	 * into the gateway. (callback method, see metadata.xml).
	 * 
	 * @param detector
	 *            A new PresenceDetector (proxy)
	 */
    @RequiresDevice(id = "pushButton", type = "bind", aggregate=true)
    public void bindProxy(PushButton detector) {
		logger.info("A new proxy has been found, id "
				+ detector.getSerialNumber());
		synchronized (detectors) {
			detectors.put(detector.getSerialNumber(), detector);
		}
		detector.addListener(this);
	}
    @RequiresDevice(id = "pushButton", type = "unbind")
	public void unbindProxy(PushButton detector) {
		logger.info("A proxy is now outside from the zone, id "
				+ detector.getSerialNumber());
		synchronized (detectors) {
			detectors.remove(detector.getSerialNumber());
		}
		detector.removeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.icasa.device.DeviceListener#notifyDeviceEvent(java.lang
	 * .String)
	 */
	public void notifyDeviceEvent(String deviceSerialNumber) {

        PushButton detector = null;
		synchronized (detectors) {
			detector = detectors.get(deviceSerialNumber);
		}

		if (detector != null) {
			Measure measure = new Measure();
		    measure.setDeviceId(deviceSerialNumber);
		    Data data = new Data(measure);

            // simulation reliability
            int res = counter % 4;
            if (res != 0)
                measure.setReliability(100);
            else
                measure.setReliability(60);
            counter++;

            notifyDataArrival(data);
		}

	}

	public void deviceAdded(PushButton device) {
		// do nothing
	}

	public void deviceRemoved(PushButton device) {
		// do nothing
	}

	public void devicePropertyModified(PushButton device,
			String propertyName, Object oldValue, Object newValue) {
        if(propertyName.compareTo(PushButton.PUSH_AND_HOLD) == 0){
            notifyDeviceEvent(device.getSerialNumber());
        }
	}

	public void devicePropertyAdded(PushButton device, String propertyName) {
		// do nothing
	}

	public void devicePropertyRemoved(PushButton device, String propertyName) {
        // do nothing
	}

	@Override
	public void deviceEvent(PushButton detector, Object data) {
        if ((data != null) && (data instanceof Boolean)) {
            boolean movementDetected = (Boolean) data;
            if (movementDetected)
                notifyDeviceEvent(detector.getSerialNumber());
        }
	}

}
