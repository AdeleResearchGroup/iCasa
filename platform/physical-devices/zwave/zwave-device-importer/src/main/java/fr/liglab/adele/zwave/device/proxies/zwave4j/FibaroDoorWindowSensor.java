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
import fr.liglab.adele.icasa.device.doorWindow.DoorWindowSensor;
import fr.liglab.adele.icasa.device.testable.Testable;
import fr.liglab.adele.icasa.helpers.device.provider.TestableDoorWindowSensor;
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


@ContextEntity(coreServices = {DoorWindowSensor.class,Zwave4jDevice.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)
@FunctionalExtension(id="testable",contextServices = Testable.class,implementation = TestableDoorWindowSensor.class)

public class FibaroDoorWindowSensor extends AbstractZwave4jDevice implements  GenericDevice, Zwave4jDevice, DoorWindowSensor  {

	private static final Logger LOG = LoggerFactory.getLogger(FibaroWallPlug.class);

	/**
	 * Injected Behavior
	 */
	@InjectedFunctionalExtension(id="ZwaveBehavior")
	private ZwaveDevice device;

	/**
	 * Device network status
	 */
	@ServiceController(value=true, specification=DoorWindowSensor.class)
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
			case SENSOR_BINARY:
				openingDetectionChanged((Boolean)getValue(manager,valueId));
				break;
			case BATTERY:
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


	@ContextEntity.State.Field(service = DoorWindowSensor.class,state = DoorWindowSensor.DOOR_WINDOW_SENSOR_OPENING_DETECTCION,value = "false")
	private boolean status;

	@ContextEntity.State.Push(service = DoorWindowSensor.class,state =DoorWindowSensor.DOOR_WINDOW_SENSOR_OPENING_DETECTCION)
	public boolean openingDetectionChanged(boolean newStatus){
		return newStatus;
	}

	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
	private String serialNumber;



	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public boolean isOpened() {
		return status;
	}

}
