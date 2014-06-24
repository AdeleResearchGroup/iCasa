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
/**
 *
 */
package fr.liglab.adele.icasa.zigbee.device.factory;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeDeviceListener;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeModuleDriver;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zigbee power switch factory.
 */
@Component(name="zigbeePowerSwitch")
@Provides
public class ZigbeePowerSwitch extends AbstractDevice implements PowerSwitch, ZigbeeDevice,ZigbeeDeviceListener {

    @Requires
    private ZigbeeModuleDriver driver;

    @Property(mandatory = true, name = "zigbee.moduleAddress")
    private String moduleAddress;



    private static final Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG_DEVICE + ".zigBee.powerSwitch");

    @ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String serialNumber;

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    public ZigbeePowerSwitch(){
        super();
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, GenericDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(POWER_SWITCH_CURRENT_STATUS, false);
        super.setPropertyValue(BATTERY_LEVEL, 0f);
    }

    @Validate
    public void start() {
        driver.addListener(this,moduleAddress);
    }

    @Invalidate
    public void stop() {
        driver.removeListener(this);
    }

    @Override
    public boolean getStatus() {
        Boolean powerStatus = (Boolean) getPropertyValue(PowerSwitch.POWER_SWITCH_CURRENT_STATUS);
        if (powerStatus == null){
            return false;
        }
        return powerStatus;
    }

    @Override
    public boolean switchOff() {
        logger.error("Power switch status modification is not allowed");
        return getStatus();
    }

    @Override
    public boolean switchOn() {
        logger.error("Power switch status modification is not allowed");
        return getStatus();
    }

    /**
     * Called when a device data has changed.
     *
     * @param newData       new device data
     */
    @Override
    public void deviceDataChanged(String newData) {
        boolean status = newData.equalsIgnoreCase("1")? true: false;
        setPropertyValue(PowerSwitch.POWER_SWITCH_CURRENT_STATUS, status);

    }

    /**
     * Called when a device battery level has changed.
     *
     * @param newBatteryLevel new device battery level
     */
    @Override
    public void deviceBatteryLevelChanged(float newBatteryLevel) {
        setPropertyValue(BATTERY_LEVEL, newBatteryLevel);
    }
}