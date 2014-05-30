package fr.liglab.adele.habits.monitoring.measure.generator;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.icasa.clock.Clock;

/**
 * 
 */
public class TimeStampEnricher {
	
	private Clock clock;

	private static final Logger logger = LoggerFactory
			.getLogger(TimeStampEnricher.class);

	public Data process(Data data) {
		if (data != null) {
			Measure measure = (Measure) data.getContent();
			long date = clock.currentTimeMillis();
			measure.setTimestamp(date);
			logger.debug("The gatewayId timestamp has been set to : " + new Date(date).toString());
			return data;
		}
		return null;
	}
}
