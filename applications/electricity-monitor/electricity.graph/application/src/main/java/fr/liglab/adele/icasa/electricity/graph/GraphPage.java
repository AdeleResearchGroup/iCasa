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
package fr.liglab.adele.icasa.electricity.graph;


import com.google.common.collect.ImmutableMap;
import fr.liglab.adele.icasa.electricity.manager.ElectricityManager;
import fr.liglab.adele.icasa.electricity.manager.ElectricityManagerListener;
import fr.liglab.adele.icasa.location.Zone;
import org.joda.time.DateTime;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.templates.Template;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by horakm on 6/9/14.
 */
@Controller
@Path("/graph")
public class GraphPage extends DefaultController implements ElectricityManagerListener {

    @Requires
    Publisher publisher;

    @Requires
    ApplicationConfiguration configuration;

    @Requires
    Json json;

    @View("graph")
    private Template graph;

    @Requires
    private ElectricityManager electricityManager;

    private Set<String> _zones;
    private HashMap<String, Double> _devices;

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component graph controller is starting...");
        electricityManager.addListener(this);
        _devices = new HashMap<String, Double>();
        _zones = new HashSet<String>();
    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        electricityManager.removeListener(this);
        System.out.println("Component graph controller is stopping...");
    }

    /**
     * @return the graph page.
     */
    @Route(method = HttpMethod.GET, uri = "")
    public Result index() {
        return ok(render(graph));
    }

    /**
     * Build an immutable map containing the current consumption of devices
     *
     */
    @Override
    public void deviceConsumptionModified(String location, String device, Double consumption, DateTime date) {
        if(!_devices.containsKey(device)) {
            _devices.put(device,consumption);
        }else if(_devices.get(device) != consumption) {
            _devices.put(device, consumption);
        }
        updateDevicesConsumption();
    }

    public void updateDevicesConsumption() {
        Double totalConsumption = 0.0;
        Collection<Double> values = _devices.values();

        for(Double value : values) {
            totalConsumption = totalConsumption + value;
        }

        publisher.publish("/graph/update", json.toJson(
                ImmutableMap.<String, Object>builder()
                        .put("type", "totalConsumption")
                        .put("total", totalConsumption)
                        .build()
        ));
    }

    /**
     * Build an immutable map containing the current consumption of zone
     *
     */
    @Override
    public void zoneConsumptionModified(String zone, Double consumption, DateTime date) {
        publisher.publish("/graph/update", json.toJson(
                ImmutableMap.<String, Object>builder()
                        .put("type", "sample")
                        .put("consumption", consumption)
                        .put("zone",zone)
                        .build()
        ));
    }

    /**
     * Notify the graph about new zone
     *
     * @param zones
     */
    public void addZones(String zones) {
        publisher.publish("/graph/update", json.toJson(
            ImmutableMap.<String, Object>builder()
                .put("type", "AddZone")
                .put("zones", zones)
                .build()
        ));
    }

    /**
     * Notify the graph about removed zone
     *
     * @param zones
     */
    public void removeZones(String zones) {
        publisher.publish("/graph/update", json.toJson(
                ImmutableMap.<String, Object>builder()
                        .put("type", "RemoveZone")
                        .put("zones", zones)
                        .build()
        ));
    }

    @Override
    public void zoneAdded(Zone zone) {
        _zones.add(zone.getId());
        addZones(zone.getId());
    }

    @Override
    public void zoneRemoved(Zone zone) {
        _zones.remove(zone.getId());
        removeZones(zone.getId());
    }
}
