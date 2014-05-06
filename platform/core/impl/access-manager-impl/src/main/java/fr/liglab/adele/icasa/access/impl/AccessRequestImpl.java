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
package fr.liglab.adele.icasa.access.impl;

import fr.liglab.adele.icasa.access.AccessRight;
import fr.liglab.adele.icasa.access.AccessRightListener;

import java.util.Date;

/**
 *
 */
public class AccessRequestImpl implements AccessRightListener{



    private final Date requestDate;
    private AccessRightImpl accessRight;

    public AccessRequestImpl(AccessRightImpl accessRight ) {
        this.accessRight = accessRight;
        this.requestDate = new Date();
        accessRight.addListener(this);
    }

    public String getApplicationId() {
        return accessRight.getApplicationId();
    }

    public String getDeviceId() {
        return accessRight.getDeviceId();
    }

    public Date getRequestDate() {
        return requestDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessRequestImpl that = (AccessRequestImpl) o;

        if (!getApplicationId().equals(that.getApplicationId())) return false;
        if (!getDeviceId().equals(that.getDeviceId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getApplicationId().hashCode();
        result = 31 * result + getDeviceId().hashCode();
        return result;
    }

    /**
     * Method called when the right to access the device has been modified.
     *
     * @param accessRight the access right object that has been changed.
     */
    @Override
    public void onAccessRightModified(AccessRight accessRight) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Method called when the access right to a specific method has been modified.
     *
     * @param accessRight The access right object.
     * @param methodName  The method name which access right has been modified.
     */
    @Override
    public void onMethodAccessRightModified(AccessRight accessRight, String methodName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public AccessRightImpl getAccessRight() {
        return accessRight;
    }
}
