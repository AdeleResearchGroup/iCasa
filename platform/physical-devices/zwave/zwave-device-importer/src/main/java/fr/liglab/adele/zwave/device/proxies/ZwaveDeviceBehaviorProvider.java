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
package fr.liglab.adele.zwave.device.proxies;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.ArrayList;
import java.util.List;

@FunctionalExtender(contextServices = ZwaveDevice.class)
public class ZwaveDeviceBehaviorProvider implements ZwaveDevice{

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.HOME_ID)
    private int zwaveHomeId;

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.NODE_ID)
    private int zwaveNodeId;

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.MANUFACTURER_ID)
    private int manufacturerId;

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_TYPE)
    private int deviceType;

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_ID)
    private int deviceId;

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.NEIGHBORS)
    private List<Integer> neighbors;

    @ContextEntity.Relation.Field(value = "isZwaveNeighbor",owner = ZwaveDevice.class)
    @Requires(id="zwavesNeighbors",specification=ZwaveDevice.class,optional=true)
    private List<ZwaveDevice> zwaveDevices;

    @Bind(id = "zwavesNeighbors")
    private void bindZDevice(ZwaveDevice device){
        pushNeighbors();
    }

    @Unbind(id= "zwavesNeighbors")
    private void unbindZDevice(ZwaveDevice device){
        pushNeighbors();
    }

    @ContextEntity.State.Push(service = ZwaveDevice.class,state = ZwaveDevice.NEIGHBORS)
    public List<Integer> pushNeighbors() {
        List<Integer> neighbors = new ArrayList<>();
        for (ZwaveDevice device : zwaveDevices){
            neighbors.add(device.getNodeId());
        }
        return neighbors;
    }


    @Override
    public int getHomeId() {
        return zwaveHomeId;
    }

    @Override
    public int getNodeId() {
        return zwaveNodeId;
    }

    @Override
    public int getManufacturerId() {
        return manufacturerId;
    }

    @Override
    public int getDeviceType() {
        return deviceType;
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    @Override
    public List<Integer> getNeighbors() {
        return neighbors;
    }
}
