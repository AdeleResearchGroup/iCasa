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

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.remote.wisdom.SimulatedDeviceManager;
import fr.liglab.adele.icasa.remote.wisdom.util.DeviceJSON;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wisdom.api.DefaultController;

import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
@Component
@Provides
@Instantiate
@Path("/icasa/devices")
public class DeviceREST extends DefaultController {

    @Requires
    private ContextManager _contextMgr;
    
    @Requires (optional = true) 
    SimulatedDeviceManager simulator;
    



    @Route(method = HttpMethod.GET, uri = "/deviceTypes")
    public Result deviceTypes() {
        return ok(getDeviceTypes()).as(MimeTypes.JSON);
    }



    @Route(method = HttpMethod.GET, uri = "/simulatedDeviceTypes")
    public Result simulatedDeviceTypes() {
    	if(simulator == null) {
    		return internalServerError();
    	}
        return ok(getSimulatedDeviceTypes()).as(MimeTypes.JSON);
    }
    

    @Route(method = HttpMethod.GET, uri = "/devices")
    public Result devices() {
        return ok(getDevices()).as(MimeTypes.JSON);
    }

    /**
     * Retrieve a device.
     *
     * @param deviceId The ID of the device to retrieve
     * @return The required device,
     * return <code>null<code> if the device does not exist.
     * @throws java.text.ParseException
     */
    @Route(method = HttpMethod.GET, uri = "/device/{deviceId}")
    public Result device(@Parameter("deviceId") String deviceId) {
        if (deviceId == null || deviceId.length()<1){
            return ok(getDevices()).as(MimeTypes.JSON);
        }

        LocatedDevice foundDevice = findDevice(deviceId);
        if (foundDevice == null) {
            return notFound();
        } else {
            JSONObject foundDeviceJSON = IcasaJSONUtil.getDeviceJSON(foundDevice, _contextMgr);

            return ok(foundDeviceJSON.toString()).as(MimeTypes.JSON);
        }
    }

    private LocatedDevice findDevice(String deviceId) {
        return _contextMgr.getDevice(deviceId);
    }

    @Route(method = HttpMethod.PUT, uri = "/device/{deviceId}")
    public Result updatesDevice(@Parameter("deviceId") String deviceId) {
        if (deviceId == null || deviceId.length()<1){
            return notFound();
        }

        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }

        LocatedDevice device = findDevice(deviceId);
        if (device == null){
            return notFound();
        }

        DeviceJSON updatedDevice = DeviceJSON.fromString(content);
        if (updatedDevice != null) {
            updatedDevice.setId(deviceId);

            if (updatedDevice.getState() != null)
                device.setPropertyValue(GenericDevice.STATE_PROPERTY_NAME, updatedDevice.getState());
            if (updatedDevice.getFault() != null)
                device.setPropertyValue(GenericDevice.FAULT_PROPERTY_NAME, updatedDevice.getFault());
            if ((updatedDevice.getPositionX() != null) || (updatedDevice.getPositionY() != null)) {
                Position position = _contextMgr.getDevicePosition(deviceId);
                int newPosX = position.x;
                int newPosY = position.y;
                if (updatedDevice.getPositionX() != null)
                    newPosX = updatedDevice.getPositionX();
                if (updatedDevice.getPositionY() != null)
                    newPosY = updatedDevice.getPositionY();
                _contextMgr.setDevicePosition(deviceId, new Position(newPosX, newPosY));
            } else if (updatedDevice.getLocation() != null)
                _contextMgr.getDevice(device.getSerialNumber()).setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, updatedDevice.getLocation());
        }

        JSONObject deviceJSON = IcasaJSONUtil.getDeviceJSON(device, _contextMgr);

        return ok(deviceJSON.toString()).as(MimeTypes.JSON);
    }

    /**
     * Create a new device.
     *
     * @return
     */
    @Route(method = HttpMethod.POST, uri = "/device")
    public Result createDevice() {
        String content = null;
        try {
            BufferedReader reader = context().reader();
            content = IcasaJSONUtil.getContent(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
    	if(simulator == null) {

    		return internalServerError();
    	}
        DeviceJSON deviceJSON = DeviceJSON.fromString(content);

        String deviceType = deviceJSON.getType();

        LocatedDevice newDevice = null;
        if (deviceType != null) {

            String deviceId = deviceJSON.getId();
            
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(GenericDevice.DEVICE_SERIAL_NUMBER, deviceId);

            try {
            	simulator.createDevice(deviceType, deviceId, properties);
            } catch (Exception e) {
                e.printStackTrace();
                newDevice = null;
            }

            newDevice = _contextMgr.getDevice(deviceId);
        }

        if (newDevice == null)
            return internalServerError();

        JSONObject newDeviceJSON = IcasaJSONUtil.getDeviceJSON(newDevice, _contextMgr);

        return ok(newDeviceJSON.toString()).as(MimeTypes.JSON);
    }

    /**
     * Delete specified device.
     *
     * @param deviceId device id
     * @return true if chain is successful deleted, false if it does not exist.
     */
    @Route(method = HttpMethod.DELETE, uri = "/device/{deviceId}")
    public Result deleteDevice(@Parameter("deviceId") String deviceId) {

    	if(simulator == null) {
    		return status(Status.INTERNAL_SERVER_ERROR);
    	}
        LocatedDevice foundDevice = findDevice(deviceId);
        if (foundDevice == null)
            return notFound();
        try {
            simulator.removeDevice(deviceId);
        } catch (Exception e) {
            return status(Status.INTERNAL_SERVER_ERROR);
        }

        return ok();
    }
    
    
    /**
     * Returns a JSON array containing all devices.
     *
     * @return a JSON array containing all devices.
     */
    private String getDeviceTypes() {
        JSONArray currentDevices = new JSONArray();
        for (String deviceTypeStr : _contextMgr.getDeviceTypes()) {
            JSONObject deviceType = IcasaJSONUtil.getDeviceTypeJSON(deviceTypeStr, _contextMgr);
            if (deviceType == null)
                continue;

            currentDevices.put(deviceType);
        }

        return currentDevices.toString();
    }
    
    /**
     * Returns a JSON array containing all devices.
     *
     * @return a JSON array containing all devices.
     */
    private String getSimulatedDeviceTypes() {
        JSONArray currentDevices = new JSONArray();
        for (String deviceTypeStr : simulator.getDeviceTypes()) {
            JSONObject deviceType = IcasaJSONUtil.getDeviceTypeJSON(deviceTypeStr, _contextMgr);
            if (deviceType == null)
                continue;

            currentDevices.put(deviceType);
        }

        return currentDevices.toString();
    }
    
    /**
     * Returns a JSON array containing all devices.
     *
     * @return a JSON array containing all devices.
     */
    private String getDevices() {
        //boolean atLeastOne = false;
        JSONArray currentDevices = new JSONArray();
        for (LocatedDevice device : _contextMgr.getDevices()) {
            JSONObject deviceJSON =  IcasaJSONUtil.getDeviceJSON(device, _contextMgr);
            if (deviceJSON == null)
                continue;

            currentDevices.put(deviceJSON);
        }

        return currentDevices.toString();
    }

}
