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
import fr.liglab.adele.icasa.access.DeviceAccessPolicy;
import fr.liglab.adele.icasa.access.MemberAccessPolicy;
import fr.liglab.adele.icasa.access.impl.util.AccessRightJSON;
import fr.liglab.adele.icasa.remote.AbstractREST;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

/**
 * User: garciai@imag.fr
 * Date: 7/15/13
 * Time: 4:59 PM
 */
@Component(name="iCasaAccessManagerRemote")
@Instantiate(name="iCasaAccessManagerRemote-1")
@Provides(specifications={AccessManagerRemote.class}, properties = {@StaticServiceProperty(name = AbstractREST.ICASA_REST_PROPERTY_NAME, value="true", type="java.lang.Boolean")} )
@Path(value="/access")
public class AccessManagerRemote extends AbstractREST {

    @Requires
    AccessManager manager;

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value="/policies")
    public Response getAccessRightOptions() {
        return makeCORS(Response.ok());
    }

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value="/policyTypes")
    public Response getAccessRightPoliciesTypesOptions() {
        return makeCORS(Response.ok());
    }

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value="/policies/{applicationId}")
    public Response getApplicationAccessRightOptions(@PathParam("applicationId")String applicationId) {
        return makeCORS(Response.ok());
    }

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value="/policies/{applicationId}/{deviceId}")
    public Response getApplicationDeviceAccessRightOptions(@PathParam("applicationId")String applicationId, @PathParam("deviceId")String deviceId) {
        return makeCORS(Response.ok());
    }




    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value="/policies")
    public Response getRightAccess() {
        return makeCORS(Response.ok(getAllRightAccess()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value="/policyTypes")
    public Response getRightAccessPoliciesTypes() {
        String result = null;
        try {
            result = getAllPoliciesTypes();
        } catch (JSONException e) {
            return makeCORS(Response.status(Response.Status.BAD_REQUEST));
        }
        return makeCORS(Response.ok(result));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value="/policies/{applicationId}")
    public Response getApplicationAccessRight(@PathParam("applicationId")String applicationId) {
        return makeCORS(Response.ok(getAllRightAccess(applicationId)));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value="/policies/{applicationId}/{deviceId}")
    public Response getApplicationAccessRight(@PathParam("applicationId")String applicationId, @PathParam("deviceId")String deviceId) {
        return makeCORS(Response.ok(getAccessRight(applicationId, deviceId)));
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(value="/policies")
    public Response setRightAccess(String content) {
        JSONObject jsonAccessRight = null;
        AccessRight right = null;
        try {
            jsonAccessRight = AccessRightJSON.fromString(content);
            right = manager.setDeviceAccess(jsonAccessRight.getString("applicationId"), jsonAccessRight.getString("deviceId"), DeviceAccessPolicy.fromString(jsonAccessRight.getString("policy")));
            //TODO: methodAccessRight is not sent/received
/*            if(jsonAccessRight.has("methodAccessRight")){
                JSONArray jsonMethodRights = jsonAccessRight.getJSONArray("methodAccessRight");
                for(int i = 0; i < jsonMethodRights.length(); i++){
                    JSONObject methodRight = jsonMethodRights.getJSONObject(i);
                    String methodName = methodRight.getString("method");
                    MemberAccessPolicy methodRightAccessValue = MemberAccessPolicy.fromString(methodRight.getString("access"));
                    right = manager.setMethodAccess(jsonAccessRight.getString("applicationId"), jsonAccessRight.getString("deviceId"), methodName, methodRightAccessValue);
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return makeCORS(Response.status(Response.Status.BAD_REQUEST));
        }
        return makeCORS(Response.ok(AccessRightJSON.toJSON(right)));
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(value="/policies")
    public Response updateApplicationAccess(String content){
        return setRightAccess(content);
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

    private String getAllPoliciesTypes() throws JSONException{
        JSONArray policies = new JSONArray();
        //add the policies. It is not a for to void non-deterministic identifiers.
        policies.put(getJSONPolicyType(0,DeviceAccessPolicy.HIDDEN));
        policies.put(getJSONPolicyType(1,DeviceAccessPolicy.VISIBLE));
        policies.put(getJSONPolicyType(2,DeviceAccessPolicy.PARTIAL));
        policies.put(getJSONPolicyType(3,DeviceAccessPolicy.TOTAL));
        return policies.toString();
    }
    private JSONObject getJSONPolicyType(int id, DeviceAccessPolicy policy) throws JSONException {
        JSONObject jsonPolicy = new JSONObject();
        jsonPolicy.put("id", id);
        jsonPolicy.put("name", policy.toString());
        return jsonPolicy;
    }


}
