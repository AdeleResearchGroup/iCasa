package fr.liglab.adele.icasa.electricity.viewer;

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 *
 */
public interface ElectricityViewer {

    /**
     * Get the consumption of all device.
     *
     * @return the consumption of all the device present on the platform in Watt.
     */
    public double getTotalConsumption();

    /**
     * Get the consumption of a specific zone.
     *
     * @param zoneId : id of the zone.
     * @return the consumption of the all the device present in the zone in Watt.
     */
    public double getZoneConsumption(String zoneId);

    /**
     * Get the consumption of a group of device.
     *
     * @param clazz : Interface that you want to compute the electricity consumption.
     * @param <T> : class that extend Generic device.
     * @return the consumption of the group of device in Watt.
     */
    public <T extends GenericDevice> double getGroupOfDeviceConsumption(Class<T> clazz);

    /**
     * Add a listener, must be synchronized.
     *
     * @param listener : listener to add.
     */
    public void addListener(ElectricityViewerListener listener);

    /**
     * Remove a listener, must be synchronized.
     *
     * @param listener : listener to remove.
     */
    public void removeListener(ElectricityViewerListener listener);
}
