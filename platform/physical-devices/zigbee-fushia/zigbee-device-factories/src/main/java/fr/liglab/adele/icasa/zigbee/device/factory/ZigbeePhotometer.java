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
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.zigbee.dongle.api.Data;
import fr.liglab.adele.icasa.zigbee.dongle.api.DeviceInfo;
import fr.liglab.adele.icasa.zigbee.dongle.api.ZigbeeDeviceListener;
import fr.liglab.adele.icasa.zigbee.dongle.api.ZigbeeDriver;
import org.apache.felix.ipojo.annotations.*;

import java.text.DecimalFormat;

@Component(name = "zigbeePhotometer")
@Provides
/**
 * Zigbee light sensor factory.
 *
 */
public class ZigbeePhotometer extends AbstractDevice implements Photometer,ZigbeeDevice,ZigbeeDeviceListener {

	@Requires
	private ZigbeeDriver driver;

	@Property(mandatory = true, name = "zigbee.moduleAddress")
	private String moduleAddress;

	@ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String serialNumber;

	public ZigbeePhotometer() {
		super();
		super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME,
				GenericDevice.LOCATION_UNKNOWN);
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
	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public void deviceDataChanged(String moduleAddress, Data oldData,
			Data newData) {
		if (moduleAddress.compareTo(this.moduleAddress) == 0) {
			String data = newData.getData();
			Double computedIlluminance = computeIlluminance(data);
			if (computedIlluminance != null) {
				setPropertyValue(PHOTOMETER_CURRENT_ILLUMINANCE,
						computedIlluminance);
			}
		}
	}

	/**
	 * Compute Illuminance from the given data.
	 * 
	 * @param data
	 * @return
	 */
	public Double computeIlluminance(String data) {

		if (data.length() != 4) {
			return 0.0;
		}

		double c0, c1, lux;

		StringBuilder convertedData = new StringBuilder();

		for (byte b : data.getBytes()) {
			String hex = String.format("%04x", (int) b);
			char c = hex.charAt(hex.length() - 1);
			convertedData.append(c);
		}

		c0 = tri_val(Integer.valueOf(convertedData.substring(0, 2), 16)); // valeur
																			// reconstituee
																			// des
																			// deux
																			// premiers
																			// octets
		c1 = tri_val(Integer.valueOf(convertedData.substring(2, 4), 16)); // valeur
																			// reconstituee
																			// des
																			// deux
																			// derniers
																			// octets

		if ((c0 != -1) && (c1 != -1)) {
			lux = c0 * (0.46) * (Math.pow(2.71828, -3.13 * c1 / c0));
		} else {
			lux = 0.0;
		}
		
		// format lux to take integer part only
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(0);
		df.setGroupingUsed(false);
		
		return Double.valueOf(df.format(lux));
	}

	private double tri_val(int val) {

		double value, chord = 0, step = 0, step_number;
		char i;

		if (val < 128) {
			value = -1.0;
		} else {
			i = (char) ((val & 0x70) >> 4);
			switch (i) {
			case 0:
				chord = 0.0;
				step = 1.0;
				break;
			case 1:
				chord = 16.0;
				step = 2.0;
				break;
			case 2:
				chord = 49.0;
				step = 4.0;
				break;
			case 3:
				chord = 115.0;
				step = 8.0;
				break;
			case 4:
				chord = 247.0;
				step = 16.0;
				break;
			case 5:
				chord = 511.0;
				step = 32.0;
				break;
			case 6:
				chord = 1039.0;
				step = 64.0;
				break;
			case 7:
				chord = 2095.0;
				step = 128.0;
				break;
			}
			step_number = (double) (val & 0x0F);
			value = (double) (chord + (step * step_number));
		}
		return value;
	}

	@Override
	public void deviceBatteryLevelChanged(String moduleAddress,
			float oldBatteryLevel, float newBatteryLevel) {
		if (moduleAddress.compareToIgnoreCase(this.moduleAddress) == 0) { // this
																			// device.
			setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, newBatteryLevel);
		}
	}

	@Override
	public double getIlluminance() {
		Double illuminance = (Double) getPropertyValue(PHOTOMETER_CURRENT_ILLUMINANCE);
		if (illuminance == null)
			return 0;
		return illuminance;
	}
}
