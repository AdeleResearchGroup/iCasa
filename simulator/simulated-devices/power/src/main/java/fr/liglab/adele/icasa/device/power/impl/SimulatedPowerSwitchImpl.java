package fr.liglab.adele.icasa.device.power.impl;

import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import fr.liglab.adele.icasa.simulator.Zone;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Constants;

import java.util.List;

/**
 * @author Thomas Leveque
 */
@Component(name = "iCASA.ToggleSwitch")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedPowerSwitchImpl extends AbstractDevice implements PowerSwitch, SimulatedDevice {

    @ServiceProperty(name = PresenceSensor.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String m_serialNumber;

    @Requires
    private SimulationManager manager;

    /**
     * Influence zone corresponding to the zone with highest level where the
     * device is located
     */
    private volatile Zone m_zone;

    public SimulatedPowerSwitchImpl() {
        setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        setPropertyValue("status", false);
    }

    @Override
    public boolean getStatus() {
        return (Boolean) getPropertyValue("status");
    }

    @Override
    public boolean switchOn() {
        setPropertyValue("status", true);
        return getStatus();
    }

    @Override
    public boolean switchOff() {
        setPropertyValue("status", false);
        return getStatus();
    }

    @Override
    public void enterInZones(List<Zone> zones) {
        if (!zones.isEmpty()) {
            m_zone = zones.get(0);
            setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, m_zone.getId());
        }
    }

    @Override
    public void leavingZones(List<Zone> zones) {
        m_zone = null;
        setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
    }

    @Override
    public String getSerialNumber() {
        return m_serialNumber;
    }
}
