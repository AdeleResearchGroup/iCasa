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
package fr.liglab.adele.icasa.simulator.context.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.felix.ipojo.ContextListener;
import org.apache.felix.ipojo.ContextSource;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.simulator.Person;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import fr.liglab.adele.icasa.simulator.listener.PersonListener;

/**
 * 
 * This class feeds the iPOJO context source "registry" with person location information. Properties of type
 * "person.$name.location" will be added to the context when persons are created. By example, if a person having "paul" as
 * name is created and placed in the kitchen location, the property "person.paul.location" is created and its value set
 * to kitchen.
 *
 * 
 */
@Component(name = "ICasaContextSourceBuilder")
@Provides
@Instantiate
public class ICasaContextSourceBuilder implements ContextSource {

	@Requires
	private SimulationManager manager;

	private PersonListener personListener;

	Hashtable<String, String> personLocations = new Hashtable<String, String>();

	Hashtable<String, List<ContextListener>> listeners = new Hashtable<String, List<ContextListener>>();

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	public ICasaContextSourceBuilder(BundleContext context) {
		personListener = new IcasaContextSourcePersonListener();
	}

	public Object getProperty(String property) {
		if (isPersonProperty(property))
			return personLocations.get(property);
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Dictionary getContext() {
		return personLocations;
	}

	public void registerContextListener(ContextListener listener, String[] properties) {
		lock.writeLock().lock();
		for (String property : properties) {
			if (isPersonProperty(property)) {
				List<ContextListener> listenersList = listeners.get(property);
				if (listenersList == null) {
					listenersList = new ArrayList<ContextListener>();
					listeners.put(property, listenersList);
				}
				listenersList.add(listener);
			}
		}
		lock.writeLock().unlock();
	}

	public void unregisterContextListener(ContextListener listener) {
		lock.writeLock().lock();
		Iterator<List<ContextListener>> it = listeners.values().iterator();
		while (it.hasNext()) {
			List<ContextListener> listenersList = it.next();
			listenersList.remove(listener);
		}
		lock.writeLock().unlock();
	}

	@Validate
	protected void start() {
		manager.addListener(personListener);
	}

	protected void stop() {
		manager.removeListener(personListener);
	}

	private void notifyListeners(String property, String value) {

		ArrayList<ContextListener> copyList = new ArrayList<ContextListener>();
		lock.readLock().lock();
		try {
			List<ContextListener> tempCopy = listeners.get(property);
			if (tempCopy != null)
				copyList = new ArrayList<ContextListener>(tempCopy);
		} finally {
			lock.readLock().unlock();
		}

		// Null values must be notified ? How to notify elimination of the property?
		for (ContextListener contextListener : copyList) {
			contextListener.update(this, property, value);
		}
	}

	private boolean isPersonProperty(String property) {
		if (property.startsWith("person.")) {
			String[] parts = property.split("\\.");
			if (parts.length == 3)
				if (parts[2].equals("location"))
					return true;
		}
		return false;
	}

	class IcasaContextSourcePersonListener implements PersonListener {

		@Override
		public void personRemoved(Person person) {
			String key = getKey(person);

			lock.writeLock().lock();
			personLocations.remove(key);
			lock.writeLock().unlock();

			notifyListeners(key, null);
		}

		@Override
		public void personAdded(Person person) {
			String key = getKey(person);
			String location = person.getLocation();

			lock.writeLock().lock();
			personLocations.put(key, location);
			lock.writeLock().unlock();

			notifyListeners(key, location);
		}

		@Override
		public void personMoved(Person person, Position position) {
			String key = getKey(person);
			String location = person.getLocation();

			lock.writeLock().lock();
			personLocations.put(key, location);
			lock.writeLock().unlock();

			notifyListeners(key, location);
		}

		private String getKey(Person person) {
			return "person." + person.getName() + ".location";
		}

		@Override
		public void personDeviceAttached(Person person, LocatedDevice device) {
			// Nothing to be done!

		}

		@Override
		public void personDeviceDetached(Person person, LocatedDevice device) {
			// Nothing to be done!
		}
	}

}
