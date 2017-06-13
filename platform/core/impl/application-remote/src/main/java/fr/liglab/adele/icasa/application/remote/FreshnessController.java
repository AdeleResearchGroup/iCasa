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
package fr.liglab.adele.icasa.application.remote;

import fr.liglab.adele.icasa.application.ApplicationManager;
import fr.liglab.adele.icasa.application.FreshnessTracker;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

@Component
@Instantiate
@Provides
@Path("/icasa/freshness")
public class FreshnessController extends DefaultController {

    @Requires
    private ApplicationManager _applicationMgr;

    @Requires
    private FreshnessTracker freshnessTracker;


    @Route(method = HttpMethod.GET, uri = "/applications")
    public Result applications() {
        return ok(freshnessTracker.getApplicationDemands()).as(MimeTypes.JSON);
    }

    /**
     * Retrieves an application demand.
     *
     * @param appId The ID of the application to retrieve
     * @return The required application
     */
    @Route(method = HttpMethod.GET, uri = "/applications/{appId}")
    public Result getApplication(@Parameter("appId") String appId) {
        if (appId == null || appId.length() < 1) {
            return badRequest();
        }

        Object result = freshnessTracker.getApplicationDemand(appId);
        if (result == null)
            return notFound();
        else return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/devices")
    public Result devices() {
        return ok(freshnessTracker.getDeviceDemands()).as(MimeTypes.JSON);
    }

    /**
     * Retrieves an application demand.
     *
     * @param deviceId The ID of the device to retrieve
     * @return The required device
     */
    @Route(method = HttpMethod.GET, uri = "/devices/{deviceId}")
    public Result getDevice(@Parameter("deviceId") String deviceId) {
        if (deviceId == null || deviceId.length() < 1) {
            return badRequest();
        }

        Object result = freshnessTracker.getDeviceDemand(deviceId);
        if (result == null)
            return notFound();
        else return ok();
    }
}
