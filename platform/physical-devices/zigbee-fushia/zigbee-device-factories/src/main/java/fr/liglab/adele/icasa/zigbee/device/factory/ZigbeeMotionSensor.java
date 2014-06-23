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

import fr.liglab.adele.icasa.device.DeviceDataEvent;
import fr.liglab.adele.icasa.device.DeviceEventType;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.Data;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeDeviceListener;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeModuleDriver;
import org.apache.felix.ipojo.annotations.*;


/**
 *
 */
@Component(name="zigbeeMotionSensor")
@Provides
public class ZigbeeMotionSensor extends AbstractDevice implements MotionSensor, ZigbeeDevice,ZigbeeDeviceListener {

    @Requires
    private ZigbeeModuleDriver driver;

    @Property(mandatory=true, name="zigbee.moduleAddress")
    private String moduleAddress;

    @ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String serialNumber;

    public ZigbeeMotionSensor(){
        super();
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, GenericDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, 0f);
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
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Called when a new device has been discovered by the driver.
     *
     * @param deviceInfo information about the device
     */

    /**
     * Called when a device data has changed.
     *
     * @param newData       new device data
     */
    @Override
    public void deviceDataChanged( String newData) {
        String data = newData;
        if (Integer.parseInt(data) == 1){
            this.notifyListeners(new DeviceDataEvent<Boolean>(this, DeviceEventType.DEVICE_EVENT, Boolean.TRUE));
        }
    }

    /**
     * Called when a device battery level has changed.
     *
     * @param newBatteryLevel new device battery level
     */
    @Override
    public void deviceBatteryLevelChanged( float newBatteryLevel) {
        setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, newBatteryLevel);
    }
}
