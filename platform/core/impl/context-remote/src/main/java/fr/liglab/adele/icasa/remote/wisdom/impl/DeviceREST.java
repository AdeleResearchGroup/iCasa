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

import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
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
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.remote.wisdom.util.DeviceJSON;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.IOException;
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

    private final static Logger LOG = LoggerFactory.getLogger(DeviceREST.class);

    @Requires(specification = BinaryLight.class,optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<BinaryLight> binaryLights;

    @Requires(specification = DimmerLight.class,optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<DimmerLight> dimmerLights;

    @Requires(specification = PresenceSensor.class,optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<PresenceSensor> presenceSensors;

    @Requires(specification = MotionSensor.class,optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<MotionSensor> motionSensors;

    @Requires(specification = PushButton.class,optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<PushButton> pushButtons;

    @Requires(specification = Photometer.class, optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<Photometer> photometers;

    @Requires(specification = Thermometer.class,optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<Thermometer> thermometers;

    @Requires(specification = Cooler.class,optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<Cooler> coolers;

    @Requires(specification = Heater.class,optional = true,proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    List<Heater> heaters;

    @Route(method = HttpMethod.GET, uri = "/devices")
    public Result devices() {
        try {
            String deviceList = getDevices();
            return ok(getDevices()).as(MimeTypes.JSON);
        }catch (JSONException e){
            return internalServerError(e);
        }
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
    public synchronized Result device(@Parameter("deviceId") String deviceId) {
        if (deviceId == null || deviceId.length()<1){
            return devices();
        }

        GenericDevice foundDevice = findDevice(deviceId);

        if (foundDevice == null) {
            return notFound();
        } else {
            JSONObject deviceJSON = null;
            if (foundDevice instanceof Heater){
                try {
                    deviceJSON = IcasaJSONUtil.getHeaterJSON((Heater) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }

            if (foundDevice instanceof Cooler){
                try {
                    deviceJSON = IcasaJSONUtil.getCoolerJSON((Cooler) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }

            if (foundDevice instanceof Thermometer){
                try {
                    deviceJSON = IcasaJSONUtil.getThermometerJSON((Thermometer) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }

            if (foundDevice instanceof BinaryLight){
                try {
                    deviceJSON = IcasaJSONUtil.getBinaryLightJSON((BinaryLight) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }

            if (foundDevice instanceof DimmerLight){
                try {
                    deviceJSON = IcasaJSONUtil.getDimmerLightJSON((DimmerLight) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }

            if (foundDevice instanceof PresenceSensor){
                try {
                    deviceJSON = IcasaJSONUtil.getPresenceSensorJSON((PresenceSensor) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }

            if (foundDevice instanceof MotionSensor){
                try {
                    deviceJSON = IcasaJSONUtil.getMotionSensorJSON((MotionSensor) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }

            if (foundDevice instanceof Photometer){
                try {
                    deviceJSON = IcasaJSONUtil.getPhotometerJSON((Photometer) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }

            if (foundDevice instanceof PushButton){
                try {
                    deviceJSON = IcasaJSONUtil.getPushButtonJSON((PushButton) foundDevice);
                } catch (JSONException e) {
                    return internalServerError(e);
                }
            }
            return ok(deviceJSON.toString()).as(MimeTypes.JSON);
        }
    }

    @Route(method = HttpMethod.PUT, uri = "/device/{deviceId}")
    public synchronized Result updatesDevice(@Parameter("deviceId") String deviceId) {
        if (deviceId == null || deviceId.length()<1){
            return notFound();
        }

        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            LOG.error("",e);
            return internalServerError();
        }

        GenericDevice device = findDevice(deviceId);
        if (device == null){
            return notFound();
        }
        if (! (device instanceof LocatedObject)){
            return forbidden();
        }
        LocatedObject locatedObject = (LocatedObject) device;
        DeviceJSON updatedDevice = DeviceJSON.fromString(content);
        if (updatedDevice != null) {
            if (updatedDevice.getPositionX() != null && updatedDevice.getPositionY() != null){
                Position newPostion = new Position(updatedDevice.getPositionX(),updatedDevice.getPositionY());
                locatedObject.setPosition(newPostion);
            }
        }

        return ok();
    }

    private synchronized GenericDevice findDevice(String deviceSerialNumber){
        for (Heater heater:heaters){
            if (deviceSerialNumber.equals(heater.getSerialNumber()))return heater;
        }
        for (DimmerLight light:dimmerLights){
            if (deviceSerialNumber.equals(light.getSerialNumber()))return light;
        }
        for (Cooler cooler:coolers){
            if (deviceSerialNumber.equals(cooler.getSerialNumber()))return cooler;
        }
        for (Thermometer thermometer:thermometers){
            if (deviceSerialNumber.equals(thermometer.getSerialNumber()))return thermometer;
        }
        for (BinaryLight binaryLight:binaryLights){
            if (deviceSerialNumber.equals(binaryLight.getSerialNumber()))return binaryLight;
        }
        for (PresenceSensor presenceSensor:presenceSensors){
            if (deviceSerialNumber.equals(presenceSensor.getSerialNumber()))return presenceSensor;
        }
        for (MotionSensor motionSensor:motionSensors){
            if (deviceSerialNumber.equals(motionSensor.getSerialNumber()))return motionSensor;
        }
        for (PushButton pushButton:pushButtons){
            if (deviceSerialNumber.equals(pushButton.getSerialNumber()))return pushButton;
        }
        for (Photometer photometer : photometers){
            if (deviceSerialNumber.equals(photometer.getSerialNumber()))return photometer;
        }
        return null;
    }

    /**
     * Returns a JSON array containing all devices.
     *
     * @return a JSON array containing all devices.
     */
    private String getDevices() throws JSONException {
        JSONArray currentDevices = new JSONArray();
        for (Heater heater:heaters){
            JSONObject deviceJSON =  IcasaJSONUtil.getHeaterJSON(heater);
            currentDevices.put(deviceJSON);
        }
        for (DimmerLight light:dimmerLights){
            JSONObject deviceJSON =  IcasaJSONUtil.getDimmerLightJSON(light);
            currentDevices.put(deviceJSON);
        }
        for (Cooler cooler:coolers){
            JSONObject deviceJSON =  IcasaJSONUtil.getCoolerJSON(cooler);
            currentDevices.put(deviceJSON);
        }
        for (Thermometer thermometer:thermometers){
            JSONObject deviceJSON =  IcasaJSONUtil.getThermometerJSON(thermometer);
            currentDevices.put(deviceJSON);
        }
        for (BinaryLight binaryLight:binaryLights){
            JSONObject deviceJSON =  IcasaJSONUtil.getBinaryLightJSON(binaryLight);
            currentDevices.put(deviceJSON);
        }
        for (PresenceSensor presenceSensor:presenceSensors){
            JSONObject deviceJSON =  IcasaJSONUtil.getPresenceSensorJSON(presenceSensor);
            currentDevices.put(deviceJSON);
        }
        for (MotionSensor motionSensor:motionSensors){
            JSONObject deviceJSON =  IcasaJSONUtil.getMotionSensorJSON(motionSensor);
            currentDevices.put(deviceJSON);
        }
        for (PushButton pushButton:pushButtons){
            JSONObject deviceJSON =  IcasaJSONUtil.getPushButtonJSON(pushButton);
            currentDevices.put(deviceJSON);
        }
        for (Photometer photometer : photometers){
            JSONObject deviceJSON =  IcasaJSONUtil.getPhotometerJSON(photometer);
            currentDevices.put(deviceJSON);
        }
        return currentDevices.toString();
    }
}
