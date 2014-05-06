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
package fr.liglab.adele.icasa.simulator.remote.wisdom.impl;

import fr.liglab.adele.icasa.commands.ICasaCommand;
import fr.liglab.adele.icasa.commands.Signature;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 *
 */
@Component
@Provides
@Instantiate
@Path("/icasa/shell")
public class RemoteShell extends DefaultController {

    /**
     * Simulator commands added to the platorm
     */
    private Map<String, ICasaCommand> commands = new HashMap<String, ICasaCommand>();


    @Route(method = HttpMethod.POST, uri = "/execute/{name}")
    public Result executeCommand(@Parameter("name") String name) {
        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
        //help
        if(name.compareTo("help") == 0){
            return getCommands();
        }

        ICasaCommand command = commands.get(name);
        if (command == null){
            return notFound();
        }

        JSONObject receivedParams;
        StringBuffer result = new StringBuffer();
        JSONObject params;

        //build parameters in the expected format.
        try {
            receivedParams = new JSONObject(content);
            List<String> arguments = getReceivedParams(receivedParams);
            params = getParamsInJSON(command, arguments);
        } catch (JSONException e) {
            e.printStackTrace();
            return internalServerError();
        }

        //create OutputStream to retrieve messages.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(byteArrayOutputStream);
        try {
            command.execute(System.in, ps, params);
        } catch (Exception e) {
            result.append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        //created returned json object.
        result.append(new String(byteArrayOutputStream.toByteArray()));
        JSONObject returnedObject = new JSONObject();
        try {
            returnedObject.put("result", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return internalServerError();
        }
        return ok(returnedObject.toString()).as(MimeTypes.JSON);
    }

    private List getReceivedParams(JSONObject params) throws JSONException {
        JSONArray stringParameters = null;
        List list = new ArrayList();
        stringParameters = params.getJSONArray("parameters");

        for (int i=0; i<stringParameters.length(); i++) {
                list.add( stringParameters.getString(i) );
        }
        return list;
    }


    @Route(method = HttpMethod.GET, uri = "/commands")
    public Result getCommands() {
        JSONObject result;
        try {
            result = getCommandsInJSON();
        } catch (JSONException e) {
            e.printStackTrace();
            return internalServerError();
        }
        return ok(result.toString()).as(MimeTypes.JSON);
    }

    private JSONObject getCommandsInJSON() throws JSONException {
        JSONObject result = new JSONObject();
        //JSONArray list = new JSONArray();
        StringBuilder builder = new StringBuilder();
        Set<String> commandNames = new TreeSet(commands.keySet());
        for(String name :commandNames){
            //JSONObject command = new JSONObject();
            String description = "unknown";
            //command.put("name", name);
            ICasaCommand icasaCommand = commands.get(name);
            if(icasaCommand != null){
                description = icasaCommand.getDescription();
            }
            builder.append(name).append("\n").append("\t").append(description).append("\n");
            //command.put("description", description);
            //list.put(command);
        }
        result.put("result", builder.toString());
        return result;
    }

    private JSONObject getParamsInJSON(ICasaCommand command, List arguments) throws JSONException {
        JSONObject params = new JSONObject();
        int argumentsSize = (arguments!=null)?arguments.size() : 0;
        Signature signature =  command.getSignature(argumentsSize);
        if (signature != null){
            String paramsNames[] = signature.getParameters();
            if (arguments != null) {
                for (int i = 0; i < paramsNames.length && argumentsSize > i; i++) {
                    params.put(paramsNames[i], arguments.get(i));
                }
            }
        }
        return params;
    }

    // -- Component bind methods -- //
    @Bind(id = "commands", aggregate = true, optional = true)
    public void bindCommand(ICasaCommand commandService) {
        String name = commandService.getName();
        commands.put(name, commandService);
    }

    @Unbind(id = "commands")
    public void unbindCommand(ICasaCommand commandService) {
        String name = commandService.getName();
        commands.remove(name);
    }

}
