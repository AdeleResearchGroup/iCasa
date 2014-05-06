package fr.liglab.adele.icasa.electricity.viewer;

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 *
 */
public interface ElectricityViewerListener {

    /**
     * Notify if a device change his electricity consumption.
     *
     * @param device : The device that modify his consumption.
     * @param newConsumption : the new consumption of the device in Watt. If the device has been removed of the platform the new consumption equals -1.
     * @param oldConsumption : the old consumption of the device in Watt. If the device has been added of the platform the old consumption equals -1.
     */
    public void deviceConsumptionModified(GenericDevice device,double newConsumption, double oldConsumption);

    /**
     * Notify if the consumption of a zone change his electricity consumption
     *
     * @param zoneId : The id of the zone that modify his consumption.
     * @param newConsumption : the new consumption of the device in Watt. If the zone has been removed of the platform the new consumption equals -1.
     * @param oldConsumption : the old consumption of the zone in Watt. If the zone has been added of the platform the new consumption equals -1.
     */
    public void zoneConsumptionModified(String zoneId,double newConsumption, double oldConsumption);
}
