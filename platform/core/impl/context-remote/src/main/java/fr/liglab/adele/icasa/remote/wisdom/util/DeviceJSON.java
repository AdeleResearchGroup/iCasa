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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO
 */
public class DeviceJSON {

	public static final String PROPERTIES_PROP = "properties";
	public static final String POSITION_Y_PROP = "positionY";
	public static final String POSITION_X_PROP = "positionX";
	public static final String LOCATION_PROP = "location";
	public static final String FAULT_PROP = "fault";
	public static final String STATE_PROP = "state";
	public static final String TYPE_PROP = "type";
	public static final String NAME_PROP = "name";
	public static final String DEVICE_ID_PROP = "deviceId";
	public static final String ID_PROP = "id";
    public static final String SERVICES = "services";

	private String type;
	private String name;
	private String id;
	private String state;
	private String location;
	private String fault;
	private Integer positionX;
	private Integer positionY;


	public static DeviceJSON fromString(String jsonStr) {
		DeviceJSON device = null;
		JSONObject json = null;
		try {
			json = new JSONObject(jsonStr);
			device = new DeviceJSON();
			if (json.has(ID_PROP)) {
				device.setId(json.getString(ID_PROP));
			} else if (json.has(DEVICE_ID_PROP)) {
				device.setId(json.getString(DEVICE_ID_PROP));
			}
			;
			if (json.has(TYPE_PROP))
				device.setType(json.getString(TYPE_PROP));
			if (json.has(NAME_PROP))
				device.setName(json.getString(NAME_PROP));
			if (json.has(STATE_PROP))
				device.setState(json.getString(STATE_PROP));
			if (json.has(LOCATION_PROP))
				device.setLocation(json.getString(LOCATION_PROP));
			if (json.has(FAULT_PROP))
				device.setFault(json.getString(FAULT_PROP));
			if (json.has(POSITION_X_PROP))
				device.setPositionX(json.getInt(POSITION_X_PROP));
			if (json.has(POSITION_Y_PROP))
				device.setPositionY(json.getInt(POSITION_Y_PROP));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return device;
	}

	private void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getFault() {
		return fault;
	}

	public void setFault(String fault) {
		this.fault = fault;
	}

	public Integer getPositionX() {
		return positionX;
	}

	public void setPositionX(Integer positionX) {
		this.positionX = positionX;
	}

	public Integer getPositionY() {
		return positionY;
	}

	public void setPositionY(Integer positionY) {
		this.positionY = positionY;
	}

}
