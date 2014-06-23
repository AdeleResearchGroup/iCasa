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
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeDeviceListener;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeModuleDriver;
import org.apache.felix.ipojo.annotations.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 *
 */
@Component(name = "zigbeeThermometer")
@Provides
public class ZigbeeThermometer extends AbstractDevice implements Thermometer,ZigbeeDevice,ZigbeeDeviceListener {

    @Requires
    private ZigbeeModuleDriver driver;

    @Property(mandatory = true, name = "zigbee.moduleAddress")
    private String moduleAddress;

    @ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String serialNumber;

    public ZigbeeThermometer() {
        super();
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME,
                GenericDevice.LOCATION_UNKNOWN);
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
     * Called when a device data has changed.
     *
     * @param newData
     *            new device data
     */
    @Override
    public void deviceDataChanged(String newData) {
        String data = newData;
        String computedTemperature = computeTemperature(data);
        if (computedTemperature != null) {
            setPropertyValue(THERMOMETER_CURRENT_TEMPERATURE,
                    computedTemperature);
        }

    }

    /**
     * Compute temperature from the given hexadecimal value.
     *
     * @param data
     * @return
     */
    public String computeTemperature(String data) {

        if (data.length() != 4) {
            return null;
        }

        double computedTemp;

        StringBuilder convertedData = new StringBuilder();

        for (byte b : data.getBytes()) {
            String hex = String.format("%04x", (int) b);
            char c = hex.charAt(hex.length() - 1);
            convertedData.append(c);
        }
        convertedData.deleteCharAt(convertedData.length() - 1);

        String sign = convertedData.substring(0, 1);

        if (Integer.valueOf(sign, 16) > 7) { // negative value
            computedTemp = -(Integer.parseInt("1000", 16) - Integer.valueOf(
                    convertedData.toString(), 16)) * 0.0625;
        } else { // positive value
            computedTemp = Integer.valueOf(convertedData.toString(), 16) * 0.0625;
        }

        // format temperature to take integer part and 2 decimal values
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));

        return df.format(computedTemp);
    }

    /**
     * Called when a device battery level has changed.
     *
     * @param newBatteryLevel
     *            new device battery level
     */
    @Override
    public void deviceBatteryLevelChanged(float newBatteryLevel) {
        setPropertyValue(ZigbeeDevice.BATTERY_LEVEL, newBatteryLevel);
    }

    @Override
    public double getTemperature() {
        Double temperature = (Double) getPropertyValue(THERMOMETER_CURRENT_TEMPERATURE);
        if (temperature == null) {
            return 0;
        }
        return temperature;
    }
}
