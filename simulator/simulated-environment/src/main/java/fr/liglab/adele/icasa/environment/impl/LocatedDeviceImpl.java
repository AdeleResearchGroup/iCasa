/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.environment.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.liglab.adele.icasa.environment.LocatedDevice;
import fr.liglab.adele.icasa.environment.LocatedDeviceListener;
import fr.liglab.adele.icasa.environment.Position;

public class LocatedDeviceImpl implements LocatedDevice {

	private String m_serialNumber;

	private Map<String, Object> properties = new HashMap<String, Object>();

	private final List<LocatedDeviceListener> listeners = new ArrayList<LocatedDeviceListener>();
	
	private Position m_position;

	public LocatedDeviceImpl(String serialNumber, Position position) {
		m_serialNumber = serialNumber;
		if (position!=null)
			m_position = position.clone();
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public Set<String> getProperties() {
		synchronized (properties) {
			return properties.keySet();
		}
	}

	@Override
	public Object getPropertyValue(String propertyName) {
		if (propertyName == null) {
			throw new NullPointerException("Null property name");
		}
		synchronized (properties) {
			return properties.get(propertyName);
		}
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		if (propertyName == null) {
			throw new NullPointerException("Null property name");
		}
		synchronized (properties) {
			properties.put(propertyName, value);
		}
	}

	@Override
	public synchronized void addListener(final LocatedDeviceListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public synchronized void removeListener(final LocatedDeviceListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
   public Position getAbsolutePosition() {
	   return m_position.clone();
   }

	@Override
   public void setAbsolutePosition(Position position) {
		Position oldPosition = m_position.clone();
		m_position = position.clone();
				
		// Listeners notification
		for (LocatedDeviceListener listener : listeners) {
			listener.deviceMoved(this, oldPosition);
		}	   
   }
	
	@Override
	public String toString() {
		return "Id: " + getSerialNumber() + " - Position: " + m_position;
	}

}
