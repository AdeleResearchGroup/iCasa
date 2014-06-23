package fr.liglab.adele.icasa.electricity.manager.sample;

import org.joda.time.DateTime;

/**
 * Created by horakm on 4/17/14.
 */
public interface ConsumptionSample {
    public double getConsumption();
    public DateTime getDate();
    public long getId();
}
