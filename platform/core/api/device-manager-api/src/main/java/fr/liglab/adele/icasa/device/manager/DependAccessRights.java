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

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Represents application access rights for a specific device dependency.
 */
public interface DependAccessRights {

    /**
     * Returns corresponding device dependency.
     *
     * @return corresponding device dependency.
     */
    public DeviceDependency getDependency();

    /**
     * Returns all access rights associated to the related dependency.
     *
     * @return all access rights associated to the related dependency.
     */
    public Set<DeviceAccessRight> getAccessRights();

    /**
     * Returns true if specified method is currently allowed.
     *
     * @param method a device method
     * @return true if specified method is currently allowed.
     */
    public boolean isAllowed(Method method);

    /**
     * Returns true if specified method is currently allowed and exclusively allocated to the corresponding application.
     *
     * @param method a device method
     * @return true if specified method is currently allowed and exclusively allocated to the corresponding application.
     */
    public boolean isExclusive(Method method);
}
