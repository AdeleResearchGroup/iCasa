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
package fr.liglab.adele.icasa.remote.wisdom.util;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.clockservice.util.DateTextUtil;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.remote.wisdom.impl.ClockREST;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.units.ri.unit.Units;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class IcasaJSONUtil {

    private final static Logger LOG = LoggerFactory.getLogger(IcasaJSONUtil.class);

    private final static String NO_UNIT = "N/A";

    public static  JSONObject getPushButtonJSON(PushButton pushButton) throws JSONException{
        JSONObject deviceJSON = buildDeviceJsonObject(pushButton);
        Set<String> services = new HashSet<>();
        services.add(PushButton.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        JSONArray propObject = new JSONArray();
        propObject.put(buildDeviceProperty(PushButton.PUSH_AND_HOLD,pushButton.isPushed(),"Lux"));
        deviceJSON.putOnce(DeviceJSON.PROPERTIES_PROP,propObject);
        return deviceJSON;
    }

    public static JSONObject getPhotometerJSON(Photometer photometer)throws JSONException{

        JSONObject deviceJSON = buildDeviceJsonObject(photometer);
        Set<String> services = new HashSet<>();
        services.add(Photometer.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        JSONArray propObject = new JSONArray();
        if (photometer.getIlluminance() != null){
            propObject.put(buildDeviceProperty(Photometer.PHOTOMETER_CURRENT_ILLUMINANCE,photometer.getIlluminance().to(Units.LUX).getValue(),"Lux"));
        }else {
            propObject.put(buildDeviceProperty(Photometer.PHOTOMETER_CURRENT_ILLUMINANCE,"none","Lux"));
        }

        deviceJSON.putOnce(DeviceJSON.PROPERTIES_PROP,propObject);
        return deviceJSON;
    }

    public static JSONObject getHeaterJSON(Heater heater) throws JSONException {
        JSONObject deviceJSON = buildDeviceJsonObject(heater);
        Set<String> services = new HashSet<>();
        services.add(Heater.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        JSONArray propObject = new JSONArray();
        propObject.put(buildDeviceProperty("heater.powerLevel",heater.getPowerLevel(),NO_UNIT));
        deviceJSON.putOnce(DeviceJSON.PROPERTIES_PROP,propObject);
        return deviceJSON;

    }

    public static JSONObject getCoolerJSON(Cooler cooler) throws JSONException{
        JSONObject deviceJSON = buildDeviceJsonObject(cooler);
        Set<String> services = new HashSet<>();
        services.add(Cooler.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        JSONArray propObject = new JSONArray();
        propObject.put(buildDeviceProperty("cooler.powerLevel",cooler.getPowerLevel(),NO_UNIT));
        deviceJSON.putOnce(DeviceJSON.PROPERTIES_PROP,propObject);
        return deviceJSON;
    }

    public static JSONObject getPresenceSensorJSON(PresenceSensor presenceSensor)throws JSONException{
        JSONObject deviceJSON = buildDeviceJsonObject(presenceSensor);
        Set<String> services = new HashSet<>();
        services.add(PresenceSensor.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        JSONArray propObject = new JSONArray();
        propObject.put(buildDeviceProperty("presenceSensor.sensedPresence",presenceSensor.getSensedPresence(),NO_UNIT));
        deviceJSON.putOnce(DeviceJSON.PROPERTIES_PROP,propObject);
        return deviceJSON;
    }

    public static JSONObject getMotionSensorJSON(MotionSensor motion)throws JSONException{
        JSONObject deviceJSON = buildDeviceJsonObject(motion);
        Set<String> services = new HashSet<>();
        services.add(MotionSensor.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        return deviceJSON;
    }

    public static JSONObject getDimmerLightJSON(DimmerLight dimmerLight)throws JSONException{
        JSONObject deviceJSON = buildDeviceJsonObject(dimmerLight);
        Set<String> services = new HashSet<>();
        services.add(DimmerLight.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        JSONArray propObject = new JSONArray();
        propObject.put(buildDeviceProperty("dimmerLight.powerLevel",dimmerLight.getPowerLevel(),NO_UNIT));
        deviceJSON.putOnce(DeviceJSON.PROPERTIES_PROP,propObject);
        return deviceJSON;
    }

    public static JSONObject getBinaryLightJSON(BinaryLight binaryLight) throws JSONException{
        JSONObject deviceJSON = buildDeviceJsonObject(binaryLight);
        Set<String> services = new HashSet<>();
        services.add(BinaryLight.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        JSONArray propObject = new JSONArray();
        propObject.put(buildDeviceProperty("binaryLight.powerStatus",binaryLight.getPowerStatus(),NO_UNIT));
        deviceJSON.putOnce(DeviceJSON.PROPERTIES_PROP,propObject);
        return deviceJSON;
    }

    public static JSONObject getThermometerJSON(Thermometer thermometer)throws JSONException{
        JSONObject deviceJSON = buildDeviceJsonObject(thermometer);
        Set<String> services = new HashSet<>();
        services.add(Thermometer.class.getName());
        deviceJSON.putOnce(DeviceJSON.SERVICES,services);
        JSONArray propObject = new JSONArray();
        if (thermometer.getTemperature() != null){
            propObject.put(buildDeviceProperty(Thermometer.THERMOMETER_CURRENT_TEMPERATURE,thermometer.getTemperature().to(Units.KELVIN).getValue(),"Kelvin"));
        }else {
            propObject.put(buildDeviceProperty(Thermometer.THERMOMETER_CURRENT_TEMPERATURE,"none","Kelvin"));
        }

        deviceJSON.putOnce(DeviceJSON.PROPERTIES_PROP,propObject);
        return deviceJSON;
    }

    private static JSONObject buildDeviceJsonObject(GenericDevice device)throws JSONException{
        JSONObject deviceJSON = new JSONObject();;
        addSerialNumber(deviceJSON,device);
        addDevicePosition(deviceJSON,device);
        return deviceJSON;
    }

    private static void addDevicePosition(JSONObject jsonObject,GenericDevice device) throws JSONException{
        if (device instanceof LocatedObject){
            LocatedObject object = (LocatedObject) device;
            jsonObject.put(DeviceJSON.POSITION_X_PROP, object.getPosition().x);
            jsonObject.put(DeviceJSON.POSITION_Y_PROP, object.getPosition().y);
            jsonObject.put(DeviceJSON.LOCATION_PROP, NO_UNIT); //TODO change
        }
    }

    private static void addSerialNumber(JSONObject jsonObject,GenericDevice device)throws JSONException{
        jsonObject.putOnce(DeviceJSON.ID_PROP, device.getSerialNumber());
        jsonObject.putOnce(DeviceJSON.NAME_PROP, device.getSerialNumber());
    }

    private static JSONObject buildDeviceProperty(String propertyName, Object value,String unit)throws JSONException{
        JSONObject prop = new JSONObject();
        prop.put("name", propertyName);
        prop.put("value", getValidObject(value));
        prop.put("unit", unit);
        return prop;
    }

    public static JSONObject getPersonTypeJSON(String personTypeStr) {
        JSONObject personTypeJSON = null;
        try {
            personTypeJSON = new JSONObject();
            personTypeJSON.putOnce("id", personTypeStr);
            personTypeJSON.putOnce("name", personTypeStr);
        } catch (JSONException e) {
            e.printStackTrace();
            personTypeJSON = null;
        }

        return personTypeJSON;
    }

    public static JSONObject getZoneJSON(Zone zone) {
        JSONObject zoneJSON = null;
        try {
            String zoneId = zone.getZoneName();

            zoneJSON = new JSONObject();
            zoneJSON.putOnce(ZoneJSON.ID_PROP, zoneId);
            zoneJSON.putOnce(ZoneJSON.NAME_PROP, zoneId);
            zoneJSON.put(ZoneJSON.POSITION_LEFTX_PROP, zone.getLeftTopAbsolutePosition().x);
            zoneJSON.put(ZoneJSON.POSITION_TOPY_PROP, zone.getLeftTopAbsolutePosition().y);
            zoneJSON.put(ZoneJSON.POSITION_RIGHTX_PROP, zone.getRightBottomAbsolutePosition().x);
            zoneJSON.put(ZoneJSON.POSITION_BOTTOMY_PROP, zone.getRightBottomAbsolutePosition().y);
            zoneJSON.put(ZoneJSON.IS_ROOM_PROP, true); // TODO change it when Zone API will be improved

            JSONArray propObject = new JSONArray();
            JSONObject xlenghtPoperty = new JSONObject();
            xlenghtPoperty.put("name", Zone.X_LENGHT);
            xlenghtPoperty.put("value", getValidObject(zone.getXLength()));
            xlenghtPoperty.put("unit", "m");
            propObject.put(xlenghtPoperty);
            JSONObject ylenghtPoperty = new JSONObject();
            ylenghtPoperty.put("name", Zone.Y_LENGHT);
            ylenghtPoperty.put("value", getValidObject(zone.getYLength()));
            ylenghtPoperty.put("unit", "m");
            propObject.put(ylenghtPoperty);
            JSONObject zlenghtPoperty = new JSONObject();
            zlenghtPoperty.put("name", Zone.Z_LENGHT);
            zlenghtPoperty.put("value", getValidObject(zone.getZLength()));
            zlenghtPoperty.put("unit", "m");
            propObject.put(zlenghtPoperty);
            zoneJSON.put(ZoneJSON.VARIABLE_PROP, propObject);

        } catch (JSONException e) {
            e.printStackTrace();
            zoneJSON = null;
        }

        return zoneJSON;
    }

    private static Object getValidObject(Object variableValue) {
        if(variableValue instanceof Float && (Float.isInfinite((Float)variableValue) || Float.isNaN((Float)variableValue))){
            return String.valueOf(variableValue);
        } else if(variableValue instanceof Double && (Double.isInfinite((Double)variableValue) || Double.isNaN((Double)variableValue))){
            return String.valueOf(variableValue);
        }
        return variableValue;
    }

    public static JSONObject getClockJSON(Clock clock) {
        JSONObject clockJSON = null;
        try {
            clockJSON = new JSONObject();
            clockJSON.putOnce("id", ClockREST.DEFAULT_INSTANCE_NAME); //TODO should be changed to manage multiple clocks
            clockJSON.putOnce("startDateStr", DateTextUtil.getTextDate(clock.getStartDate()));
            clockJSON.putOnce("startDate", clock.getStartDate());
            clockJSON.putOnce("currentDateStr", DateTextUtil.getTextDate((clock.currentTimeMillis())));
            clockJSON.putOnce("currentTime", clock.currentTimeMillis());
            clockJSON.putOnce("factor", clock.getFactor());
            clockJSON.putOnce("pause", clock.isPaused());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return clockJSON;
    }

    public static String getContent(BufferedReader reader){
        StringBuffer content = new StringBuffer();
        String line = null;
        try {
            while((line = reader.readLine()) != null){
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return content.toString();
        }
        return content.toString();
    }





}
