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

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.access.AccessManager;
import fr.liglab.adele.icasa.access.AccessRight;
import fr.liglab.adele.icasa.access.DeviceAccessPolicy;
import fr.liglab.adele.icasa.access.MemberAccessPolicy;
import fr.liglab.adele.icasa.access.impl.util.AccessRightJSON;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.IOException;


/**
 *
 */
@Component
@Instantiate
@Provides(specifications={AccessManagerRemote.class, Controller.class} )
public class AccessManagerRemote extends DefaultController {


    @Requires
    AccessManager manager;


    @Route(method = HttpMethod.GET, uri = "/icasa/policies")
    public Result getRightAccess() {
        return ok(getAllRightAccess()).as(MimeTypes.JSON);
    }


    @Route(method = HttpMethod.GET, uri = "/icasa/policies/policyTypes")
    public Result getRightAccessPoliciesTypes() {
        String result = null;
        try {
            result = getAllPoliciesTypes();
        } catch (JSONException e) {
            return badRequest();
        }
        return ok(result).as(MimeTypes.JSON);
    }

    @Route(method = HttpMethod.GET, uri = "/icasa/policies/application/{applicationId}")
    public Result getApplicationAccessRight(@Parameter("applicationId")String applicationId) {
        return ok(getAllRightAccess(applicationId)).as(MimeTypes.JSON);
    }

    @Route(method = HttpMethod.GET, uri = "/icasa/policies/policy/{id}")
    public Result getPolicyAccessRight(@Parameter("id")String policyId) {
        String policy = getPolicy(policyId);
        if (policy == null){
            return notFound();
        }
        return ok(getPolicy(policyId)).as(MimeTypes.JSON);
    }




    @Route(method = HttpMethod.POST, uri = "/icasa/policies/policy")
    public Result setRightAccess() {
        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
        JSONObject jsonAccessRight = null;
        AccessRight right = updateAccessRight(content);
        return ok(AccessRightJSON.toJSON(right).toString()).as(MimeTypes.JSON);
    }

    @Route(method = HttpMethod.PUT, uri = "/icasa/policies/policy/{id}")
    public Result updateApplicationAccess(@Parameter("id")String policyId){
        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
        AccessRight right = updateAccessRight(content);
        return ok(AccessRightJSON.toJSON(right).toString()).as(MimeTypes.JSON);
    }

    private AccessRight updateAccessRight(String content){
        JSONObject jsonAccessRight = null;
        AccessRight right = null;
        try {
            jsonAccessRight = AccessRightJSON.fromString(content);
            right = manager.setDeviceAccess(jsonAccessRight.getString("applicationId"), jsonAccessRight.getString("deviceId"), DeviceAccessPolicy.fromString(jsonAccessRight.getString("policy")));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
        return right;
    }

    private String getAllRightAccess() {
        JSONArray accessRights = new JSONArray();
        AccessRight[] rights = manager.getAllAccessRight();
        for(AccessRight right: rights){
            accessRights.put(AccessRightJSON.toJSON(right));
        }
        return accessRights.toString();
    }

    private String getAllRightAccess(String applicationId) {
        JSONArray accessRights = new JSONArray();
        AccessRight[] rights = manager.getAccessRight(applicationId);
        for(AccessRight right: rights){
            accessRights.put(AccessRightJSON.toJSON(right));
        }
        return accessRights.toString();
    }

    private String getAccessRight(String applicationId, String deviceId) {
        AccessRight rights = manager.getAccessRight(applicationId, deviceId);
        return AccessRightJSON.toJSON(rights).toString();
    }

    private String getPolicy(String policyId) {
        String stringfiedRight = null;
        Long identifier = null;
        try{
            identifier = Long.decode(policyId);
        }catch(Exception ex){
            identifier = -1L;
        }

        AccessRight rights = manager.getAccessRightFromId(identifier);
        if (rights != null){
            stringfiedRight =  AccessRightJSON.toJSON(rights).toString();
        }
        return stringfiedRight;
    }

    private String getAllPoliciesTypes() throws JSONException{
        JSONArray policies = new JSONArray();
        //add the policies. It is not a for to void non-deterministic identifiers.
        policies.put(getJSONPolicyType(0,DeviceAccessPolicy.HIDDEN));
        policies.put(getJSONPolicyType(2,DeviceAccessPolicy.VISIBLE));
        policies.put(getJSONPolicyType(3,DeviceAccessPolicy.PARTIAL));
        policies.put(getJSONPolicyType(4,DeviceAccessPolicy.TOTAL));
        return policies.toString();
    }
    private JSONObject getJSONPolicyType(int id, DeviceAccessPolicy policy) throws JSONException {
        JSONObject jsonPolicy = new JSONObject();
        jsonPolicy.put("id", id);
        jsonPolicy.put("name", policy.toString());
        return jsonPolicy;
    }


}
