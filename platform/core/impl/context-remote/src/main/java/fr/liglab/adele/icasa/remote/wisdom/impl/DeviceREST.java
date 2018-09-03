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
/**
 *
 */
package fr.liglab.adele.icasa.remote.wisdom.impl;


import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.power.ControllablePowerSwitch;
import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Position;

import fr.liglab.adele.icasa.remote.wisdom.util.DeviceJSON;
import static fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil.*;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;
import org.json.JSONArray;
import org.json.JSONException;


import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
@Component(immediate = true)
@Provides
@Instantiate
@Path("/icasa/devices")
public class DeviceREST extends DefaultController {


    private List<GenericDevice> devices = new ArrayList<>();

    @Bind(id="located", optional=true, proxy=false, aggregate=true)
    public void bound(LocatedObject object) {
    	if (object instanceof GenericDevice) {
    		devices.add((GenericDevice)object);
    	}
    }

    @Unbind(id="located")
    public void unbound(LocatedObject object) {
    	if (object instanceof GenericDevice) {
    		devices.remove((GenericDevice)object);
    	}
    }

    private synchronized GenericDevice device(String serialNumber) {
		
    	for (GenericDevice device : devices) {
			if (serialNumber.equals(device.getSerialNumber()))
				return device;
		}

		return null;
    }

    /**
     * Returns a JSON array containing all devices.
     *
     */
    public String devices() throws JSONException {
    	
        JSONArray result = new JSONArray();
        
        for (GenericDevice device : devices) {
			result.put(serialize(device));
        }

        return result.toString();
    }

    @Route(method = HttpMethod.GET, uri = "/devices")
    public Result getDevices() {
        try {
            return ok(devices()).as(MimeTypes.JSON);
        }catch (JSONException e){
            return internalServerError(e);
        }
    }

    /**
     * Retrieve a device.
     *
     */
    @Route(method = HttpMethod.GET, uri = "/device/{deviceId}")
    public synchronized Result getDevice(@Parameter("deviceId") String deviceId) {
        
    	if (deviceId == null || deviceId.isEmpty()){
            return getDevices();
        }

        GenericDevice device = device(deviceId);

        if (device == null) {
            return notFound();
        } 

		try {
			return ok(serialize(device).toString()).as(MimeTypes.JSON);
		} catch (JSONException e) {
			return internalServerError(e);
		}
    }

    @Route(method = HttpMethod.PUT, uri = "/device/{deviceId}")
    public synchronized Result updatesDevice(@Parameter("deviceId") String deviceId) {
    	
        if (deviceId == null || deviceId.isEmpty()) {
            return notFound();
        }

        GenericDevice device = device(deviceId);
		if (device == null) {
			return notFound();
		}
        
        if (! (device instanceof LocatedObject)){
            return forbidden();
        }

		try {
	        LocatedObject located 	= (LocatedObject) device;
	        DeviceJSON update		= DeviceJSON.from(content(context()));
	        
	        if (update.getPositionX() != null && update.getPositionY() != null) {
	            Position newPostion = new Position(update.getPositionX(),update.getPositionY());
	            located.setPosition(newPostion);
	        }

	        return ok();
	        
		} catch (JSONException | IOException e) {
			return internalServerError(e);
		}
        
    }

    /**
     * Retrieve a device property.
     *
     */
    @Route(method = HttpMethod.GET, uri = "/device/{deviceId}/{service}/{property}")
    public synchronized Result getProperty(@Parameter("deviceId") String deviceId, @Parameter("service") String service, @Parameter("property") String property) {
        
    	if (deviceId == null || deviceId.isEmpty()){
            return getDevices();
        }

        GenericDevice device = device(deviceId);

        if (device == null) {
            return notFound();
        } 

        String value = null;

    	if (service.equalsIgnoreCase("PowerSwitch") && property.equals(PowerSwitch.CURRENT_STATUS) && device instanceof PowerSwitch) {
    		value = Boolean.toString(((PowerSwitch) device).getStatus());
        }

    	if (service.equalsIgnoreCase("PresenceSensor") && property.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE) && device instanceof PresenceSensor) {
    		value = Boolean.toString(((PresenceSensor) device).getSensedPresence());
        }


    	if (service.equalsIgnoreCase("BinaryLight") && property.equals("PowerStatus") && device instanceof BinaryLight) {
    		value = Boolean.toString(((BinaryLight) device).getPowerStatus());
        }

        return value != null ? ok(value).as(MimeTypes.TEXT) : notFound();
    }

    @Route(method = HttpMethod.PUT, uri = "/device/{deviceId}/{service}/{property}")
    public synchronized Result setProperty(@Parameter("deviceId") String deviceId, @Parameter("service") String service, @Parameter("property") String property) {
        
    	if (deviceId == null || deviceId.isEmpty()) {
            return getDevices();
        }

        GenericDevice device = device(deviceId);

        if (device == null) {
            return notFound();
        }

        try {

        	String update = content(context());
            
        	if (service.equalsIgnoreCase("PowerSwitch") && property.equals(PowerSwitch.CURRENT_STATUS) && device instanceof ControllablePowerSwitch) {
            	((ControllablePowerSwitch) device).setStatus(Boolean.valueOf(update));
            	return ok();
            }


        	if (service.equalsIgnoreCase("BinaryLight") && property.equalsIgnoreCase("PowerStatus") && device instanceof BinaryLight) {
            	((BinaryLight) device).setPowerStatus(Boolean.valueOf(update));
            	return ok();
            }

            return notFound();

        } catch (IOException e) {
            return internalServerError();
        }
        
    }


}
