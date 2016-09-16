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
package fr.liglab.adele.icasa.zigbee.device.factories;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.zigbee.driver.Data;
import fr.liglab.adele.icasa.device.zigbee.driver.DeviceInfo;
import fr.liglab.adele.icasa.device.zigbee.driver.ZigbeeDeviceTracker;
import fr.liglab.adele.icasa.device.zigbee.driver.ZigbeeDriver;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.zigbee.device.api.ZigbeeDevice;
import org.apache.felix.ipojo.annotations.Requires;

@ContextEntity(services = {PresenceSensor.class, ZigbeeDevice.class,ZigbeeDeviceTracker.class})

@Behavior(id="LocatedBehavior",spec = LocatedObject.class,implem = LocatedObjectBehaviorProvider.class)
public class ZigbeePresenceSensor  implements PresenceSensor, ZigbeeDevice, ZigbeeDeviceTracker,GenericDevice {

    @Requires
    private ZigbeeDriver driver;

    @ContextEntity.State.Field(service = PresenceSensor.class,state = PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE,value = "false")
    private boolean sensedPresence;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @ContextEntity.State.Field(service = ZigbeeDevice.class,state = MODULE_ADRESS)
    private String moduleAddress;

    @ContextEntity.State.Field(service = ZigbeeDevice.class,state = BATTERY_LEVEL)
    private float batteryLevel;

    @Override
    public boolean getSensedPresence() {
        return sensedPresence;
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
    @Override
    public void deviceAdded(DeviceInfo deviceInfo)  {/*nothing to do*/}

    /**
     * Called when a device has been discovered by the driver.
     *
     * @param deviceInfo information about the device
     */
    @Override
    public void deviceRemoved(DeviceInfo deviceInfo) {/*nothing to do*/}

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
            boolean status;
            if( data.compareTo("1") == 0){
                status = true;
            }else{
                status = false;
            }
            pushSensedStatus(status);
        }
    }

    @ContextEntity.State.Push(service = PresenceSensor.class,state = PRESENCE_SENSOR_SENSED_PRESENCE)
    public boolean pushSensedStatus(boolean status){
        return status;
    }
    /**
     * Called when a device battery level has changed.
     *
     * @param address   a device module address
     * @param oldBatteryLevel previous device battery level
     * @param newBatteryLevel new device battery level
     */
    @Override
    public void deviceBatteryLevelChanged(String address, float oldBatteryLevel, float newBatteryLevel) {
        if(address.compareToIgnoreCase(this.moduleAddress) == 0){ //this device.
            pushBatteryLevel(newBatteryLevel);
        }
    }

    @ContextEntity.State.Push(service = ZigbeeDevice.class,state = BATTERY_LEVEL)
    public float pushBatteryLevel(float battery){
        return battery;
    }
}
