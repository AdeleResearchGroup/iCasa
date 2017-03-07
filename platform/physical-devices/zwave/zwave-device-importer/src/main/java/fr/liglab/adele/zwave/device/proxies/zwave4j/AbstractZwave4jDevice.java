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

import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.ValueId;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractZwave4jDevice implements Zwave4jDevice {


	protected void valueChanged(Manager manager, ValueId valueId){

	}

	protected void nodeStatusChanged(Manager manager, short status){

	}

	protected void nodeEvent(Manager manager, short value){

	}

	@Override
	public void notification(Manager manager, Notification notification) {
		switch (notification.getType()) {
		case NOTIFICATION:
			nodeStatusChanged(manager, notification.getByte());
			break;
		case VALUE_CHANGED:
			valueChanged(manager, notification.getValueId());
			break;
			case NODE_EVENT:
				nodeEvent(manager,notification.getEvent());
				break;
		default:
			break;
		}

	}

	protected final Object getValue(Manager manager, ValueId valueId) {
		switch (valueId.getType()) {
		case BOOL:
			AtomicReference<Boolean> b = new AtomicReference<>();
			manager.getValueAsBool(valueId, b);
			return b.get();
		case BYTE:
			AtomicReference<Short> bb = new AtomicReference<>();
			manager.getValueAsByte(valueId, bb);
			return bb.get();
		case DECIMAL:
			AtomicReference<Float> f = new AtomicReference<>();
			manager.getValueAsFloat(valueId, f);
			return f.get();
		case INT:
			AtomicReference<Integer> i = new AtomicReference<>();
			manager.getValueAsInt(valueId, i);
			return i.get();
		case LIST:
			return null;
		case SCHEDULE:
			return null;
		case SHORT:
			AtomicReference<Short> s = new AtomicReference<>();
			manager.getValueAsShort(valueId, s);
			return s.get();
		case STRING:
			AtomicReference<String> ss = new AtomicReference<>();
			manager.getValueAsString(valueId, ss);
			return ss.get();
		case BUTTON:
			return null;
		case RAW:
			AtomicReference<short[]> sss = new AtomicReference<>();
			manager.getValueAsRaw(valueId, sss);
			return sss.get();
		default:
			return null;
		}
	}

}