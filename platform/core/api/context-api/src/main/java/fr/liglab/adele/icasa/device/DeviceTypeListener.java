/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under the Apache License, Version 2.0 (the "License");
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
package fr.liglab.adele.icasa.device;

import fr.liglab.adele.icasa.listener.IcasaListener;

/**
 * Listener used to be notified about the device types in the platform.
 * 
 * @author Thomas Leveque
 */
public interface DeviceTypeListener extends IcasaListener {

	/**
	 * Callback notifying the addition of a device type to the platform.
	 * 
	 * @param deviceType Device type added
	 */
	public void deviceTypeAdded(String deviceType);

	/**
	 * Callback notifying the elimination of a device type from the platform.
	 * 
	 * @param deviceType Device type removed
	 */
	public void deviceTypeRemoved(String deviceType);
}
