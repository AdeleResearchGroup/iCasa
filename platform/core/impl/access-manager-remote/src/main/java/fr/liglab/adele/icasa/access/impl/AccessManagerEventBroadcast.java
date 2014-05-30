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

import fr.liglab.adele.icasa.access.*;
import fr.liglab.adele.icasa.access.impl.util.AccessRightJSON;
import fr.liglab.adele.icasa.remote.wisdom.RemoteEventBroadcast;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
@Component(name = "iCasa-access-manager-event-broadcast")
@Instantiate(name = "iCasa-access-manager-event-broadcast-0")
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
        if(accessRight.getPolicy().equals(DeviceAccessPolicy.TOHIDE)){
            return; //dont sent transitive event change.
        }
        JSONObject json = new JSONObject();
        try {
            json.put("accessRight", AccessRightJSON.toJSON(accessRight));
            eventBroadcast.sendEvent("access-right-modified", json);
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
     * Method called when the access right to a specific method has been modified.
     *
     * @param accessRight The access right object.
     * @param methodName  The method name which access right has been modified.
     */
    @Override
    public void onMethodAccessRightModified(AccessRight accessRight, String methodName) {
        JSONObject json = new JSONObject();
        try {
            json.put("accessRight", AccessRightJSON.toJSON(accessRight));
            eventBroadcast.sendEvent("access-right-modified", json);
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void onAccessRightAdded(AccessRight accessRight) {
        JSONObject json = new JSONObject();
        try {
            json.put("accessRight", AccessRightJSON.toJSON(accessRight));
            eventBroadcast.sendEvent("access-right-added", json);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
