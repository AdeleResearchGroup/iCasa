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

import fr.liglab.adele.icasa.application.Application;
import fr.liglab.adele.icasa.application.ApplicationCategory;
import fr.liglab.adele.icasa.application.ApplicationDescription;
import fr.liglab.adele.icasa.application.ApplicationState;
import fr.liglab.adele.icasa.common.ProgressMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import java.util.HashSet;
import java.util.Set;

public class ApplicationImpl  implements Application {

    private  String _id;

    private String _name;

    private Version _version;

    private String _vendor;

    private String _category;

    private Set<Bundle> _bundles = new HashSet<Bundle>();

    private ApplicationState _state;

    private ApplicationManagerImpl _appMgr;

    private final Object _lock;

    public ApplicationImpl(ApplicationDescription description, ApplicationManagerImpl appMgr) {
        this(description.getId(),description.getName(),description.getVersion(),description.getVendor(),description.getCategory(), description.getBundles(), ApplicationState.STOPPED, appMgr);
    }

    private ApplicationImpl(String id, String name,Version version,String vendor,String category,Set<Bundle> bundles, ApplicationState state,ApplicationManagerImpl appMgr) {
        _lock = new Object();
        _id = id;
        _name =name;
        _version = version;
        _vendor = vendor;
        _category=category;
        _bundles=bundles;
        _state = state;
        _appMgr = appMgr;

    }

    @Override
    public String getId() {
        synchronized (_lock){
            return _id;
        }
    }

    @Override
    public String getName() {
        synchronized (_lock){
            return _name;
        }
    }

    @Override
    public String getVendor() {
        synchronized (_lock){
            return _vendor;
        }
    }

    @Override
    public ApplicationCategory getCategory() {
        synchronized (_lock){
            return new ApplicationCategoryImpl(_category);
        }
    }

    @Override
    public synchronized void start(ProgressMonitor monitor) {

    }

    @Override
    public synchronized void stop(ProgressMonitor monitor) {

    }

    @Override
    public synchronized void resume(ProgressMonitor monitor) {

    }

    @Override
    public synchronized void pause(ProgressMonitor monitor) {

    }

    @Override
    public Set<Bundle> getBundles() {
        synchronized (_lock){
            return _bundles;
        }
    }

    @Override
    public ApplicationState getState() {
        synchronized (_lock){
            return _state;
        }
    }

    public void setState(ApplicationState newState) {
        synchronized (_lock){
            _state = newState;
        }
    }

    @Override
    public Version getVersion() {
       synchronized (_lock){
           return _version;
       }
    }

    /**
     * Determines if this application contains a Bundle
     *
     * @param bundleSymbolicName bundle name
     * @return true if app contains the bundle. False otherwise
     */
    public boolean containsBundle(String bundleSymbolicName) {
        synchronized (_lock){
            for (Bundle bundle : _bundles) {
                if (bundle.getSymbolicName().equals(bundleSymbolicName))
                    return true;
            }
            return false;
        }
    }

    /**
     * Get a string set with bundles symbolic names in this App
     *
     * @return
     */
    public Set<String> getBundlesIds() {
        Set<String> bundlesIds = new HashSet<String>();
        synchronized (_lock){
            for (Bundle bundle : _bundles) {
                bundlesIds.add(bundle.getSymbolicName());
            }
            return bundlesIds;
        }
    }

    /**
     * Get a string set with bundles symbolic names in this App
     *
     */
    public void updateApplicationImpl(ApplicationDescription description){
        synchronized (_lock){
            _id = description.getId();
            _name = description.getName();
            _category = description.getCategory();
            _vendor = description.getVendor();
            _version = description.getVersion();
            _bundles = description.getBundles();
        }
    }

}
