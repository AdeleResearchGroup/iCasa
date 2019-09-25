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


import javax.measure.Quantity;
import javax.measure.Unit;

import org.json.JSONException;
import org.json.JSONObject;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;


public class SerializedDevice extends SerializedItem {


	private static final String DEVICE_ID_PROP 	= "deviceId";
	private static final String NAME_PROP 		= "name";
	
	private static final String TYPE_PROP 		= "type";

	private static final String LOCATION_PROP 	= "location";
	private static final String POSITION_X_PROP = "positionX";
	private static final String POSITION_Y_PROP = "positionY";
	
	private static final String SERVICES 		= "services";
	private static final String PROPERTIES_PROP = "properties";


	public SerializedDevice(String  serialization) throws JSONException {
		super(serialization);
	}

	private SerializedDevice() {
		super();
	}

	public String getId() {
		return serialization.optString(DEVICE_ID_PROP, super.getId());
	}

	public String getType() {
		return serialization.optString(TYPE_PROP, null);
	}

	public SerializedDevice(GenericDevice device) throws JSONException {
		this();

		setId(device.getSerialNumber());
		setAttribute(NAME_PROP, device.getSerialNumber());

		service(LocatedObject.class, device,SerializedDevice::serialize);
		
		service(PushButton.class, device,SerializedServices::serialize);
		service(Photometer.class, device,SerializedServices::serialize);
		service(Heater.class, device,SerializedServices::serialize);
		service(Cooler.class, device,SerializedServices::serialize);
		service(PresenceSensor.class, device,SerializedServices::serialize);
		service(PowerSwitch.class, device,SerializedServices::serialize);
		service(MotionSensor.class, device,SerializedServices::serialize);
		service(DimmerLight.class, device,SerializedServices::serialize);
		service(WindowShutter.class, device,SerializedServices::serialize);
		service(BinaryLight.class, device,SerializedServices::serialize);
		service(Thermometer.class, device,SerializedServices::serialize);
		service(ThermometerExt.class, device,SerializedServices::serialize);
		
	}

    private static void serialize(SerializedDevice result, LocatedObject located) throws JSONException {
    	result.serialization.put(POSITION_X_PROP, located.getPosition().x);
    	result.serialization.put(POSITION_Y_PROP, located.getPosition().y);
    	result.serialization.put(LOCATION_PROP, NOT_AVAILABLE); //TODO change
    }

    public void service(Class<?> service) {
    	try {
			serialization.append(SerializedDevice.SERVICES,service.getName());
		} catch (JSONException unexpected) {
		}
    }

	@FunctionalInterface
	public interface Serializer<S> {
		
		public void serialize(SerializedDevice result, S service) throws JSONException ;

	}


    public <S> void service(Class<S> service, GenericDevice device, Serializer<S> serializer) {
    	if (service.isInstance(device)) {
    		service(service,service.cast(device),serializer);
    	}
    }

    private <S> void service(Class<S> service, S instance, Serializer<S> serializer) {
    	try {
			serialization.append(SerializedDevice.SERVICES,service.getName());
			serializer.serialize(this,instance);
		} catch (JSONException unexpected) {
		}
    }

    public void  addProperty(String name, Object value) throws JSONException {
    	addProperty(name,value,null);
    }

    public <Q extends Quantity<Q>> void  addProperty(String name, Quantity<Q> measure) throws JSONException {
    	addProperty(name, measure != null ? measure.getValue() : "none", measure != null ? measure.getUnit() : null);
    }

    public <Q extends Quantity<Q>> void  addProperty(String name, Object value, Unit<Q> unit) throws JSONException {
        addProperty(SerializedDevice.PROPERTIES_PROP,name,value,unit);
    }


	public void updateDevice(GenericDevice device) {

		if (device instanceof LocatedObject) {
			SerializedDevice.update(this,(LocatedObject) device);
		}
	}

    private static void update(SerializedDevice from, LocatedObject located) {
    	
    	JSONObject serialization = from.serialization;

    	if (serialization.has(POSITION_X_PROP) && serialization.has(POSITION_Y_PROP)) {
            Position newPostion = new Position(serialization.optInt(POSITION_X_PROP),serialization.optInt(POSITION_Y_PROP));
            located.setPosition(newPostion);
    	}
    }


}
