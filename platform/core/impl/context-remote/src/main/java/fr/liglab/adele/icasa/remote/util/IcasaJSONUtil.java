/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.remote.util;

import fr.liglab.adele.icasa.remote.impl.ClockREST;
import org.json.JSONException;
import org.json.JSONObject;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.clock.util.DateTextUtil;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;

import java.util.Set;

public class IcasaJSONUtil {

	public static JSONObject getDeviceJSON(LocatedDevice device, ContextManager _ctxMgr) {

		String deviceType = device.getType();
		if (deviceType == null){
			deviceType = "undefined";
        }

		Position devicePosition = _ctxMgr.getDevicePosition(device.getSerialNumber());
        Set<String> specifications = _ctxMgr.getProvidedServices(deviceType);

        JSONObject deviceJSON = null;
		try {
			deviceJSON = new JSONObject();
			deviceJSON.putOnce(DeviceJSON.ID_PROP, device.getSerialNumber());
			deviceJSON.putOnce(DeviceJSON.NAME_PROP, device.getSerialNumber());
			deviceJSON.put(DeviceJSON.FAULT_PROP, device.getPropertyValue(GenericDevice.FAULT_PROPERTY_NAME));
			deviceJSON.put(DeviceJSON.LOCATION_PROP, device.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME));
			deviceJSON.put(DeviceJSON.STATE_PROP, device.getPropertyValue(GenericDevice.STATE_PROPERTY_NAME));
			deviceJSON.put(DeviceJSON.TYPE_PROP, deviceType);
            if(specifications != null){
                deviceJSON.put(DeviceJSON.SERVICES, specifications );
            }
			if (devicePosition != null) {
				deviceJSON.put(DeviceJSON.POSITION_X_PROP, devicePosition.x);
				deviceJSON.put(DeviceJSON.POSITION_Y_PROP, devicePosition.y);
			}
			JSONObject propObject = new JSONObject();
			for (String property : device.getProperties()) {
				propObject.put(property, device.getPropertyValue(property));
			}
			deviceJSON.put(DeviceJSON.PROPERTIES_PROP, propObject);
		} catch (JSONException e) {
			e.printStackTrace();
			deviceJSON = null;
		}

		return deviceJSON;
	}

	public static JSONObject getDeviceTypeJSON(String deviceTypeStr, ContextManager ctx) {
		JSONObject deviceTypeJSON = null;
        Set<String> specifications = ctx.getProvidedServices(deviceTypeStr);
		try {
			deviceTypeJSON = new JSONObject();
			deviceTypeJSON.putOnce("id", deviceTypeStr);
			deviceTypeJSON.putOnce("name", deviceTypeStr);
            if(specifications != null){
                deviceTypeJSON.put(DeviceJSON.SERVICES, specifications );
            }
		} catch (JSONException e) {
			e.printStackTrace();
			deviceTypeJSON = null;
		}

		return deviceTypeJSON;
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
			String zoneId = zone.getId();
			zoneJSON = new JSONObject();
			zoneJSON.putOnce(ZoneJSON.ID_PROP, zoneId);
			zoneJSON.putOnce(ZoneJSON.NAME_PROP, zoneId);
			zoneJSON.put(ZoneJSON.POSITION_LEFTX_PROP, zone.getLeftTopAbsolutePosition().x);
			zoneJSON.put(ZoneJSON.POSITION_TOPY_PROP, zone.getLeftTopAbsolutePosition().y);
			zoneJSON.put(ZoneJSON.POSITION_RIGHTX_PROP, zone.getRightBottomAbsolutePosition().x);
			zoneJSON.put(ZoneJSON.POSITION_BOTTOMY_PROP, zone.getRightBottomAbsolutePosition().y);
			zoneJSON.put(ZoneJSON.IS_ROOM_PROP, true); // TODO change it when Zone API will be improved

			JSONObject propObject = new JSONObject();
			for (String variable : zone.getVariableNames()) {
				propObject.put(variable, zone.getVariableValue(variable));
			}
			zoneJSON.put(ZoneJSON.VARIABLE_PROP, propObject);

		} catch (JSONException e) {
			e.printStackTrace();
			zoneJSON = null;
		}

		return zoneJSON;
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

	
	
	
	
	
}
