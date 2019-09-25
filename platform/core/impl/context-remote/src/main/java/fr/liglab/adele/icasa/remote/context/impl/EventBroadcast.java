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
package fr.liglab.adele.icasa.remote.context.impl;

import fr.liglab.adele.icasa.Constants;

import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.remote.context.Serializer;
import fr.liglab.adele.icasa.remote.context.serialization.SerializedDevice;
import fr.liglab.adele.icasa.remote.context.serialization.SerializedZone;
import fr.liglab.adele.icasa.remote.wisdom.RemoteEventBroadcast;
import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.clockservice.ClockListener;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedObject;

import org.apache.felix.ipojo.annotations.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;

import org.json.JSONException;
import org.json.JSONObject;

import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;

import java.util.Date;
import java.util.UUID;

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

	}



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
	
	private void sendClockModifiedEvent() {
		JSONObject event = new JSONObject();
		try {
			event.put("clock", IcasaJSONUtil.serialize(_clock));
			sendEvent("clock-modified", event);
		} catch (JSONException e) {
            logger.error("Building message error" + event, e);
			e.printStackTrace();
		}
	}


	@Bind(id="zones",specification = Zone.class, optional = true,aggregate = true)
	public synchronized void bindZone(Zone zone) {
		JSONObject event = new JSONObject();
		try {
			event.put("zoneId", zone.getZoneName());
			event.put("zone", new SerializedZone(zone).toJson());
			sendEvent("zone-added", event);
		} catch (JSONException e) {
			logger.error("Building message error" + event, e);
		}
	}

	@Modified(id="zones")
	public synchronized void modifiedZone(Zone zone) {
		JSONObject event = new JSONObject();
		try {
			event.put("zoneId", zone.getZoneName());
			event.put("zone", new SerializedZone(zone).toJson());
			sendEvent("zone-moved", event);
			sendEvent("zone-resized", event);
		} catch (JSONException e) {
			logger.error("Building message error" + event, e);
		}
	}

	@Unbind(id="zones")
	public synchronized void unbindZone(Zone zone) {
		JSONObject event = new JSONObject();
		try {
			event.put("zoneId", zone.getZoneName());
			sendEvent("zone-removed", event);
		} catch (JSONException e) {
			logger.error("Building message error" + event, e);
		}
	}

	@Requires(optional=true, proxy=false, policy=BindingPolicy.DYNAMIC)
	Serializer[] serializers;
	
	private JSONObject serialize(GenericDevice device) throws JSONException {
		SerializedDevice result = new SerializedDevice(device);
		for (Serializer serializer : serializers) {
			serializer.serialize(result, device);
		}
		
		return result.toJson();
	}

	@Bind(id="locatedDevices",specification = LocatedObject.class, optional = true, aggregate = true)
	public synchronized void bindLocated(LocatedObject located) {
		
		if (!(located instanceof GenericDevice)) {
			return;
		}
		
		GenericDevice device = (GenericDevice) located;
		
		JSONObject event = new JSONObject();
		try {
			event.put("deviceId", device.getSerialNumber());
			event.put("device", serialize(device));
			sendEvent("device-added", event);
		} catch (JSONException e) {
			logger.error("Building message error" + event, e);
		}
	}

	@Modified(id="locatedDevices")
	public synchronized void modifiedLocated(LocatedObject located) {

		if (!(located instanceof GenericDevice)) {
			return;
		}
		
		GenericDevice device = (GenericDevice) located;

		JSONObject event = new JSONObject();
		try {
			event.put("deviceId", device.getSerialNumber());
			event.put("device", serialize(device));
			sendEvent("device-position-update", event);
			sendEvent("device-property-updated",event);
		} catch (JSONException e) {
			logger.error("Building message error" + event, e);
		}
	}

	@Unbind(id="locatedDevices")
	public synchronized void unbindLocated(LocatedObject located) {
		if (!(located instanceof GenericDevice)) {
			return;
		}
		
		GenericDevice device = (GenericDevice) located;

		JSONObject event = new JSONObject();
		try {
			event.put("deviceId", device.getSerialNumber());
			sendEvent("device-removed", event);
		} catch (JSONException e) {
			logger.error("Building message error" + event, e);
		}
	}

}
