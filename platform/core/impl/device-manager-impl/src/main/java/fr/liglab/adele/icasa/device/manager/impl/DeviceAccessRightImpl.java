/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
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
package fr.liglab.adele.icasa.device.manager.impl;

import fr.liglab.adele.icasa.device.manager.AccessRight;
import fr.liglab.adele.icasa.device.manager.DeviceAccessRight;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: thomas
 * Date: 21/05/13
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
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
