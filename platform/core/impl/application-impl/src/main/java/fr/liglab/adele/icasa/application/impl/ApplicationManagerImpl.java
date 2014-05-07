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
     * Map keeping the record of applications
     */
    private Map<String, ApplicationImpl> _appPerId = new HashMap<String, ApplicationImpl>();

    /**
     *  guard the applicationPerIdMap & listener ( bad but it's because of the dirty implementation of the dependency device annotation )
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock readLock = lock.readLock();

    private final Lock writeLock = lock.writeLock();


    private BundleContext _context;

    public ApplicationManagerImpl(BundleContext context) {
        _context = context;
        _appTracker = new ServiceTracker(_context, ApplicationDescription.class.getName(),this);
    }

    @Override
    public Set<ApplicationCategory> getCategories() {
        Set<ApplicationCategory> categories = new HashSet<ApplicationCategory>();
        readLock.lock();
        try{
            for(String key : _appPerId.keySet()){
                categories.add(_appPerId.get(key).getCategory());
            }
            return categories;
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<Application> getApplications() {
        readLock.lock();
        try{
            Collection<ApplicationImpl> appsCollection = _appPerId.values();
            return Collections.unmodifiableSet(new HashSet<Application>(appsCollection));
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public Application getApplication(String appId) {
        readLock.lock();
        try{
            Application app = _appPerId.get(appId);
            return app;
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public void addApplicationListener(ApplicationListener listener) {
        writeLock.lock();
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
            writeLock.unlock();
        }


    }

    @Override
    public void removeApplicationListener(ApplicationListener listener) {
        writeLock.lock();
        try{
            _listeners.remove(listener);
        }finally {
            writeLock.unlock();
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
        writeLock.lock();
        try{
            _appPerId.clear();
            _listeners.clear();
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public Application getApplicationOfBundle(String bundleSymbolicName) {
        Application resultApp = null;
        readLock.lock();
        try{
            for (ApplicationImpl app : _appPerId.values()) {
                if (app.containsBundle(bundleSymbolicName)) {
                    resultApp = app;
                    break;
                }
            }
            return resultApp;
        }finally{
            readLock.unlock();
        }
    }

    @Override
    public boolean isApplicationBundle(Bundle bundle) {
        return getApplicationOfBundle(bundle.getSymbolicName()) != null;
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        ApplicationDescription applicationDescription = (ApplicationDescription) _context.getService(serviceReference);
        writeLock.lock();
        try{
            _appPerId.put(applicationDescription.getId(),new ApplicationImpl(applicationDescription,this));
            Application application = _appPerId.get(applicationDescription.getId());
            for(ApplicationListener listener : _listeners){
                listener.addApplication(application);
                for(Bundle bundle : application.getBundles()){
                    listener.bundleAdded(application,bundle.getSymbolicName());
                }
            }
            return applicationDescription;
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        ApplicationDescription applicationDescription = (ApplicationDescription) o;
        writeLock.lock();
        try{
            Application application = _appPerId.get(applicationDescription.getId());
            Set<Bundle> bundles = new HashSet<Bundle>(application.getBundles());
            _appPerId.get(applicationDescription.getId()).updateApplicationImpl(applicationDescription);
            Set<Bundle> newBundles = new HashSet<Bundle>(application.getBundles());
            if(!(bundles.containsAll(newBundles) && newBundles.containsAll(bundles))){
                if((bundles.containsAll(newBundles))){
                    bundles.removeAll(newBundles);

                    for(Bundle bundle : bundles){
                        for(ApplicationListener listener : _listeners){
                            listener.bundleAdded(application,bundle.getSymbolicName());
                        }
                    }
                }else {
                    newBundles.removeAll(bundles);
                    for(Bundle bundle : newBundles){
                        for(ApplicationListener listener : _listeners){
                            listener.bundleAdded(application,bundle.getSymbolicName());
                        }
                    }
                }
            }
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        ApplicationDescription applicationDescription = (ApplicationDescription) o;
        writeLock.lock();
        try{
                Application application = _appPerId.get(applicationDescription.getId());
                for(ApplicationListener listener : _listeners){
                    listener.removeApplication(application);
                    for(Bundle bundle : application.getBundles()){
                        listener.bundleRemoved(application, bundle.getSymbolicName());
                    }
                }

            _appPerId.remove(((ApplicationDescription) o).getId());
        }finally {
            writeLock.unlock();
        }
    }
}
