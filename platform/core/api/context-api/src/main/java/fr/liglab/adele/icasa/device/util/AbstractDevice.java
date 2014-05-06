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
package fr.liglab.adele.icasa.device.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.device.*;
import fr.liglab.adele.icasa.location.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the {@link fr.liglab.adele.icasa.device.GenericDevice} interface that manages the
 * listeners addition, removal and notifications.
 *
 */
public abstract class AbstractDevice implements GenericDevice {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG_DEVICE);

    private final List<DeviceListener> m_listeners = new LinkedList<DeviceListener>();

	private Map<String, Object> _properties = new HashMap<String, Object>();

	public AbstractDevice() {
		setPropertyValue(GenericDevice.STATE_PROPERTY_NAME, GenericDevice.STATE_ACTIVATED);
		setPropertyValue(GenericDevice.FAULT_PROPERTY_NAME, GenericDevice.FAULT_NO);
	}

	@Override
	public Set<String> getProperties() {
		synchronized (_properties) {
			return _properties.keySet();
		}
	}

	@Override
	public Object getPropertyValue(String propertyName) {
		if (propertyName == null) {
            logger.warn("Unable to retrieve null property");
			throw new NullPointerException("Null property name");
		}
		Object value = null;
		synchronized (_properties) {
			value = _properties.get(propertyName);
		}
		return value;
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		if (propertyName == null) {
            logger.warn("Unable to set null property");
            throw new NullPointerException("Null property name");
		}
		boolean modified = false;
		boolean added = false;

        logger.trace("["+getSerialNumber()+"] Property "+ propertyName + " to be updated to " + value + "(old value = "+ ")");

        Object oldValue = null;
		synchronized (_properties) {
			
			added = !_properties.containsKey(propertyName);
						
			if (!added) {
				oldValue = _properties.get(propertyName);
				if (oldValue != null) {
					if (!oldValue.equals(value)) {
						_properties.put(propertyName, value);
						modified = true;
					}
				} else {
					if (value!=null) {
						_properties.put(propertyName, value);
						modified = true;
					}
				}
			} else {
				_properties.put(propertyName, value);
				modified = true;				
			}
		}

		if (added) {
            logger.trace("["+getSerialNumber()+"] Property "+ propertyName + " added");
			notifyListeners(new DevicePropertyEvent(this, DeviceEventType.PROP_ADDED, propertyName, oldValue, value));
        }
		if (modified) {
            logger.trace("["+getSerialNumber()+"] Property "+ propertyName + " modified");
			notifyListeners(new DevicePropertyEvent(this, DeviceEventType.PROP_MODIFIED, propertyName, oldValue, value));
        }
	}

	@Override
	public boolean removeProperty(String propertyName) {
		boolean existProperty;
		Object oldValue = null;
        synchronized (_properties) {
			existProperty = _properties.containsKey(propertyName);
			if (existProperty) {
				oldValue = _properties.remove(propertyName);
			}
		}
		if (existProperty) {
            logger.trace("["+getSerialNumber()+"] Property "+ propertyName + " removed");
			notifyListeners(new DevicePropertyEvent(this, DeviceEventType.PROP_REMOVED, propertyName, oldValue, null));
		}
		return existProperty;
	}

	@Override
	public boolean constainsProperty(String propertyName) {
		boolean existProperty;
		synchronized (_properties) {
			existProperty = _properties.containsKey(propertyName);
		}
		return existProperty;
	}

	@Override
	public boolean hasPropertyValue(String propertyName) {
		Object propertyValue;
		synchronized (_properties) {
			propertyValue = _properties.get(propertyName);
		}
		return (propertyValue != null);
	}

	@Override
	public String getState() {
		return (String) getPropertyValue(STATE_PROPERTY_NAME);
	}

	@Override
	public void setState(String state) {
		setPropertyValue(STATE_PROPERTY_NAME, state);
	}

	@Override
	public String getFault() {
		return (String) getPropertyValue(FAULT_PROPERTY_NAME);
	}

	@Override
	public void setFault(String fault) {
		setPropertyValue(FAULT_PROPERTY_NAME, fault);
	}

	@Override
	public void addListener(DeviceListener<?> listener) {
		if (listener == null) {
			return;
		}
		synchronized (m_listeners) {
			if (!m_listeners.contains(listener)) {
				m_listeners.add(listener);
			}
		}
	}

	@Override
	public void removeListener(DeviceListener<?> listener) {
		synchronized (m_listeners) {
			m_listeners.remove(listener);
		}
	}

	/**
	 * Notify all listeners. In case of exceptions, exceptions are dumped to the standard error stream.
	 */
	protected void notifyListeners(DeviceEvent event) {
		List<DeviceListener> listeners;
		// Make a snapshot of the listeners list
		synchronized (m_listeners) {
			listeners = Collections.unmodifiableList(new ArrayList<DeviceListener>(m_listeners));
		}
		// Call all listeners sequentially
		for (DeviceListener listener : listeners) {
			try {
				if (DeviceEventType.ADDED.equals(event.getType())) {
					listener.deviceAdded(event.getDevice());
					continue;
				} else if (DeviceEventType.REMOVED.equals(event.getType())) {
					listener.deviceRemoved(event.getDevice());
					continue;
				} else if (DeviceEventType.DEVICE_EVENT.equals(event.getType()) && event instanceof DeviceDataEvent) {
					DeviceDataEvent dataEvent = (DeviceDataEvent) event;
					listener.deviceEvent(dataEvent.getDevice(), dataEvent.getData());
					continue;
				} else if (DeviceEventType.PROP_ADDED.equals(event.getType()) && event instanceof DevicePropertyEvent) {
					DevicePropertyEvent devicePropertyEvent = (DevicePropertyEvent) event;
					listener.devicePropertyAdded(devicePropertyEvent.getDevice(), devicePropertyEvent.getPropertyName());
					continue;
				} else if (DeviceEventType.PROP_REMOVED.equals(event.getType()) && event instanceof DevicePropertyEvent) {
					DevicePropertyEvent devicePropertyEvent = (DevicePropertyEvent) event;
					listener.devicePropertyRemoved(devicePropertyEvent.getDevice(), devicePropertyEvent.getPropertyName());
					continue;
				} else if (DeviceEventType.PROP_MODIFIED.equals(event.getType()) && event instanceof DevicePropertyEvent) {
					DevicePropertyEvent devicePropertyEvent = (DevicePropertyEvent) event;
					listener.devicePropertyModified(devicePropertyEvent.getDevice(), devicePropertyEvent.getPropertyName(),
					      devicePropertyEvent.getOldValue(), devicePropertyEvent.getNewValue());
					continue;
				} else {
					Exception ee = new Exception("Malformed Event '" + event);
					ee.printStackTrace();
					throw ee;
				}
			} catch (Exception e) {
				Exception ee = new Exception("Exception in device listener '" + listener + "'", e);
				ee.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.device.GenericDevice#enterInZones(java.util.List)
	 */
	@Override
	public void enterInZones(List<Zone> zones) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.device.GenericDevice#leavingZones(java.util.List)
	 */
	@Override
	public void leavingZones(List<Zone> zones) {
		// do nothing
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof GenericDevice))
			return false;

		GenericDevice otherDevice = (GenericDevice) o;
		String otherSN = otherDevice.getSerialNumber();
		if (otherSN == null)
			return (getSerialNumber() == null);

		return otherSN.equals(getSerialNumber());
	}

	@Override
	public int hashCode() {
		String serialNumber = getSerialNumber();
		return serialNumber == null ? 0 : serialNumber.hashCode();
	}
}
