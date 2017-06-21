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
import fr.liglab.adele.icasa.device.power.SmartPlug;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.ValueId;


@ContextEntity(coreServices = {SmartPlug.class,Zwave4jDevice.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)

public class FibaroWallPlug extends AbstractZwave4jDevice implements  GenericDevice, Zwave4jDevice, SmartPlug  {

	private static final Logger LOG = LoggerFactory.getLogger(FibaroWallPlug.class);


	/**
	 * Injected Behavior
	 */
	@InjectedFunctionalExtension(id="ZwaveBehavior")
	private ZwaveDevice device;

	/**
	 * Device network status
	 */
	@ServiceController(value=true, specification=SmartPlug.class)
	private boolean active;


	@Override
	public void initialize(Manager manager) {
		active = isActive(manager);
	}

	@Override
	public void notification(Manager manager, Notification notification) {
		super.notification(manager, notification);
	}

	@Override
	protected void nodeStatusChanged(Manager manager, short status) {
		active = isActive(manager);
	}

	@Override
	protected void valueChanged(Manager manager, ValueId valueId) {
		ZWaveCommandClass command = ZWaveCommandClass.valueOf(valueId.getCommandClassId());
		LOG.debug("Value changed = "+command+" instance "+valueId.getInstance()+" index "+valueId.getIndex()+" type "+valueId.getType());

		switch (command) {
			case SWITCH_BINARY:
				statusChange((Boolean)getValue(manager,valueId));
				break;
			case SENSOR_MULTILEVEL:
				consumptionChange((Float)getValue(manager,valueId));
				break;
			case POWERLEVEL:
				break;
			case METER:
				break;
			default:
				break;
		}

	}

	protected final boolean isActive(Manager manager) {

		boolean listening = manager.isNodeListeningDevice(device.getHomeId(),(short) device.getNodeId());
		boolean awake = manager.isNodeAwake(device.getHomeId(),(short) device.getNodeId());
		boolean failed = manager.isNodeFailed(device.getHomeId(),(short) device.getNodeId());

		return (listening && !failed) || (!listening && awake);
	}


	/**
	 * STATES
	 */
	@ContextEntity.State.Field(service = SmartPlug.class,state = SmartPlug.SMART_PLUG_STATUS,value = "false")
	private boolean status;

	@ContextEntity.State.Push(service = SmartPlug.class,state =SmartPlug.SMART_PLUG_STATUS )
	public boolean statusChange(boolean newStatus){
		return newStatus;
	}

	@ContextEntity.State.Field(service = SmartPlug.class,state = SmartPlug.SMART_PLUG_CONSUMPTION,value = "0.0")
	private float consumption;
	@ContextEntity.State.Push(service = SmartPlug.class,state =SmartPlug.SMART_PLUG_CONSUMPTION )
	public float consumptionChange(float newConso){
		return newConso;
	}

	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
	private String serialNumber;


	@Override
	public boolean isOn() {
		return status;
	}

	@Override
	public float currentConsumption() {
		return consumption;
	}

	@Override
	public String getSerialNumber() {
		return serialNumber;
	}


}
