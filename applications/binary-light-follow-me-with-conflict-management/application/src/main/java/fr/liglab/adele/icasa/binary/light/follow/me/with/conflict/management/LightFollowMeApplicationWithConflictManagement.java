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
package fr.liglab.adele.icasa.binary.light.follow.me.with.conflict.management;

import fr.liglab.adele.conflict.ipojo.annotation.ConflictManagement;
import fr.liglab.adele.conflict.service.ConflictManagerService;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.PresenceService;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Component(name="LightFollowMeApplicationWithConflictManagement")

@Provides(properties= {
        @StaticServiceProperty(name="icasa.application", type="String", value="Light.Follow.Me.Application", immutable=true)
})

@Instantiate
public class LightFollowMeApplicationWithConflictManagement {

    private static final Logger LOG = LoggerFactory.getLogger(LightFollowMeApplicationWithConflictManagement.class);

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {

    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        // do nothing
    }

    @ConflictManagement
	private ConflictManagerService conflictManager;
    private Map<BinaryLight,String> services = new HashMap<>();
    private final static String ID_PROP = "service.id";

    @Requires(id="lights",optional = true,specification = BinaryLight.class,filter = "(!(locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+"))",proxy = false)
    @ContextRequirement(spec = LocatedObject.class)
    private List<BinaryLight> binaryLights;

    @Requires(id="presence",optional = false,specification = PresenceService.class)
    private List<PresenceService> presenceServices;

    @Bind(id="lights")
    public void bindBinaryLight(BinaryLight binaryLight, Map<String, Object> properties){
        String serviceID = properties.get(ID_PROP).toString();
        services.put(binaryLight, serviceID);
        if (computePresenceInZone(getPresenceService(((LocatedObject)binaryLight).getZone()))){
            conflictManager.lockService(serviceID);
            binaryLight.turnOn();
        }else {
            binaryLight.turnOff();
            conflictManager.unlockService(serviceID);
        }
    }

    @Modified(id="lights")
    public void modifiedBinaryLight(BinaryLight binaryLight, Map<String, Object> properties){
        String serviceID = properties.get(ID_PROP).toString();
        services.put(binaryLight, serviceID);
        if (computePresenceInZone(getPresenceService(((LocatedObject)binaryLight).getZone()))){
            conflictManager.lockService(serviceID);
            binaryLight.turnOn();
        }else {
            binaryLight.turnOff();
            conflictManager.unlockService(serviceID);
        }
    }


    @Unbind(id="lights")
    public void unbindBinaryLight(BinaryLight binaryLight, Map<String, Object> properties){
        //TODO VERIFY (need to turn of if the device is not localised anymore?)
        //String serviceID = properties.get(ID_PROP).toString();
        //binaryLight.turnOff();
        //conflictManager.unlockService(serviceID);
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
        lightInZone.stream().forEach((light) -> conflictManager.unlockService(services.get(light)) );
    }

    private void managelight(PresenceService presenceService){
        String zoneName = presenceService.sensePresenceIn();
        Set<BinaryLight> lightInZone = getLightInZone(zoneName);
        if (presenceService.havePresenceInZone().equals(PresenceService.PresenceSensing.YES)){
            lightInZone.stream().forEach((light) -> conflictManager.lockService(services.get(light)) );
            lightInZone.stream().forEach((light) ->light.turnOn() );
        }else {
            lightInZone.stream().forEach((light) ->light.turnOff() );
            lightInZone.stream().forEach((light) -> conflictManager.unlockService(services.get(light)) );
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
