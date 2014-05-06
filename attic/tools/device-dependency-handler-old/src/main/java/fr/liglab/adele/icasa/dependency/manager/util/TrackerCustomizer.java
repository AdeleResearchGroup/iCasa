/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under the Apache License, Version 2.0 (the "License");
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
package fr.liglab.adele.icasa.dependency.manager.util;

import org.osgi.framework.ServiceReference;

/**
 * LocatedDeviceTracker Customizer.
 */
public interface TrackerCustomizer {

    /**
     * A service is being added to the LocatedDeviceTracker object.
     * This method is called before a service which matched the search parameters of the LocatedDeviceTracker object is added to it. This method should return the service object to be tracked for this ServiceReference object.
     * The returned service object is stored in the LocatedDeviceTracker object and is available from the getService and getServices methods.
     * @param reference the Reference to service being added to the LocatedDeviceTracker object.
     * @return The service object to be tracked for the ServiceReference object or null if the ServiceReference object should not be tracked.
     */
    boolean addingService(ServiceReference reference);
    
    /**
     * A service tracked by the LocatedDeviceTracker object has been added in the list.
     * This method is called when a service has been added in the managed list (after addingService) and if the service has not disappeared before during the callback.
     * @param reference the added reference.
     */
    void addedService(ServiceReference reference);

    /**
     * A service tracked by the LocatedDeviceTracker object has been modified.
     * This method is called when a service being tracked by the LocatedDeviceTracker object has had it properties modified.
     * @param reference the Reference to service that has been modified.
     * @param service The service object for the modified service.
     */
    void modifiedService(ServiceReference reference, Object service);

    /**
     * A service tracked by the LocatedDeviceTracker object has been removed.
     * This method is called after a service is no longer being tracked by the LocatedDeviceTracker object.
     * @param reference the Reference to service that has been removed.
     * @param service The service object for the removed service.
     */
    void removedService(ServiceReference reference, Object service);

}
