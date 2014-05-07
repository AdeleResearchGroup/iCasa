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

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.application.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.event.Event;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of an application manager.
 *
 */
@Component(name = "Application-Manager-Impl")
@Instantiate(name = "Application-Manager-Impl-0")
@Provides(specifications = ApplicationManager.class)
public class ApplicationManagerImpl implements ApplicationManager, ServiceTrackerCustomizer {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + ".applications");

    private ServiceTracker _appTracker;


    /**
     * List of application trackers
     */
    private List<ApplicationListener> _listeners = new ArrayList<ApplicationListener>();

    /**
     *  guard the applicationPerIdMap
     */
    private final ReadWriteLock lockListener = new ReentrantReadWriteLock();

    private final Lock readLockListener = lockListener.readLock();

    private final Lock writeLockListener = lockListener.writeLock();

    /**
     * Map keeping the record of applications
     */
    private Map<String, ApplicationImpl> _appPerId = new HashMap<String, ApplicationImpl>();

    /**
     *  guard the applicationPerIdMap
     */
    private final ReadWriteLock lockAppPerId = new ReentrantReadWriteLock();

    private final Lock readLockAppPerId = lockAppPerId.readLock();

    private final Lock writeLockAppPerId = lockAppPerId.writeLock();


    private BundleContext _context;

    public ApplicationManagerImpl(BundleContext context) {
        _context = context;
        _appTracker = new ServiceTracker(_context, ApplicationDescription.class.getName(),this);
    }

    @Override
    public Set<ApplicationCategory> getCategories() {
        Set<ApplicationCategory> categories = new HashSet<ApplicationCategory>();
        readLockAppPerId.lock();
        try{
            for(String key : _appPerId.keySet()){
                categories.add(_appPerId.get(key).getCategory());
            }
            return categories;
        }finally {
            readLockAppPerId.unlock();
        }
    }

    @Override
    public Set<Application> getApplications() {
        readLockAppPerId.lock();
        try{
            Collection<ApplicationImpl> appsCollection = _appPerId.values();
            return Collections.unmodifiableSet(new HashSet<Application>(appsCollection));
        }finally {
            readLockAppPerId.unlock();
        }
    }

    @Override
    public Application getApplication(String appId) {
        readLockAppPerId.lock();
        try{
            Application app = _appPerId.get(appId);
            return app;
        }finally {
            readLockAppPerId.unlock();
        }
    }

    @Override
    public void addApplicationListener(ApplicationListener listener) {
        readLockAppPerId.lock();
        writeLockListener.lock();
        try{
            _listeners.add(listener);
            for(String key : _appPerId.keySet()){
                Application application = _appPerId.get(key);
                listener.addApplication(application);
                for(Bundle bundle : application.getBundles()){
                    listener.bundleAdded(application,bundle.getSymbolicName());
                }
            }
        }finally {
            writeLockListener.unlock();
            readLockAppPerId.unlock();
        }


    }

    @Override
    public void removeApplicationListener(ApplicationListener listener) {
        writeLockListener.lock();
        try{
            _listeners.remove(listener);
        }finally {
            writeLockListener.unlock();
        }
    }

    @Validate
    public void start() {
        logger.debug("start");
        _appTracker.open();
    }

    @Invalidate
    public void stop() {
        _appTracker.close();
        logger.debug("stop");

        // Not sure this part is mandatory
        writeLockAppPerId.lock();
        try{
            _appPerId.clear();
        }finally {
            writeLockAppPerId.unlock();
        }

        writeLockListener.lock();
        try{
            _listeners.clear();
        }finally {
            writeLockListener.unlock();
        }
    }

    @Override
    public Application getApplicationOfBundle(String bundleSymbolicName) {
        Application resultApp = null;
        readLockAppPerId.lock();
        try{
            for (ApplicationImpl app : _appPerId.values()) {
                if (app.containsBundle(bundleSymbolicName)) {
                    resultApp = app;
                    break;
                }
            }
            return resultApp;
        }finally{
            readLockAppPerId.unlock();
        }
    }

    @Override
    public boolean isApplicationBundle(Bundle bundle) {
        return getApplicationOfBundle(bundle.getSymbolicName()) != null;
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        ApplicationDescription applicationDescription = (ApplicationDescription) _context.getService(serviceReference);
        writeLockAppPerId.lock();
        try{
            _appPerId.put(applicationDescription.getId(),new ApplicationImpl(applicationDescription,this));
            readLockListener.lock();
            try{
                Application application = _appPerId.get(applicationDescription.getId());
                for(ApplicationListener listener : _listeners){
                    listener.addApplication(application);
                    for(Bundle bundle : application.getBundles()){
                        listener.bundleAdded(application,bundle.getSymbolicName());
                    }
                }
            }finally {
                readLockListener.unlock();
            }
            return applicationDescription;
        }finally {
            writeLockAppPerId.unlock();
        }
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        ApplicationDescription applicationDescription = (ApplicationDescription) o;
        writeLockAppPerId.lock();
        try{
            Application application = _appPerId.get(applicationDescription.getId());
            Set<Bundle> bundles = new HashSet<Bundle>(application.getBundles());
            _appPerId.get(applicationDescription.getId()).updateApplicationImpl(applicationDescription);
            Set<Bundle> newBundles = new HashSet<Bundle>(application.getBundles());
            if(!(bundles.containsAll(newBundles) && newBundles.containsAll(bundles))){
                if((bundles.containsAll(newBundles))){
                    bundles.removeAll(newBundles);
                    readLockListener.lock();
                    try{
                        for(Bundle bundle : bundles){
                            for(ApplicationListener listener : _listeners){
                                listener.bundleAdded(application,bundle.getSymbolicName());
                            }
                        }
                    }finally {
                        readLockListener.unlock();
                    }
                }else {
                    newBundles.removeAll(bundles);
                    readLockListener.lock();
                    try{
                        for(Bundle bundle : newBundles){
                            for(ApplicationListener listener : _listeners){
                                listener.bundleAdded(application,bundle.getSymbolicName());
                            }
                        }
                    }finally {
                        readLockListener.unlock();
                    }
                }
            }
        }finally {
            writeLockAppPerId.unlock();
        }
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        ApplicationDescription applicationDescription = (ApplicationDescription) o;
        writeLockAppPerId.lock();
        try{
            readLockListener.lock();
            try{
                Application application = _appPerId.get(applicationDescription.getId());
                for(ApplicationListener listener : _listeners){
                    listener.removeApplication(application);
                    for(Bundle bundle : application.getBundles()){
                        listener.bundleRemoved(application, bundle.getSymbolicName());
                    }
                }
            }finally {
                readLockListener.unlock();
            }
            _appPerId.remove(((ApplicationDescription) o).getId());
        }finally {
            writeLockAppPerId.unlock();
        }
    }
}
