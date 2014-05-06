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
package fr.liglab.adele.icasa.remote.wisdom.impl;


import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;


/**
 *
 */
@Component
@Provides
@Instantiate
public class iCasaREST extends DefaultController {

    private BundleContext context;

    public iCasaREST(BundleContext _context){
        this.context = _context;
    }


    @Route(method = HttpMethod.GET, uri = "/icasa/backend")
    public Result info() {
        String info = null;
        try {
            info = getInfo();
        } catch (JSONException e) {
            e.printStackTrace();
            return status(Status.INTERNAL_SERVER_ERROR);
        }
        return ok(info).as(MimeTypes.JSON);
    }

    /**
     * Get the backend information
     * @return the information of the backend in a JSON format String
     * @throws JSONException if there is an error with the json object.
     */
    public String getInfo() throws JSONException {
        JSONObject backendInfo = new JSONObject();
        backendInfo.put("version", getVersion());
        return backendInfo.toString();
    }

    /**
     * Get the backend version obtained from the bundle context.
     * If the version has SNAPSHOT as qualifier, this method will return the maven way (major.minor.micro-SNAPSHOT)
     * @return the version of the icasa-remote bundle.
     */
    private String getVersion() {
        String qualifiedVersion = context.getBundle().getVersion().toString();
        return qualifiedVersion.replace(".SNAPSHOT", "-SNAPSHOT");//This is to maintain maven version format
    }
}
