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
package fr.liglab.adele.icasa.access.impl.util;

import fr.liglab.adele.icasa.access.AccessRight;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class AccessRightJSON  {

    AccessRight right;

    public static JSONObject toJSON(AccessRight accessRight){
        JSONObject accessRightJSON = new JSONObject();
        try {
            accessRightJSON.put("id", accessRight.getIdentifier());
            accessRightJSON.put("applicationId", accessRight.getApplicationId());
            accessRightJSON.put("deviceId", accessRight.getDeviceId());
            accessRightJSON.put("policy", accessRight.getPolicy());
            //TODO: Method access are not sent.
            //JSONArray methodsAccess = getMethodAccess(accessRight);
            //accessRightJSON.put("methodAccessRight", methodsAccess);
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return accessRightJSON;
    }

    public static JSONObject fromString(String accessRight) throws JSONException {
        JSONObject accessRightJSON = new JSONObject(accessRight);
        if(!accessRightJSON.has("applicationId") || !accessRightJSON.has("deviceId") || !accessRightJSON.has("policy")  ){
            throw new JSONException("Malformed Access Right JSON object. It must contain applicationId, deviceId and policy");
        }
        return accessRightJSON;
    }

    public static JSONArray getMethodAccess(AccessRight accessRight) throws JSONException {
        JSONArray methodsAccess = new JSONArray();
        String[] methodNames = accessRight.getMethodList();
        for (String method: methodNames){
            JSONObject methodRightAccess = new JSONObject();
            methodRightAccess.put("method", method);
            methodRightAccess.put("access", accessRight.hasMethodAccess(method));
            methodsAccess.put(methodRightAccess);
        }
        return methodsAccess;
    }



}
