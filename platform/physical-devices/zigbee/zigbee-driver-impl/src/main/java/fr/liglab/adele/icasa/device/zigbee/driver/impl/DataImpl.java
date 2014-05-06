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

/**
 *
 */
public class DataImpl implements Data {
	
	private String data;
	private Date timeStamp;
	

	public void setData(String data) {
		this.data = data;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.habits.monitoring.zigbee.driver.Data#getTimeStamp()
	 */
	@Override
	public Date getTimeStamp() {
		return this.timeStamp;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.habits.monitoring.zigbee.driver.Data#getData()
	 */
	@Override
	public String getData() {
		return this.data;
	}

}
