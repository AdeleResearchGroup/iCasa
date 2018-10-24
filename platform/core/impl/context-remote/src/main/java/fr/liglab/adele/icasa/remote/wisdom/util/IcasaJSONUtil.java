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


import java.io.BufferedReader;
import java.io.IOException;

import fr.liglab.adele.icasa.device.temperature.ThermometerExt;
import org.wisdom.api.http.Context;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedObject;

import fr.liglab.adele.icasa.location.Zone;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.clockservice.util.DateTextUtil;

import javax.measure.Quantity;
import javax.measure.Unit;

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

import fr.liglab.adele.icasa.remote.wisdom.impl.ClockREST;

import org.json.JSONException;
import org.json.JSONObject;



public class IcasaJSONUtil {

    private final static String NO_UNIT = "N/A";

    public static String content(Context context) throws IOException {
        
    	StringBuilder content = new StringBuilder();
        
        try {
        	
        	BufferedReader reader = context.reader();
        	String line = null;
        	
            while ( (line = reader.readLine()) != null) {
                content.append(line);
            }
            
        } catch (IOException e) {
        }
        
        return content.toString();
    }
    

    public static JSONObject serialize(GenericDevice device) throws JSONException{

    	JSONObject result = new JSONObject();;
    	
    	result.putOnce(DeviceJSON.ID_PROP, device.getSerialNumber());
    	result.putOnce(DeviceJSON.NAME_PROP, device.getSerialNumber());
        
    	serializer(LocatedObject.class,IcasaJSONUtil::serialize).serialize(result,device);

    	serializer(PushButton.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(Photometer.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(Heater.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(Cooler.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(PresenceSensor.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(PowerSwitch.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(MotionSensor.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(DimmerLight.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(WindowShutter.class,IcasaJSONUtil::serialize).serialize(result,device);
    	serializer(BinaryLight.class,IcasaJSONUtil::serialize).serialize(result,device);
        serializer(Thermometer.class,IcasaJSONUtil::serialize).serialize(result,device);
        serializer(ThermometerExt.class,IcasaJSONUtil::serialize).serialize(result,device);
    	
        return result;
    }

    @FunctionalInterface
    public interface Serializer<S> {
    	
    	public void serialize(JSONObject result, S device) throws JSONException;

    }
        
    public static <T> Serializer<GenericDevice> serializer(Class<T> service, Serializer<T> builder) {
    	return (JSONObject result, GenericDevice device) -> {
        	if (service.isInstance(device)) {
        		service(result,service);
        		builder.serialize(result,service.cast(device));
        	}
    	};
    }

    private static void service(JSONObject result, Class<?> service) throws JSONException {
        result.append(DeviceJSON.SERVICES,service.getName());
    }

    private static void  property(JSONObject result, String name, Object value) throws JSONException {
    	property(result,name,value,null);
    }

    private static <Q extends Quantity<Q>> void  property(JSONObject result, String name, Quantity<Q> measure) throws JSONException {
    	property(result,name, measure != null ? measure.getValue() : "none", measure != null ? measure.getUnit() : null);
    }

    private static <Q extends Quantity<Q>> void  property(JSONObject result, String name, Object value, Unit<Q> unit) throws JSONException {
        property(result,DeviceJSON.PROPERTIES_PROP,name,value,unit);
    }

    private static <Q extends Quantity<Q>> void  property(JSONObject result, String container, String name, Object value, Unit<Q> unit) throws JSONException {
        
    	JSONObject property = new JSONObject();
        
    	property.put("name", name);
    	property.put("value", representationOf(value));
    	property.put("unit", unit != null ? unit.getSymbol() : NO_UNIT);
        
        result.append(container,property);
    }

    private static Object representationOf(Object value) {
        
    	if (value instanceof Float && (Float.isInfinite((Float)value) || Float.isNaN((Float)value))){
            return String.valueOf(value);
        }
        
        if (value instanceof Double && (Double.isInfinite((Double)value) || Double.isNaN((Double)value))){
            return String.valueOf(value);
        }
        
        return value;
    }

    private static void serialize(JSONObject result, LocatedObject device) throws JSONException {
        LocatedObject object = (LocatedObject) device;
        result.put(DeviceJSON.POSITION_X_PROP, object.getPosition().x);
        result.put(DeviceJSON.POSITION_Y_PROP, object.getPosition().y);
        result.put(DeviceJSON.LOCATION_PROP, NO_UNIT); //TODO change
    }

    public static void serialize(JSONObject result, PushButton pushButton) throws JSONException {
    	property(result,PushButton.PUSH_AND_HOLD, pushButton.isPushed());
    }

    public static void serialize(JSONObject result, Photometer photometer)throws JSONException {
        property(result, Photometer.PHOTOMETER_CURRENT_ILLUMINANCE, photometer.getIlluminance());
    }

    public static void serialize(JSONObject result, Heater heater) throws JSONException {
        property(result,Heater.HEATER_POWER_LEVEL, heater.getPowerLevel(), Units.PERCENT);
    }

    public static void serialize(JSONObject result, Cooler cooler) throws JSONException {
        property(result,Cooler.COOLER_POWER_LEVEL, cooler.getPowerLevel(), Units.PERCENT);
    }

    public static void serialize(JSONObject result, PresenceSensor presenceSensor)throws JSONException{
        property(result,"presenceSensor."+PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE, presenceSensor.getSensedPresence());
    }

    public static void serialize(JSONObject result, PowerSwitch power) throws JSONException{
        property(result,"powerSwitch."+PowerSwitch.CURRENT_STATUS, power.getStatus());
    }

    public static void serialize(JSONObject result, MotionSensor motion) throws JSONException {
    }

    public static void serialize(JSONObject result, DimmerLight dimmerLight) throws JSONException{
        property(result,DimmerLight.DIMMER_LIGHT_POWER_LEVEL, dimmerLight.getPowerLevel(), Units.PERCENT);
    }

    public static void serialize(JSONObject result, WindowShutter windowShutter) throws JSONException{
        property(result,WindowShutter.SHUTTER_LEVEL,windowShutter.getShutterLevel(), Units.PERCENT);
    }

    public static void serialize(JSONObject result, BinaryLight binaryLight) throws JSONException{
        property(result,BinaryLight.BINARY_LIGHT_POWER_STATUS,binaryLight.getPowerStatus());
    }

    public static void serialize(JSONObject result, Thermometer thermometer) throws JSONException{
        property(result,Thermometer.THERMOMETER_CURRENT_TEMPERATURE,thermometer.getTemperature());
    }

    public static void serialize(JSONObject result, ThermometerExt thermometer) throws JSONException{
        property(result,ThermometerExt.THERMOMETER_CURRENT_TEMPERATURE,thermometer.getTemperature());
    }

    public static JSONObject serialize(String person) throws JSONException {
       	JSONObject result = new JSONObject();
       	
       	result.putOnce("id", person);
       	result.putOnce("name", person);

		return result;
    }

    public static JSONObject serialize(Zone zone) throws JSONException {

        JSONObject result = new JSONObject();

        String zoneId = zone.getZoneName();
        result.putOnce(ZoneJSON.ID_PROP, zoneId);
        result.putOnce(ZoneJSON.NAME_PROP, zoneId);
        
        result.put(ZoneJSON.POSITION_LEFTX_PROP, zone.getLeftTopAbsolutePosition().x);
        result.put(ZoneJSON.POSITION_TOPY_PROP, zone.getLeftTopAbsolutePosition().y);
        result.put(ZoneJSON.POSITION_RIGHTX_PROP, zone.getRightBottomAbsolutePosition().x);
        result.put(ZoneJSON.POSITION_BOTTOMY_PROP, zone.getRightBottomAbsolutePosition().y);
        result.put(ZoneJSON.IS_ROOM_PROP, true); // TODO change it when Zone API will be improved

        property(result,ZoneJSON.VARIABLE_PROP,Zone.X_LENGHT,zone.getXLength(),Units.METRE);
        property(result,ZoneJSON.VARIABLE_PROP,Zone.Y_LENGHT,zone.getYLength(),Units.METRE);
        property(result,ZoneJSON.VARIABLE_PROP,Zone.Z_LENGHT,zone.getZLength(),Units.METRE);

        return result;
    }

    public static JSONObject serialize(Clock clock) throws JSONException {
    	
    	JSONObject result = new JSONObject();
    	
    	result.putOnce("id", ClockREST.DEFAULT_INSTANCE_NAME); //TODO should be changed to manage multiple clocks
    	result.putOnce("startDateStr", DateTextUtil.getTextDate(clock.getStartDate()));
    	result.putOnce("startDate", clock.getStartDate());
    	result.putOnce("currentDateStr", DateTextUtil.getTextDate((clock.currentTimeMillis())));
    	result.putOnce("currentTime", clock.currentTimeMillis());
    	result.putOnce("factor", clock.getFactor());
    	result.putOnce("pause", clock.isPaused());
    	
    	return result;
    }


}
