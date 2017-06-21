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
package fr.liglab.adele.zwave.device.proxies.zwave4j;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.InjectedFunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.ValueId;


@ContextEntity(coreServices = {Zwave4jDevice.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)

public class AeonRepeaterSlave extends AbstractZwave4jDevice implements  GenericDevice, Zwave4jDevice {

	private static final Logger LOG = LoggerFactory.getLogger(AeonRepeaterSlave.class);


    /**
     * Injected Behavior
     */
    @InjectedFunctionalExtension(id="ZwaveBehavior")
    private ZwaveDevice device;


	@Override
	public void initialize(Manager manager) {
		//Do nothing
	}

	@Override
	public void notification(Manager manager, Notification notification) {
		super.notification(manager, notification);
	}
	
	@Override
	protected void nodeStatusChanged(Manager manager, short status) {
		//Do nothing
	}

	@Override
	protected void valueChanged(Manager manager, ValueId valueId) {
		ZWaveCommandClass command = ZWaveCommandClass.valueOf(valueId.getCommandClassId());
		LOG.debug("Value changed = "+command+" instance "+valueId.getInstance()+" index "+valueId.getIndex()+" type "+valueId.getType());
		
	}

	/**
     * STATES
     */
    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

 
}
