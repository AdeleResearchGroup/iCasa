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

import java.util.function.Supplier;

import javax.measure.Quantity;
import javax.measure.quantity.Illuminance;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;

import fr.liglab.adele.icasa.device.GenericDevice;

import fr.liglab.adele.icasa.simulator.device.SimulatedDevice;

import fr.liglab.adele.icasa.device.light.Photometer;

import fr.liglab.adele.icasa.simulator.model.api.LuminosityModel;

/**
 * Implementation of a simulated photometer device.
 *
 */
@ContextEntity(coreServices = {Photometer.class, SimulatedDevice.class, GenericDevice.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)

public class SimulatedPhotometerImpl implements Photometer, SimulatedDevice, GenericDevice {

    public final static String SIMULATED_PHOTOMETER = "iCasa.Photometer";


    @ContextEntity.State.Field(service=SimulatedDevice.class, state=SIMULATED_DEVICE_TYPE, value=SIMULATED_PHOTOMETER)
    private String deviceType;

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @ContextEntity.State.Field(service=GenericDevice.class, state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;


    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * IMPORTANT NOTE : this requirement is marked optional as the device is not always necessarily attached to a zone. 
     * The measured value when the device is outside the zone is undefined.
     * 
     */
    @Requires(id ="Model", filter="(luminositymodel.zone.attached=${locatedobject.object.zone})", optional=true, nullable=false, proxy=false)
    private LuminosityModel model;

    private final static Quantity<Illuminance> UNDEFINED =  Quantities.getQuantity(Double.NaN,Units.LUX);

    private static Quantity<Illuminance> sensed(LuminosityModel model) {
    	return model != null ? Quantities.getQuantity(model.getCurrentLuminosity(), Units.LUX) : UNDEFINED;
    }

    @Bind(id ="Model")
    private void modelBound() {
    	push(sensed(model));
    }

    @Modified(id = "Model")
    private void modelUpdated() {
    	push(sensed(model));
    }

    @Unbind(id = "Model")
    private void modelUnbound() {
    	push(sensed(null));
    }

    @ContextEntity.State.Field(service=Photometer.class, state=PHOTOMETER_CURRENT_ILLUMINANCE)
    private Quantity<Illuminance> currentSensedIlluminance;

    @ContextEntity.State.Pull(service=Photometer.class, state=PHOTOMETER_CURRENT_ILLUMINANCE)
    private Supplier<Quantity<Illuminance> > pull = this::pull;
    
    private Quantity<Illuminance> pull() {
    	return sensed(model);
    }

    @ContextEntity.State.Push(service=Photometer.class, state=Photometer.PHOTOMETER_CURRENT_ILLUMINANCE)
    public Quantity<Illuminance> push(Quantity<Illuminance> sensed) {
        return sensed; 
    }

    @Override
    public Quantity<Illuminance> getIlluminance() {
        return currentSensedIlluminance;
    }


}
