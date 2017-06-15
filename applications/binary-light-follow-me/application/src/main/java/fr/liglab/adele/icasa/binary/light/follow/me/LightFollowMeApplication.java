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
package fr.liglab.adele.icasa.binary.light.follow.me;

import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.PresenceService;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component(name="LightFollowMeApplication")

@Provides(properties= {
        @StaticServiceProperty(name="icasa.application", type="String", value="Light.Follow.Me.Application", immutable=true)
})

@Instantiate
public class LightFollowMeApplication {

    private static final Logger LOG = LoggerFactory.getLogger(LightFollowMeApplication.class);

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {

    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        // do nothing
    }

    @Requires(id="lights",optional = true,specification = BinaryLight.class,filter = "(!(locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+"))",proxy = false)
    private List<BinaryLight> binaryLights;

    @Requires(id="presence",optional = false,specification = PresenceService.class)
    private List<PresenceService> presenceServices;

    @Bind(id="lights")
    public void bindBinaryLight(BinaryLight binaryLight){
        if (computePresenceInZone(getPresenceService(((LocatedObject)binaryLight).getZone()))){
            binaryLight.turnOn();
        }else {
            binaryLight.turnOff();
        }

    }

    @Modified(id="lights")
    public void modifiedBinaryLight(BinaryLight binaryLight){
        if (computePresenceInZone(getPresenceService(((LocatedObject)binaryLight).getZone()))){
            binaryLight.turnOn();
        }else {
            binaryLight.turnOff();
        }
    }


    @Unbind(id="lights")
    public void unbindBinaryLight(BinaryLight binaryLight){
        binaryLight.turnOff();
    }

    @Bind(id="presence")
    public void bindPresenceService(PresenceService presenceService){
        managelight(presenceService);
    }

    @Modified(id="presence")
    public void modifiedPresenceService(PresenceService presenceService){
        managelight(presenceService);
    }

    @Unbind(id="presence")
    public void unbindPresenceService(PresenceService presenceService){
        String zoneName = presenceService.sensePresenceIn();
        Set<BinaryLight> lightInZone = getLightInZone(zoneName);
        lightInZone.stream().forEach((light) ->light.turnOff() );
    }

    private void managelight(PresenceService presenceService){
        String zoneName = presenceService.sensePresenceIn();
        Set<BinaryLight> lightInZone = getLightInZone(zoneName);
        if (presenceService.havePresenceInZone().equals(PresenceService.PresenceSensing.YES)){
            lightInZone.stream().forEach((light) ->light.turnOn() );
        }else {
            lightInZone.stream().forEach((light) ->light.turnOff() );
        }
    }

    private boolean computePresenceInZone(PresenceService service){
        if (service == null)return false;
        return service.havePresenceInZone().equals(PresenceService.PresenceSensing.YES);
    }

    private PresenceService getPresenceService(String zone){
        if (zone == null)return null;
        for (PresenceService service : presenceServices){
            if (zone.equals(service.sensePresenceIn())){
                return service;
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
