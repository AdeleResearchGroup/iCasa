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

// *
// *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
// *   Group Licensed under a specific end user license agreement;
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
//
///
//// *
//// *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
//// *   Group Licensed under a specific end user license agreement;
//// *   you may not use this file except in compliance with the License.
//// *   You may obtain a copy of the License at
//// *
//// *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
//// *
//// *   Unless required by applicable law or agreed to in writing, software
//// *   distributed under the License is distributed on an "AS IS" BASIS,
//// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// *   See the License for the specific language governing permissions and
//// *   limitations under the License.
////
package fr.liglab.adele.icasa.simulator.device.provider;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.device.SimulatedDeviceProvider;
import fr.liglab.adele.icasa.simulator.device.light.impl.SimulatedBinaryLightImpl;
import fr.liglab.adele.icasa.simulator.device.light.impl.SimulatedDimmerLightImpl;
import fr.liglab.adele.icasa.simulator.device.light.impl.SimulatedPhotometerImpl;
import fr.liglab.adele.icasa.simulator.device.presence.impl.SimulatedPresenceSensorImpl;
import fr.liglab.adele.icasa.simulator.device.temperature.impl.SimulatedCoolerImpl;
import fr.liglab.adele.icasa.simulator.device.temperature.impl.SimulatedHeaterImpl;
import fr.liglab.adele.icasa.simulator.device.temperature.impl.SimulatedThermometerImpl;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(immediate = true,publicFactory=false)
@Provides(specifications = SimulatedDeviceProvider.class)
@Instantiate
public class SimulatedDeviceProviderImpl implements SimulatedDeviceProvider{

    public final static Logger LOG = LoggerFactory.getLogger(SimulatedDeviceProviderImpl.class);

    @Requires(specification = SimulatedDevice.class,optional = true)
    List<SimulatedDevice> simulatedDevices;

    @Creator.Field Creator.Entity<SimulatedBinaryLightImpl> simulatedBinaryLightCreator;

    @Creator.Field Creator.Entity<SimulatedDimmerLightImpl> simulatedDimmerLightCreator;

    @Creator.Field Creator.Entity<SimulatedPhotometerImpl> simulatedPhotometerCreator;

    @Creator.Field Creator.Entity<SimulatedCoolerImpl> simulatedCoolerCreator;

    @Creator.Field Creator.Entity<SimulatedHeaterImpl> simulatedHeaterCreator;

    @Creator.Field Creator.Entity<SimulatedThermometerImpl> simulatedThermometerCreator;

    @Creator.Field Creator.Entity<SimulatedPresenceSensorImpl> simulatedPresenceSensorCreator;

    @Validate
    public void start(){

    }

    @Invalidate
    public void stop(){

    }

    @Override
    public synchronized void createDevice(String deviceType, String deviceId) {
        LOG.info(" Try to create : " + deviceId + " with type " + deviceType);
        if (deviceId == null || deviceType == null){
            return;
        }
        if (checkIfSimulatedDeviceAlreadyExist(deviceId)){
            return;
        }
        Map<String,Object> entityParam = new HashMap<>();
        Creator.Entity creator = this.getCreator(deviceType);
        if (creator == null){
            return;
        }

        entityParam.put(ContextEntity.State.id(GenericDevice.class,GenericDevice.DEVICE_SERIAL_NUMBER),deviceId);
        creator.create(deviceId,entityParam);
    }

    @Override
    public synchronized void removeSimulatedDevice(String deviceId) {
        if (deviceId == null){
            return;
        }
        for (SimulatedDevice device : simulatedDevices){
            if (device.getSerialNumber() != null && device.getSerialNumber().equals(deviceId)){
                Creator.Entity creator = getCreator(device.getDeviceType());
                if (creator == null){
                    return;
                }
                creator.delete(deviceId);
            }
        }
    }

    @Override
    public Set<String> getSimulatedDeviceTypes() {
        Set<String> returnSet = new HashSet<>();
        returnSet.add(SimulatedBinaryLightImpl.SIMULATED_BINARY_LIGHT);
        returnSet.add(SimulatedDimmerLightImpl.SIMULATED_DIMMER_LIGHT);
        returnSet.add(SimulatedPhotometerImpl.SIMULATED_PHOTOMETER);
        returnSet.add(SimulatedCoolerImpl.SIMULATED_COOLER);
        returnSet.add(SimulatedHeaterImpl.SIMULATED_HEATER);
        returnSet.add(SimulatedThermometerImpl.SIMULATED_THERMOMETER);
        returnSet.add(SimulatedPresenceSensorImpl.SIMULATED_PRESENCE_SENSOR);
        return returnSet;
    }

    @Override
    public void removeAllSimulatedDevices() {
        simulatedBinaryLightCreator.deleteAll();
        simulatedDimmerLightCreator.deleteAll();
        simulatedPhotometerCreator.deleteAll();
        simulatedCoolerCreator.deleteAll();
        simulatedHeaterCreator.deleteAll();
        simulatedThermometerCreator.deleteAll();
        simulatedPresenceSensorCreator.deleteAll();
    }

    private Creator.Entity getCreator(String deviceType){
        switch (deviceType) {
            case SimulatedBinaryLightImpl.SIMULATED_BINARY_LIGHT:
                return simulatedBinaryLightCreator;
            case SimulatedDimmerLightImpl.SIMULATED_DIMMER_LIGHT:
                return simulatedDimmerLightCreator;
            case SimulatedPhotometerImpl.SIMULATED_PHOTOMETER:
                return simulatedPhotometerCreator;
            case SimulatedHeaterImpl.SIMULATED_HEATER:
                return simulatedHeaterCreator;
            case SimulatedCoolerImpl.SIMULATED_COOLER:
                return simulatedCoolerCreator;
            case SimulatedThermometerImpl.SIMULATED_THERMOMETER:
                return simulatedThermometerCreator;
            case SimulatedPresenceSensorImpl.SIMULATED_PRESENCE_SENSOR:
                return simulatedPresenceSensorCreator;
            default:return null;
        }
    }

    private boolean checkIfSimulatedDeviceAlreadyExist(String deviceId){
        if (deviceId == null){
            return true;
        }
        for (SimulatedDevice device : simulatedDevices){
            if (device.getSerialNumber() != null && device.getSerialNumber().equals(deviceId)){
                return true;
            }
        }
        return false;
    }
}
