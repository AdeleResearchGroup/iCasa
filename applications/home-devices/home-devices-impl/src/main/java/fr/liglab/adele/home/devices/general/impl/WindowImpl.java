package fr.liglab.adele.home.devices.general.impl;


import fr.liglab.adele.home.devices.general.Window;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.framework.Constants;

import java.util.List;

/**
 * Simple implementation of a simulated window.
 */
@Component(name = "iCasa.Window")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class WindowImpl extends AbstractDevice implements Window, SimulatedDevice {

    @ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String m_serialNumber;

    /**
     * The zone where the windows is located.
     */
    private Zone zone;

    public WindowImpl(){
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
    }

    /**
     * Get the window location.
     *
     * @return the zone location name where the window is.
     */
    @Override
    public String getLocation() {
        return String.valueOf(getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME));
    }

    /**
     * Retrieve the window serial number
     * @return the serial number.
     */
    @Override
    public String getSerialNumber() {
        return m_serialNumber;
    }

    /**
     * Callback called when the window is moved into a zone.
     * @param zones
     */
    @Override
    public void enterInZones(List<Zone> zones) {
        if (!zones.isEmpty()) {
            zone = zones.get(0);
            super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, zone.getId());
        }
    }

    /**
     * Callback called when the window is leaving a set of zones.
     * @param zones
     */
    @Override
    public void leavingZones(List<Zone> zones) {
        zone = null;
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
    }
}
