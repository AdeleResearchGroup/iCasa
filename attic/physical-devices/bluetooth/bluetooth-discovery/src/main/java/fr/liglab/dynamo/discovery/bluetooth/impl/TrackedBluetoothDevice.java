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
package fr.liglab.dynamo.discovery.bluetooth.impl;

import java.util.HashMap;
import java.util.Map;

import org.ow2.chameleon.rose.disco.RemoteEntityDescriptionManager;

/**
 * 
 */
class TrackedBluetoothDevice {
	private final String bundleFilename;
	private final Map<String, Object> configuration;
	/**
	 * Given to ROSE as the identifier of the service.
	 */
	private final Map<String, RemoteEntityDescriptionManager> discoveredAddresses;
	private final String friendlyName;
	private final String ipojoFactoryName;

	public TrackedBluetoothDevice(String friendlyName, String ipojoFactoryName, String bundleFilename, Map<String, Object> configuration) {
		if (bundleFilename == null || configuration == null || friendlyName == null || ipojoFactoryName == null) {
			throw new NullPointerException();
		}
		this.bundleFilename = bundleFilename;
		this.configuration = configuration;
		this.friendlyName = friendlyName;
		this.ipojoFactoryName = ipojoFactoryName;
		this.discoveredAddresses = new HashMap<String, RemoteEntityDescriptionManager>();
	}

	public String getBundleFilename() {
		return bundleFilename;
	}

	/**
	 * Returns the partially filled configuration of the service. The
	 * returned object is a copy, thus the caller if free to modify it.
	 * 
	 * @return a copy of the configuration
	 */
	public Map<String, Object> getConfiguration() {
		return new HashMap<String, Object>(configuration);
	}

	public Map<String, RemoteEntityDescriptionManager> getDiscoveredAddresses() {
		return new HashMap<String, RemoteEntityDescriptionManager>(discoveredAddresses);
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public String getIpojoFactoryName() {
		return ipojoFactoryName;
	}

	public void setDiscoveredAddresses(Map<String, RemoteEntityDescriptionManager> discoveredAddresses) {
		this.discoveredAddresses.clear();
		this.discoveredAddresses.putAll(discoveredAddresses);
	}
}
