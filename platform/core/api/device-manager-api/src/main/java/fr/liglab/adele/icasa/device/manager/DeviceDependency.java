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
package fr.liglab.adele.icasa.device.manager;

import fr.liglab.adele.icasa.application.Application;

import java.util.Set;

/**
 * Represents a device dependency from an application.
 */
public interface DeviceDependency {

    /**
     * Returns the application owner of this dependency.
     *
     * @return the application owner of this dependency.
     */
    public Application getApplication();

    /**
     * Returns all required access rights from related application device dependency.
     *
     * @return all required access rights from related application device dependency.
     */
    public Set<DeviceAccessRightRequest> getAccesRightRequests();

    public void updateAccessRightRequest(Set<DeviceAccessRightRequest> newRequest);

    /**
     * Must be called by the dependency owner to notify that this dependency is no more used.
     */
    public void unregister();
}
