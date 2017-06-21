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

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.battery.BatteryObservable;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.zigbee.driver.Data;
import fr.liglab.adele.icasa.device.zigbee.driver.DeviceInfo;
import fr.liglab.adele.icasa.device.zigbee.driver.ZigbeeDeviceTracker;
import fr.liglab.adele.icasa.device.zigbee.driver.ZigbeeDriver;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.zigbee.device.api.ZigbeeDevice;
import org.apache.felix.ipojo.annotations.Requires;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@ContextEntity(coreServices = {Thermometer.class, ZigbeeDevice.class,ZigbeeDeviceTracker.class,BatteryObservable.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
public class ZigbeeThermometer implements Thermometer, ZigbeeDevice, ZigbeeDeviceTracker,GenericDevice,BatteryObservable {

    @Requires
    private ZigbeeDriver driver;

    @ContextEntity.State.Field(service = Thermometer.class,state = THERMOMETER_CURRENT_TEMPERATURE)
    private Quantity<Temperature> currentTemperature;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @ContextEntity.State.Field(service = ZigbeeDevice.class,state = ZigbeeDevice.MODULE_ADRESS)
    private String moduleAddress;

    @ContextEntity.State.Field(service = BatteryObservable.class,state = BatteryObservable.BATTERY_LEVEL)
    private float batteryLevel;


    @Override
    public Quantity<Temperature> getTemperature() {
        return currentTemperature;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Called when a new device has been discovered by the driver.
     *
     * @param deviceInfo
     *            information about the device
     */
    @Override
    public void deviceAdded(DeviceInfo deviceInfo) {/* nothing to do */
    }

    /**
     * Called when a device has been removed by the driver.
     *
     * @param deviceInfo
     *            information about the device
     */
    @Override
    public void deviceRemoved(DeviceInfo deviceInfo) {/* nothing to do */
    }

    /**
     * Called when a device data has changed.
     *
     * @param moduleAddress
     *            a device module address
     * @param oldData
     *            previous device data
     * @param newData
     *            new device data
     */
    @Override
    public void deviceDataChanged(String moduleAddress, Data oldData,
                                  Data newData) {
        if (moduleAddress.compareTo(this.moduleAddress) == 0) {
            String data = newData.getData();
            String computedTemperature = computeTemperature(data);
            pushTemperature(Double.valueOf(computedTemperature));
        }
    }

    @ContextEntity.State.Push(service = Thermometer.class,state = THERMOMETER_CURRENT_TEMPERATURE)
    public Quantity<Temperature> pushTemperature(double temperature){
        return Quantities.getQuantity(temperature, Units.KELVIN);
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
     * @param moduleAddress
     *            a device module address
     * @param oldBatteryLevel
     *            previous device battery level
     * @param newBatteryLevel
     *            new device battery level
     */
    @Override
    public void deviceBatteryLevelChanged(String moduleAddress,
                                          float oldBatteryLevel, float newBatteryLevel) {
        if (moduleAddress.compareToIgnoreCase(this.moduleAddress) == 0) { // this
            // device.
            pushBatteryLevel(newBatteryLevel);
        }
    }

    @ContextEntity.State.Push(service = BatteryObservable.class,state = BATTERY_LEVEL)
    public float pushBatteryLevel(float battery){
        return battery;
    }

    @Override
    public double getBatteryPercentage() {
        return batteryLevel;
    }
}
