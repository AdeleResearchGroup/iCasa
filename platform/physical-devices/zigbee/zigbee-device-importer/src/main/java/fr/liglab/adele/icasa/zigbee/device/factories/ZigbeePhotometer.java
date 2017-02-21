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
import fr.liglab.adele.icasa.device.light.Photometer;
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
import javax.measure.quantity.Illuminance;
import java.text.DecimalFormat;

@ContextEntity(services = {Photometer.class, ZigbeeDevice.class,ZigbeeDeviceTracker.class,BatteryObservable.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)

public class ZigbeePhotometer implements Photometer, ZigbeeDevice, ZigbeeDeviceTracker,GenericDevice,BatteryObservable {

    @ContextEntity.State.Field(service = Photometer.class,state = Photometer.PHOTOMETER_CURRENT_ILLUMINANCE)
    private Quantity<Illuminance> currentIlluminance;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @ContextEntity.State.Field(service = ZigbeeDevice.class,state = MODULE_ADRESS)
    private String moduleAddress;

    @ContextEntity.State.Field(service = BatteryObservable.class,state = BATTERY_LEVEL)
    private float batteryLevel;

    @Requires
    private ZigbeeDriver driver;

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public Quantity<Illuminance> getIlluminance() {
        return currentIlluminance;
    }


    @Override
    public void deviceAdded(DeviceInfo deviceInfo) {/* nothing to do */
    }

    @Override
    public void deviceRemoved(DeviceInfo deviceInfo) {/* nothing to do */
    }

    @Override
    public void deviceDataChanged(String moduleAddress, Data oldData,
                                  Data newData) {
        if (moduleAddress.compareTo(this.moduleAddress) == 0) {
            String data = newData.getData();
            Double computedIlluminance = computeIlluminance(data);
            pushIlluminance(computedIlluminance);
        }
    }

    @ContextEntity.State.Push(service = Photometer.class,state = PHOTOMETER_CURRENT_ILLUMINANCE)
    public Quantity<Illuminance> pushIlluminance(double computeIlluminance){
        return Quantities.getQuantity(computeIlluminance, Units.LUX);
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
            pushBatteryLevel( newBatteryLevel);
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
