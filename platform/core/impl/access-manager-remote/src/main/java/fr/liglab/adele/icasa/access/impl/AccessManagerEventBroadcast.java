/*
 * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 * Group Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.liglab.adele.icasa.access.impl;

import fr.liglab.adele.icasa.access.AccessManager;
import fr.liglab.adele.icasa.access.AccessRight;
import fr.liglab.adele.icasa.access.AccessRightListener;
import fr.liglab.adele.icasa.access.AccessRightManagerListener;
import fr.liglab.adele.icasa.access.impl.util.AccessRightJSON;
import fr.liglab.adele.icasa.remote.RemoteEventBroadcast;
import org.apache.felix.ipojo.annotations.*;

/**
 * User: garciai@imag.fr
 * Date: 7/18/13
 * Time: 2:39 PM
 */
@Component(name = "iCasa-access-manager-event-broadcast")
@Instantiate(name = "iCasa-access-manager-event-broadcast-1")
public class AccessManagerEventBroadcast implements AccessRightManagerListener{

    @Requires
    RemoteEventBroadcast eventBroadcast;

    @Requires
    AccessManager accessManager;

    @Validate
    public void start(){
        accessManager.addListener(this);

    }

    @Invalidate
    public void stop(){
        accessManager.removeListener(this);
    }

    /**
     * Method called when the right to access the device has been modified.
     *
     * @param accessRight the access right object that has been changed.
     */
    @Override
    public void onAccessRightModified(AccessRight accessRight) {
        eventBroadcast.sendEvent("access-right-modified", AccessRightJSON.toJSON(accessRight));
    }

    /**
     * Method called when the access right to a specific method has been modified.
     *
     * @param accessRight The access right object.
     * @param methodName  The method name which access right has been modified.
     */
    @Override
    public void onMethodAccessRightModified(AccessRight accessRight, String methodName) {
        eventBroadcast.sendEvent("access-right-modified", AccessRightJSON.toJSON(accessRight));
    }

    @Override
    public void onAccessRightAdded(AccessRight accessRight) {
        eventBroadcast.sendEvent("access-right-added", AccessRightJSON.toJSON(accessRight));
    }
}
