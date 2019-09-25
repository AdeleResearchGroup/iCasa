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
package fr.liglab.adele.icasa.remote.context.serialization;



import fr.liglab.adele.icasa.device.temperature.ThermometerExt;

import tec.units.ri.unit.Units;

import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;

import org.json.JSONException;

public class SerializedServices {


    public static void serialize(SerializedDevice result, PushButton pushButton) throws JSONException {
    	result.addProperty(PushButton.PUSH_AND_HOLD, pushButton.isPushed());
    }

    public static void serialize(SerializedDevice result, Photometer photometer)throws JSONException {
    	result.addProperty(Photometer.PHOTOMETER_CURRENT_ILLUMINANCE, photometer.getIlluminance());
    }

    public static void serialize(SerializedDevice result, Heater heater) throws JSONException {
    	result.addProperty(Heater.HEATER_POWER_LEVEL, heater.getPowerLevel(), Units.PERCENT);
    }

    public static void serialize(SerializedDevice result, Cooler cooler) throws JSONException {
    	result.addProperty(Cooler.COOLER_POWER_LEVEL, cooler.getPowerLevel(), Units.PERCENT);
    }

    public static void serialize(SerializedDevice result, PresenceSensor presenceSensor)throws JSONException{
    	result.addProperty("presenceSensor."+PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE, presenceSensor.getSensedPresence());
    }

    public static void serialize(SerializedDevice result, PowerSwitch power) throws JSONException{
    	result.addProperty("powerSwitch."+PowerSwitch.CURRENT_STATUS, power.getStatus());
    }

    public static void serialize(SerializedDevice result, MotionSensor motion) throws JSONException {
    }

    public static void serialize(SerializedDevice result, DimmerLight dimmerLight) throws JSONException{
    	result.addProperty(DimmerLight.DIMMER_LIGHT_POWER_LEVEL, dimmerLight.getPowerLevel(), Units.PERCENT);
    }

    public static void serialize(SerializedDevice result, WindowShutter windowShutter) throws JSONException{
    	result.addProperty(WindowShutter.SHUTTER_LEVEL,windowShutter.getShutterLevel(), Units.PERCENT);
    }

    public static void serialize(SerializedDevice result, BinaryLight binaryLight) throws JSONException{
    	result.addProperty(BinaryLight.BINARY_LIGHT_POWER_STATUS,binaryLight.getPowerStatus());
    }

    public static void serialize(SerializedDevice result, Thermometer thermometer) throws JSONException{
    	result.addProperty(Thermometer.THERMOMETER_CURRENT_TEMPERATURE,thermometer.getTemperature());
    }

    public static void serialize(SerializedDevice result, ThermometerExt thermometer) throws JSONException{
    	result.addProperty(ThermometerExt.THERMOMETER_CURRENT_TEMPERATURE,thermometer.getTemperature());
    }

}
