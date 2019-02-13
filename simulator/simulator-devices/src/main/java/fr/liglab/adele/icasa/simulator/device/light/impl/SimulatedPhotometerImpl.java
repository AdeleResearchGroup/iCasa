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
package fr.liglab.adele.icasa.simulator.device.light.impl;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;
import fr.liglab.adele.icasa.simulator.model.api.LuminosityModel;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Illuminance;

/**
 * Implementation of a simulated photometer device.
 *
 */
@ContextEntity(coreServices = {Photometer.class, SimulatedDevice.class})
@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
public class SimulatedPhotometerImpl implements Photometer, SimulatedDevice,GenericDevice {

    public final static String SIMULATED_PHOTOMETER = "iCasa.Photometer";


    @ContextEntity.State.Field(service=SimulatedDevice.class, state=SIMULATED_DEVICE_TYPE, value=SIMULATED_PHOTOMETER)
    private String deviceType;

    @ContextEntity.State.Field(service=GenericDevice.class, state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @ContextEntity.State.Field(service=Photometer.class, state=PHOTOMETER_CURRENT_ILLUMINANCE)
    private Quantity<Illuminance> currentSensedIlluminance;

    @ContextEntity.State.Push(service=Photometer.class, state=Photometer.PHOTOMETER_CURRENT_ILLUMINANCE)
    public Quantity<Illuminance> push(double illuminance) {
        return Quantities.getQuantity(illuminance, Units.LUX);
    }

    @Override
    public Quantity<Illuminance> getIlluminance() {
        return currentSensedIlluminance;
    }

    /**
     * IMPORTANT NOTE : this requirement is marked optional as the device is not always necessarily attached to a zone. 
     * The measured value when the device is outside the zone is undefined.
     * 
     */
    @Requires(id ="Model", filter="(luminositymodel.zone.attached=${locatedobject.object.zone})", optional=true)
    LuminosityModel model;
    
    @Bind(id ="Model")
    public void modelBound() {
    	push(model.getCurrentLuminosity());
    }

    @Modified(id = "Model")
    public void modelUpdated() {
    	push(model.getCurrentLuminosity());
    }

    @Unbind(id = "Model")
    public void modelUnbound() {
    	push(FAULT_VALUE);
    }


}
