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
package fr.liglab.adele.icasa.device.zigbee.driver.impl;

import java.util.Date;

import fr.liglab.adele.icasa.device.zigbee.driver.Data;
import fr.liglab.adele.icasa.device.zigbee.driver.DeviceInfo;
import fr.liglab.adele.icasa.device.zigbee.driver.TypeCode;

/**
 * Implementation class for the device info interface.
 */
public class DeviceInfoImpl implements DeviceInfo {
	
	private String moduleAddress;
	private float batteryLevel;
	private TypeCode typeCode;
	private Data deviceData;
	private Date lastConnexionDate;
	
	
	public Data getDeviceData() {
		return this.deviceData;
	}

	public void setDeviceData(Data deviceData) {
		this.deviceData = deviceData;
	}

	public void setModuleAddress(String moduleAddress) {
		this.moduleAddress = moduleAddress;
	}

	public void setBatteryLevel(float batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public void setTypeCode(TypeCode typeCode) {
		this.typeCode = typeCode;
	}

	@Override
	public String getModuleAddress() {
		return this.moduleAddress;
	}

	@Override
	public float getBatteryLevel() {
		return batteryLevel/10;
	}

	@Override
	public TypeCode getTypeCode() {
		return this.typeCode;
	}

	public Date getLastConnexionDate() {
		return lastConnexionDate;
	}

	public void setLastConnexionDate(Date lastConnexionDate) {
		this.lastConnexionDate = lastConnexionDate;
	}
	
}
