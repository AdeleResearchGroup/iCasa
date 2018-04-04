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
import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.device.power.Powermeter;

import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;

import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;

import org.apache.felix.ipojo.annotations.ServiceController;

import org.zwave4j.Manager;


@ContextEntity(coreServices = {PowerSwitch.class,Powermeter.class,Zwave4jDevice.class})

@FunctionalExtension(id="LocatedBehavior",contextServices = LocatedObject.class,implementation = LocatedObjectBehaviorProvider.class)
@FunctionalExtension(id="ZwaveBehavior",contextServices = ZwaveDevice.class,implementation = ZwaveDeviceBehaviorProvider.class)

public class FibaroWallPlug extends AbstractZwave4jDevice implements  GenericDevice, Zwave4jDevice, PowerSwitch, Powermeter {

	/**
	 * Injected Behavior
	 */
	@InjectedFunctionalExtension(id="ZwaveBehavior")
	private ZwaveDevice device;

	/**
	 * Device network status
	 */
	@ServiceController(value=true, specification=PowerSwitch.class)
	private boolean active;

	/**
	 * STATES
	 */
	
	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
	private String serialNumber;

	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	
	@ContextEntity.State.Field(service = PowerSwitch.class,state = PowerSwitch.CURRENT_STATUS, value = "false")
	private boolean status;

	@Override
	public boolean getStatus() {
		return status;
	}

	@ContextEntity.State.Push(service = PowerSwitch.class,state = PowerSwitch.CURRENT_STATUS)
	public boolean statusChanged(boolean status) {
		return status;
	}


	@ContextEntity.State.Field(service = Powermeter.class,state = Powermeter.CURRENT_RATING,value = "0.0")
	private double consumption;

	@Override
	public double getCurrentPowerRating() {
		return consumption;
	}

	@ContextEntity.State.Push(service = Powermeter.class,state =Powermeter.CURRENT_RATING )
	public double consumptionChanged(float consumption) {
		return consumption;
	}



	@Override
	public void initialize(Manager manager) {
		super.initialize(manager);
		active = isActive();
	}

	@Override
	protected void nodeStatusChanged(short status) {
		active = isActive();
	}

	protected final boolean isActive() {

		boolean listening 	= manager.isNodeListeningDevice(device.getHomeId(),(short) device.getNodeId());
		boolean awake 		= manager.isNodeAwake(device.getHomeId(),(short) device.getNodeId());
		boolean failed 		= manager.isNodeFailed(device.getHomeId(),(short) device.getNodeId());

		return (listening && !failed) || (!listening && awake);
	}

	@Override
	protected void valueChanged(ZWaveCommandClass command, short instance, short index, boolean value) {
		switch (command) {
			case SWITCH_BINARY:
				statusChanged(value);
				break;
			default:
				break;
		}
	}

	@Override
	protected void valueChanged(ZWaveCommandClass command, short instance, short index, float value) {
		switch (command) {
			case SENSOR_MULTILEVEL:
				consumptionChanged(value);
				break;
			default:
				break;
		}
	}


}
