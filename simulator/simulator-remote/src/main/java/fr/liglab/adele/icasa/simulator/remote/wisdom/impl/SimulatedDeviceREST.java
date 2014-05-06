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
package fr.liglab.adele.icasa.simulator.remote.wisdom.impl;

import java.util.Map;
import java.util.Set;

import fr.liglab.adele.icasa.remote.wisdom.SimulatedDeviceManager;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.icasa.simulator.SimulationManager;

/**
 *
 */
@Component
@Instantiate
@Provides(specifications={SimulatedDeviceManager.class})
public class SimulatedDeviceREST implements SimulatedDeviceManager {

    @Requires
    private SimulationManager _simulationMgr;
   
	/* (non-Javadoc)
	 * @see fr.liglab.adele.icasa.remote.SimulatedDeviceManager#createDevice(java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public void createDevice(String deviceType, String deviceId,
			Map<String, Object> properties) {
		_simulationMgr.createDevice(deviceType, deviceId, properties);		
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.icasa.remote.SimulatedDeviceManager#removeDevice(java.lang.String)
	 */
	@Override
	public void removeDevice(String deviceId) {
        _simulationMgr.removeDevice(deviceId);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.icasa.remote.SimulatedDeviceManager#getDeviceTypes()
	 */
	@Override
	public Set<String> getDeviceTypes() {
		return _simulationMgr.getSimulatedDeviceTypes();
	}
    
  
}
