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
package fr.liglab.dynamo.discovery.bluetooth;

import java.util.Map;

/**
 * 
 */
public interface BluetoothDiscovery {
	/**
	 * Start to track a Bluetooth device. If the Bluetooth device already
	 * tracked, do nothing.
	 * The tracking service is based on the <a href=
	 * "http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/RemoteDevice.html#getFriendlyName%28boolean%29"
	 * >friendly name</a>.
	 * 
	 * @param friendlyName the begin of friendly name to track.
	 * @param ipojoComponentName the name of the iPOJO component to be started
	 * as soon as the service is discovered
	 * @param bundleFilename the filename of the bundle where the factory is
	 * nested
	 * @param configuration partially filled configuration for iPOJO component
	 * and its handler
	 */
	public void trackBluetoothDevice(String friendlyName, String ipojoComponentName, String bundleFilename, Map<String, Object> configuration);

	/**
	 * Stop tracking. Throws an IllegalArgumentException if the service is not
	 * tracked.
	 * 
	 * @param busNames the friendly name used to track the service
	 */
	public void untrackBluetoothDevice(String busNames);
}
