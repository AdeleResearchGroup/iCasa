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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.ValueId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;



public abstract class AbstractZwave4jDevice implements Zwave4jDevice {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractZwave4jDevice.class);

	protected Manager manager;
	
	@Override
	public void initialize(Manager manager) {
		this.manager = manager;
	} 

	@Override
	public void notification(Manager manager, Notification notification) {
		
		switch (notification.getType()) {
			case NOTIFICATION:
				nodeStatusChanged(notification.getByte());
				break;
			case VALUE_CHANGED:
				valueChanged(notification.getValueId());
				break;
			case NODE_EVENT:
				nodeEvent(notification.getEvent());
				break;
		default:
			break;
		}

	}

	protected void nodeStatusChanged(short status) {}

	protected void nodeEvent(short event) {}

	private void valueChanged(ValueId value) {

		ZWaveCommandClass command 	= ZWaveCommandClass.valueOf(value.getCommandClassId());
		short instance				= value.getInstance();
		short index					= value.getIndex();
		
		switch (value.getType()) {
			case BOOL: {
				AtomicReference<Boolean> reference = new AtomicReference<>();
				manager.getValueAsBool(value, reference);
				LOG.debug("Value changed = "+command+" instance "+instance+" index "+index+" type "+value.getType() + " value "+reference.get());
				valueChanged(command,instance,index,reference.get());
				break;
			}
			case INT: {
				AtomicReference<Integer> reference = new AtomicReference<>();
				manager.getValueAsInt(value, reference);
				LOG.debug("Value changed = "+command+" instance "+instance+" index "+index+" type "+value.getType() + " value "+reference.get());
				valueChanged(command,instance,index,reference.get());
				break;
			}
			case BYTE: {
				AtomicReference<Short> reference = new AtomicReference<>();
				manager.getValueAsByte(value, reference);
				LOG.debug("Value changed = "+command+" instance "+instance+" index "+index+" type "+value.getType() + " value "+reference.get());
				valueChanged(command,instance,index,(byte) reference.get().shortValue());
				break;
			}
			case SHORT: {
				AtomicReference<Short> reference = new AtomicReference<>();
				manager.getValueAsShort(value, reference);
				LOG.debug("Value changed = "+command+" instance "+instance+" index "+index+" type "+value.getType() + " value "+reference.get());
				valueChanged(command,instance,index,reference.get());
				break;
			}
			case DECIMAL: {
				AtomicReference<Float> reference = new AtomicReference<>();
				manager.getValueAsFloat(value, reference);
				LOG.debug("Value changed = "+command+" instance "+instance+" index "+index+" type "+value.getType() + " value "+reference.get());
				valueChanged(command,instance,index,reference.get());
				break;
			}
			case STRING: { 
				AtomicReference<String> reference = new AtomicReference<>();
				manager.getValueAsString(value, reference);
				LOG.debug("Value changed = "+command+" instance "+instance+" index "+index+" type "+value.getType() + " value "+reference.get());
				valueChanged(command,instance,index,reference.get());
				break;
			}
			case LIST: {
				List<String> reference = new ArrayList<>();
				manager.getValueListItems(value, reference);
				LOG.debug("Value changed = "+command+" instance "+instance+" index "+index+" type "+value.getType() + " value "+reference);
				valueChanged(command,instance,index,reference);
				break;
			}
			case SCHEDULE:
			case BUTTON:
			case RAW:
			default: {
				AtomicReference<short[]> reference = new AtomicReference<>();
				manager.getValueAsRaw(value, reference);
				LOG.debug("Value changed = "+command+" instance "+instance+" index "+index+" type "+value.getType() + " value "+reference.get());
				valueChanged(command,instance,index,reference.get());
				break;
			}
		}
	}
	
	protected void valueChanged(ZWaveCommandClass command, short instance, short index, boolean value) {
	}

	protected void valueChanged(ZWaveCommandClass command, short instance, short index, int value) {
	}

	protected void valueChanged(ZWaveCommandClass command, short instance, short index, byte value) {
	}

	protected void valueChanged(ZWaveCommandClass command, short instance, short index, short value) {
	}

	protected void valueChanged(ZWaveCommandClass command, short instance, short index, float value) {
	}

	protected void valueChanged(ZWaveCommandClass command, short instance, short index, String value) {
	}

	protected void valueChanged(ZWaveCommandClass command, short instance, short index, List<String> value) {
	}

	protected void valueChanged(ZWaveCommandClass command, short instance, short index, short[] value) {
	}

	
}