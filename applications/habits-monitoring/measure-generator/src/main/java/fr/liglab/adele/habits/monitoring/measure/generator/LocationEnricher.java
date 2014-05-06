package fr.liglab.adele.habits.monitoring.measure.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.location.LocatedDevice;

/**
 * Location enricher mediator.
 *
 */
public class LocationEnricher {

	private static final Logger logger = LoggerFactory
			.getLogger(LocationEnricher.class);
	
	public ContextManager icasa;
	
	public Data process(Data data) {
		if (data != null) {
			Measure measure = (Measure) data.getContent();
			LocatedDevice device = icasa.getDevice(measure.getDeviceId());
			String location = (String) device.getPropertyValue("Location");
			measure.setLocalisation(location);
			logger.debug("The location has been set to : " + location);
			return data;
		}
		return null;
	}
}
