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
package fr.liglab.adele.m2mappbuilder.upnp.presence.detector;

import java.beans.PropertyChangeEvent;

import org.apache.felix.upnp.extra.util.UPnPEventNotifier;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import org.osgi.service.upnp.UPnPLocalStateVariable;

import fr.liglab.adele.icasa.device.DeviceListener;

/**
 *
 */
public class DetectedPresenceStateVariable implements UPnPLocalStateVariable, DeviceListener {

	final private String NAME = "DetectedPresence";
	final private Boolean DEFAULT_VALUE = Boolean.FALSE;
	private UPnPEventNotifier notifier;
	private Boolean presence = Boolean.FALSE;

	private PresenceSensor _presenceSensor;
	
	private SimulateSensorThread _simulateThread;
	private boolean _isSimulated;
	
	private class SimulateSensorThread extends Thread {
		
		private boolean sensedPresence = false;
		private boolean _stoped = false;
		
		public void run() {
			synchronized (this) {
				while (!_stoped ) {
					setPresence(sensedPresence);
					sensedPresence = !sensedPresence;

					try {
						wait(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void stopSimulation() {
			_stoped = true;
		}
	}

	public DetectedPresenceStateVariable(PresenceSensor presenceSensor, boolean isSimulated) {
		_presenceSensor = presenceSensor;
		_isSimulated = isSimulated;

		if (_presenceSensor == null) {
			if (_isSimulated) {
				_simulateThread = new SimulateSensorThread();
				_simulateThread.start();
			}
		} else {
			_presenceSensor.addListener(this);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#getName()
	 */
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#getJavaDataType()
	 */
	public Class getJavaDataType() {
		return Boolean.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#getUPnPDataType()
	 */
	public String getUPnPDataType() {
		return TYPE_BOOLEAN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#getDefaultValue()
	 */
	public Object getDefaultValue() {
		return DEFAULT_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#getAllowedValues()
	 */
	public String[] getAllowedValues() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#getMinimum()
	 */
	public Number getMinimum() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#getMaximum()
	 */
	public Number getMaximum() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#getStep()
	 */
	public Number getStep() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.upnp.UPnPStateVariable#sendsEvents()
	 */
	public boolean sendsEvents() {
		return true;
	}

	public Boolean getCurrentPresence() {
		if (_presenceSensor == null)
			return presence;
		else
			return _presenceSensor.getSensedPresence();
	}

	public void setPresence(Boolean value) {
//		System.out.println("Modified Presence " + value);
		if (!value.equals(presence)) {
			Boolean oldValue = presence;
			presence = value;
			if (notifier != null)
				notifier.propertyChange(new PropertyChangeEvent(this, NAME,
						oldValue, value));
		}
	}

	public void setNotifier(UPnPEventNotifier notifier) {
		this.notifier = notifier;
	}

	public Object getCurrentValue() {
		return getCurrentPresence();
	}

	public PresenceSensor getPresenceSensor() {
		return _presenceSensor;
	}

	public void setPresenceSensor(PresenceSensor presenceSensor) {
		if (_presenceSensor != null) {
			_presenceSensor.removeListener(this);
		} else {
			if (_isSimulated) {
				_simulateThread.stopSimulation();
				_simulateThread = null;
			}
		}
		
		this._presenceSensor = presenceSensor;
		if (_presenceSensor != null)
			_presenceSensor.addListener(this);
		else {
			if (_isSimulated) {
				_simulateThread = new SimulateSensorThread();
				_simulateThread.start();
			}
		}
	}

    public void deviceAdded(fr.liglab.adele.icasa.device.GenericDevice genericDevice) {
        //do nothing
    }

    public void deviceRemoved(fr.liglab.adele.icasa.device.GenericDevice genericDevice) {
        //do nothing
    }

    public void devicePropertyModified(fr.liglab.adele.icasa.device.GenericDevice genericDevice, java.lang.String s, java.lang.Object o) {
        setPresence(getCurrentPresence());
    }

    public void devicePropertyAdded(fr.liglab.adele.icasa.device.GenericDevice genericDevice, java.lang.String s) {
        setPresence(getCurrentPresence());
    }

    public void devicePropertyRemoved(fr.liglab.adele.icasa.device.GenericDevice genericDevice, java.lang.String s) {
        //do nothing
    }
}
