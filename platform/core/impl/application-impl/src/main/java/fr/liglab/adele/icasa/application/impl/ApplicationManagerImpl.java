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
package fr.liglab.adele.icasa.application.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fr.liglab.adele.icasa.Constants;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;

import fr.liglab.adele.icasa.application.Application;
import fr.liglab.adele.icasa.application.ApplicationCategory;
import fr.liglab.adele.icasa.application.ApplicationManager;
import fr.liglab.adele.icasa.application.ApplicationState;
import fr.liglab.adele.icasa.application.ApplicationTracker;
import fr.liglab.adele.icasa.application.impl.internal.DeploymentPackageRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an application manager.
 *
 */
@Component(name = "Application-Manager-Impl")
@Instantiate(name = "Application-Manager-Impl-1")
@Provides(specifications = { ApplicationManager.class, EventHandler.class }, properties = { @StaticServiceProperty(name = EventConstants.EVENT_TOPIC, type = "java.lang.String[]", value = "{org/osgi/service/deployment/COMPLETE}") })
public class ApplicationManagerImpl implements ApplicationManager, EventHandler {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + ".applications");

    @Requires
    DeploymentAdmin deploymentAdmin;

    /**
     * Platform Categories list
     */
    private List<ApplicationCategory> _categories = new ArrayList<ApplicationCategory>();

    /**
     * List of application trackers
     */
    private List<ApplicationTracker> _listeners = new ArrayList<ApplicationTracker>();

    /**
     * Map keeping the record of applications
     */
    private Map<String, ApplicationImpl> _appPerId = new HashMap<String, ApplicationImpl>();

    private ApplicationCategoryImpl _undefinedCateg;

    private BundleContext _context;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private Lock readLock = lock.readLock();

    private Lock writeLock = lock.writeLock();

    public ApplicationManagerImpl(BundleContext context) {
        _context = context;

        final ApplicationCategoryImpl humanSafeCateg = new ApplicationCategoryImpl("Human Safety");
        _categories.add(humanSafeCateg);
        _categories.add(new ApplicationCategoryImpl("Material Safety"));
        final ApplicationCategoryImpl humanConfortCateg = new ApplicationCategoryImpl("Human Confort");
        _categories.add(humanConfortCateg);
        _categories.add(new ApplicationCategoryImpl("Energy Efficiency"));
        _categories.add(new ApplicationCategoryImpl("Material Durability"));
        _undefinedCateg = new ApplicationCategoryImpl("Undefined");
        _categories.add(_undefinedCateg);
    }

    @Override
    public List<ApplicationCategory> getCategories() {
        return Collections.unmodifiableList(_categories);
    }

    @Override
    public List<Application> getApplications() {
        readLock.lock();
        Collection<ApplicationImpl> appsCollection = _appPerId.values();
        readLock.unlock();
        return Collections.unmodifiableList(new ArrayList<Application>(appsCollection));
    }

    @Override
    public Application getApplication(String appId) {
        readLock.lock();
        Application app = _appPerId.get(appId);
        readLock.unlock();
        return app;
    }

    @Override
    public void addApplicationListener(ApplicationTracker listener) {
        writeLock.lock();
        _listeners.add(listener);
        writeLock.unlock();
        notifyCurrentApplicationSet(listener);
    }

    @Override
    public void removeApplicationListener(ApplicationTracker listener) {
        synchronized (_listeners) {
            _listeners.remove(listener);
            notifyStop(listener);
        }
    }

    private void notifyDeploymentPackagedAdded(Application app, DeploymentPackageRepresentation dpRepresentation) {
        readLock.lock();
        List<ApplicationTracker> listenersCopy = Collections.unmodifiableList(_listeners);
        readLock.unlock();

        for (ApplicationTracker listener : listenersCopy) {
            listener.deploymentPackageAdded(app, dpRepresentation.getName());
            for (String bundleId : dpRepresentation.getBundleIds()) {
                listener.bundleAdded(app, bundleId);
            }
        }
    }

    private void notifyDeploymentPackagedRemoved(Application app, DeploymentPackageRepresentation dpRepresentation) {
        readLock.lock();
        List<ApplicationTracker> listenersCopy = Collections.unmodifiableList(_listeners);
        readLock.unlock();

        for (ApplicationTracker listener : listenersCopy) {
            listener.deploymentPackageRemoved(app, dpRepresentation.getName());
            for (String bundleId : dpRepresentation.getBundleIds()) {
                listener.bundleRemoved(app, bundleId);
            }
        }
    }

    private void notifyApplicationAdded(Application app) {
        readLock.lock();
        List<ApplicationTracker> listenersCopy = Collections.unmodifiableList(_listeners);
        readLock.unlock();

        for (ApplicationTracker listener : listenersCopy) {
            listener.addApplication(app);
        }
    }

    private void notifyApplicationRemoved(Application app) {
        readLock.lock();
        List<ApplicationTracker> listenersCopy = Collections.unmodifiableList(_listeners);
        readLock.unlock();

        for (ApplicationTracker listener : listenersCopy) {
            listener.removeApplication(app);
        }
    }

    private void notifyCurrentApplicationSet(ApplicationTracker listener) {
        for (Application app : getApplications()) {
            listener.addApplication(app);
            ApplicationImpl tempApp = (ApplicationImpl) app;
            List<DeploymentPackageRepresentation> dps = tempApp.getAllDeploymentPackageRepresentations();
            for (DeploymentPackageRepresentation deploymentPackageRepresentation : dps) {
                listener.deploymentPackageAdded(tempApp, deploymentPackageRepresentation.getName());
                
                Set<String> bundlesIds = deploymentPackageRepresentation.getBundleIds();
                for (String bundleId : bundlesIds) {
                    listener.bundleAdded(tempApp, bundleId);
                }
            }                      
        }
    }

    private void notifyStop(ApplicationTracker listener) {
        for (Application app : getApplications()) {
            listener.removeApplication(app);
            
            ApplicationImpl tempApp = (ApplicationImpl) app;
            List<DeploymentPackageRepresentation> dps = tempApp.getAllDeploymentPackageRepresentations();
            for (DeploymentPackageRepresentation deploymentPackageRepresentation : dps) {
                listener.deploymentPackageRemoved(tempApp, deploymentPackageRepresentation.getName());
                
                Set<String> bundlesIds = deploymentPackageRepresentation.getBundleIds();
                for (String bundleId : bundlesIds) {
                    listener.bundleRemoved(tempApp, bundleId);
                }
            }              
        }
    }

    @Validate
    public void start() {
        logger.debug("start");
        DeploymentPackage[] packages = deploymentAdmin.listDeploymentPackages();

        // Installing the deployment packages presents in the platform
        for (DeploymentPackage deploymentPackage : packages) {
            onDeploymePackageArrival(deploymentPackage);
        }

        readLock.lock();
        List<ApplicationTracker> listenersCopy = Collections.unmodifiableList(_listeners);
        readLock.unlock();

        for (ApplicationTracker listener : listenersCopy) {
            notifyCurrentApplicationSet(listener);
        }

    }

    @Invalidate
    public void stop() {
        logger.debug("stop");

        readLock.lock();
        List<ApplicationTracker> listenersCopy = Collections.unmodifiableList(_listeners);
        readLock.unlock();

        for (ApplicationTracker listener : listenersCopy) {
            notifyStop(listener);
        }

        writeLock.lock();
        _appPerId.clear();
        writeLock.unlock();
    }

    @Override
    public Application getApplicationOfBundle(String bundleSymbolicName) {
        Application resultApp = null;
        readLock.lock();
        for (ApplicationImpl app : _appPerId.values()) {
            if (app.constainsBundle(bundleSymbolicName)) {
                resultApp = app;
                break;
            }
        }
        readLock.unlock();
        return resultApp;
    }

    private Application getApplicationOfDeploymentPackage(String dpSymbolicName) {
        Application resultApp = null;
        readLock.lock();
        for (ApplicationImpl app : _appPerId.values()) {
            if (app.containsDeploymentPackageRepresentation(dpSymbolicName)) {
                resultApp = app;
                break;
            }
        }
        readLock.unlock();
        return resultApp;
    }

    private void onDeploymePackageArrival(DeploymentPackage deploymentPackage) {
        logger.debug("DP arrival " + deploymentPackage.getName());
        String appId = (String) deploymentPackage.getHeader(Application.APP_ID_BUNDLE_HEADER);
        if (appId == null) // not an application deployment package
            return;

        String appName = (String) deploymentPackage.getHeader(Application.APP_NAME_BUNDLE_HEADER);
        String appVersion = (String) deploymentPackage.getHeader(Application.APP_VERSION_BUNDLE_HEADER);
        if (appVersion == null) { // version is mandatory
            // ignore if version is not provided
            logger.error("Deployment Package " + deploymentPackage.getName() + " must specify an application version.");
            return;
        }

        writeLock.lock();

        ApplicationImpl app = _appPerId.get(appId);
        boolean isNewApp = (app == null);

        if (isNewApp) {
            logger.debug("New application detected: " + appId);
            app = new ApplicationImpl(appId, null, appName, _undefinedCateg, this, _context);
            app.setVersion(appVersion);
            app.setState(ApplicationState.STOPED);
        }

        DeploymentPackageRepresentation dpRepresentation = DeploymentPackageRepresentation
                .builFromDeploymentPackage(deploymentPackage);

        app.addDeploymentPackageRepresentation(dpRepresentation);

        _appPerId.put(app.getId(), app);

        writeLock.unlock();

        // Notifies listeners if is a new app
        if (isNewApp) {
            notifyApplicationAdded(app);
        }

        // Notifies listeners of new deployment packages arrival
        notifyDeploymentPackagedAdded(app, dpRepresentation);
    }

    private void onDeploymePackageDeparture(String dpSymbolicName) {
        logger.debug("DP is leaving: " + dpSymbolicName);
        ApplicationImpl tempApp = (ApplicationImpl) getApplicationOfDeploymentPackage(dpSymbolicName);

        if (tempApp == null) // The deployment package is not in the application registry
            return;

        DeploymentPackageRepresentation dpRepresentation = tempApp.getDeploymentPackageRepresentation(dpSymbolicName);

        if (dpRepresentation != null) {
            tempApp.removeDeploymentPackageRepresentation(dpSymbolicName);
            
            // Notifies that the deployment packages has been removed
            notifyDeploymentPackagedRemoved(tempApp, dpRepresentation);
            
            if (tempApp.isEmptyApplication()) {
                logger.debug("Removing empty application: " + tempApp.getId());
                writeLock.lock();
                _appPerId.remove(tempApp.getId());
                writeLock.unlock();
                
                // Notifies that application has been removed
                notifyApplicationRemoved(tempApp);
            }

        }
    }

    public Set<Bundle> getBundles(String appId) {
        Application app = getApplication(appId);
        if (app == null)
            return Collections.emptySet();

        ApplicationImpl tempApp = (ApplicationImpl) app;
        Set<String> bundleIds = tempApp.getBundlesIds();

        Set<Bundle> bundles = new HashSet<Bundle>();
        for (String bundleId : bundleIds) {
            Bundle bundle = getBundle(bundleId);
            bundles.add(bundle);
        }
        return bundles;
    }

    private Bundle getBundle(String symbolicName) {
        Bundle result = null;
        for (Bundle candidate : _context.getBundles()) {
            if (symbolicName.equals(candidate.getSymbolicName())) {
                if (result == null || result.getVersion().compareTo(candidate.getVersion()) < 0) {
                    result = candidate;
                }
            }
        }
        return result;
    }

    @Override
    public boolean isApplicationBundle(Bundle bundle) {
        return getApplicationOfBundle(bundle.getSymbolicName()) != null;
    }

    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();

        // Notification confirming the installation or uninstallation of a Deployment Package
        if (topic.equals("org/osgi/service/deployment/COMPLETE")) {
            boolean sucessfull = (Boolean) event.getProperty("successful");
            if (!sucessfull)
                return;
            String dpSymbolicName = (String) event.getProperty(DeploymentPackage.EVENT_DEPLOYMENTPACKAGE_NAME);
            DeploymentPackage deploymentPackage = deploymentAdmin.getDeploymentPackage(dpSymbolicName);
            if (deploymentPackage != null) { // Installation Confirmation
                String appId = (String) deploymentPackage.getHeader(Application.APP_ID_BUNDLE_HEADER);
                String appVersion = (String) deploymentPackage.getHeader(Application.APP_VERSION_BUNDLE_HEADER);
                if (appId != null && appVersion != null) // iCasa App Deployment Package
                    onDeploymePackageArrival(deploymentPackage);
            } else { // unInstallation Confirmation
                onDeploymePackageDeparture(dpSymbolicName);
            }
        }
    }

}
