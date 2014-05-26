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
package fr.liglab.adele.habits.monitoring.presence.sensor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;
import fr.liglab.adele.habits.monitoring.measure.generator.Measure;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;

/**
 * Measure Collector for presence sensor devices.
 *
 * 
 */
public class MeasureCollector extends AbstractCollector implements
		DeviceListener<PresenceSensor> {

	private Map<String, PresenceSensor> detectors = new HashMap<String, PresenceSensor>();

	private int counter = 1;

	private static final Logger logger = LoggerFactory
			.getLogger(MeasureCollector.class);

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
	public void bindProxy(PresenceSensor detector) {
		logger.info("A new proxy has been found, id "
				+ detector.getSerialNumber());
		synchronized (detectors) {
			detectors.put(detector.getSerialNumber(), detector);
		}
		detector.addListener(this);
	}

	public void unbindProxy(PresenceSensor detector) {
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

		PresenceSensor detector = null;
		synchronized (detectors) {
			detector = detectors.get(deviceSerialNumber);
		}

		if (detector != null) {
			if (detector.getSensedPresence()) {
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

	}

	public void deviceAdded(PresenceSensor device) {
		// do nothing
	}

	public void deviceRemoved(PresenceSensor device) {
		// do nothing
	}

	public void devicePropertyModified(PresenceSensor device,
			String propertyName, Object oldValue, Object newValue) {
		notifyDeviceEvent(device.getSerialNumber());
	}

	public void devicePropertyAdded(PresenceSensor device, String propertyName) {
		notifyDeviceEvent(device.getSerialNumber());
	}

	public void devicePropertyRemoved(PresenceSensor device, String propertyName) {
		notifyDeviceEvent(device.getSerialNumber());
	}

	@Override
	public void deviceEvent(PresenceSensor arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
