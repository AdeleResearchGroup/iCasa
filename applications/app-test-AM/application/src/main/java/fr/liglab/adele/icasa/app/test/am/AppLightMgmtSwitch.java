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
package fr.liglab.adele.icasa.app.test.am;

import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.MultiwaySwitch;
import fr.liglab.adele.icasa.physical.abstraction.PresenceService;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component(name = "app-test-mgmt-switch")
public class AppLightMgmtSwitch {

    private static final Logger LOG = LoggerFactory.getLogger(AppLightMgmtSwitch.class);

    @Validate
    @SuppressWarnings("unused")
    public void start() {
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop() {

    }

    @Requires(id="lights",optional = true,specification = BinaryLight.class,filter = "(!(locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+"))",proxy = false)
    @SuppressWarnings("unused")
    private List<BinaryLight> binaryLights;

    @Requires(id="switches",optional = true,specification = MultiwaySwitch.class)
    @SuppressWarnings("unused")
    private List<MultiwaySwitch> multiwaySwitches;

    @Bind(id="lights")
    @SuppressWarnings("unused")
    public void bindBinaryLight(BinaryLight binaryLight){
        if (computeSwitchStateInZone(getMultiwaySwitch(((LocatedObject)binaryLight).getZone()))){
            binaryLight.turnOn();
        }else {
            binaryLight.turnOff();
        }
    }

    @Modified(id="lights")
    @SuppressWarnings("unused")
    public void modifiedBinaryLight(BinaryLight binaryLight){
        if (computeSwitchStateInZone(getMultiwaySwitch(((LocatedObject)binaryLight).getZone()))){
            binaryLight.turnOn();
        }else {
            binaryLight.turnOff();
        }
    }

    @Unbind(id="lights")
    @SuppressWarnings("unused")
    public void unbindBinaryLight(BinaryLight binaryLight){
        binaryLight.turnOff();
    }


    @Bind(id="switches")
    @SuppressWarnings("unused")
    public void bindMultiwaySwitching(MultiwaySwitch multiwaySwitch){
        managelight(multiwaySwitch);
    }

    @Modified(id="switches")
    @SuppressWarnings("unused")
    public void modifiedMultiwaySwitching(MultiwaySwitch multiwaySwitch){
        managelight(multiwaySwitch);
    }

    @Unbind(id="switches")
    @SuppressWarnings("unused")
    public void unbindMultiwaySwitching(MultiwaySwitch multiwaySwitch){
        String zoneName = multiwaySwitch.switchStateOf();
        Set<BinaryLight> lightInZone = getLightInZone(zoneName);
        lightInZone.forEach(BinaryLight::turnOff);
    }

    private void managelight(MultiwaySwitch multiwaySwitch){
        String zoneName = multiwaySwitch.switchStateOf();
        Set<BinaryLight> lightInZone = getLightInZone(zoneName);
        if (multiwaySwitch.stateInZone().equals(MultiwaySwitch.SwitchState.ON)){
            lightInZone.forEach(BinaryLight::turnOn);
        }else {
            lightInZone.forEach(BinaryLight::turnOff);
        }
    }

    private boolean computeSwitchStateInZone(MultiwaySwitch multiwaySwitch) {
        return multiwaySwitch != null && multiwaySwitch.stateInZone().equals(MultiwaySwitch.SwitchState.ON);
    }

    private MultiwaySwitch getMultiwaySwitch(String zone){
        if (zone == null) return null;
        for (MultiwaySwitch multiwaySwitch : multiwaySwitches){
            if (zone.equals(multiwaySwitch.switchStateOf())){
                return multiwaySwitch;
            }
        }
        return null;
    }

    private Set<BinaryLight> getLightInZone(String zone){

        Set<BinaryLight> lightInZone = new HashSet<>();
        if (zone == null){
            return lightInZone;
        }
        lightInZone = binaryLights.stream().filter((light) ->
                zone.equals(((LocatedObject)light).getZone())
        ).collect(Collectors.toSet());
        return lightInZone;
    }
}