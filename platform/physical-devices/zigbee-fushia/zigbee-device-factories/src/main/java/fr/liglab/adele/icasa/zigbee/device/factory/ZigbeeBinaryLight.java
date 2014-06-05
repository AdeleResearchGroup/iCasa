package fr.liglab.adele.icasa.zigbee.device.factory;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.zigbee.dongle.api.Data;
import fr.liglab.adele.icasa.zigbee.dongle.api.ZigbeeDeviceListener;
import fr.liglab.adele.icasa.zigbee.dongle.api.ZigbeeDriver;
import org.apache.felix.ipojo.annotations.*;


@Component(name = "zigbeeBinaryLight")
@Provides(specifications={GenericDevice.class, BinaryLight.class})
public class ZigbeeBinaryLight extends AbstractDevice implements BinaryLight,ZigbeeDevice,ZigbeeDeviceListener {

    @ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String serialNumber;

    @Property(mandatory = true, name = "zigbee.moduleAddress")
    private String moduleAddress;

    @Requires
    private ZigbeeDriver driver;

    public ZigbeeBinaryLight() {
        super();
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, GenericDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(BinaryLight.BINARY_LIGHT_POWER_STATUS, false);
        super.setPropertyValue(BinaryLight.BINARY_LIGHT_MAX_POWER_LEVEL, 1.0d); // TODO demander a jean paul
        super.setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, 0f);
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public boolean getPowerStatus() {
        Boolean powerStatus = (Boolean) getPropertyValue(BinaryLight.BINARY_LIGHT_POWER_STATUS);
        if (powerStatus == null)
            return false;

        return powerStatus;
    }

    @Override
    public boolean setPowerStatus(boolean status) {
        setPowerStatusToDevice(status);
        return status;
    }

    @Override
    public double getMaxPowerLevel() {
        Double maxLevel = (Double) getPropertyValue(BinaryLight.BINARY_LIGHT_MAX_POWER_LEVEL);
        if (maxLevel == null)
            return 0;

        return maxLevel;
    }

    @Override
    public void turnOn() {
        setPowerStatus(true);
    }

    @Override
    public void turnOff() {
        setPowerStatus(false);
    }

    private boolean getPowerStatusFromDevice() {
        boolean value = false;

        Data devData = driver.getData(moduleAddress);
        if (devData == null)
            return value;
        String strValue = devData.getData();
        Integer intValue = null;
        try {
            intValue = Integer.valueOf(strValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return value;
        }

        return (intValue != null) && (intValue == 1);
    }

    private boolean setPowerStatusToDevice(boolean powerStatus) {
        if (powerStatus) {
            driver.setData(moduleAddress, "1");
        } else {
            driver.setData(moduleAddress, "0");
        }
        return getPowerStatus();
    }

    private boolean setPowerStatusToSimulatedDevice(boolean powerStatus) {
        super.setPropertyValue(BINARY_LIGHT_POWER_STATUS, powerStatus);
        return getPowerStatus();
    }

    @Validate
    public void start() {
        driver.addListener(this);
        boolean initialValue = getPowerStatusFromDevice();
        setPowerStatusToSimulatedDevice(initialValue); //TODO manage in a better way the initial value
    }

    @Invalidate
    public void stop() {
        driver.removeListener(this);
    }

    /**
     * If the given propertyName is the power status, it will set it to the device;
     * the value will not change, if is not changed in the device. So a @see{deviceDataChanged} will be
     * charged to change this value in the iCasa Platform.
     * @param propertyName
     * @param value
     */
    @Override
    public void setPropertyValue(String propertyName, Object value){
        if (BinaryLight.BINARY_LIGHT_POWER_STATUS.equals(propertyName)) {
            Boolean newPowerStatus = (Boolean) value;
            if (newPowerStatus != null){
                setPowerStatusToDevice(newPowerStatus);
            }
        } else{
            super.setPropertyValue(propertyName, value);
        }
    }

    /**
     * Called when a device data has changed.
     *
     * @param address a device module address
     * @param oldData       previous device data
     * @param newData       new device data
     */
    @Override
    public void deviceDataChanged(String address, Data oldData, Data newData) {
        if(address.compareTo(this.moduleAddress) == 0){
            String data = newData.getData();
            boolean status = data.compareTo("1")==0? true : false;
            setPowerStatusToSimulatedDevice(status);
        }
    }

    /**
     * Called when a device battery level has changed.
     *
     * @param address   a device module address
     * @param oldBatteryLevel previous device battery level
     * @param newBatteryLevel new device battery level
     */
    @Override
    public void deviceBatteryLevelChanged(String address,
                                          float oldBatteryLevel,
                                          float newBatteryLevel) {
        if(address.compareToIgnoreCase(this.moduleAddress) == 0){ //this device.
            setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, newBatteryLevel);
        }
    }

}