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
package fr.liglab.adele.icasa.application.mock.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;

import fr.liglab.adele.icasa.application.Application;
import fr.liglab.adele.icasa.application.ApplicationCategory;
import fr.liglab.adele.icasa.application.ApplicationManager;
import fr.liglab.adele.icasa.application.ApplicationState;
import fr.liglab.adele.icasa.application.ApplicationTracker;


/**
 * Fake Implementation of a digital service manager for demonstration purposes.
 *
 *
 */
@Component(name="application-manager-mock-impl")
@Instantiate
@Provides
public class ApplicationManagerImpl implements ApplicationManager {

	private List<ApplicationCategory> _categories = new ArrayList<ApplicationCategory>();
	
	private List<Application> _apps = new ArrayList<Application>();
	
	private List<ApplicationTracker> _listeners = new ArrayList<ApplicationTracker>();
	
	public ApplicationManagerImpl() {
		final ApplicationCategoryImpl humanSafeCateg = new ApplicationCategoryImpl("Human Safety");
		_categories.add(humanSafeCateg);
		_categories.add(new ApplicationCategoryImpl("Material Safety"));
		final ApplicationCategoryImpl humanConfortCateg = new ApplicationCategoryImpl("Human Confort");
		_categories.add(humanConfortCateg);
		_categories.add(new ApplicationCategoryImpl("Energy Efficiency"));
		_categories.add(new ApplicationCategoryImpl("Material Durability"));
		final ApplicationCategoryImpl devToolCateg = new ApplicationCategoryImpl("Development Tool");
		_categories.add(devToolCateg);
		
		_apps.add(new ApplicationImpl("Safe At Home", "Orange", humanConfortCateg, ApplicationState.STARTED));
		_apps.add(new ApplicationImpl("Health Care", "Orange", humanSafeCateg));
		_apps.add(new ApplicationImpl("Music Follow Me", "LIG/ADELE", humanConfortCateg));
		//_apps.add(new ApplicationImpl("Digital Home Simulator", "LIG/ADELE", devToolCateg, ApplicationState.STARTED));
	}
	
	@Override
	public List<ApplicationCategory> getCategories() {
		return Collections.unmodifiableList(_categories);
	}

	@Override
	public List<Application> getApplications() {
		return Collections.unmodifiableList(_apps);
	}

	@Override
	public Application getApplication(String appId) {
		for (Application curApplication : _apps) {
			if (curApplication.getId().equals(appId))
				return curApplication;
		}
		
		return null;
	}

	@Override
	public void addApplicationListener(ApplicationTracker listener) {
		synchronized(_listeners) {
			_listeners.add(listener);
			notifyCurrentApplicationSet(listener);
		}
	}

	@Override
	public void removeApplicationListener(ApplicationTracker listener) {
		synchronized(_listeners) {
			_listeners.remove(listener);
			notifyStop(listener);
		}
	}
	
	@Validate
	public void start() {
		synchronized (_listeners) {
			for (ApplicationTracker listener : _listeners) {
				notifyCurrentApplicationSet(listener);
			}
		}
	}

	private void notifyCurrentApplicationSet(ApplicationTracker listener) {
		for (Application app : getApplications()) {
			listener.addApplication(app);
		}
	}

	@Invalidate
	public void stop() {
		synchronized (_listeners) {
			for (ApplicationTracker listener : _listeners) {
				notifyStop(listener);
			}
		}
	}

	private void notifyStop(ApplicationTracker listener) {
		for (Application app : getApplications()) {
			listener.removeApplication(app);
		}
	}

	@Override
	public Application getApplicationOfBundle(String bundleSymbolicName) {
		return null;
	}

	@Override
	public boolean isApplicationBundle(Bundle bundle) {
		return false;
	}
}
