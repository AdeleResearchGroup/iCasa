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

/**
 *
 */
public interface AccessRightListener {

    /**
     * Method called when the right to access the device has been modified.
     * @param accessRight the access right object that has been changed.
     */
    void onAccessRightModified(AccessRight accessRight);

    /**
     * Method called when the access right to a specific method has been modified.
     * @param accessRight The access right object.
     * @param methodName The method name which access right has been modified.
     */
    void onMethodAccessRightModified(AccessRight accessRight, String methodName);

}
