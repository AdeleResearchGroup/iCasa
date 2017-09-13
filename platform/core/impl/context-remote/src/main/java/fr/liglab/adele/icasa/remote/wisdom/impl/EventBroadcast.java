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
package fr.liglab.adele.icasa.remote.wisdom.impl;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.clockservice.ClockListener;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.remote.wisdom.RemoteEventBroadcast;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;

import java.util.Date;
import java.util.UUID;

import org.wisdom.api.Controller;

@Component
@Provides(specifications = {RemoteEventBroadcast.class, Controller.class})
@Instantiate
public class EventBroadcast extends DefaultController implements RemoteEventBroadcast {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG_REMOTE+".event");

    @Property (name = "url", value="/icasa/websocket/event")
	private String url;

	@Requires
	private Publisher publisher;

	@Requires
	private Clock _clock;

	private ClockEventListener _clockListener;



	@Validate
	protected void start() {
        logger.debug("start");
		// Register iCasa listeners

		_clockListener = new ClockEventListener();
		_clock.addListener(_clockListener);

	}

	@Invalidate
	protected void stop() {
        logger.debug("stop");
		// Unregister iCasa listeners

		if (_clockListener != null) {
			_clock.removeListener(_clockListener);
			_clockListener = null;
		}

	}


	private UUID _lastEventId = UUID.randomUUID();

	private String generateUUID() {
		_lastEventId = UUID.randomUUID();
		return _lastEventId.toString();
	}

    @Opened("{name}")
    public void open(@Parameter("name") String name) {
        System.out.println("Web socket opened => " + name);
    }

    @Closed("{name}")
    public void close(@Parameter("name") String name) {
        System.out.println("Web socket closed => " + name);
    }

    @Route(method = HttpMethod.GET, uri = "/icasa/websocket/event")
    public Result handshake() {
        return ok();
    }

    @OnMessage("{name}")
    public void onMessage(@Parameter("name") String name) {
        System.out.println("Receiving message on " + name + " : ");
        publisher.publish(url, "OK");
    }

	public void sendEvent(String eventType, JSONObject event) {
        logger.debug("!! sending event : " + eventType + "to " + url);
		try {
			event.put("eventType", eventType);
			event.put("id", generateUUID());
			event.put("time", new Date().getTime());
            publisher.publish(url, event.toString());
		} catch (JSONException e) {
            logger.error("Building message error" + eventType, e);
		}
	}

	@Bind(id="zones",specification = Zone.class,optional = true,aggregate = true)
	public synchronized void bindZone(Zone zone){
		JSONObject json = new JSONObject();
		try {
			json.put("zoneId", zone.getZoneName());
			json.put("zone", IcasaJSONUtil.getZoneJSON(zone));
			sendEvent("zone-added", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Modified(id="zones")
	public synchronized void modifiedZone(Zone zone){
		JSONObject json = new JSONObject();
		try {
			json.put("zoneId", zone.getZoneName());
			json.put("zone", IcasaJSONUtil.getZoneJSON(zone));
			sendEvent("zone-moved", json);
			sendEvent("zone-resized", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Unbind(id="zones")
	public synchronized void unbindZone(Zone zone){
		JSONObject json = new JSONObject();
		try {
			json.put("zoneId", zone.getZoneName());
			sendEvent("zone-removed", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	private class ClockEventListener implements ClockListener {

		@Override
		public void factorModified(int oldFactor) {
			sendClockModifiedEvent();
		}

		@Override
		public void startDateModified(long oldStartDate) {
			sendClockModifiedEvent();
		}

		@Override
		public void clockPaused() {
			sendClockModifiedEvent();
		}

		@Override
		public void clockResumed() {
			sendClockModifiedEvent();
		}

		@Override
		public void clockReset() {
			sendClockModifiedEvent();
		}

		private void sendClockModifiedEvent() {
			JSONObject json = new JSONObject();
			try {
				json.put("clock", IcasaJSONUtil.getClockJSON(_clock));
				sendEvent("clock-modified", json);
			} catch (JSONException e) {
                logger.error("Building message error" + json, e);
				e.printStackTrace();
			}
		}

	}

	@Bind(id="binaryLights",specification = BinaryLight.class,optional = true,aggregate = true)
	public synchronized void bindBinaryLight(BinaryLight binaryLight){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", binaryLight.getSerialNumber());
			json.put("device", IcasaJSONUtil.getBinaryLightJSON(binaryLight));
			sendEvent("device-added", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Modified(id="binaryLights")
	public synchronized void modifiedBinaryLight(BinaryLight binaryLight){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", binaryLight.getSerialNumber());
			json.put("device", IcasaJSONUtil.getBinaryLightJSON(binaryLight));
			sendEvent("device-position-update", json);
			sendEvent("device-property-updated",json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Unbind(id="binaryLights")
	public synchronized void unbindBinaryLight(BinaryLight binaryLight){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", binaryLight.getSerialNumber());
			sendEvent("device-removed", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Bind(id="dimmerLights",specification = DimmerLight.class,optional = true,aggregate = true)
	public synchronized void bindDimmerLight(DimmerLight dimmerLight){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", dimmerLight.getSerialNumber());
			json.put("device", IcasaJSONUtil.getDimmerLightJSON(dimmerLight));
			sendEvent("device-added", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Modified(id="dimmerLights")
	public synchronized void modifiedDimmerLight(DimmerLight dimmerLight){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", dimmerLight.getSerialNumber());
			json.put("device", IcasaJSONUtil.getDimmerLightJSON(dimmerLight));
			sendEvent("device-position-update", json);
			sendEvent("device-property-updated",json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Unbind(id="dimmerLights")
	public synchronized void unbindDimmerLight(DimmerLight dimmerLight){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", dimmerLight.getSerialNumber());
			sendEvent("device-removed", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Bind(id="thermometers",specification = Thermometer.class,optional = true,aggregate = true)
	public synchronized void bindThermometer(Thermometer thermometer){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", thermometer.getSerialNumber());
			json.put("device", IcasaJSONUtil.getThermometerJSON(thermometer));
			sendEvent("device-added", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Modified(id="thermometers")
	public synchronized void modifiedThermometer(Thermometer thermometer){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", thermometer.getSerialNumber());
			json.put("device", IcasaJSONUtil.getThermometerJSON(thermometer));
			sendEvent("device-position-update", json);
			sendEvent("device-property-updated",json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Unbind(id="thermometers")
	public synchronized void unbindThermometer(Thermometer thermometer){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", thermometer.getSerialNumber());
			sendEvent("device-removed", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Bind(id="coolers",specification = Cooler.class,optional = true,aggregate = true)
	public synchronized void bindCooler(Cooler cooler){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", cooler.getSerialNumber());
			json.put("device", IcasaJSONUtil.getCoolerJSON(cooler));
			sendEvent("device-added", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Modified(id="coolers")
	public synchronized void modifiedCooler(Cooler cooler){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", cooler.getSerialNumber());
			json.put("device", IcasaJSONUtil.getCoolerJSON(cooler));
			sendEvent("device-position-update", json);
			sendEvent("device-property-updated",json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Unbind(id="coolers")
	public synchronized void unbindCooler(Cooler cooler){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", cooler.getSerialNumber());
			sendEvent("device-removed", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Bind(id="heaters",specification = Heater.class,optional = true,aggregate = true)
	public synchronized void bindHeater(Heater heater){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", heater.getSerialNumber());
			json.put("device", IcasaJSONUtil.getHeaterJSON(heater));
			sendEvent("device-added", json);
			sendEvent("device-property-updated",json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Modified(id="heaters")
	public synchronized void modifiedHeater(Heater heater){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", heater.getSerialNumber());
			json.put("device", IcasaJSONUtil.getHeaterJSON(heater));
			sendEvent("device-position-update", json);
			sendEvent("device-property-updated",json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Unbind(id="heaters")
	public synchronized void unbindHeater(Heater heater){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", heater.getSerialNumber());
			sendEvent("device-removed", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Bind(id="photometers",specification = Photometer.class,optional = true,aggregate = true)
	public synchronized void bindPhotometer(Photometer photometer){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", photometer.getSerialNumber());
			json.put("device", IcasaJSONUtil.getPhotometerJSON(photometer));
			sendEvent("device-added", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Modified(id="photometers")
	public synchronized void modifiedPhotometer(Photometer photometer){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", photometer.getSerialNumber());
			json.put("device", IcasaJSONUtil.getPhotometerJSON(photometer));
			sendEvent("device-position-update", json);
			sendEvent("device-property-updated",json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Unbind(id="photometers")
	public synchronized void unbindPhotometer(Photometer photometer){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", photometer.getSerialNumber());
			sendEvent("device-removed", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Bind(id="presenceSensors",specification = PresenceSensor.class,optional = true,aggregate = true)
	public synchronized void bindPresenceSensor(PresenceSensor presenceSensor){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", presenceSensor.getSerialNumber());
			json.put("device", IcasaJSONUtil.getPresenceSensorJSON(presenceSensor));
			sendEvent("device-added", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Modified(id="presenceSensors")
	public synchronized void modifiedPresenceSensor(PresenceSensor presenceSensor){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", presenceSensor.getSerialNumber());
			json.put("device", IcasaJSONUtil.getPresenceSensorJSON	(presenceSensor));
			sendEvent("device-position-update", json);
			sendEvent("device-property-updated",json);
		sendEvent("device-property-updated",json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

	@Unbind(id="presenceSensors")
	public synchronized void unbindPresenceSensor(PresenceSensor presenceSensor){
		JSONObject json = new JSONObject();
		try {
			json.put("deviceId", presenceSensor.getSerialNumber());
			sendEvent("device-removed", json);
		} catch (JSONException e) {
			logger.error("Building message error" + json, e);
		}
	}

}
