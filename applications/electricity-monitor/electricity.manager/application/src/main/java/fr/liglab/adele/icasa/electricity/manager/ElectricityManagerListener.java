package fr.liglab.adele.icasa.electricity.manager;

import fr.liglab.adele.icasa.location.Zone;
import org.joda.time.DateTime;

/**
 * Created by horakm on 5/23/14.
 */
public interface ElectricityManagerListener {

    /**
     * Notify if the consumption of a device changed
     */
    public void deviceConsumptionModified(String location, String device, Double consumption, DateTime date);

    /**
     * Notify if the consumption of a zone changed
    */
    public void zoneConsumptionModified(String zone, Double consumption, DateTime date);

    /**
     * Notify if a zone is added
     */
    public void zoneAdded(Zone zone);

    /**
     * Notify if a zone is removed
     */
    public void zoneRemoved(Zone zone);
}
