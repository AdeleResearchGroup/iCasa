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
package fr.liglab.adele.icasa.device.zigbee.driver.serial.model;

/**
 * Enum class for zigbee data response types.
 *
 */
public enum ResponseType {

	WATCHDOG('w'), DATA('d'), REQUEST('r'), IDENTIFICATION('i');

	private char value;

	private ResponseType(char value) {
		this.value = value;
	}

	@Override
	public String toString() {
		switch (this) {
		case WATCHDOG:
			System.out.println("Watchdog : " + value);
			break;
		case DATA:
			System.out.println("Data : " + value);
			break;
		case REQUEST:
			System.out.println("Request : " + value);
			break;
		case IDENTIFICATION:
			System.out.println("Identification : " + value);
		}
		return super.toString();
	}
	
	public char getValue(){
		return this.value;
	}

}
