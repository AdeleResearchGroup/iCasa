/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.zigbee.device.factory;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.Data;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeDeviceListener;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeModuleDriver;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(name = "zigbeeBinaryLight")
@Provides(specifications={GenericDevice.class, BinaryLight.class})
public class ZigbeeBinaryLight extends AbstractDevice implements BinaryLight,ZigbeeDevice,ZigbeeDeviceListener {

    private static final Logger LOG = LoggerFactory.getLogger(ZigbeeBinaryLight.class);

    @ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String serialNumber;

    @Property(mandatory = true, name = "zigbee.moduleAddress")
    private String moduleAddress;

    @Requires
    private ZigbeeModuleDriver driver;

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
            LOG.error("exception in get power status",e);
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
        driver.addListener(this,moduleAddress);
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
     * @param newData       new device data
     */
    @Override
    public void deviceDataChanged(String newData) {
            String data = newData;
            boolean status = data.compareTo("1")==0? true : false;
            setPowerStatusToSimulatedDevice(status);
    }

    /**
     * Called when a device battery level has changed.
     *
     * @param newBatteryLevel new device battery level
     */
    @Override
    public void deviceBatteryLevelChanged(float newBatteryLevel) {
            setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, newBatteryLevel);
    }

}