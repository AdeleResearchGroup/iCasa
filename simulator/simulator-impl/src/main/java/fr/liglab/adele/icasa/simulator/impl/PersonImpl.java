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
package fr.liglab.adele.icasa.simulator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.impl.LocatedObjectImpl;
import fr.liglab.adele.icasa.simulator.Person;
import fr.liglab.adele.icasa.simulator.PersonType;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import fr.liglab.adele.icasa.simulator.listener.PersonListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 */
public class PersonImpl extends LocatedObjectImpl implements Person {

	private String m_name;

	private PersonType personType;

	private List<PersonListener> listeners = new ArrayList<PersonListener>();

	ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private SimulationManager manager;

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG);

	public PersonImpl(String name, Position position, PersonType personType, SimulationManager manager) {
		super(position);
		m_name = name;
		this.personType = personType;
		this.manager = manager;
	}

	@Override
	public String getName() {
		lock.readLock().lock();
		try {
			return m_name;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String getLocation() {
		Zone zone = manager.getZoneFromPosition(getCenterAbsolutePosition());
		if (zone != null)
			return zone.getId();
		return "unknown";
	}

	@Override
	public void addListener(PersonListener listener) {
		lock.writeLock().lock();
		try {
			listeners.add(listener);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void removeListener(PersonListener listener) {
		lock.writeLock().lock();
		try {
			listeners.remove(listener);
		} finally {
			lock.writeLock().unlock();
		}

	}

	@Override
	public void setName(String name) {
		lock.writeLock().lock();
		m_name = name;
		lock.writeLock().unlock();
	}

	@Override
	public void setCenterAbsolutePosition(Position position) {
		Position oldPosition = getCenterAbsolutePosition();
		super.setCenterAbsolutePosition(position);

		// Listeners notification
		List<PersonListener> snapshotListeners = getListeners();
		for (PersonListener listener : snapshotListeners) {
            try{
			    listener.personMoved(this, oldPosition);
            }catch(Exception ex){
                 logger.error("Exception in person listener when moving person", ex);
            }
		}
	}

	@Override
	protected void notifyAttachedObject(LocatedObject attachedObject) {
		LocatedDevice device;

		if (attachedObject instanceof LocatedDevice) {
			device = (LocatedDevice) attachedObject;
		} else {
			return; // nothing to notify.
		}
		List<PersonListener> snapshotListeners = getListeners();
		for (PersonListener listener : snapshotListeners) {
            try{
			    listener.personDeviceAttached(this, device);
            }catch(Exception ex){
                logger.error("Exception in person listener when notify attached object", ex);
            }
		}
	}

	@Override
	protected void notifyDetachedObject(LocatedObject attachedObject) {
		LocatedDevice device;

		if (attachedObject instanceof LocatedDevice) {
			device = (LocatedDevice) attachedObject;
		} else {
			return; // nothing to notify.
		}
		List<PersonListener> snapshotListeners = getListeners();
		for (PersonListener listener : snapshotListeners) {
            try{
			    listener.personDeviceDetached(this, device);
            }catch(Exception ex){
                logger.error("Exception in person listener when detaching object", ex);
            }
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return "Person: " + m_name + " - Position: " + getCenterAbsolutePosition() + " - Type: " + getPersonType().getName();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public PersonType getPersonType() {
		lock.readLock().lock();
		try {
			return personType;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void setPersonType(PersonType personType) {
		lock.writeLock().lock();
		this.personType = personType;
		lock.writeLock().unlock();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PersonImpl person = (PersonImpl) o;
		lock.readLock().lock();// this lock
		person.lock.readLock().lock();// take the person read lock
		try {
			if (!m_name.equals(person.m_name))
				return false;
			if (!personType.equals(person.personType))
				return false;
		} finally {
			person.lock.readLock().unlock();// free the person read lock.
			lock.readLock().unlock();// this lock
		}
		return true;
	}

	@Override
	public int hashCode() {
		lock.readLock().lock();
		int result = m_name.hashCode();
		result = 31 * result + personType.hashCode();
		lock.readLock().unlock();
		return result;
	}

	public List<PersonListener> getListeners() {
		lock.readLock().lock();
		try {
			return new ArrayList<PersonListener>(listeners);
		} finally {
			lock.readLock().unlock();
		}
	}
}
