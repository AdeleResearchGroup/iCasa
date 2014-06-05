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

import fr.liglab.adele.icasa.zigbee.dongle.api.Data;
import fr.liglab.adele.icasa.zigbee.dongle.api.ZigbeeDeviceListener;
import fr.liglab.adele.icasa.zigbee.dongle.api.ZigbeeDriver;
import org.apache.felix.ipojo.annotations.*;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.util.AbstractDevice;

@Component(name="zigbeePresenceSensor")
@Provides
/**
 * Zigbee presence sensor factory.
 *
 */
public class ZigbeePresenceSensor extends AbstractDevice implements PresenceSensor, ZigbeeDevice,ZigbeeDeviceListener {
	
	@Requires
	private ZigbeeDriver driver;
	
	@Property(mandatory=true, name="zigbee.moduleAddress")
	private String moduleAddress;
	
	@ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String serialNumber;
	
	public ZigbeePresenceSensor(){
		super();
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, GenericDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE, false);
        super.setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, 0f);
	}

    @Validate
    public void start() {
        driver.addListener(this);
    }

    @Invalidate
    public void stop() {
        driver.removeListener(this);
    }

	@Override
	public boolean getSensedPresence() {
		Boolean presence = (Boolean) getPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE);
		if (presence != null)
			return presence;
		return false;
	}

	@Override
	public String getSerialNumber() {
		return serialNumber;
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
            setPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE, status);
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
    public void deviceBatteryLevelChanged(String address, float oldBatteryLevel, float newBatteryLevel) {
        if(address.compareToIgnoreCase(this.moduleAddress) == 0){ //this device.
            setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, newBatteryLevel);
        }
    }
}
