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
package fr.liglab.adele.icasa.simulator.remote.wisdom.impl;

import fr.liglab.adele.icasa.remote.wisdom.util.DeviceJSON;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import fr.liglab.adele.icasa.simulator.device.SimulatedDeviceProvider;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 */
@Component
@Instantiate
@Provides(specifications={Controller.class})
@Path("/icasa/devices")
public class SimulatedDeviceREST extends DefaultController {

    @Requires
    private SimulatedDeviceProvider mySimulatedDeviceManager;

    @Route(method = HttpMethod.POST, uri = "/device")
    public Result createDevice() {
        try {
        	String  content 		= IcasaJSONUtil.content(context());
            DeviceJSON deviceJSON 	= DeviceJSON.from(content);

            String deviceType = deviceJSON.getType();

            if (deviceType != null) {

                String deviceId = deviceJSON.getId();

                mySimulatedDeviceManager.createDevice(deviceType, deviceId);
            }

            return ok();

        } catch (JSONException | IOException e) {
            return internalServerError(e);
        }
    }

    @Route(method = HttpMethod.DELETE, uri = "/device/{deviceId}")
    public Result deleteDevice(@Parameter("deviceId") String deviceId) {
        mySimulatedDeviceManager.removeSimulatedDevice(deviceId);
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/simulatedDeviceTypes")
    public Result deviceTypes(){
        try{
            String deviceTypes = getDeviceTypes();
            return ok(deviceTypes).as(MimeTypes.JSON);
        }catch (JSONException e){
            return internalServerError(e);
        }

    }

    private String getDeviceTypes() throws JSONException{
        JSONArray currentDevices = new JSONArray();
        for (String deviceTypeStr : mySimulatedDeviceManager.getSimulatedDeviceTypes()) {
            JSONObject deviceType = getDeviceTypeJSON(deviceTypeStr);
            if (deviceType == null)
                continue;
            currentDevices.put(deviceType);
        }

        return currentDevices.toString();
    }

    private JSONObject getDeviceTypeJSON(String deviceTypeStr) throws JSONException{
        JSONObject deviceTypeJSON = new JSONObject();
        deviceTypeJSON.putOnce("id", deviceTypeStr);
        deviceTypeJSON.putOnce("name", deviceTypeStr);

        return deviceTypeJSON;
    }
}
