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
package fr.liglab.adele.icasa.simulator.device.temperature.impl;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;


/**
 * Implementation of a simulated cooler device.
 *
 */
@ContextEntity(coreServices = {Cooler.class,SimulatedDevice.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
public class SimulatedCoolerImpl implements Cooler, SimulatedDevice,GenericDevice {

    public final static String SIMULATED_COOLER = "iCasa.Cooler";

    @ContextEntity.State.Field(service = Cooler.class,state = COOLER_POWER_LEVEL,directAccess = true,value ="0")
    private double powerLevel;

    @ContextEntity.State.Field(service = SimulatedDevice.class,state = SIMULATED_DEVICE_TYPE,value = SIMULATED_COOLER)
    private String deviceType;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @Override
    public double getPowerLevel() {
        return powerLevel;
    }

    @Override
    public void setPowerLevel(double level) {
        if (level < 0.0d || level > 1.0d || Double.isNaN(level)){
            throw new IllegalArgumentException("Invalid power level : " + level);
        }
        powerLevel = level;
    }

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

}
