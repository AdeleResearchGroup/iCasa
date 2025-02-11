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
package fr.liglab.adele.icasa.access;


import java.lang.reflect.Method;

/**
 * The AccessRight interface allows to inspect the right access for an application to use a service.
 * There is an AccessRight object for each application trying to access each device.
 */
public interface AccessRight {
    /**
     * See if the application has the right to access the device.
     * @return true when the application has the right to access the device. False if not.
     */
    boolean isVisible();

    /**
     * See if the application has the right access to call the given method.
     * @param method the method to see the right access.
     * @return true when the application can call the method, false if not.
     */
    boolean hasMethodAccess(Method method) throws NullPointerException;

    /**
     * See if the application has the right access to call the given method.
     * @param method the method to see the right access.
     * @return true when the application can call the method, false if not.
     */
    boolean hasMethodAccess(String method) throws NullPointerException;

    /**
     * Get the list of method whose access has been defined.
     * If an existent device method, does not appear in the list, the access to the method
     * is denied.
     * @return an array of the existent method access.
     */
    String[] getMethodList();


    /**
     * Get the application wanting to access the device.
     * @return the application identifier.
     */
    String getApplicationId();

    /**
     * Get the device identifier the application wants to access.
     * @return the device identifier.
     */
    String getDeviceId();

    /**
     * Add a listener to be notified when the access right has been changed.
     * @param listener The listener to be called when an access right has changed.
     */
    void addListener(AccessRightListener listener);

    /**
     * Remove a listener.
     * @param listener The listener to be called when an access right has changed.
     */
    void removeListener(AccessRightListener listener);

    /**
     * Get the device policy value.
     * @return the device policy.
     */
    DeviceAccessPolicy getPolicy();

    /**
     * Get an identifier of this AccessRight.
     * @return
     */
    Long getIdentifier();
}
