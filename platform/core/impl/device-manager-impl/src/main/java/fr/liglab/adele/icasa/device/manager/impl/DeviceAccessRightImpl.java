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
package fr.liglab.adele.icasa.device.manager.impl;

import fr.liglab.adele.icasa.device.manager.AccessRight;
import fr.liglab.adele.icasa.device.manager.DeviceAccessRight;

import java.lang.reflect.Method;

/**
 *
 */
public class DeviceAccessRightImpl implements DeviceAccessRight {

    private final Method _deviceMethod;
    private final AccessRight _accessRight;

    public DeviceAccessRightImpl(Method deviceMethod, AccessRight accessRight) {
        _deviceMethod = deviceMethod;
        _accessRight = accessRight;
    }

    @Override
    public Method getMethod() {
        return _deviceMethod;
    }

    @Override
    public AccessRight getAccessRight() {
        return _accessRight;
    }
}
