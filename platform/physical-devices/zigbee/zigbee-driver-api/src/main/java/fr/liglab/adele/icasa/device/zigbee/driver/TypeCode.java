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
package fr.liglab.adele.icasa.device.zigbee.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a type of device.
 *
 */
public enum TypeCode {

	C001("PUSH_BUTTON"), C002("POWER_SWITCH"), C003("MOTION_SENSOR"), C004(
			"LIGHT_SENSOR"), C005("TEMPERATURE_SENSOR"), A001("BINARY_LIGHT");

	private static final Logger logger = LoggerFactory
			.getLogger(TypeCode.class);

	private String friendlyName;

	private TypeCode(String name) {
		this.friendlyName = name;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public static TypeCode getTypeCodeByFriendlyName(String friendlyName) {

		if (C001.friendlyName.equals(friendlyName)) {
			return TypeCode.C001;
		} else if (C002.friendlyName.equals(friendlyName)) {
			return TypeCode.C002;
		} else if (C003.friendlyName.equals(friendlyName)) {
			return TypeCode.C003;
		} else if (C004.friendlyName.equals(friendlyName)) {
			return TypeCode.C004;
		} else if (A001.friendlyName.equals(friendlyName)) {
			return TypeCode.A001;
		} else if (C005.friendlyName.equals(friendlyName)) {
			return TypeCode.C005;
		} else {
			logger.error("unknown device type friendly name : " + friendlyName);
			return null;
		}
	}

	public String toString() {
		switch (this) {
		case C001:
			return "C001";
		case C002:
			return "C002";
		case C003:
			return "C003";
		case C004:
			return "C004";
		case C005:
			return "C005";
		case A001:
			return "A001";
		default:
			return null;
		}
	}
}
