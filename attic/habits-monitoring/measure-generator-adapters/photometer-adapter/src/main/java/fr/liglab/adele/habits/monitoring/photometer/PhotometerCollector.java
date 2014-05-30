/**
 * 
 */
package fr.liglab.adele.habits.monitoring.photometer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;
import fr.liglab.adele.habits.monitoring.measure.generator.Measure;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.light.Photometer;

/**
 * Collector adapter for the photometer devices events. this adapter is
 * triggered when a photometer device illuminance property change.
 *
 */
public class PhotometerCollector extends AbstractCollector implements
		DeviceListener<Photometer> {

	private static final Logger logger = LoggerFactory
			.getLogger(PhotometerCollector.class);

	private Map<String, Photometer> detectors = new HashMap<String, Photometer>();

	private int counter = 1; 

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.icasa.device.DeviceListener#notifyDeviceEvent(java.lang
	 * .String)
	 */
	public void notifyDeviceEvent(String deviceSerialNumber) {

		Photometer detector = null;

		synchronized (detectors) {
			detector = (Photometer) detectors.get(deviceSerialNumber);
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

	/**
	 * It is called when a new service of type Photometer is registered into the
	 * gateway. (callback method, see metadata.xml).
	 * 
	 * @param detector
	 *            A new PresenceDetector (proxy)
	 */
	public void bindProxy(Photometer detector) {
		logger.info("A new proxy has been found, id "
				+ detector.getSerialNumber());
		synchronized (detectors) {
			detectors.put(detector.getSerialNumber(), detector);
		}
		detector.addListener(this);
	}

	public void unbindProxy(Photometer detector) {
		logger.info("A proxy is now outside from the zone, id "
				+ detector.getSerialNumber());
		synchronized (detectors) {
			detectors.remove(detector.getSerialNumber());
		}
		detector.removeListener(this);
	}

	public void deviceAdded(Photometer device) {
		// do nothing
	}

	public void devicePropertyAdded(Photometer device, String propertyName) {
		// notifyDeviceEvent(device.getSerialNumber());
	}

	public void devicePropertyModified(Photometer device,
			String propertyName, Object oldValue, Object newValue) {
		logger.debug("property that changed : " + propertyName);
		logger.debug("property old value : " + oldValue);
		logger.debug("property new  value : "
				+ device.getPropertyValue(propertyName));
		if (Photometer.PHOTOMETER_CURRENT_ILLUMINANCE.equals(propertyName)) {
			notifyDeviceEvent(device.getSerialNumber());
		}
	}

	public void devicePropertyRemoved(Photometer device, String propertyName) {
		// notifyDeviceEvent(device.getSerialNumber());
	}

	public void deviceRemoved(Photometer device) {
		// do nothing
	}

	@Override
	public void deviceEvent(Photometer arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
