/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package sample;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextUpdate;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.battery.BatteryObservable;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.templates.Template;

import java.util.List;

/**
 * Your first Wisdom Controller.
 */
@Controller
public class BatteryController extends DefaultController {

    public final static String BATTERY_WEB_SOCKET = "/battery/ws";


    @Requires(id="battery",specification = BatteryObservable.class,optional = true)
    @ContextRequirement(spec = GenericDevice.class)
    List<BatteryObservable> observableDevices;

    @Bind(id="battery")
    public synchronized void batteryBind(BatteryObservable batteryObservable){
        logger().info(" bind ");
        ObjectNode node = json.newObject();
        node.put("nodeId",((GenericDevice) batteryObservable).getSerialNumber());
        node.put("level",batteryObservable.getBatteryPercentage());
        node.put("departure",false);
        webSocketPublisher.publish(BATTERY_WEB_SOCKET,node);
    }

    @ContextUpdate(specification = BatteryObservable.class,stateId = BatteryObservable.BATTERY_LEVEL)
    public synchronized void changeOnBatteryLevel(BatteryObservable observable,Object newO,Object oldO){
        logger().info(" change ");
        ObjectNode node = json.newObject();
        node.put("nodeId",((GenericDevice) observable).getSerialNumber());
        node.put("level",observable.getBatteryPercentage());
        node.put("departure",false);

        webSocketPublisher.publish(BATTERY_WEB_SOCKET,node);
    }

    @Unbind(id="battery")
    public synchronized void batteryUnbind(BatteryObservable batteryObservable){
        logger().info(" unbind ");
        ObjectNode node = json.newObject();
        node.put("nodeId",((GenericDevice) batteryObservable).getSerialNumber());
        node.put("departure",true);
        webSocketPublisher.publish(BATTERY_WEB_SOCKET,node);
    }

    /**
     * Injects a template named 'welcome'.
     */
    @View("welcome")
    Template welcome;

    @Requires
    Publisher webSocketPublisher;

    @Requires
    Json json;


    @Opened(BATTERY_WEB_SOCKET)
    public void opened(@Parameter("client") String client) {
        logger().info("Client connection on battery web socket {}", client);
    }

    @Closed(BATTERY_WEB_SOCKET)
    public void closed(@Parameter("client") String client) {
        logger().info("Client disconnection on battery web socket {}", client);
    }

    /**
     * The action method returning the welcome page. It handles
     * HTTP GET request on the "/" URL.
     *
     * @return the welcome page
     */
    @Route(method = HttpMethod.GET, uri = "/battery")
    public Result welcome() {
        return ok(render(welcome));
    }

    @Route(method = HttpMethod.GET, uri = "/batteries")
    public Result getBatteries(){
        return ok(buildBatterieList());
    }

    @Route(method = HttpMethod.GET, uri = "/batteries/{id}")
    public Result getIndividualBatteries(@Parameter(value = "id") String id){
        return ok(buildIndividualBattery(id));
    }



    private ObjectNode buildBatterieList(){
        ObjectNode node = json.newObject();
        for (BatteryObservable batteryObservable: observableDevices){
            node.put(((GenericDevice) batteryObservable).getSerialNumber(),batteryObservable.getBatteryPercentage());
        }
        return node;
    }

    private ObjectNode buildIndividualBattery(String deviceId){
        ObjectNode node = json.newObject();
        for (BatteryObservable batteryObservable:observableDevices){
            if(((GenericDevice) batteryObservable).getSerialNumber().equals(deviceId)){
                node.put("nodeId",((GenericDevice) batteryObservable).getSerialNumber());
                node.put("level",batteryObservable.getBatteryPercentage());
            }
        }
        return node;
    }
}
